package com.example.chicksevent.fragment_org;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.R;
import com.example.chicksevent.adapter.HostedEventAdapter;
import com.example.chicksevent.databinding.FragmentHostedEventBinding;
import com.example.chicksevent.misc.Event;
import com.example.chicksevent.misc.FirebaseService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Fragment that lists events hosted by the current organizer (device user).
 * <p>
 * The list is populated by reading from the <code>Event</code> root and filtering for entries
 * where the <code>organizer</code> matches this device's Android ID. Each list row (inflated from
 * {@code item_hosted_event.xml}) exposes actions to view organizer details for an event or open an
 * update flow for that event.
 * </p>
 *
 * <p><b>Navigation:</b>
 * <ul>
 *   <li>To {@code NotificationFragment}</li>
 *   <li>To {@code EventFragment}</li>
 *   <li>To {@code CreateEventFragment}</li>
 *   <li>To {@code EventDetailOrgFragment} (view action)</li>
 *   <li>To {@code UpdateEventFragment} (update action)</li>
 * </ul>
 * </p>
 *
 * @author Jordan Kwan
 */
public class HostedEventFragment extends Fragment {

    /** View binding for the hosted events layout. */
    private FragmentHostedEventBinding binding;

    /** Backing list of hosted events. */
    private ArrayList<Event> eventDataList = new ArrayList<>();

    /** Firebase service for the "Event" root. */
    private FirebaseService eventService;

    /** Firebase service for the "WaitingList" root (reserved for future use). */
    private FirebaseService waitingListService;

    /** Log tag. */
    private String TAG = "RTD8";

    /** ListView that renders hosted events. */
    ListView eventView;

    /** Adapter used to bind hosted events to the list view. */
    HostedEventAdapter hostedEventAdapter;

    /** Android device ID used to identify the organizer's events. */
    private String androidId;

    /**
     * Inflates the fragment layout using ViewBinding.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        Log.i("sigma", "create view");
        binding = FragmentHostedEventBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    /**
     * Initializes Firebase services, resolves the device ID, wires up navigation buttons, and
     * triggers the initial event list load.
     *
     * @param view The root view returned by {@link #onCreateView}.
     * @param savedInstanceState Previously saved state, if any.
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i("sigma", "life");

        eventService = new FirebaseService("Event");
        waitingListService = new FirebaseService("WaitingList");

        androidId = Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        hostedEventAdapter = new HostedEventAdapter(getContext(), eventDataList, (_e, _t) -> {});
        eventView =  view.findViewById(R.id.recycler_notifications);
////
        eventView.setAdapter(hostedEventAdapter);

        Button notificationButton = view.findViewById(R.id.btn_notification);
        Button eventButton = view.findViewById(R.id.btn_events);
        Button createEventButton = view.findViewById(R.id.btn_addEvent);

        notificationButton.setOnClickListener(v ->
                NavHostFragment.findNavController(HostedEventFragment.this)
                        .navigate(R.id.action_HostedEventFragment_to_NotificationFragment)
        );
//
        createEventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(HostedEventFragment.this).navigate(R.id.action_HostedEventFragment_to_CreateEventFragment);
        });


        eventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(HostedEventFragment.this).navigate(R.id.action_HostedEventFragment_to_EventFragment);
        });

        Log.i("sigma", "wtf");
        listEvents();
    }

    /**
     * Queries the <code>Event</code> root once, filters for events whose <code>organizer</code> equals
     * this device's {@link #androidId}, and binds the result set to the list view.
     * <p>
     * On item interaction, navigates to {@code EventDetailOrgFragment} (view) or
     * {@code UpdateEventFragment} (update) depending on the clicked control.
     * </p>
     */
    public void listEvents() {
        Log.i("sigma", "what");
        Log.i(TAG, "e" + eventService);
        eventDataList = new ArrayList<>();
        eventService.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "=== SHOW the event ===");

                // Iterate through all children
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String key = childSnapshot.getKey();
                    HashMap<String, String> value = (HashMap<String, String>) childSnapshot.getValue();
//                    new Event();



                    Log.d(TAG, "Key: " + key);
                    Log.d(TAG, "Value: " + value);
                    if (value.get("organizer").equals(androidId)) {
                        Log.d("sigma", "yes success " + key);
                        Event e = new Event("e", value.get("id"), value.get("name"), value.get("eventDetails"), "N/A", "N/A", value.get("registrationEndDate"), value.get("registrationStartDate"), 32, "N/A", value.get("tag"));
                        eventDataList.add(e);
                    }


                    Log.d(TAG, "---");
                }
                HostedEventAdapter eventAdapter = new HostedEventAdapter(getContext(), eventDataList, (item, type) -> {
                    NavController navController = NavHostFragment.findNavController(HostedEventFragment.this);

                    Bundle bundle = new Bundle();
                    bundle.putString("eventName", item.getId());
//                    bundle.putString("organizerId", item.getId());

                    if (type == 0) {
                        navController.navigate(R.id.action_HostedEventFragment_to_EventDetailOrgFragment, bundle);
                    } else {
                        navController.navigate(R.id.action_HostedEventFragment_to_UpdateEventFragment, bundle);

                    }

                });

                eventView.setAdapter(eventAdapter);


//                Log.d(TAG, "Total children: " + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error reading data: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Releases binding references when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}