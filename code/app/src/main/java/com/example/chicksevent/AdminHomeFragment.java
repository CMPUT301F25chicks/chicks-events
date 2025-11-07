package com.example.chicksevent;

import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.databinding.FragmentAdminHomeBinding;

import java.util.ArrayList;


public class AdminHomeFragment extends Fragment {

    private FragmentAdminHomeBinding binding;
    ArrayList<Notification> notificationDataList = new ArrayList<Notification>();
    FirebaseService notificationService;
    NotificationAdapter notificationAdapter;
    NotificationAdapter notificationView;

    public AdminHomeFragment() {
        notificationService = new FirebaseService("Notification");
        // You can keep the constructor-empty and inflate via binding below
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button eventButton = view.findViewById(R.id.btn_events);
        Button createEventButton = view.findViewById(R.id.btn_addEvent);
        Button profileButton = view.findViewById(R.id.btn_profile);


        eventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(AdminHomeFragment.this).navigate(R.id.action_AdminHomeFragment_to_EventFragment);
        });

        createEventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(AdminHomeFragment.this).navigate(R.id.action_AdminHomeFragment_to_CreateEventFragment);
        });

        profileButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(AdminHomeFragment.this).navigate(R.id.action_AdminHomeFragment_to_ProfileFragment);

        });

        Button btnEvents = view.findViewById(R.id.btn_admin_event);
        Button btnOrganizers = view.findViewById(R.id.btn_admin_org);
        Button btnProfiles = view.findViewById(R.id.btn_admin_profile);
        User userToUpdate = new User(Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        ));

        ListView notificationView = view.findViewById(R.id.recycler_notifications);




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
//            notificationDataList = notifNewList;
////
//            notificationAdapter.notifyDataSetChanged();


//            Log.i(TAG, String.valueOf(notificationDataList.size()));

        });
        btnEvents.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminHomeFragment.this)
                        .navigate(R.id.action_adminHome_to_eventAdminFragment));

        btnOrganizers.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminHomeFragment.this)
                        .navigate(R.id.action_adminHome_to_orgAdminFragment));

        btnProfiles.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminHomeFragment.this)
                        .navigate(R.id.action_adminHome_to_profileAdminFragment));




//        (new User("ejwlrkwrer")).createMockUser();
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
