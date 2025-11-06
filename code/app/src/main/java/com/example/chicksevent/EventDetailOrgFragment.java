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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.databinding.FragmentEventDetailOrgBinding;

/**
 * Fragment displaying detailed information about an event from the organizer's perspective.
 * <p>
 * This screen enables organizers to view event details, navigate to related fragments
 * (such as the waiting list, notifications, or the event list), and create new events.
 * It passes the selected event name between fragments using a {@link Bundle}.
 * </p>
 *
 * <p><b>Navigation:</b>
 * <ul>
 *   <li>Navigate to {@code NotificationFragment}</li>
 *   <li>Navigate to {@code EventFragment}</li>
 *   <li>Navigate to {@code CreateEventFragment}</li>
 *   <li>Navigate to {@code WaitingListFragment} (with event name argument)</li>
 * </ul>
 * </p>
 *
 * <p><b>Usage:</b> Typically accessed when an organizer selects an event they manage.
 * It retrieves the event name from fragment arguments and binds it to the view.
 * </p>
 *
 * @author Jordan Kwan
 */
public class EventDetailOrgFragment extends Fragment {

    /** View binding for the organizer event detail layout. */
    private FragmentEventDetailOrgBinding binding;

    /**
     * Default public constructor for fragment instantiation.
     */
    public EventDetailOrgFragment() {
        // Default empty constructor
    }

    /**
     * Inflates the layout for the organizer event detail fragment.
     *
     * @param inflater LayoutInflater used to inflate the fragment's views.
     * @param container Parent view that the fragment's UI should attach to.
     * @param savedInstanceState Saved state from previous instance, if any.
     * @return the inflated root view for this fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventDetailOrgBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the fragment view hierarchy has been created.
     * Initializes event detail display, sets up navigation and button interactions.
     *
     * @param view the root view returned by {@link #onCreateView}.
     * @param savedInstanceState Previously saved state, if available.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView eventName = view.findViewById(R.id.tv_event_name);

        // Retrieve and display event name from arguments
        Bundle args = getArguments();
        if (args != null) {
            eventName.setText(args.getString("eventName"));
        }

        // Initialize navigation buttons
        Button eventButton = view.findViewById(R.id.btn_events);
        Button createEventButton = view.findViewById(R.id.btn_addEvent);
        Button notificationButton = view.findViewById(R.id.btn_notification);
        Button viewWaitingListButton = view.findViewById(R.id.btn_waiting_list);

        // Navigate to Waiting List Fragment, passing event name as argument
        viewWaitingListButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailOrgFragment.this);

            Bundle bundle = new Bundle();
            if (args != null) {
                bundle.putString("eventName", args.getString("eventName"));
            }

            navController.navigate(R.id.action_EventDetailOrgFragment_to_WaitingListFragment, bundle);
        });

        // Navigate to Notification Fragment
        notificationButton.setOnClickListener(v ->
                NavHostFragment.findNavController(EventDetailOrgFragment.this)
                        .navigate(R.id.action_EventDetailOrgFragment_to_NotificationFragment)
        );

        // Navigate to Event Fragment
        eventButton.setOnClickListener(v ->
                NavHostFragment.findNavController(EventDetailOrgFragment.this)
                        .navigate(R.id.action_EventDetailOrgFragment_to_EventFragment)
        );

        // Navigate to Create Event Fragment
        createEventButton.setOnClickListener(v ->
                NavHostFragment.findNavController(EventDetailOrgFragment.this)
                        .navigate(R.id.action_EventDetailOrgFragment_to_CreateEventFragment)
        );
    }

    /**
     * Utility method to safely trim a {@link CharSequence}.
     *
     * @param cs the input CharSequence.
     * @return a trimmed String or an empty String if null.
     */
    private static String s(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }

    /**
     * Displays a short {@link Toast} message.
     *
     * @param msg the message to display.
     */
    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Cleans up binding resources when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}