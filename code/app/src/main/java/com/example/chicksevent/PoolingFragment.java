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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.databinding.FragmentCreateEventBinding;
import com.example.chicksevent.databinding.FragmentPoolingBinding;
import com.example.chicksevent.databinding.FragmentUpdateEventDetailBinding;
import com.example.chicksevent.databinding.FragmentWaitingListBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PoolingFragment extends Fragment {

    private FragmentPoolingBinding binding;
    private ListView userView;
    private UserAdapter waitingListAdapter;
    private ArrayList<User> userDataList = new ArrayList<>();
    private FirebaseService waitingListService = new FirebaseService("WaitingList");
    private String TAG = "RTD8";
    String eventId;
    public PoolingFragment() {
        // You can keep the constructor-empty and inflate via binding below
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPoolingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventName");
            // Use it to populate UI
        }

        Button eventButton = view.findViewById(R.id.btn_events);
        Button createEventButton = view.findViewById(R.id.btn_addEvent);
        Button notificationButton = view.findViewById(R.id.btn_notification);
        Button poolingButton = view.findViewById(R.id.btn_pool);
        waitingListAdapter = new UserAdapter(getContext(), userDataList);
        userView =  view.findViewById(R.id.rv_selected_entrants);
////
        userView.setAdapter(waitingListAdapter);
//
        notificationButton.setOnClickListener(v -> {
                    NavHostFragment.findNavController(PoolingFragment.this)
                            .navigate(R.id.action_PoolingFragment_to_NotificationFragment);
                }
//
        );

        eventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(PoolingFragment.this).navigate(R.id.action_PoolingFragment_to_EventFragment);
        });

        createEventButton.setOnClickListener(v -> {
//            NavHostFragment.findNavController(UpdateEventFragment.this).navigate(R.id.action_SecondFragment_to_CreateEventFragment);

            NavHostFragment.findNavController(PoolingFragment.this).navigate(R.id.action_PoolingFragment_to_CreateEventFragment);
        });

        poolingButton.setOnClickListener(v -> {
//            NavHostFragment.findNavController(UpdateEventFragment.this).navigate(R.id.action_SecondFragment_to_CreateEventFragment);
            Lottery l = new Lottery(eventId);
            l.runLottery();
            listEntrants(EntrantStatus.INVITED);
        });


//        eventName
    }

    public void listEntrants() {
        listEntrants(EntrantStatus.WAITING);
    }
    public void listEntrants(EntrantStatus status) {

        Log.i(TAG, "in here " + eventId + " " + status);
        waitingListService.getReference().child(eventId).child(status.toString())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i(TAG, "IN HERE bef " + status);
                        userDataList = new ArrayList<>();
                        for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                            userDataList.add(new User(childSnap.getKey()));
//                            Log.i(TAG, "child key: " + childSnap.getKey());
                        }

                        waitingListAdapter = new UserAdapter(getContext(), userDataList);
////
                        userView.setAdapter(waitingListAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Error reading data: " + databaseError.getMessage());
                    }
                });
    }

    private static String s(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
