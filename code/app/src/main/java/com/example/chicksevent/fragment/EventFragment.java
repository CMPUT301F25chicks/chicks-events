package com.example.chicksevent.fragment;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.R;
import com.example.chicksevent.adapter.EventAdapter;
import com.example.chicksevent.databinding.FragmentEventBinding;
import com.example.chicksevent.misc.Event;
import com.example.chicksevent.misc.FirebaseService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Fragment displaying a list of available events and entry points to related actions.
 * <p>
 * Users can view all events, filter a subset (when arguments are provided), navigate to the
 * event-creation flow, open notifications, search, and view their joined/hosted events. The
 * fragment binds results to a {@link ListView} via {@link EventAdapter}.
 * </p>
 *
 * <b>Firebase roots used:</b>
 * <ul>
 *   <li><code>Event</code> — source of event listings</li>
 *   <li><code>WaitingList</code> — used to compute "joined events" for the current device</li>
 * </ul>
 *
 * <p><b>Arguments:</b> If a {@link Bundle} argument contains an <code>ArrayList String</code>
 * under the key <code>"eventList"</code>, the fragment displays only those events whose ids match
 * the provided values.</p>
 *
 * @author Jordan Kwan
 */
public class EventFragment extends Fragment {

    /** View binding for the event list layout. */
    private FragmentEventBinding binding;

    /** Backing list for events rendered in the adapter. */
    private ArrayList<Event> eventDataList = new ArrayList<>();

    /** Optional list of event ids used to filter the displayed set. */
    private ArrayList<String> eventFilterList = new ArrayList<>();

    /** Whether a filter from arguments has been applied. */
    private Boolean filterApplied = false;

    /** Firebase service for the "Event" root. */
    private FirebaseService eventService;

    /** Firebase service for the "WaitingList" root. */
    private FirebaseService waitingListService;

    /** Log tag. */
    private String TAG = "RTD8";

    /** The list view displaying events. */
    ListView eventView;

    /** Adapter bridging event data to the list view. */
    EventAdapter eventAdapter;

    /** The Android device ID (used to correlate joined events). */
    private String androidId;

    private String filterAvailability;

    private LocalDate filterStart;
    private LocalDate filterEnd;


