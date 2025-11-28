package com.example.chicksevent.fragment_admin;

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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.R;
import com.example.chicksevent.adapter.NotificationAdapter;
import com.example.chicksevent.databinding.FragmentAdminHomeBinding;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.Notification;
import com.example.chicksevent.misc.User;

import java.util.ArrayList;


/**
 * A {@link Fragment} subclass representing the admin home screen.
 * Displays navigation options for managing events, organizers, profiles, and shows
 * a list of user notifications retrieved from Firebase.
 * Handles navigation to various admin sections and manages notification deletion.
 * @author Jordan Kwan
 */
public class AdminHomeFragment extends Fragment {

    /** View binding for the fragment's layout. */
    private FragmentAdminHomeBinding binding;

    /** List holding the current notifications to be displayed. */
    ArrayList<Notification> notificationDataList = new ArrayList<Notification>();

    /** Service for interacting with Firebase "Notification" subcollection. */
    FirebaseService notificationService;

    /** Adapter for binding notification data to the ListView. */
    NotificationAdapter notificationAdapter;

    /** Reference to the ListView displaying notifications (kept for adapter updates). */
    ListView notificationView;

    User userToUpdate;

    /**
     * Default constructor. Initializes the {@link FirebaseService} for notifications.
     */
    public AdminHomeFragment() {
        notificationService = new FirebaseService("Notification");
    }

    /**
     * Inflates the fragment layout using view binding.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views.
     * @param container          Parent view that the fragment UI should be attached to.
     * @param savedInstanceState Previous saved state, if any.
     * @return The root view of the inflated layout.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called immediately after {@link #onCreateView} has returned.
     * Sets up button click listeners for navigation and loads user notifications.
     *
     * @param view               The inflated view.
     * @param savedInstanceState Previous saved state, if any.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Admin management buttons
        Button btnNotification = view.findViewById(R.id.btn_admin_notification);
        Button btnEvents = view.findViewById(R.id.btn_admin_event);
        Button btnOrganizers = view.findViewById(R.id.btn_admin_org);
        Button btnProfiles = view.findViewById(R.id.btn_admin_profile);
        Button btnImages = view.findViewById(R.id.btn_admin_avatar);
        // Current user identified by Android ID
        userToUpdate = new User(Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        ));

        // ListView to display notifications
        notificationView = view.findViewById(R.id.recycler_notifications);

        // Load notifications and set up adapter with click-to-delete behavior
        userToUpdate.getNotificationList().addOnCompleteListener(task -> {
//            Log.i(TAG, "should i change");

            notificationDataList = task.getResult();



            notificationAdapter = new NotificationAdapter(getContext(), notificationDataList, item -> {
                notificationDeleteListener(item);
            }, item -> notificationEventListener(item));



            notificationView.setAdapter(notificationAdapter);


//            Log.i(TAG, String.valueOf(notificationDataList.size()));

        });

        // Admin section navigation
        btnEvents.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminHomeFragment.this)
                        .navigate(R.id.action_adminHome_to_eventAdminFragment));

        btnOrganizers.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminHomeFragment.this)
                        .navigate(R.id.action_adminHome_to_orgAdminFragment));

        btnProfiles.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminHomeFragment.this)
                        .navigate(R.id.action_adminHome_to_profileAdminFragment));
        btnImages.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminHomeFragment.this)
                        .navigate(R.id.action_adminHome_to_imageAdminFragment));
        btnNotification.setOnClickListener(v -> NavHostFragment.findNavController(AdminHomeFragment.this)
                .navigate(R.id.action_adminHome_to_notificationAdminFragment));
    }

    public void notificationEventListener(Notification item) {
        Log.i("going to event detail", "");
        NavController navController = NavHostFragment.findNavController(this);

        Bundle bundle = new Bundle();
        bundle.putString("eventId", item.getEventId());

        navController.navigate(R.id.action_AdminHomeFragment_to_EventDetailFragment, bundle);
    }

    public void notificationDeleteListener(Notification item) {
//        ArrayList<Notification> notifNewList = new ArrayList<>();
        Notification notifDelete = null;
        Log.i("WATTHE", notificationDataList.size() + " : " + item.getEventId() + " : " + item.getNotificationType().toString());
        for (Notification notif : notificationDataList) {
            Log.i("WATTHE", item.getEventId() + " : " + item.getNotificationType().toString());

            if (item.getNotificationType() == notif.getNotificationType() && item.getEventId().equals(notif.getEventId())) {
                notificationService.deleteSubCollectionEntry(userToUpdate.getUserId(), item.getEventId(), item.getNotificationType().toString());
                notifDelete = notif;
//                notificationAdapter.remove(item);
//                notificationAdapter.notifyDataSetChanged();
            } else {
//                notifNewList.add(notif);
            }
        }

        if (notifDelete != null) {
            notificationAdapter.remove(item);
            notificationAdapter.notifyDataSetChanged();
        }
//        Log.i("WATTHE", "hi : " + notifNewList.size());
//        notificationDataList = notifNewList;
//        // DON'T DELETE THIS CUZ WE NEED TO RESET NOTIF
//        notificationAdapter = new NotificationAdapter(getContext(), notificationDataList, b -> notificationDeleteListener(b));
//        notificationView.setAdapter(notificationAdapter);
    }

    /**
     * Utility method to safely convert a CharSequence to a trimmed String.
     *
     * @param cs The CharSequence to convert; may be null.
     * @return A non-null trimmed string; empty if input is null.
     */
    private static String s(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }

    /**
     * Displays a short toast message.
     *
     * @param msg The message to display.
     */
    private void toast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when the view hierarchy is being destroyed.
     * Releases the binding reference to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}