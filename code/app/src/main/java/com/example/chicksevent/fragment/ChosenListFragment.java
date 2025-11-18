package com.example.chicksevent.fragment;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.R;
import com.example.chicksevent.adapter.UserAdapter;
import com.example.chicksevent.databinding.FragmentChosenListBinding;
import com.example.chicksevent.enums.EntrantStatus;
import com.example.chicksevent.misc.Entrant;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.Organizer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Fragment responsible for displaying and managing the "chosen list" (INVITED entrants)
 * associated with a specific event.
 *
 * <p>
 * This screen allows organizers to:
 * <ul>
 *   <li>View the list of invited entrants fetched from Firebase</li>
 *   <li>Navigate to event management, creation, or notification screens</li>
 *   <li>Send bulk notifications to invited and uninvited entrants</li>
 * </ul>
 * Firebase data is retrieved through {@link FirebaseService}, and all list data is rendered
 * using a {@link com.example.chicksevent.adapter.UserAdapter}.
 * </p>
 *
 * <p><b>Firebase roots used:</b></p>
 * <ul>
 *   <li><code>WaitingList/{eventId}/INVITED</code> — load invited entrants</li>
 *   <li><code>WaitingList/{eventId}/UNINVITED</code> — send “not chosen” notifications</li>
 * </ul>
 *
 * <p>
 * This fragment does not perform access control checks; callers must ensure that the current
 * user is authorized to view or modify event-related data before navigating to this screen.
 * </p>
 */

public class ChosenListFragment extends Fragment {

    /** View binding object for the fragment layout. */
    private FragmentChosenListBinding binding;

    /** ListView used to display invited entrants. */
    private ListView userView;

    /** Adapter that binds entrant data to the ListView. */
    private UserAdapter waitingListAdapter;

    /** List of entrant data to display in the chosen list. */
    private ArrayList<Entrant> userDataList = new ArrayList<>();

    /** Firebase service referencing the "WaitingList" node. */
    private FirebaseService waitingListService = new FirebaseService("WaitingList");

    /** Tag used for logging. */
    private final String TAG = "ChosenList";

    /** ID of the event whose chosen list is displayed. */
    private String eventId;

    /** Required empty public constructor. */
    public ChosenListFragment() {}

    /**
     * Inflates the fragment layout and initializes view binding.
     *
     * @param inflater  LayoutInflater used to inflate the view.
     * @param container Parent view group.
     * @param savedInstanceState Previous state bundle.
     * @return The root view for this fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChosenListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the view is created. Initializes UI components,
     * sets up button navigation, loads invited entrants, and handles notifications.
     *
     * @param view The fragment's root view.
     * @param savedInstanceState Previously saved instance state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventName");
        }

        Button eventButton = view.findViewById(R.id.btn_events);
        Button createEventButton = view.findViewById(R.id.btn_addEvent);
        Button notificationButton = view.findViewById(R.id.btn_notification);
        Button sendNotificationButton = view.findViewById(R.id.btn_notification1);

        userView = view.findViewById(R.id.recycler_chosenUser);

        waitingListAdapter = new UserAdapter(getContext(), userDataList, eventId);
        userView.setAdapter(waitingListAdapter);

        notificationButton.setOnClickListener(v ->
                NavHostFragment.findNavController(ChosenListFragment.this)
                        .navigate(R.id.action_ChosenListFragment_to_NotificationFragment)
        );

        eventButton.setOnClickListener(v ->
                NavHostFragment.findNavController(ChosenListFragment.this)
                        .navigate(R.id.action_ChosenListFragment_to_EventFragment)
        );

        createEventButton.setOnClickListener(v ->
                NavHostFragment.findNavController(ChosenListFragment.this)
                        .navigate(R.id.action_ChosenListFragment_to_CreateEventFragment)
        );

        // Load INVITED entrants
        listEntrants(EntrantStatus.INVITED);

        sendNotificationButton.setOnClickListener(v -> {
            Organizer organizer = new Organizer(
                    Settings.Secure.getString(
                            getContext().getContentResolver(),
                            Settings.Secure.ANDROID_ID
                    ),
                    eventId
            );

            organizer.sendWaitingListNotification(EntrantStatus.INVITED, "chosen list notification");
            organizer.sendWaitingListNotification(EntrantStatus.UNINVITED, "not chosen notification");

            Toast.makeText(getContext(), "Notifications sent.", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Loads entrants from Firebase under the specified status (e.g., INVITED)
     * and updates the ListView with the retrieved data.
     *
     * @param status The status category to query from Firebase.
     */
    public void listEntrants(EntrantStatus status) {
        waitingListService.getReference().child(eventId).child(status.toString())
                .addValueEventListener(new ValueEventListener() {

                    /**
                     * Called when Firebase data is successfully retrieved.
                     * Rebuilds the entrant list and refreshes the adapter.
                     *
                     * @param dataSnapshot Snapshot of Firebase data.
                     */
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        userDataList = new ArrayList<>();

                        for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                            String entrantId = childSnap.getKey();
                            String name = childSnap.child("name").getValue(String.class);
                            Entrant entrant = new Entrant(entrantId, name, status);
                            userDataList.add(entrant);
                        }

                        waitingListAdapter = new UserAdapter(getContext(), userDataList, eventId);
                        userView.setAdapter(waitingListAdapter);
                    }

                    /**
                     * Called when a Firebase database read fails.
                     *
                     * @param databaseError Error from Firebase.
                     */
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Error reading data: " + databaseError.getMessage());
                    }
                });
    }

    /**
     * Clears the binding reference when the view is destroyed to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
