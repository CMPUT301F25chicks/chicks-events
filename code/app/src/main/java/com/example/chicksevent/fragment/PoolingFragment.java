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

import com.example.chicksevent.R;
import com.example.chicksevent.adapter.EntrantAdapter;
import com.example.chicksevent.adapter.UserAdapter;
import com.example.chicksevent.databinding.FragmentPoolingBinding;
import com.example.chicksevent.enums.EntrantStatus;
import com.example.chicksevent.misc.Entrant;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.Lottery;
import com.example.chicksevent.misc.Organizer;
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
    public ArrayList<Entrant> entrantDataList = new ArrayList<>();

    /** Firebase service for reading/writing waiting-list buckets. */
    private FirebaseService waitingListService;

    /** Log tag. */
    private static final String TAG = "RTD8";

    public int targetEntrants;


    /** The event id whose waiting list is being managed. */
    String eventId;

    public PoolingFragment() {}

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

        if (waitingListService == null) {
            waitingListService = createWaitingListService();
        }

        Bundle args = getArguments();
        if (args != null) eventId = args.getString("eventId");
        loadTargetEntrants();

        userView = view.findViewById(R.id.rv_selected_entrants);

        Button poolingButton = view.findViewById(R.id.btn_pool);


        if (!isAdded()) {
            Log.e(TAG, "Fragment not attached — skipping adapter update");
            return;
        }

        if (getContext() == null) {
            Log.e(TAG, "Context is null — skipping adapter update");
            return;
        }

        waitingListAdapter = new EntrantAdapter(requireContext(), entrantDataList);
        userView.setAdapter(waitingListAdapter);

        // Pool button click
        poolingButton.setOnClickListener(v -> poolReplacementIfNeeded());

        // Initially list invited users
        listEntrants(EntrantStatus.INVITED);
        updateCounters();
    }
    protected FirebaseService createWaitingListService() {
        return new FirebaseService("WaitingList");
    }


    /**
     * Pools replacement entrants if current chosen < target.
     */
    public void poolReplacementIfNeeded() {
        int target = getTargetEntrants();
        int current = getCurrentChosen();

        int toPool = target - current;
        if (toPool <= 0) return; // nothing to pool

        Lottery lottery = new Lottery(eventId);
        lottery.drawOrPool();

        // Refresh list and counters after a small delay to allow Firebase update
        userView.postDelayed(() -> {
            listEntrants(EntrantStatus.INVITED);
            updateCounters();
            Organizer organizer = new Organizer(Settings.Secure.getString(
                    getContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID
            ), eventId);
            organizer.sendWaitingListNotification(EntrantStatus.INVITED, "YOU are the CHOSEN one");
            organizer.sendWaitingListNotification(EntrantStatus.UNINVITED, "you were NOT CHOSEN :(");
        }, 500); // 500ms delay (adjust if needed)

        Log.i("notification", "sending notif");



        Toast.makeText(getContext(), "chosen list notfication sent", Toast.LENGTH_SHORT).show();
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

    private void loadTargetEntrants() {
        new FirebaseService("Event").getReference()
                .child(eventId)
                .child("entrantLimit")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Integer limit = snapshot.getValue(Integer.class);
                        // If null or zero → unlimited
                        if (limit == null || limit < 0) {
                            limit = Integer.MAX_VALUE; // represent "no limit"
                        }

                        targetEntrants = limit; // <-- STORING REAL VALUE

                        binding.tvTargetEntrants.setText("Target Entrants: " + limit);
                        updateCounters();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
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
                        if (getContext() == null) return;

                        entrantDataList = new ArrayList<>();

                        if (dataSnapshot != null && dataSnapshot.exists()) {
                            for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                                Entrant e = new Entrant(childSnap.getKey(), eventId);
                                e.setStatus(EntrantStatus.INVITED);
                                entrantDataList.add(e);
//                                entrantDataList.add(new Entrant(childSnap.getKey(), eventId));
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