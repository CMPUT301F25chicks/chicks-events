package com.example.chicksevent;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.databinding.FragmentEventDetailBinding;

/**
 * Fragment displaying detailed information about a specific event.
 * <p>
 * This screen allows users to view the event name, navigate to related fragments (Notification,
 * Events, Create Event), and optionally join the event's waiting list as an {@link Entrant}.
 * </p>
 *
 * <p><b>Navigation:</b>
 * <ul>
 *   <li>Navigate to {@code NotificationFragment}</li>
 *   <li>Navigate to {@code EventFragment}</li>
 *   <li>Navigate to {@code CreateEventFragment}</li>
 * </ul>
 * </p>
 *
 * <p>Joining the waiting list uses the device's Android ID as the entrant ID and calls
 * {@link Entrant#joinWaitingList()}.</p>
 *
 * @author Jordan Kwan
 */
public class EventDetailFragment extends Fragment {

    /** View binding for accessing the fragment layout. */
    private FragmentEventDetailBinding binding;

    /**
     * Default constructor required for fragment instantiation.
     */
    public EventDetailFragment() {
        // Default empty constructor
    }

    /**
     * Inflates the event detail layout.
     *
     * @param inflater  LayoutInflater used to inflate views in the fragment.
     * @param container Parent view that the fragment's UI should attach to.
     * @param savedInstanceState Previously saved state if available.
     * @return the inflated root view.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Initializes UI components and sets up navigation and interaction logic after view creation.
     *
     * @param view The root view returned by {@link #onCreateView}.
     * @param savedInstanceState The previously saved instance state, if any.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView eventName = view.findViewById(R.id.tv_event_name);

        // Retrieve arguments passed to this fragment
        Bundle args = getArguments();
        if (args != null) {
            eventName.setText(args.getString("eventName"));
        }

        // Initialize buttons
        Button eventButton = view.findViewById(R.id.btn_events);
        Button createEventButton = view.findViewById(R.id.btn_addEvent);
        Button notificationButton = view.findViewById(R.id.btn_notification);
        Button joinButton = view.findViewById(R.id.btn_waiting_list);

        // Navigate to Notification Fragment
        notificationButton.setOnClickListener(v ->
                NavHostFragment.findNavController(EventDetailFragment.this)
                        .navigate(R.id.action_EventDetailFragment_to_NotificationFragment)
        );

        // Navigate to Event Fragment
        eventButton.setOnClickListener(v ->
                NavHostFragment.findNavController(EventDetailFragment.this)
                        .navigate(R.id.action_EventDetailFragment_to_EventFragment)
        );

        // Navigate to Create Event Fragment
        createEventButton.setOnClickListener(v ->
                NavHostFragment.findNavController(EventDetailFragment.this)
                        .navigate(R.id.action_EventDetailFragment_to_CreateEventFragment)
        );

        // Join waiting list for the current event
        joinButton.setOnClickListener(v -> {
            if (args == null || args.getString("eventName") == null) {
                toast("Event information not available");
                return;
            }

            Entrant entrant = new Entrant(
                    Settings.Secure.getString(
                            requireContext().getContentResolver(),
                            Settings.Secure.ANDROID_ID
                    ),
                    args.getString("eventName")
            );

            entrant.joinWaitingList();
            toast("Joined waiting list for: " + args.getString("eventName"));
        });
    }

    /**
     * Utility method to safely convert CharSequence to a trimmed String.
     *
     * @param cs The CharSequence to convert.
     * @return A trimmed string or an empty string if {@code cs} is null.
     */
    private static String s(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }

    /**
     * Displays a short-duration {@link Toast} message.
     *
     * @param msg the message to display.
     */
    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Releases the binding reference when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}