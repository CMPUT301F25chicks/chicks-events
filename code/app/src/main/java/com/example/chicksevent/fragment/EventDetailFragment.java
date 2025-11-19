package com.example.chicksevent.fragment;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.google.android.gms.tasks.Tasks;
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
    private FirebaseService userService;

    /** Firebase service wrapper for accessing event data. */
    private FirebaseService eventService;

    /** Unique identifier for the current user, derived from the device Android ID. */
    String userId;
    String eventId;

    String eventNameString;

    private FirebaseService waitingListService;
    private TextView eventDetails;
    private TextView eventNameReal;
    private Integer waitingListCount;

    /**
     * Default constructor required for Fragment instantiation.
     */
    public EventDetailFragment() {
        // You can keep the constructor empty and inflate via binding below
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
        waitingListService = new FirebaseService("WaitingList");


        TextView eventName = view.findViewById(R.id.tv_event_name);
        eventDetails = view.findViewById(R.id.tv_event_details);
        eventNameReal = view.findViewById(R.id.tv_time);

        Bundle args = getArguments();
        if (args != null) {
            eventNameString = args.getString("eventName");
            eventName.setText(eventNameString);
        }



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
        Button leaveButton = view.findViewById(R.id.btn_leave_waiting_list);
        LinearLayout waitingStatus = view.findViewById(R.id.layout_waiting_status);
        TextView waitingCount = view.findViewById(R.id.tv_waiting_count);
        Button acceptButton = view.findViewById(R.id.btn_accept);
        Button declineButton = view.findViewById(R.id.btn_decline);
        LinearLayout invitedStatus = view.findViewById(R.id.layout_chosen_status);
        Button rejoinButton = view.findViewById(R.id.btn_rejoin_waiting_list);
        LinearLayout uninvitedStatus = view.findViewById(R.id.layout_not_chosen_status);
        LinearLayout acceptedStatus = view.findViewById(R.id.layout_accepted_status);
        LinearLayout declinedStatus = view.findViewById(R.id.layout_declined_status);

        getEventDetail().addOnCompleteListener(t -> {
//            Log.i("browaiting", t.getResult().toString());
            if (t.getResult()==1) {
                waitingStatus.setVisibility(View.VISIBLE);
                waitingCount.setText("Number of Entrants: " + waitingListCount);
                joinButton.setVisibility(View.INVISIBLE);
            }
            if (t.getResult()==2) {
                invitedStatus.setVisibility(View.VISIBLE);
                joinButton.setVisibility(View.INVISIBLE);
            }
            if (t.getResult()==3) {
                uninvitedStatus.setVisibility(View.VISIBLE);
                joinButton.setVisibility(View.INVISIBLE);
            }
            if (t.getResult()==4) {
                acceptedStatus.setVisibility(View.VISIBLE);
                joinButton.setVisibility(View.INVISIBLE);
            }
            if (t.getResult()==5) {
                declinedStatus.setVisibility(View.VISIBLE);
                joinButton.setVisibility(View.INVISIBLE);
            }
        });

        joinButton.setOnClickListener(v -> {
            userExists().addOnSuccessListener(boole -> {
                if (boole) {
                    Entrant e = new Entrant(userId, args.getString("eventName"));
                    e.joinWaitingList();
                    Toast.makeText(getContext(),
                            "Joined waiting list :)",
                            Toast.LENGTH_SHORT).show();
                    waitingStatus.setVisibility(View.VISIBLE);
                    joinButton.setVisibility(View.INVISIBLE);
                } else {
                    Toast.makeText(getContext(),
                            "You need to create a profile to join the waiting list.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        leaveButton.setOnClickListener(v -> {
            Entrant e = new Entrant(userId, args.getString("eventName"));

            e.leaveWaitingList();
            Toast.makeText(getContext(),
                    "You left the waiting list.",
                    Toast.LENGTH_SHORT).show();
            waitingStatus.setVisibility(View.INVISIBLE);
            joinButton.setVisibility(View.VISIBLE);

        });

        acceptButton.setOnClickListener(v -> {
            Entrant e = new Entrant(userId, args.getString("eventName"));

            e.acceptWaitingList();
            Toast.makeText(getContext(),
                    "You accept the invitation. Yah!!!.",
                    Toast.LENGTH_SHORT).show();
            invitedStatus.setVisibility(View.INVISIBLE);
            acceptedStatus.setVisibility(View.VISIBLE);
        });

        declineButton.setOnClickListener(v -> {
            Entrant e = new Entrant(userId, args.getString("eventName"));

            e.declineWaitingList();
            Toast.makeText(getContext(),
                    "You decline the invitation :(((",
                    Toast.LENGTH_SHORT).show();
            invitedStatus.setVisibility(View.INVISIBLE);
            declinedStatus.setVisibility(View.VISIBLE);
        });

        rejoinButton.setOnClickListener(v -> {
            Entrant e = new Entrant(userId, args.getString("eventName"));

            e.joinWaitingList();
            waitingListService.deleteSubCollectionEntry(eventId, "UNINVITED", e.getEntrantId());
            Toast.makeText(getContext(),
                    "You rejoin the waiting list.",
                    Toast.LENGTH_SHORT).show();
            uninvitedStatus.setVisibility(View.INVISIBLE);
            waitingStatus.setVisibility(View.VISIBLE);
            joinButton.setVisibility(View.INVISIBLE);
        });

    }


    public Task<Integer> getWaitingCount() {
        return waitingListService.getReference().child(eventId).get().continueWith(task -> {
            Log.i("browaiting", "in waiting");
            Integer total = 0;
            for (DataSnapshot obj : task.getResult().getChildren()) {

                for (HashMap.Entry<String, Object> entry : ((HashMap<String, Object>) obj.getValue()).entrySet()) {
                    if (obj.getKey().equals("WAITING")) {
                        total += 1;
                    }
                }
            }
            waitingListCount = total;
            return total;
        });
    }
    public Task<Integer> getEventDetail() {
        return eventService.getReference().get().continueWithTask(task -> {
            for (DataSnapshot ds : task.getResult().getChildren()) {

                Log.i("browaiting", ds.getKey() + " : " + eventNameString + " ");
                if (ds.getKey().equals(eventNameString)) {
                    HashMap<String, String> hash = (HashMap<String, String>) ds.getValue();
                    eventNameReal.setText(hash.get("name"));
                    eventDetails.setText(hash.get("eventDetails"));
                    eventId = hash.get("id");

                    getWaitingCount();

                    // Return Task<Boolean> directly (no extra wrapping)
                    return lookWaitingList();
                }
            }

            // No matching event found, return a completed Task with 'false'
            return Tasks.forResult(0);
        });
    }

    public Task<Integer> lookWaitingList() {
        Log.i("checking", "out waiting " + eventId);

        return waitingListService.getReference().child(eventId).get().continueWith(task -> {
            Log.i("checking", "in waiting");
            for (DataSnapshot obj : task.getResult().getChildren()) {
                for (HashMap.Entry<String, Object> entry : ((HashMap<String, Object>) obj.getValue()).entrySet()) {
                    if (userId.equals(entry.getKey()) && obj.getKey().equals("WAITING")) {
                        return 1;
                    }
                    if (userId.equals(entry.getKey()) && obj.getKey().equals("INVITED")) {
                        return 2;
                    }
                    if (userId.equals(entry.getKey()) && obj.getKey().equals("UNINVITED")) {
                        return 3;
                    }
                    if (userId.equals(entry.getKey()) && obj.getKey().equals("ACCEPTED")) {
                        return 4;
                    }
                    if (userId.equals(entry.getKey()) && obj.getKey().equals("DECLINED")) {
                        return 5;
                    }
                }
            }
            return 0;
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