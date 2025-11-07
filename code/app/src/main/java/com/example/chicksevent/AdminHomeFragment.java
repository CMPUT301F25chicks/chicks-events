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
    NotificationAdapter notificationView;

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

        // Navigation buttons for main sections
        Button eventButton = view.findViewById(R.id.btn_events);
        Button createEventButton = view.findViewById(R.id.btn_addEvent);
        Button profileButton = view.findViewById(R.id.btn_profile);

        eventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(AdminHomeFragment.this)
                    .navigate(R.id.action_AdminHomeFragment_to_EventFragment);
        });

        createEventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(AdminHomeFragment.this)
                    .navigate(R.id.action_AdminHomeFragment_to_CreateEventFragment);
        });

        profileButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(AdminHomeFragment.this)
                    .navigate(R.id.action_AdminHomeFragment_to_ProfileFragment);
        });

        // Admin management buttons
        Button btnEvents = view.findViewById(R.id.btn_admin_event);
        Button btnOrganizers = view.findViewById(R.id.btn_admin_org);
        Button btnProfiles = view.findViewById(R.id.btn_admin_profile);

        // Current user identified by Android ID
        User userToUpdate = new User(Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        ));

        // ListView to display notifications
        ListView notificationView = view.findViewById(R.id.recycler_notifications);

        // Load notifications and set up adapter with click-to-delete behavior
        userToUpdate.getNotificationList().addOnCompleteListener(task -> {
            notificationDataList = task.getResult();
            ArrayList<Notification> notifNewList = new ArrayList<>();

            notificationAdapter = new NotificationAdapter(getContext(), notificationDataList, item -> {
                Log.i("WATTHE", notificationDataList.size() + " : " + item.getEventId() + " : " + item.getNotificationType().toString());

                // Remove clicked notification from Firebase and update local list
                for (Notification notif : notificationDataList) {
                    if (item.getNotificationType() == notif.getNotificationType() &&
                            item.getEventId().equals(notif.getEventId())) {
                        notificationService.deleteSubCollectionEntry(
                                userToUpdate.getUserId(),
                                item.getEventId(),
                                item.getNotificationType().toString());
                    } else {
                        notifNewList.add(notif);
                    }
                }

                // Update data source and refresh adapter
                notificationDataList = notifNewList;
                notificationAdapter = new NotificationAdapter(getContext(), notificationDataList, b -> {});
                notificationView.setAdapter(notificationAdapter);
            });

            // Initial adapter setup
            notificationView.setAdapter(notificationAdapter);
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