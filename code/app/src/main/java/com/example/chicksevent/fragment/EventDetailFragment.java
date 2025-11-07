package com.example.chicksevent.fragment;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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

import com.example.chicksevent.R;
import com.example.chicksevent.databinding.FragmentEventDetailBinding;
import com.example.chicksevent.misc.Entrant;
import com.example.chicksevent.misc.FirebaseService;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;

/**
 * Fragment displaying detailed information about a specific event.
 * <p>
 * This screen allows users to view the event name, description, and other details.
 * It provides navigation to related fragments (Notification, Events, Create Event)
 * and enables users to join the event's waiting list as an {@link Entrant}.
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
 * <p>
 * Joining the waiting list uses the device's Android ID as the entrant ID and calls
 * {@link Entrant#joinWaitingList()}. Users must have a profile in Firebase to join.
 * </p>
 *
 * @author Jordan Kwan
 */
public class EventDetailFragment extends Fragment {

    /** View binding for the event detail layout. */
    private FragmentEventDetailBinding binding;

    /** Firebase service wrapper for accessing user data. */
    public FirebaseService userService;

    /** Firebase service wrapper for accessing event data. */
    public FirebaseService eventService;

    /** Unique identifier for the current user, derived from device Android ID. */
    String userId;

    /**
     * Default constructor required for Fragment instantiation.
     */
    public EventDetailFragment() {
        // You can keep the constructor-empty and inflate via binding below
    }

    /**
     * Inflates the fragment layout using View Binding.
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
        binding = FragmentEventDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the view is created. Initializes Firebase services, loads event data,
     * sets up navigation and join button listeners, and retrieves the current user ID.
     *
     * @param view               the root view returned by {@link #onCreateView}
     * @param savedInstanceState previous saved state (not used)
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userService = new FirebaseService("User");
        eventService = new FirebaseService("Event");

        TextView eventName = view.findViewById(R.id.tv_event_name);
        TextView eventDetails = view.findViewById(R.id.tv_event_details);
        TextView eventNameReal = view.findViewById(R.id.tv_time);

        Bundle args = getArguments();
        if (args != null) {
            eventName.setText(args.getString("eventName"));
        }

        eventService.getReference().get().continueWith(task -> {
            for (DataSnapshot ds : task.getResult().getChildren()) {
                if (ds.getKey().equals(args.getString("eventName"))) {
                    HashMap<String, String> hash = (HashMap<String, String>) ds.getValue();
                    eventNameReal.setText(hash.get("name"));
                    eventDetails.setText(hash.get("eventDetails"));
                }
            }
            return null;
        });

        userId = Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        Button notificationButton = view.findViewById(R.id.btn_notification);
        notificationButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(EventDetailFragment.this)
                    .navigate(R.id.action_EventDetailFragment_to_NotificationFragment);
        });

        Button eventButton = view.findViewById(R.id.btn_events);
        eventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(EventDetailFragment.this)
                    .navigate(R.id.action_EventDetailFragment_to_EventFragment);
        });

        Button createEventButton = view.findViewById(R.id.btn_addEvent);
        createEventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(EventDetailFragment.this)
                    .navigate(R.id.action_EventDetailFragment_to_CreateEventFragment);
        });

        Button joinButton = view.findViewById(R.id.btn_waiting_list);
        joinButton.setOnClickListener(v -> {
            userExists().addOnSuccessListener(boole -> {
                if (boole) {
                    Entrant e = new Entrant(userId, args.getString("eventName"));
                    e.joinWaitingList();
                    Toast.makeText(getContext(),
                            "Joined waiting list :)",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(),
                            "You need to create profile to join waiting list",
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /**
     * Checks whether a user profile exists in Firebase for the current {@link #userId}.
     * <p>
     * Reads all children under the "User" node and checks if any key matches {@code userId}.
     * Returns {@code true} if found, {@code false} otherwise.
     * </p>
     *
     * @return a {@link Task} that resolves to {@code true} if the user exists,
     *         {@code false} if not
     */
    public Task<Boolean> userExists() {
        return userService.getReference().get().continueWith(ds -> {
            boolean userExists = false;
            for (DataSnapshot d : ds.getResult().getChildren()) {
                Log.i("TAGwerw", d.getKey());
                try {
                    HashMap<String, Object> userHash = (HashMap<String, Object>) d.getValue();
                    if (userId.equals(d.getKey())) {
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
     * Cleans up the View Binding reference to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}