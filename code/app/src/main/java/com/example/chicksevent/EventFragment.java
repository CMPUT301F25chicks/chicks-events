package com.example.chicksevent;

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

import com.example.chicksevent.databinding.FragmentEventBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventFragment extends Fragment {

    private FragmentEventBinding binding;
    private ArrayList<Event> eventDataList = new ArrayList<>();

    private FirebaseService eventService;
    private FirebaseService waitingListService;

    private String TAG = "RTD8";
    ListView eventView;
    EventAdapter eventAdapter;

    private String androidId;



    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentEventBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventService = new FirebaseService("Event");
        waitingListService = new FirebaseService("WaitingList");

        androidId = Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        Button notificationButton = view.findViewById(R.id.btn_notification);


        notificationButton.setOnClickListener(v ->
                NavHostFragment.findNavController(EventFragment.this)
                        .navigate(R.id.action_EventFragment_to_NotificationFragment)
        );

        Button createEventButton = view.findViewById(R.id.btn_addEvent);
        createEventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(EventFragment.this).navigate(R.id.action_EventFragment_to_CreateEventFragment);
        });

        eventView =  view.findViewById(R.id.recycler_notifications);;
//
        eventAdapter = new EventAdapter(getContext(), eventDataList, item -> {});
        eventView.setAdapter(eventAdapter);


        Button joinedEvents = view.findViewById(R.id.btn_joined_events);
        Button hostedEvents = view.findViewById(R.id.btn_hosted_events);

        joinedEvents.setOnClickListener(l -> {
            showJoinedEvents();
        });

        hostedEvents.setOnClickListener(l -> {
            showHostedEvents();
        });


//            eventAdapter = new EventAdapter(getContext(), eventDataList, item -> {});
//            eventView.setAdapter(eventAdapter);
//        });
//
        listEvents();
    }

    public void showJoinedEvents() {
        ArrayList<String> arr = new ArrayList<>();
        waitingListService.getReference().get().continueWith(task -> {
            for (DataSnapshot ds : task.getResult().getChildren()) {
                HashMap<String, HashMap<String, Object>> waitingList = (HashMap<String, HashMap<String, Object>>) ds.getValue();
                for (Map.Entry<String, HashMap<String, Object>> entry : waitingList.entrySet()) {
//                        Log.i(TAG, "key " + entry.getKey() + " " + "value" + entry.getValue());
                    for (Map.Entry<String, Object> entry2 : ((HashMap<String, Object>) entry.getValue()).entrySet()) {
//                            Log.i(TAG, "what is );
                        String uid = entry2.getKey();
                        if (androidId.compareTo(uid) == 0) {

                            arr.add(ds.getKey());
//                                eventDataList
                            Log.i(TAG, "found event " + ds.getKey());
//                                arr.add();
                        }


                    }


                }
            }

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
            eventAdapter = new EventAdapter(getContext(), newEventDataList, item -> {});
            eventView.setAdapter(eventAdapter);

            return null;
        });
    }

    public void showHostedEvents() {
        
    }

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

                    Log.d(TAG, "Key: " + key);
                    Log.d(TAG, "Value: " + value);
                    Event e = new Event("e", value.get("id"), value.get("name"), value.get("eventDetails"), "N/A", "N/A", value.get("registrationEndDate"), value.get("registrationStartDate"), 32, "N/A", "tag");
                    eventDataList.add(e);

                    Log.d(TAG, "---");
                }
                EventAdapter eventAdapter = new EventAdapter(getContext(), eventDataList, item -> {
                    NavController navController = NavHostFragment.findNavController(EventFragment.this);

                    Bundle bundle = new Bundle();
                    bundle.putString("eventName", item.getId());

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}