package com.example.chicksevent.fragment;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.R;
import com.example.chicksevent.adapter.EntrantAdapter;
import com.example.chicksevent.databinding.FragmentChosenListBinding;
import com.example.chicksevent.enums.EntrantStatus;
import com.example.chicksevent.misc.Entrant;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.Organizer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChosenListFragment extends Fragment {

    private FragmentChosenListBinding binding;
    private ListView userView;
    private EntrantAdapter entrantAdapter;
    private ArrayList<Entrant> entrantDataList = new ArrayList<>();
    private FirebaseService waitingListService = new FirebaseService("WaitingList");
    private static final String TAG = "RTD8";
    private String eventId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChosenListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) eventId = args.getString("eventName");

        userView = view.findViewById(R.id.recycler_chosenUser);

        Button eventButton = view.findViewById(R.id.btn_events);
        Button createEventButton = view.findViewById(R.id.btn_addEvent);
        Button notificationButton = view.findViewById(R.id.btn_notification);
        Button sendNotificationButton = view.findViewById(R.id.btn_notification1);

        entrantAdapter = new EntrantAdapter(getContext(), entrantDataList);

        userView.setAdapter(entrantAdapter);

        // Navigation
        eventButton.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_ChosenListFragment_to_EventFragment));
        createEventButton.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_ChosenListFragment_to_CreateEventFragment));
        notificationButton.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_ChosenListFragment_to_NotificationFragment));

        // Load invited entrants
        listEntrants(EntrantStatus.INVITED);

        sendNotificationButton.setOnClickListener(v -> {
            Organizer organizer = new Organizer(Settings.Secure.getString(
                    getContext().getContentResolver(), Settings.Secure.ANDROID_ID), eventId);
            organizer.sendWaitingListNotification(EntrantStatus.INVITED, "chosen list notification");
            organizer.sendWaitingListNotification(EntrantStatus.UNINVITED, "NOT chosen list notification");
            Toast.makeText(getContext(), "chosen list notification sent", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Load entrants by status
     */
    private void listEntrants(EntrantStatus status) {
        waitingListService.getReference()
                .child(eventId)
                .child(status.toString())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        entrantDataList = new ArrayList<>();
                        for (DataSnapshot childSnap : snapshot.getChildren()) {
                            entrantDataList.add(new Entrant(childSnap.getKey(), eventId));
                        }
                        entrantAdapter = new EntrantAdapter(getContext(), entrantDataList);
                        userView.setAdapter(entrantAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error reading data: " + error.getMessage());
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
