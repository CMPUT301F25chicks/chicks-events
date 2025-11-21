package com.example.chicksevent.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chicksevent.R;
import com.example.chicksevent.adapter.EntrantAdapter;
import com.example.chicksevent.databinding.FragmentCancelledListBinding;
import com.example.chicksevent.enums.EntrantStatus;
import com.example.chicksevent.misc.Entrant;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.Organizer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Fragment displaying a list of cancelled entrants for a specific event, allowing the organizer
 * to view entrants by status and send notifications.
 *
 * <p>
 * Users (organizers) can:
 * <ul>
 *   <li>View the list of cancelled entrants filtered by {@link EntrantStatus}.</li>
 *   <li>Navigate to event listing, event creation, and notification screens.</li>
 *   <li>Send notifications to cancelled entrants for the current event.</li>
 * </ul>
 * </p>
 *
 * <p><b>Firebase roots used:</b>
 * <ul>
 *   <li><code>WaitingList</code> — stores the entrants grouped by status for each event.</li>
 * </ul>
 * </p>
 *
 * <p><b>Arguments:</b> If a {@link Bundle} argument contains a string under the key
 * <code>"eventName"</code>, the fragment loads entrants for that event.</p>
 *
 * <p><b>UI Components:</b>
 * <ul>
 *   <li>{@link ListView} userView — displays the list of entrants.</li>
 *   <li>Buttons for navigation and sending notifications:
 *       <ul>
 *           <li>Event list</li>
 *           <li>Create event</li>
 *           <li>Notification screen</li>
 *           <li>Send cancelled notifications</li>
 *       </ul>
 *   </li>
 * </ul>
 * </p>
 *
 */
public class CancelledListFragment extends Fragment {
    /** View binding for the fragment layout. */
    private FragmentCancelledListBinding binding;

    /** ListView displaying chosen entrants. */
    private ListView userView;

    /** Adapter binding entrant data to the ListView. */
    private EntrantAdapter entrantAdapter;

    /** Data source for the adapter containing entrants for the event. */
    private ArrayList<Entrant> entrantDataList = new ArrayList<>();

    /** Firebase service pointing to the WaitingList root node. */
    private FirebaseService waitingListService = new FirebaseService("WaitingList");

    /** Tag used for logging. */
    private static final String TAG = "RTD8";

    /** ID of the current event loaded in this fragment. */
    private String eventId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCancelledListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) eventId = args.getString("eventId");

        userView = view.findViewById(R.id.recycler_cancelledUser);

//        Button eventButton = view.findViewById(R.id.btn_events);
//        Button createEventButton = view.findViewById(R.id.btn_addEvent);
//        Button notificationButton = view.findViewById(R.id.btn_notification);
        Button sendNotificationButton = view.findViewById(R.id.btn_notification1);

        entrantAdapter = new EntrantAdapter(getContext(), entrantDataList);
        userView.setAdapter(entrantAdapter);

        // Navigation
//        eventButton.setOnClickListener(v -> NavHostFragment.findNavController(this)
//                .navigate(R.id.action_CancelledListFragment_to_EventFragment));
//        createEventButton.setOnClickListener(v -> NavHostFragment.findNavController(this)
//                .navigate(R.id.action_CancelledListFragment_to_CreateEventFragment));
//        notificationButton.setOnClickListener(v -> NavHostFragment.findNavController(this)
//                .navigate(R.id.action_CancelledListFragment_to_NotificationFragment));

        // Load cancelled entrants by default
        listEntrants(EntrantStatus.CANCELLED);

        // Send notifications to invited/uninvited entrants
        sendNotificationButton.setOnClickListener(v -> {
            Log.i("notification", "sending notif");
            Organizer organizer = new Organizer(Settings.Secure.getString(
                    getContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID
            ), eventId);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Send Cancelled List Notification");

            final EditText input = new EditText(getContext());
            input.setHint("Type here...");
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String userInput = input.getText().toString().trim();

                    Toast.makeText(getContext(),
                            "You entered: " + userInput,
                            Toast.LENGTH_SHORT).show();

                    organizer.sendWaitingListNotification(EntrantStatus.INVITED, userInput);
//                    organizer.sendWaitingListNotification(EntrantStatus.UNINVITED, "NOT chosen list notification");
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        });
    }

    /**
     * Loads entrants for the current event filtered by the provided status.
     *
     * <p>The entrant data is fetched from Firebase under
     * <code>WaitingList/{eventId}/{status}</code> and bound to {@link #userView} via
     * {@link EntrantAdapter}.</p>
     *
     * @param status the {@link EntrantStatus} to filter entrants by
     */
    private void listEntrants(EntrantStatus status) {
        waitingListService.getReference()
                .child(eventId)
                .child(status.toString())
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (getContext() == null) return;
                        entrantDataList = new ArrayList<>();
                        for (DataSnapshot childSnap : snapshot.getChildren()) {
                            entrantDataList.add(new Entrant(childSnap.getKey(), eventId));
                        }
                        entrantAdapter = new EntrantAdapter(getContext(), entrantDataList);
                        userView.setAdapter(entrantAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error reading data: " + error.getMessage());
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
