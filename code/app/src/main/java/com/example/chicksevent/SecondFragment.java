package com.example.chicksevent;

import android.os.Bundle;
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

import com.example.chicksevent.databinding.FragmentSecondBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private ArrayList<Event> eventDataList = new ArrayList<>();
    private FirebaseService eventService;

    private String TAG = "RTD8";
    ListView eventView;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventService = new FirebaseService("Event");

        Button notificationButton = view.findViewById(R.id.btn_notification);
        notificationButton.setOnClickListener(v ->
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment)
        );

        // ✅ NEW: wire the Search Events button to navigate to SearchEventFragment
        Button searchEventsButton = view.findViewById(R.id.btn_search_events);                 // [1]
        searchEventsButton.setOnClickListener(v ->                                             // [2]
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_SearchEventFragment)           // [3]
        );

        eventView = view.findViewById(R.id.recycler_notifications);
        EventAdapter eventAdapter = new EventAdapter(getContext(), eventDataList, item -> {});
        eventView.setAdapter(eventAdapter);

        listEvents();
    }

    public void listEvents() {
        Log.i(TAG, "what");
        Log.i(TAG, "e" + eventService);
        eventDataList = new ArrayList<>();
        eventService.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "=== SHOW the event ===");

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String key = childSnapshot.getKey();
                    HashMap<String, String> value = (HashMap<String, String>) childSnapshot.getValue();

                    Log.d(TAG, "Key: " + key);
                    Log.d(TAG, "Value: " + value);
                    Event e = new Event(
                            "e",
                            value.get("id"),
                            value.get("name"),
                            value.get("eventDetails"),
                            "N/A",
                            "N/A",
                            value.get("registrationEndDate"),
                            value.get("registrationStartDate"),
                            32,
                            "N/A",
                            "tag"
                    );
                    eventDataList.add(e);

                    Log.d(TAG, "---");
                }

                EventAdapter eventAdapter = new EventAdapter(getContext(), eventDataList, item -> {
                    NavController navController = NavHostFragment.findNavController(SecondFragment.this);
                    Bundle bundle = new Bundle();
                    bundle.putString("eventName", item.getId()); // passing ID; adjust key if needed on the dest
                    navController.navigate(R.id.action_SecondFragment_to_EventDetailFragment, bundle);
                });
                eventView.setAdapter(eventAdapter);
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
