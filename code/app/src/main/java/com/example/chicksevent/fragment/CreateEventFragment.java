package com.example.chicksevent.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chicksevent.databinding.FragmentCreateEventBinding;
import com.example.chicksevent.misc.Event;
import com.example.chicksevent.misc.FirebaseService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Fragment that provides the user interface for creating a new event in the ChicksEvent app.
 * <p>
 * This fragment allows users to input event details such as name, description, start and end
 * registration dates, and optionally specify a maximum number of entrants. The event is then
 * persisted to Firebase through the {@link Event#createEvent()} method.
 * </p>
 *
 * <p><b>Navigation:</b> Provides quick access to Notification and Event fragments through buttons.
 * </p>
 *
 * @author Jinn Kasai
 */
public class CreateEventFragment extends Fragment {

    /** View binding for accessing UI elements. */
    private FragmentCreateEventBinding binding;
    private FirebaseService imageService = new FirebaseService("Image");

    private ActivityResultLauncher<Intent> pickImageLauncher;

    private Uri imageUri;

    private HashMap<String, Object> urlData = new HashMap<>();


    /**
     * Inflates the layout for this fragment using ViewBinding.
     *
     * @param inflater  The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The root view for the fragment's layout.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreateEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the view hierarchy associated with the fragment has been created.
     * Initializes listeners and button click handlers.
     *
     * @param view The root view returned by {@link #onCreateView}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Show/hide "max entrants" field when checkbox changes
        binding.cbLimitWaitingList.setOnCheckedChangeListener((btn, checked) -> {
            binding.etMaxEntrants.setVisibility(checked ? View.VISIBLE : View.GONE);
        });

        // Hook up CREATE button
        binding.btnCreateEvent.setOnClickListener(v -> {
            createEventFromForm();
        });

        // Optional: Cancel just pops back
        binding.btnCancel.setOnClickListener(v -> requireActivity().onBackPressed());

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            imageUri = data.getData();
                            binding.imgEventPoster.setImageURI(imageUri);
                        }
                    }
                }
        );

        binding.imgEventPoster.setOnClickListener(v -> {
            openImageChooser();


        });
    }

    public String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos); // compress if needed
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * Reads form data, validates it, creates an {@link Event} object, and uploads it to Firebase.
     * Displays appropriate toast messages on success or validation errors.
     */
    private void createEventFromForm() {
        // Read inputs
        String name  = s(binding.etEventName.getText());
        String desc  = s(binding.etEventDescription.getText());
        String time  = s(binding.etEventTime.getText()); // currently not stored in Event model
        String regStart = s(binding.etStartDate.getText()); // Registration Start (from your UI)
        String regEnd   = s(binding.etEndDate.getText());   // Registration End (from your UI)

        // Optional max entrants
        int entrantLimit = 999;
        if (binding.cbLimitWaitingList.isChecked()) {
            String max = s(binding.etMaxEntrants.getText());
            if (!TextUtils.isEmpty(max)) {
                try { entrantLimit = Integer.parseInt(max); }
                catch (NumberFormatException ignore) {}
            }
        }

        // Basic validation
        if (TextUtils.isEmpty(name)) {
            toast("Please enter an event name");
            return;
        }
        if (TextUtils.isEmpty(desc)) {
            toast("Please enter an event description");
            return;
        }
        // You can also enforce regStart/regEnd if required

        // Organizer/entrant id — using device id like you did in NotificationFragment
        String entrantId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        boolean geolocationRequired = binding.switchGeo.isChecked();

        // Your Event model also has eventStartDate / eventEndDate.
        // If you don’t have those fields on this screen yet, pass nulls (Firebase will omit).
        String eventStartDate = null; // TODO: add UI if needed
        String eventEndDate   = null; // TODO: add UI if needed

        // Poster/tag are optional for now
        String poster = null;
        String tag    = null;

        // id will be generated in createEvent(), pass a placeholder for constructor param
        String placeholderId = null;

        Event e = new Event(
                entrantId,
                placeholderId,
                name,
                desc,
                eventStartDate,
                eventEndDate,
                regStart,
                regEnd,
                entrantLimit,
                poster,
                tag,
                geolocationRequired
        );

        // Push to Firebase
        String id = e.createEvent();
        Bitmap bitmap = null;
//
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), imageUri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                // fallback for older versions
                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
            }
        } catch (IOException err) {
            err.printStackTrace();
        }
//
        String base64Image = bitmapToBase64(bitmap);
        urlData.put("url", base64Image);

        imageService.addEntry(urlData, id);


        toast("Event created 🎉");
        // Optionally navigate back:
        requireActivity().onBackPressed();
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    /**
     * Utility helper to safely trim CharSequence values.
     *
     * @param cs The CharSequence to trim.
     * @return The trimmed String or an empty string if null.
     */
    private static String s(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }

    /**
     * Displays a short {@link Toast} message.
     *
     * @param msg The message to display.
     */
    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Cleans up resources by nullifying the binding when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}