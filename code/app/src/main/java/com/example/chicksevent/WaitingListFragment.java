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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.databinding.FragmentCreateEventBinding;
import com.example.chicksevent.databinding.FragmentUpdateEventDetailBinding;
import com.example.chicksevent.databinding.FragmentWaitingListBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class WaitingListFragment extends Fragment {

    private FragmentWaitingListBinding binding;
    private ListView userView;
    private UserAdapter waitingListAdapter;
    private ArrayList<User> userDataList = new ArrayList<>();
    private FirebaseService waitingListService = new FirebaseService("WaitingList");
    private String TAG = "RTD8";
    String eventId;
    public WaitingListFragment() {
        // You can keep the constructor-empty and inflate via binding below
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWaitingListBinding.inflate(inflater, container, false);
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
        userView =  view.findViewById(R.id.recycler_notifications);
////
        userView.setAdapter(waitingListAdapter);

        poolingButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(WaitingListFragment.this);

            Bundle bundle = new Bundle();
            bundle.putString("eventName", args.getString("eventName"));

            navController.navigate(R.id.action_WaitingListFragment_to_PoolingFragment, bundle);
        });

        notificationButton.setOnClickListener(v -> {
                    NavHostFragment.findNavController(WaitingListFragment.this)
                            .navigate(R.id.action_WaitingListFragment_to_NotificationFragment);
                }
//
        );

        eventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(WaitingListFragment.this).navigate(R.id.action_UpdateEventFragment_to_EventFragment);
        });

        createEventButton.setOnClickListener(v -> {
//            NavHostFragment.findNavController(UpdateEventFragment.this).navigate(R.id.action_SecondFragment_to_CreateEventFragment);

            NavHostFragment.findNavController(WaitingListFragment.this).navigate(R.id.action_UpdateEventFragment_to_CreateEventFragment);
        });

        listEntrants();
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
