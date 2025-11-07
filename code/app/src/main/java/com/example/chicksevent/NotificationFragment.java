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
    FirebaseService notificationService;


    private String androidId;

    private final String TAG = "RTD8";

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        notificationService = new FirebaseService("Notification");

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
        User userToUpdate = new User(androidId);

        userToUpdate.getNotificationList().addOnCompleteListener(task -> {
//            Log.i(TAG, "should i change");

            notificationDataList = task.getResult();
            ArrayList<Notification> notifNewList = new ArrayList<>();


            notificationAdapter = new NotificationAdapter(getContext(), notificationDataList, item -> {
                Log.i("WATTHE", notificationDataList.size() + " : " + item.getEventId() + " : " + item.getNotificationType().toString());
                for (Notification notif : notificationDataList) {
                    Log.i("WATTHE", item.getEventId() + " : " + item.getNotificationType().toString());

                    if (item.getNotificationType() == notif.getNotificationType() && item.getEventId().equals(notif.getEventId())) {
                        notificationService.deleteSubCollectionEntry(userToUpdate.getUserId(), item.getEventId(), item.getNotificationType().toString());
                    } else {
                        notifNewList.add(notif);
                    }
                }
                Log.i("WATTHE", "hi : " + notifNewList.size());
                notificationDataList = notifNewList;
                // DON'T DELETE THIS CUZ WE NEED TO RESET NOTIF
                notificationAdapter = new NotificationAdapter(getContext(), notificationDataList, b -> {});
                notificationView.setAdapter(notificationAdapter);
            });



            notificationView.setAdapter(notificationAdapter);


//            Log.i(TAG, String.valueOf(notificationDataList.size()));

        });



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
