package com.example.chicksevent;

import android.app.Activity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;


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
import com.example.chicksevent.databinding.FragmentHostedEventBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HostedEventFragment extends Fragment {

    private FragmentHostedEventBinding binding;
    private ArrayList<Event> eventDataList = new ArrayList<>();

    private FirebaseService eventService;
    private FirebaseService waitingListService;

    private String TAG = "RTD8";
    ListView eventView;
    HostedEventAdapter hostedEventAdapter;

    private String androidId;



    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        Log.i("sigma", "create view");


        binding = FragmentHostedEventBinding.inflate(inflater, container, false);
//        View view = inflater.inflate(R.layout.fragment_hosted_event, container, false);



        return binding.getRoot();

    }

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
//

//
//
//
////            eventAdapter = new EventAdapter(getContext(), eventDataList, item -> {});
////            eventView.setAdapter(eventAdapter);
////        });
////
        listEvents();
    }


    public void showHostedEvents() {

    }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}