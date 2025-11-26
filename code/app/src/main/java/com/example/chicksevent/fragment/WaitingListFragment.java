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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.R;
import com.example.chicksevent.adapter.EntrantAdapter;
import com.example.chicksevent.databinding.FragmentWaitingListBinding;
import com.example.chicksevent.enums.EntrantStatus;
import com.example.chicksevent.misc.Entrant;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.Organizer;
import com.example.chicksevent.misc.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
/**
 * Fragment that displays entrants in a waiting-list bucket for a specific event.
 * <p>
 * Resolves the current {@code eventId} from arguments (key: {@code "eventId"}),
 * reads the selected bucket (default: {@link EntrantStatus#WAITING}) from Firebase under
 * the <code>WaitingList</code> root, and renders results using {@link EntrantAdapter}.
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
    private EntrantAdapter waitingListAdapter;

    /** In-memory list of users for the current bucket. */
    private ArrayList<Entrant> entrantDataList = new ArrayList<>();

    /** Firebase service for interacting with the "WaitingList" root. */
    private final FirebaseService waitingListService = new FirebaseService("WaitingList");

    /** Log tag. */
    private static final String TAG = "RTD8";

    /** The event id whose waiting list is being inspected. */
    private String eventId;

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
            eventId = args.getString("eventId");
        }

        if (getContext() == null) {
            return;
        }

        Button poolingButton = view.findViewById(R.id.btn_pool);

        Button sendNotificationButton = view.findViewById(R.id.btn_notification1);

        waitingListAdapter = new EntrantAdapter(getContext(), entrantDataList);
        userView =  view.findViewById(R.id.recycler_notifications);
////
        userView.setAdapter(waitingListAdapter);

        ImageButton back = view.findViewById(R.id.btn_check);

        if (back != null) back.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        poolingButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(WaitingListFragment.this);

            Bundle bundle = new Bundle();
            bundle.putString("eventId", args.getString("eventId"));

            navController.navigate(R.id.action_WaitingListFragment_to_PoolingFragment, bundle);
        });

        sendNotificationButton.setOnClickListener(v -> {
            Organizer organizer = new Organizer(Settings.Secure.getString(
                    getContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID
            ), eventId);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Send Waiting List Notification");

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

                    organizer.sendWaitingListNotification(EntrantStatus.WAITING, userInput);
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
//            Toast.makeText(getContext(), "waiting list notfication sent", Toast.LENGTH_SHORT).show();
        });

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

        Log.i(TAG, "in here " + eventId + " " + status);
        waitingListService.getReference().child(eventId).child(status.toString())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i(TAG, "IN HERE bef " + status);
                        if (getContext() == null) {
                            return;
                        }
                        entrantDataList = new ArrayList<>();
                        for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                            entrantDataList.add(new Entrant(childSnap.getKey(), eventId));
//                            Log.i(TAG, "child key: " + childSnap.getKey());
                        }

                        waitingListAdapter = new EntrantAdapter(getContext(), entrantDataList);
////
                        userView.setAdapter(waitingListAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Error reading data: " + databaseError.getMessage());
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
