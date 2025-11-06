package com.example.chicksevent;

import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.databinding.FragmentCreateEventBinding;

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

    /**
     * Required empty public constructor for fragment inflation.
     */
    public CreateEventFragment() {
        // Default constructor
    }

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
        Button notificationButton = view.findViewById(R.id.btn_notification);
        Button eventButton = view.findViewById(R.id.btn_events);

        // Navigation: move to NotificationFragment
        notificationButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(CreateEventFragment.this)
                    .navigate(R.id.action_CreateEventFragment_to_NotificationFragment);
        });

        // Navigation: move to EventFragment
        eventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(CreateEventFragment.this)
                    .navigate(R.id.action_CreateEventFragment_to_EventFragment);
        });

        // Toggle visibility of the "Max Entrants" field
        binding.cbLimitWaitingList.setOnCheckedChangeListener((btn, checked) -> {
            binding.etMaxEntrants.setVisibility(checked ? View.VISIBLE : View.GONE);
        });

        // Hook up the CREATE button to event creation logic
        binding.btnCreateEvent.setOnClickListener(v -> createEventFromForm());

        // Cancel button returns to the previous screen
        binding.btnCancel.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    /**
     * Reads form data, validates it, creates an {@link Event} object, and uploads it to Firebase.
     * Displays appropriate toast messages on success or validation errors.
     */
    private void createEventFromForm() {
        // Extract inputs from the form
        String name  = s(binding.etEventName.getText());
        String desc  = s(binding.etEventDescription.getText());
        String time  = s(binding.etEventTime.getText()); // Currently unused
        String regStart = s(binding.etStartDate.getText());
        String regEnd   = s(binding.etEndDate.getText());

        // Parse optional entrant limit
        int entrantLimit = 0;
        if (binding.cbLimitWaitingList.isChecked()) {
            String max = s(binding.etMaxEntrants.getText());
            if (!TextUtils.isEmpty(max)) {
                try { entrantLimit = Integer.parseInt(max); }
                catch (NumberFormatException ignore) {}
            }
        }

        // Input validation
        if (TextUtils.isEmpty(name)) {
            toast("Please enter an event name");
            return;
        }
        if (TextUtils.isEmpty(desc)) {
            toast("Please enter an event description");
            return;
        }

        // Get device ID as the organizer identifier
        String entrantId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Placeholder values for unimplemented date fields
        String eventStartDate = null; // TODO: add date/time picker
        String eventEndDate   = null;

        // Optional poster and tag
        String poster = null;
        String tag    = null;

        // Create the event model
        Event e = new Event(
                entrantId,
                null,
                name,
                desc,
                eventStartDate,
                eventEndDate,
                regStart,
                regEnd,
                entrantLimit,
                poster,
                tag
        );

        // Push to Firebase
        e.createEvent();
        toast("Event created 🎉");
        requireActivity().onBackPressed();
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
