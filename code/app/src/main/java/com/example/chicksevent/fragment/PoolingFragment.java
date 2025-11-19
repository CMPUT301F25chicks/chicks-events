package com.example.chicksevent.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.R;
import com.example.chicksevent.adapter.EntrantAdapter;
import com.example.chicksevent.adapter.UserAdapter;
import com.example.chicksevent.databinding.FragmentPoolingBinding;
import com.example.chicksevent.enums.EntrantStatus;
import com.example.chicksevent.misc.Entrant;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.Lottery;
import com.example.chicksevent.misc.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Fragment for running a lottery ("pooling") and displaying selected entrants for a given event.
 * <p>
 * This screen lets an organizer trigger the {@link Lottery} for the current event and view the
 * resulting entrant list for a particular {@link EntrantStatus} bucket (e.g., INVITED/WAITING).
 * It also provides quick navigation to Notifications, Events, and Create Event flows.
 * </p>
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Resolve the current event id from fragment arguments (key: {@code "eventName"}).</li>
 *   <li>Run the lottery and display the updated entrant list.</li>
 *   <li>Bind a {@link ListView} via {@link UserAdapter} to render user ids.</li>
 * </ul>
 * </p>
 *
 * @author Jordan Kwan
 */
public class PoolingFragment extends Fragment {

    /** View binding for the pooling layout. */
    private FragmentPoolingBinding binding;

    /** List view that renders the selected entrants. */
    private ListView userView;

    /** Adapter bridging entrant user ids to the list. */
    private EntrantAdapter waitingListAdapter;

    /** Backing list of users in the chosen status bucket. */
    private ArrayList<Entrant> entrantDataList = new ArrayList<>();

    /** Firebase service for reading/writing waiting-list buckets. */
    private FirebaseService waitingListService = new FirebaseService("WaitingList");

    /** Log tag. */
    private static final String TAG = "RTD8";

    /** The event id whose waiting list is being managed. */
    String eventId;

    /**
     * Inflates the pooling layout using ViewBinding.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPoolingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Initializes navigation buttons, resolves arguments, wires the list adapter, and sets the
     * pooling action to run the lottery and display INVITED entrants.
     *
     * @param view the root view returned by {@link #onCreateView}.
     * @param savedInstanceState previously saved state, if any.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) eventId = args.getString("eventName");

        // Buttons
        Button eventButton = view.findViewById(R.id.btn_events);
        Button createEventButton = view.findViewById(R.id.btn_addEvent);
        Button notificationButton = view.findViewById(R.id.btn_notification);
        Button poolingButton = view.findViewById(R.id.btn_pool);

        userView = view.findViewById(R.id.rv_selected_entrants);
        waitingListAdapter = new EntrantAdapter(getContext(), entrantDataList);
        userView.setAdapter(waitingListAdapter);

        // Navigation
        eventButton.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_PoolingFragment_to_EventFragment));
        createEventButton.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_PoolingFragment_to_CreateEventFragment));
        notificationButton.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_PoolingFragment_to_NotificationFragment));

        // Pool button click
        poolingButton.setOnClickListener(v -> poolReplacementIfNeeded());

        // Initially list invited users
        listEntrants(EntrantStatus.INVITED);
        updateCounters();
    }

    /**
     * Pools replacement entrants if current chosen < target.
     */
    private void poolReplacementIfNeeded() {
        int target = getTargetEntrants();
        int current = getCurrentChosen();

        int toPool = target - current;
        if (toPool <= 0) return; // nothing to pool

        Lottery lottery = new Lottery(eventId);
        lottery.poolReplacement(toPool);

        // Refresh list and counters after a small delay to allow Firebase update
        userView.postDelayed(() -> {
            listEntrants(EntrantStatus.INVITED);
            updateCounters();
        }, 500); // 500ms delay (adjust if needed)
    }

    /** Reads target entrants from tv_target_entrants (parses "Target Entrants: N"). */
    private int getTargetEntrants() {
        String text = binding.tvTargetEntrants.getText().toString();
        String[] parts = text.split(":");
        if (parts.length < 2) return 0;
        try { return Integer.parseInt(parts[1].trim()); } catch (NumberFormatException e) { return 0; }
    }

    /** Reads current chosen from userDataList size or tv_current_chosen text. */
    private int getCurrentChosen() {
        return entrantDataList.size(); // or parse text if you want
    }

    /** Updates tv_current_chosen based on current userDataList and target. */
    private void updateCounters() {
        int target = getTargetEntrants();
        int current = getCurrentChosen();
        binding.tvCurrentChosen.setText("Current Chosen: " + current + " / " + target);
    }

    /** Convenience wrapper to list entrants in the WAITING bucket. */
    public void listEntrants() {
        listEntrants(EntrantStatus.WAITING);
    }

    /**
     * Lists entrants for the provided status bucket and updates the list view.
     *
     * @param status the {@link EntrantStatus} bucket to display
     */
    public void listEntrants(EntrantStatus status) {
        Log.i(TAG, "Loading entrants for event=" + eventId + " status=" + status);

        waitingListService.getReference()
                .child(eventId)
                .child(status.toString())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        entrantDataList = new ArrayList<>();

                        if (dataSnapshot != null && dataSnapshot.exists()) {
                            for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                                entrantDataList.add(new Entrant(childSnap.getKey(), eventId));
                            }
                        }

                        // Update adapter
                        waitingListAdapter = new EntrantAdapter(getContext(), entrantDataList);
                        userView.setAdapter(waitingListAdapter);

                        // Update the counter TextView
                        updateCounters();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Error reading data: " + databaseError.getMessage());
                    }
                });
    }



    /** Clears binding references when the view is destroyed. */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
