package com.example.chicksevent.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.chicksevent.R;
import com.example.chicksevent.databinding.FragmentProfileEntrantBinding;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;

/**
 * Fragment responsible for displaying and editing the current user's profile.
 * <p>
 * Allows the user to view and modify their name, email, phone number, and notification preferences.
 * Supports navigation to events, event creation, and notifications. Also provides profile deletion.
 * </p>
 *
 * <p>User identity is determined using {@link Settings.Secure#ANDROID_ID}.</p>
 */
public class ProfileFragment extends Fragment {

    /** View binding for the profile entrant layout. */
    private FragmentProfileEntrantBinding binding;

    /** Firebase service wrapper scoped to the "User" collection in the database. */
    private FirebaseService userService = new FirebaseService("User");

    /** Log tag used for debugging and logging within this fragment. */
    private String TAG = "RTD8";

    /** EditText field for entering or displaying the user's name. */
    EditText editName;

    /** EditText field for entering or displaying the user's phone number. */
    EditText editPhone;

    /** EditText field for entering or displaying the user's email address. */
    EditText editEmail;

    /** ID of the event passed via arguments (currently unused in UI). */
    String eventId;

    /** Button to save profile changes. */
    Button saveInfoButton;

    /** Button to delete the current user's profile. */
    Button deleteProfileButton;

    /** Unique identifier for the current user, derived from device Android ID. */
    String userId;

    /** Local instance of the current user for performing profile operations. */
    User user;

    /** Switch to toggle notification preferences on or off. */
    androidx.appcompat.widget.SwitchCompat notificationSwitch;

    /**
     * Default constructor required for Fragment instantiation by the system.
     * Initializes the Firebase service for user data access.
     */
    public ProfileFragment() {
    }

    /**
     * Inflates the fragment's view using View Binding.
     *
     * @param inflater           the LayoutInflater to inflate the view
     * @param container          parent view that the fragment UI should attach to
     * @param savedInstanceState previous saved state (not used)
     * @return the root view of the fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileEntrantBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the view is created. Initializes UI components, sets up click listeners,
     * retrieves the current user ID, and loads profile data from Firebase.
     *
     * @param view               the root view returned by {@link #onCreateView}
     * @param savedInstanceState previous saved state (not used)
     */
    @SuppressLint("UseCompatLoadingForColorStateLists")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId");
        }

        userId = Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        
        
        

        editName = view.findViewById(R.id.edit_name);
        editPhone = view.findViewById(R.id.edit_phone);
        editEmail = view.findViewById(R.id.edit_email);
        notificationSwitch = view.findViewById(R.id.switch_notifications);

        saveInfoButton = view.findViewById(R.id.btn_save_info);
        deleteProfileButton = view.findViewById(R.id.btn_delete_account);

        editName.setText("LOADING...");
        editEmail.setText("LOADING...");
        editPhone.setText("LOADING...");

        deleteProfileButton.setOnClickListener(v -> {
            deleteProfile();
        });

        saveInfoButton.setOnClickListener(v -> updateProfile());

        renderProfile().addOnSuccessListener(exist -> {
            if (!exist) {
                editName.setText("");
                editEmail.setText("");
                editPhone.setText("");
            }
        });
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                notificationSwitch.setThumbTintList(
                        ContextCompat.getColorStateList(getContext(), R.color.switchThumbOn)
                );
                notificationSwitch.setTrackTintList(
                        ContextCompat.getColorStateList(getContext(), R.color.switchTrackOn)
                );
            } else {
                notificationSwitch.setThumbTintList(
                        ContextCompat.getColorStateList(getContext(), R.color.switchThumbOff)
                );
                notificationSwitch.setTrackTintList(
                        ContextCompat.getColorStateList(getContext(), R.color.switchTrackOff)
                );
            }
        });

    }

    /**
     * Loads the current user's profile data from Firebase and populates the input fields.
     * <p>
     * Reads the entire "User" node, finds the entry matching {@code userId}, and updates
     * the UI with name, email, phone, and notification preference.
     * </p>
     *
     * @return a {@link Task} that resolves to {@code true} if the user profile exists,
     *         {@code false} otherwise
     */
    private Task<Boolean> renderProfile() {
        return userService.getReference().get().continueWith(ds -> {
            boolean userExists = false;
            for (DataSnapshot d : ds.getResult().getChildren()) {
                Log.i("TAGwerw", d.getKey());
                try {
                    HashMap<String, Object> userHash = (HashMap<String, Object>) d.getValue();
                    if (userId.equals(d.getKey())) {
                        editName.setText(userHash.get("name").toString());
                        editEmail.setText(userHash.get("email").toString());
                        editPhone.setText(userHash.get("phoneNumber").toString());
                        notificationSwitch.setChecked((boolean) userHash.get("notificationsEnabled"));
                        return true;
                    }
                } catch(Exception e) {
                    Log.e("ERROR", "weird error " + e);
                }
            }
            return false;
        });
    }

    /**
     * Updates the current user's profile in Firebase using input from the UI.
     * <p>
     * Creates a new {@link User} instance with the current {@code userId} and calls
     * {@link User#updateProfile} to validate and persist changes.
     * Shows a toast message on success or failure.
     * </p>
     */
    private void updateProfile() {
        HashMap<String, Object> data = new HashMap<>();

        user = new User(userId);
        if (user.updateProfile(editName.getText().toString(), editEmail.getText().toString(), editPhone.getText().toString(), notificationSwitch.isChecked())) {
            Toast.makeText(getContext(), "Updated Profile", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to Update Profile", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Deletes the current user's profile from Firebase.
     * <p>
     * Calls {@link FirebaseService#deleteEntry} to remove the user node.
     * Clears the input fields and shows a confirmation toast.
     * Logs an error if {@code userId} is invalid.
     * </p>
     */
    public void deleteProfile() {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Cannot delete profile: User ID is not set.");
            return;
        }
        userService.deleteEntry(userId);
        Log.i(TAG, "Deletion requested for user: " + userId);
        editName.setText("");
        editEmail.setText("");
        editPhone.setText("");

        Toast.makeText(getContext(), "You are Deleted RIP :(", Toast.LENGTH_SHORT).show();
    }

    /**
     * Utility method to safely convert a {@link CharSequence} to a trimmed string.
     *
     * @param cs the input character sequence
     * @return trimmed string, or empty string if input is {@code null}
     */
    private static String s(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }

    /**
     * Convenience method to display a short toast message.
     *
     * @param msg the message to display
     */
    private void toast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Cleans up the View Binding reference to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}