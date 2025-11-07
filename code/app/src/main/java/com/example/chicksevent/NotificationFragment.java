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
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.databinding.FragmentNotificationBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationFragment extends Fragment {

    private FragmentNotificationBinding binding;
    private FirebaseService service;
    ArrayList<Notification> notificationDataList = new ArrayList<Notification>();
    NotificationAdapter notificationAdapter;


    private String androidId;

    private final String TAG = "RTD8";

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        service = new FirebaseService("bruhmoment");
        HashMap<String, Object> data = new HashMap<>();

        androidId = Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        Log.i("ANDROID-ID", "Android ID used for test: " + androidId);

//        createMockEvent();

        ListView notificationView = view.findViewById(R.id.recycler_notifications);


        notificationAdapter = new NotificationAdapter(getContext(), notificationDataList);

        notificationView.setAdapter(notificationAdapter);

        User userToUpdate = new User(androidId);

        userToUpdate.isAdmin().addOnCompleteListener(v -> {
            if (v.getResult()) {
                Log.i("im admin", "yay");
                NavHostFragment.findNavController(NotificationFragment.this)
                        .navigate(R.id.action_NotificationFragment_to_AdminHomeFragment);
            } else {
                Log.i("im admin", "no");
            }
        });

        userToUpdate.getNotificationList().addOnCompleteListener(task -> {
            Log.i(TAG, "should i change");

            notificationDataList = task.getResult();
            notificationAdapter = new NotificationAdapter(getContext(), notificationDataList);

            Log.i(TAG, String.valueOf(notificationDataList.size()));
            notificationView.setAdapter(notificationAdapter);

        });

        Button eventButton = view.findViewById(R.id.btn_events);
        Button createEventButton = view.findViewById(R.id.btn_addEvent);
        Button profileButton = view.findViewById(R.id.btn_profile);

        eventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(NotificationFragment.this).navigate(R.id.action_NotificationFragment_to_EventFragment);
        });

        createEventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(NotificationFragment.this).navigate(R.id.action_NotificationFragment_to_CreateEventFragment);
        });

        profileButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(NotificationFragment.this).navigate(R.id.action_NotificationFragment_to_ProfileFragment);
        });
    }

    private void createMockEvent() {
        User userToUpdate = new User(androidId);
        Event event = new Event(
                userToUpdate.getUserId(),
                "abc123",                           // id
                "Swimming Lessons",                 // name
                "Kids learn freestyle and backstroke", // eventDetails
                "2026-01-01",                       // eventStartDate
                "2026-02-01",                       // eventEndDate
                "2025-11-13",                       // registrationStartDate
                "2025-12-30",                       // registrationEndDate
                30,                                 // entrantLimit
                null,                               // poster
                "sports kids swimming"              // tag
        );

        event.createEvent();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
