package com.example.chicksevent.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

    /** Unique identifier for the current user, derived from device Android ID. */
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
        waitingListService = new FirebaseService("WaitingList");


        TextView eventName = view.findViewById(R.id.tv_event_name);
        eventDetails = view.findViewById(R.id.tv_event_details);
        eventNameReal = view.findViewById(R.id.tv_time);

        Bundle args = getArguments();
        if (args != null) {
            eventNameString = args.getString("eventId");
            eventName.setText(eventNameString);
        }



        userId = Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );


        Button joinButton = view.findViewById(R.id.btn_waiting_list);
        Button leaveButton = view.findViewById(R.id.btn_leave_waiting_list);
        LinearLayout waitingStatus = view.findViewById(R.id.layout_waiting_status);
        TextView waitingCount = view.findViewById(R.id.tv_waiting_count);

        getEventDetail().continueWithTask(t -> {
//            Log.i("browaiting", t.getResult().toString());
            if (t.getResult()) {
                waitingStatus.setVisibility(View.VISIBLE);
                joinButton.setVisibility(View.INVISIBLE);
            }

            return getWaitingCount();
        }).addOnCompleteListener(t -> {
            waitingCount.setText("Number of Entrants: " + t.getResult());
        });

        joinButton.setOnClickListener(v -> {
            userExists().continueWithTask(boole -> {
                if (boole.getResult()) {
                    Entrant e = new Entrant(userId, args.getString("eventId"));
                    e.joinWaitingList();
                    Toast.makeText(getContext(),
                            "Joined waiting list :)",
                            Toast.LENGTH_SHORT).show();
                    waitingStatus.setVisibility(View.VISIBLE);


                    joinButton.setVisibility(View.INVISIBLE);
                } else {
                    Toast.makeText(getContext(),
                            "You need to create profile to join waiting list",
                            Toast.LENGTH_SHORT).show();
                }

                return getWaitingCount();
            }).addOnCompleteListener(task -> {
                waitingCount.setText("Number of Entrants: " + task.getResult());
            });
        });

        leaveButton.setOnClickListener(v -> {
            Entrant e = new Entrant(userId, args.getString("eventId"));

            e.leaveWaitingList();
            Toast.makeText(getContext(),
                    "You left the waiting list",
                    Toast.LENGTH_SHORT).show();


            waitingStatus.setVisibility(View.INVISIBLE);
            joinButton.setVisibility(View.VISIBLE);

        });

        ImageView posterImageView = view.findViewById(R.id.img_event);

        new FirebaseService("Image").getReference().child(args.getString("eventId")).get().addOnCompleteListener(task -> {
            if (task.getResult().getValue() == null) return;
            String base64Image = ((HashMap<String, String>) task.getResult().getValue()).get("url");
            byte[] bytes = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            posterImageView.setImageBitmap(bitmap);
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
    public Task<Boolean> getEventDetail() {
        return eventService.getReference().get().continueWithTask(task -> {
            for (DataSnapshot ds : task.getResult().getChildren()) {

                Log.i("browaiting", ds.getKey() + " : " + eventNameString + " ");
                if (ds.getKey().equals(eventNameString)) {
                    HashMap<String, String> hash = (HashMap<String, String>) ds.getValue();
                    eventNameReal.setText(hash.get("name"));
                    eventDetails.setText(hash.get("eventDetails"));
                    eventId = hash.get("id");

                    // Return Task<Boolean> directly (no extra wrapping)
                    return lookWaitingList();
                }
            }

            // No matching event found, return a completed Task with 'false'
            return Tasks.forResult(false);
        });
    }

    public Task<Boolean> lookWaitingList() {
        Log.i("browaiting", "out waiting " + eventId);

        return waitingListService.getReference().child(eventId).child("WAITING").child(userId).get().continueWith(task -> task.getResult().exists());
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
        return userService.getReference().child(userId).get().continueWith(task -> task.getResult().exists());
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