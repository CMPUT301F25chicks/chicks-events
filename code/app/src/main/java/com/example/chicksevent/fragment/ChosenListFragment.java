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
import com.example.chicksevent.adapter.UserAdapter;
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

    /** Adapter that binds entrant data to the ListView. */
    private UserAdapter waitingListAdapter;

    /** Entrants currently in the invited list. */
    private ArrayList<Entrant> userDataList = new ArrayList<>();

    private FirebaseService waitingListService = new FirebaseService("WaitingList");

    private String TAG = "ChosenList";

    String eventId;

    public ChosenListFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChosenListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventName");
        }

        Button eventButton = view.findViewById(R.id.btn_events);
        Button createEventButton = view.findViewById(R.id.btn_addEvent);
        Button notificationButton = view.findViewById(R.id.btn_notification);
        Button sendNotificationButton = view.findViewById(R.id.btn_notification1);

        userView = view.findViewById(R.id.recycler_chosenUser);

        waitingListAdapter = new UserAdapter(getContext(), userDataList, eventId);
        userView.setAdapter(waitingListAdapter);

        notificationButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(ChosenListFragment.this)
                    .navigate(R.id.action_ChosenListFragment_to_NotificationFragment);
        });

        eventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(ChosenListFragment.this)
                    .navigate(R.id.action_ChosenListFragment_to_EventFragment);
        });

        createEventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(ChosenListFragment.this)
                    .navigate(R.id.action_ChosenListFragment_to_CreateEventFragment);
        });

        // Load INVITED entrants
        listEntrants(EntrantStatus.INVITED);

        sendNotificationButton.setOnClickListener(v -> {
            Organizer organizer = new Organizer(Settings.Secure.getString(
                    getContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID
            ), eventId);

            organizer.sendWaitingListNotification(EntrantStatus.INVITED, "chosen list notification");
            organizer.sendWaitingListNotification(EntrantStatus.UNINVITED, "not chosen notification");

            Toast.makeText(getContext(), "Notifications sent.", Toast.LENGTH_SHORT).show();
        });
    }

    public void listEntrants(EntrantStatus status) {
        waitingListService.getReference().child(eventId).child(status.toString())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        userDataList = new ArrayList<>();

                        for (DataSnapshot childSnap : dataSnapshot.getChildren()) {

                            String entrantId = childSnap.getKey();
                            String name = childSnap.child("name").getValue(String.class);
                            Entrant entrant = new Entrant(entrantId, name, status);

                            userDataList.add(entrant);
                        }

                        waitingListAdapter = new UserAdapter(getContext(), userDataList, eventId);
                        userView.setAdapter(waitingListAdapter);
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
