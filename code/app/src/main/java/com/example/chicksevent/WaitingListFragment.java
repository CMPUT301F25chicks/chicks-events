package com.example.chicksevent;

import android.os.Bundle;
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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.databinding.FragmentWaitingListBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Fragment that displays entrants in a waiting-list bucket for a specific event.
 * <p>
 * Resolves the current {@code eventId} from arguments (key: {@code "eventName"}),
 * reads the selected bucket (default: {@link EntrantStatus#WAITING}) from Firebase under
 * the <code>WaitingList</code> root, and renders results using {@link UserAdapter}.
 * Also exposes navigation to Notifications, Events, Create Event, and Pooling screens.
 * </p>
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Resolve and persist the current event id from fragment arguments.</li>
 *   <li>Fetch and render the list of user ids in a given waiting-list status.</li>
 *   <li>Provide navigation to related organizer workflows.</li>
 * </ul>
 * </p>
 *
 * @author Jordan Kwan
 */
public class WaitingListFragment extends Fragment {

    /** View binding for the Waiting List layout. */
    private FragmentWaitingListBinding binding;

    /** ListView that displays users in the selected waiting-list bucket. */
    private ListView userView;

    /** Adapter that binds {@link User} items to the list. */
    private UserAdapter waitingListAdapter;

    /** In-memory list of users for the current bucket. */
    private ArrayList<User> userDataList = new ArrayList<>();

    /** Firebase service for interacting with the "WaitingList" root. */
    private final FirebaseService waitingListService = new FirebaseService("WaitingList");

    /** Log tag. */
    private static final String TAG = "RTD8";

    /** The event id whose waiting list is being inspected. */
    private String eventId;

    /** Default empty constructor. */
    public WaitingListFragment() { }

    /**
     * Inflates the fragment layout using ViewBinding.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWaitingListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Initializes navigation, resolves the event id, wires the ListView/adapter, and
     * loads the default waiting-list bucket.
     *
     * @param view the root view returned by {@link #onCreateView}
     * @param savedInstanceState previously saved state, if any
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
        Button poolingButton = view.findViewById(R.id.btn_pool);

        waitingListAdapter = new UserAdapter(getContext(), userDataList);
        userView = view.findViewById(R.id.recycler_notifications);
        userView.setAdapter(waitingListAdapter);

        poolingButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(WaitingListFragment.this);
            Bundle bundle = new Bundle();
            bundle.putString("eventName", eventId);
            navController.navigate(R.id.action_WaitingListFragment_to_PoolingFragment, bundle);
        });

        notificationButton.setOnClickListener(v ->
                NavHostFragment.findNavController(WaitingListFragment.this)
                        .navigate(R.id.action_WaitingListFragment_to_NotificationFragment)
        );

        eventButton.setOnClickListener(v ->
                NavHostFragment.findNavController(WaitingListFragment.this)
                        .navigate(R.id.action_WaitingListFragment_to_EventFragment)
        );

        createEventButton.setOnClickListener(v ->
                NavHostFragment.findNavController(WaitingListFragment.this)
                        .navigate(R.id.action_WaitingListFragment_to_CreateEventFragment)
        );

        // Load default bucket
        listEntrants();
    }

    /** Convenience wrapper to list entrants in the WAITING bucket. */
    public void listEntrants() { listEntrants(EntrantStatus.WAITING); }

    /**
     * Fetches entrants for the provided status bucket and updates the list view.
     *
     * @param status the {@link EntrantStatus} to display
     */
    public void listEntrants(EntrantStatus status) {
        Log.i(TAG, "listEntrants eventId=" + eventId + " status=" + status);
        waitingListService.getReference().child(eventId).child(status.toString())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        userDataList = new ArrayList<>();
                        for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                            userDataList.add(new User(childSnap.getKey()));
                        }
                        waitingListAdapter = new UserAdapter(getContext(), userDataList);
                        userView.setAdapter(waitingListAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Error reading data: " + databaseError.getMessage());
                    }
                });
    }

    /** Utility to safely trim a {@link CharSequence}. */
    private static String s(CharSequence cs) { return cs == null ? "" : cs.toString().trim(); }

    /** Shows a short {@link Toast} message. */
    private void toast(String msg) { Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show(); }

    /** Clears binding references when the view is destroyed. */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
