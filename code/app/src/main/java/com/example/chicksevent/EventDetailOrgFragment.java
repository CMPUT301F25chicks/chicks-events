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

import com.example.chicksevent.databinding.FragmentCreateEventBinding;
import com.example.chicksevent.databinding.FragmentEventDetailBinding;
import com.example.chicksevent.databinding.FragmentEventDetailOrgBinding;
import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;

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

    private FirebaseService eventService;

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
        eventService = new FirebaseService("Event");

        TextView eventName = view.findViewById(R.id.tv_event_name);

        Bundle args = getArguments();
        if (args != null) {
            eventName.setText(args.getString("eventName"));
            // Use it to populate UI
        }

        Button eventButton = view.findViewById(R.id.btn_events);
        Button createEventButton = view.findViewById(R.id.btn_addEvent);
        Button notificationButton = view.findViewById(R.id.btn_notification);

        Button viewWaitingListButton = view.findViewById(R.id.btn_waiting_list);
        Button viewChosenListButton = view.findViewById(R.id.btn_chosen_entrants);
        TextView eventDetails = view.findViewById(R.id.tv_event_details);
        TextView eventNameReal = view.findViewById(R.id.tv_time);

        eventService.getReference().get().continueWith(task -> {
//            eventName =
            for (DataSnapshot ds : task.getResult().getChildren()) {
                if (ds.getKey().equals(args.getString("eventName"))) {
                    HashMap<String, String> hash = (HashMap<String, String>) ds.getValue();
                    eventNameReal.setText(hash.get("name"));
                    eventDetails.setText(hash.get("eventDetails"));
                }
            }
            return null;
        });

        viewWaitingListButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailOrgFragment.this);

            Bundle bundle = new Bundle();
            bundle.putString("eventName", args.getString("eventName"));


            navController.navigate(R.id.action_EventDetailOrgFragment_to_WaitingListFragment, bundle);
        });

        viewChosenListButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailOrgFragment.this);

            Bundle bundle = new Bundle();
            bundle.putString("eventName", args.getString("eventName"));

            navController.navigate(R.id.action_EventDetailOrgFragment_to_ChosenListFragment, bundle);
        });

        notificationButton.setOnClickListener(v -> {
                    NavHostFragment.findNavController(EventDetailOrgFragment.this)
                            .navigate(R.id.action_EventDetailOrgFragment_to_NotificationFragment);
                }
        );

        eventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(EventDetailOrgFragment.this).navigate(R.id.action_EventDetailOrgFragment_to_EventFragment);
        });

        createEventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(EventDetailOrgFragment.this).navigate(R.id.action_EventDetailOrgFragment_to_CreateEventFragment);
        });

        createEventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(EventDetailOrgFragment.this).navigate(R.id.action_EventDetailOrgFragment_to_CreateEventFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
