package com.example.chicksevent;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.databinding.FragmentCreateEventBinding;
import com.example.chicksevent.databinding.FragmentEventDetailBinding;
import com.example.chicksevent.databinding.FragmentEventDetailOrgBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;

public class EventDetailFragment extends Fragment {

    private FragmentEventDetailBinding binding;
    private FirebaseService userService;
    private FirebaseService eventService;
    String userId;


    public EventDetailFragment() {
        // You can keep the constructor-empty and inflate via binding below
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userService = new FirebaseService("User");
        eventService = new FirebaseService("Event");

        TextView eventName = view.findViewById(R.id.tv_event_name);

        Bundle args = getArguments();
        if (args != null) {
            eventName.setText(args.getString("eventName"));
            // Use it to populate UI
        }

        Button eventButton = view.findViewById(R.id.btn_events);
        Button createEventButton = view.findViewById(R.id.btn_addEvent);
        Button notificationButton = view.findViewById(R.id.btn_notification);
        Button joinButton = view.findViewById(R.id.btn_waiting_list);
        TextView eventDetails = view.findViewById(R.id.tv_event_details);
        TextView eventNameReal = view.findViewById(R.id.tv_time);

        eventService.getReference().get().continueWith(task -> {
//            eventName =
            for (DataSnapshot ds : task.getResult().getChildren()) {
                if (ds.getKey().equals(args.getString("eventName"))) {
                    HashMap<String, String> hash = (HashMap<String, String>) ds.getValue();
                    eventNameReal.setText(hash.get("name"));
                    eventDetails.setText(hash.get("eventDetails"));
                }
            }
            return null;
        });

        userId = Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        notificationButton.setOnClickListener(v -> {
                    NavHostFragment.findNavController(EventDetailFragment.this)
                        .navigate(R.id.action_EventDetailFragment_to_NotificationFragment);
                }
//
        );

        eventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(EventDetailFragment.this).navigate(R.id.action_EventDetailFragment_to_EventFragment);
        });

        createEventButton.setOnClickListener(v -> {
//            NavHostFragment.findNavController(EventDetailFragment.this).navigate(R.id.action_SecondFragment_to_CreateEventFragment);

            NavHostFragment.findNavController(EventDetailFragment.this).navigate(R.id.action_EventDetailFragment_to_CreateEventFragment);
        });

        joinButton.setOnClickListener(v -> {

            userExists().addOnSuccessListener(boole -> {
               if (boole) {
                   Entrant e = new Entrant(userId, args.getString("eventName"));

                   e.joinWaitingList();
                   Toast.makeText(getContext(),
                           "Joined waiting list :)",
                           Toast.LENGTH_SHORT).show();
               } else {
                   Toast.makeText(getContext(),
                           "You need to create profile to join waiting list",
                           Toast.LENGTH_SHORT).show();
               }
            });

        });
//        eventName
    }

    public Task<Boolean> userExists() {
        return userService.getReference().get().continueWith(ds -> {
            boolean userExists = false;
            for (DataSnapshot d : ds.getResult().getChildren()) {
                Log.i("TAGwerw", d.getKey());
                try {
                    HashMap<String, Object> userHash = (HashMap<String, Object>) d.getValue();
                    if (userId.equals(d.getKey())) {
                        return true;
                    }
                } catch(Exception e) {
                    Log.e("ERROR", "weird error " + e);
                }

            }

            return false;
        });
    }
    private static String s(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }

    private void toast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
