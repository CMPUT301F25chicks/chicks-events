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
import com.example.chicksevent.databinding.FragmentChosenListBinding;
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
 * Fragment displaying the list of entrants who have been selected (invited) for an event.
 * <p>
 * This screen shows users in the "INVITED" status from the event's waiting list.
 * It allows the organizer to send a notification to all invited users via
 * {@link Organizer#sendWaitingListNotification}.
 * </p>
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>Real-time display of invited entrants using {@link ValueEventListener}</li>
 *   <li>Send bulk notification to all invited users</li>
 *   <li>Navigation to Events, Create Event, and Notifications</li>
 * </ul>
 * </p>
 *
 * <p>
 * Data is loaded from Firebase under:
 * {@code /WaitingList/{eventId}/INVITED}
 * Each child key is treated as a user ID and displayed using {@link EntrantAdapter}.
 * </p>
 *
 * @author ChicksEvent Team
 * @see Organizer
 * @see EntrantStatus
 */
public class ChosenListFragment extends Fragment {

    /** View binding for the chosen list layout. */
    private FragmentChosenListBinding binding;

    /** ListView displaying the list of invited users. */
    private ListView userView;

    /** Adapter that binds user data to the ListView. */
    private EntrantAdapter waitingListAdapter;

    /** List holding all {@link User} objects currently in the invited list. */
    private ArrayList<Entrant> entrantDataList = new ArrayList<>();

    /** Firebase service wrapper scoped to the "WaitingList" root node. */
    private FirebaseService waitingListService = new FirebaseService("WaitingList");

    /** Log tag used for debugging and logging within this fragment. */
    private String TAG = "RTD8";

    /** The ID of the current event, passed via fragment arguments. */
    String eventId;

    /**
     * Default constructor required for Fragment instantiation.
     */
    public ChosenListFragment() {
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
        binding = FragmentChosenListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the view is created. Initializes UI components, sets up navigation,
     * configures the ListView and adapter, and loads the list of invited entrants.
     *
     * @param view               the root view returned by {@link #onCreateView}
     * @param savedInstanceState previous saved state (not used)
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId");
        }

        
        
        
        Button sendNotificationButton = view.findViewById(R.id.btn_notification1);

        waitingListAdapter = new EntrantAdapter(getContext(), entrantDataList);
        userView = view.findViewById(R.id.recycler_chosenUser);
        userView.setAdapter(waitingListAdapter);


        listEntrants(EntrantStatus.INVITED);

        sendNotificationButton.setOnClickListener(v -> {
            Log.i("notification", "sending notif");
            Organizer organizer = new Organizer(Settings.Secure.getString(
                    getContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID
            ), eventId);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Send Chosen List Notification");

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



            Toast.makeText(getContext(), "chosen list notfication sent", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Overloaded method to list entrants with a default status of {@link EntrantStatus#WAITING}.
     * <p>
     * Delegates to {@link #listEntrants(EntrantStatus)} with {@code WAITING}.
     * </p>
     */
    public void listEntrants() {
        listEntrants(EntrantStatus.WAITING);
    }

    /**
     * Attaches a real-time listener to Firebase to load entrants with the specified status.
     * <p>
     * Listens to:
     * {@code /WaitingList/{eventId}/{status}}
     * Each child key is converted to a {@link User} and added to {@link #entrantDataList}.
     * The adapter is recreated and set on every data change.
     * </p>
     *
     * @param status the {@link EntrantStatus} to filter entrants by (e.g., INVITED, WAITING)
     */
    public void listEntrants(EntrantStatus status) {
        Log.i(TAG, "in here " + eventId + " " + status);
        waitingListService.getReference().child(eventId).child(status.toString())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i(TAG, "IN HERE bef " + status);
                        if (getContext() == null) return;
                        entrantDataList = new ArrayList<>();
                        for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                            entrantDataList.add(new Entrant(childSnap.getKey(), eventId));
                        }

                        waitingListAdapter = new EntrantAdapter(getContext(), entrantDataList);
                        userView.setAdapter(waitingListAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Error reading data: " + databaseError.getMessage());
                    }
                });
    }

    /**
     * Utility method to safely convert a {@link CharSequence} to a trimmed string.
     *
     * @param cs the input character sequence
     * @return trimmed string, or empty string if input is {@code null}
     */
    private static String s(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }

    /**
     * Convenience method to display a short toast message.
     *
     * @param msg the message to display
     */
    private void toast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
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