    /**
     * Inflates the fragment layout using ViewBinding.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Initializes Firebase services, UI controls, adapters, and populates the list on first render.
     *
     * @param view The root view returned by {@link #onCreateView}.
     * @param savedInstanceState Previously saved state, if any.
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventService = new FirebaseService("Event");
        waitingListService = new FirebaseService("WaitingList");

        Bundle args = getArguments();
        if (args != null) {
            filterApplied = true;
            eventFilterList = args.getStringArrayList("eventList");
            filterAvailability = args.getString("filterAvailability");

            // Use it to populate UI
        }


        androidId = Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        eventView =  view.findViewById(R.id.recycler_notifications);;
//
//        eventAdapter = new EventAdapter(getContext(), eventDataList, item -> {});
//        eventView.setAdapter(eventAdapter);


        Button joinedEvents = view.findViewById(R.id.btn_joined_events);
        Button hostedEvents = view.findViewById(R.id.btn_hosted_events);
        Button searchEvents = view.findViewById(R.id.btn_search_events);

        joinedEvents.setOnClickListener(l -> {
            showJoinedEvents();
        });

        hostedEvents.setOnClickListener(l -> {
            NavHostFragment.findNavController(EventFragment.this)
                    .navigate(R.id.action_EventFragment_to_HostedEventFragment);
        });

        searchEvents.setOnClickListener(l -> {
            NavHostFragment.findNavController(EventFragment.this)
                    .navigate(R.id.action_EventFragment_to_SearchEventFragment);
        });
        ImageView posterImageView = view.findViewById(R.id.img_event);


//            eventAdapter = new EventAdapter(getContext(), eventDataList, item -> {});
//            eventView.setAdapter(eventAdapter);
//        });

        if (filterApplied) {
            listFilteredEvents();
        } else {
            Log.i("im printing events", "hi");
            listEvents();
        }

    }

    /**
     * Displays only those events that the current device/user has joined, inferred by
     * scanning the <code>WaitingList</code> root for entries matching {@link #androidId}.
     * The method updates the list view with a new adapter instance containing the filtered set.
     */
    public void showJoinedEvents() {
        ArrayList<String> arr = new ArrayList<>();
        waitingListService.getReference().get().continueWith(task -> {
            for (DataSnapshot ds : task.getResult().getChildren()) {
                try {
                    HashMap<String, HashMap<String, Object>> waitingList = (HashMap<String, HashMap<String, Object>>) ds.getValue();
                    for (Map.Entry<String, HashMap<String, Object>> entry : waitingList.entrySet()) {
                        for (Map.Entry<String, Object> entry2 : ((HashMap<String, Object>) entry.getValue()).entrySet()) {
                            String uid = entry2.getKey();
//                        Log.i(uid, )
                            if (androidId.equals(uid)) {
                                arr.add(ds.getKey());
                                Log.i("RTD10", "found event " + ds.getKey());
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.i("RTD10" , e.toString());
                }

            }

            Log.i("RTD10", "hi");



            ArrayList<Event> newEventDataList = new ArrayList<>();
            for (Event e : eventDataList) {
                boolean keepEvent = false;
                for (String eventIdFilter : arr) {
                    if (eventIdFilter.equals(e.getId())) {
                        keepEvent = true;
                    }
                }
                if (keepEvent) {
                    newEventDataList.add(e);
                }

            }

            Log.i("RTD10", "" + newEventDataList.size());


            EventAdapter eventAdapter = new EventAdapter(getContext(), newEventDataList, item -> {
                NavController navController = NavHostFragment.findNavController(EventFragment.this);

                Bundle bundle = new Bundle();
                bundle.putString("eventId", item.getId());

                navController.navigate(R.id.action_EventFragment_to_EventDetailFragment, bundle);

            });

            eventView.setAdapter(eventAdapter);

            return null;
        });
    }



    /**
     * Lists only the events whose ids are present in {@link #eventFilterList}. Results are read
     * in one shot from the <code>Event</code> root and bound to the list view.
     */
    public void listFilteredEvents() {
        Log.i(TAG, "what");
        Log.i(TAG, "e" + eventService);
//        Log.i("what is filter", filterAvailability);


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


                    if (eventFilterList.contains(key)) {
                        Event e = new Event("e", value.get("id"), value.get("name"),  value.get("eventDetails"), value.get("eventStartTime"), value.get("eventEndTime"), value.get("eventStartDate"), "N/A", value.get("registrationEndDate"), value.get("registrationStartDate"), 32, "N/A", value.get("tag"), false);
                        eventDataList.add(e);

                    }


                    Log.d(TAG, "---");
                }
                EventAdapter eventAdapter = new EventAdapter(getContext(), eventDataList, item -> {
                    NavController navController = NavHostFragment.findNavController(EventFragment.this);

                    Bundle bundle = new Bundle();
                    bundle.putString("eventId", item.getId());

                    navController.navigate(R.id.action_EventFragment_to_EventDetailFragment, bundle);

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
     * Lists all events from the <code>Event</code> root and binds them to the list view.
     */
    public void listEvents() {
        Log.i(TAG, "what");
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

                    Boolean onHold = (Boolean) ((HashMap<String, Object>) childSnapshot.getValue()).get("onHold");

                    if (onHold) continue;

                    Log.d(TAG, "Key: " + key);
                    Log.d(TAG, "Value: " + value);
                    Event e = new Event("e", value.get("id"), value.get("name"), value.get("eventDetails"), value.get("eventStartTime"), value.get("eventEndTime"), value.get("eventStartDate"), "N/A", value.get("registrationEndDate"), value.get("registrationStartDate"), 32, "N/A", value.get("tag"), false);
                    eventDataList.add(e);

                    Log.d(TAG, "---");
                }

                Log.i("im printing events", "" + eventDataList.size());
                EventAdapter eventAdapter = new EventAdapter(getContext(), eventDataList, item -> {
                    NavController navController = NavHostFragment.findNavController(EventFragment.this);

                    Bundle bundle = new Bundle();
                    bundle.putString("eventId", item.getId());

                    navController.navigate(R.id.action_EventFragment_to_EventDetailFragment, bundle);

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
     * Clears the binding reference when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}