package com.example.chicksevent;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.databinding.FragmentCreateEventBinding;
import com.example.chicksevent.databinding.FragmentEventDetailBinding;
import com.example.chicksevent.databinding.FragmentEventDetailOrgBinding;

public class EventDetailOrgFragment extends Fragment {

    private FragmentEventDetailOrgBinding binding;

    public EventDetailOrgFragment() {
        // You can keep the constructor-empty and inflate via binding below
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventDetailOrgBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView eventName = view.findViewById(R.id.tv_event_name);

        Bundle args = getArguments();
        if (args != null) {
            eventName.setText(args.getString("eventName"));
            // Use it to populate UI
        }

        Button eventButton = view.findViewById(R.id.btn_events);
        Button createEventButton = view.findViewById(R.id.btn_addEvent);
        Button notificationButton = view.findViewById(R.id.btn_notification);

        Button viewWaitingListButton = view.findViewById(R.id.btn_waiting_list);
        Button viewChosenListButton = view.findViewById(R.id.btn_chosen_entrants);
        viewWaitingListButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailOrgFragment.this);

            Bundle bundle = new Bundle();
            bundle.putString("eventName", args.getString("eventName"));
//            bundle.putString("organizerId", args.getString("organizerId"));


            navController.navigate(R.id.action_EventDetailOrgFragment_to_WaitingListFragment, bundle);

//            NavHostFragment.findNavController(EventDetailOrgFragment.this)
//                    .navigate(R.id.action_EventDetailOrgFragment_to_WaitingListFragment);
        });

        viewChosenListButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailOrgFragment.this);

            Bundle bundle = new Bundle();
            bundle.putString("eventName", args.getString("eventName"));
//            bundle.putString("organizerId", args.getString("organizerId"));


            navController.navigate(R.id.action_EventDetailOrgFragment_to_ChosenListFragment, bundle);

//            NavHostFragment.findNavController(EventDetailOrgFragment.this)
//                    .navigate(R.id.action_EventDetailOrgFragment_to_WaitingListFragment);
        });

        notificationButton.setOnClickListener(v -> {
                    NavHostFragment.findNavController(EventDetailOrgFragment.this)
                            .navigate(R.id.action_EventDetailOrgFragment_to_NotificationFragment);
                }
//
        );

        eventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(EventDetailOrgFragment.this).navigate(R.id.action_EventDetailOrgFragment_to_EventFragment);
        });

        createEventButton.setOnClickListener(v -> {
//            NavHostFragment.findNavController(EventDetailFragment.this).navigate(R.id.action_SecondFragment_to_CreateEventFragment);

            NavHostFragment.findNavController(EventDetailOrgFragment.this).navigate(R.id.action_EventDetailOrgFragment_to_CreateEventFragment);
        });

        createEventButton.setOnClickListener(v -> {
//            NavHostFragment.findNavController(EventDetailFragment.this).navigate(R.id.action_SecondFragment_to_CreateEventFragment);

            NavHostFragment.findNavController(EventDetailOrgFragment.this).navigate(R.id.action_EventDetailOrgFragment_to_CreateEventFragment);
        });
//        eventName
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
