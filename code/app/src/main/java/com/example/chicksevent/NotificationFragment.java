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

/**
 * Fragment that displays notifications addressed to the current user.
 * <p>
 * Binds a {@link ListView} to {@link NotificationAdapter} and loads the user's notifications
 * using their Android ID as the user key. Also exposes navigation to the Event list and
 * Create Event flows.
 * </p>
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Resolve device Android ID and use it to fetch the user's notification list.</li>
 *   <li>Initialize and bind the {@link NotificationAdapter}.</li>
 *   <li>Provide quick navigation to related screens.</li>
 * </ul>
 * </p>
 *
 * @author Jordan Kwan
 */
public class NotificationFragment extends Fragment {

    /** View binding for the notification layout. */
    private FragmentNotificationBinding binding;

    /** Firebase service (placeholder root used during development). */
    private FirebaseService service;

    /** Backing list for notifications to render. */
    ArrayList<Notification> notificationDataList = new ArrayList<>();

    /** Adapter bridging notifications to the ListView. */
    NotificationAdapter notificationAdapter;

    /** Current device Android ID (used as the user identifier). */
    private String androidId;

    /** Log tag. */
    private static final String TAG = "RTD8";

    /**
     * Inflates the fragment layout using ViewBinding.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Initializes services, resolves the device ID, sets up the list adapter, and fetches
     * notifications for the current user.
     *
     * @param view the root view returned by {@link #onCreateView}.
     * @param savedInstanceState previously saved instance state, if any.
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        service = new FirebaseService("bruhmoment");
        HashMap<String, Object> data = new HashMap<>();

        androidId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        Log.i("ANDROID-ID", "Android ID used for test: " + androidId);

        ListView notificationView = view.findViewById(R.id.recycler_notifications);
        notificationAdapter = new NotificationAdapter(getContext(), notificationDataList);
        notificationView.setAdapter(notificationAdapter);

        // Fetch notifications for the current user and update adapter
        User userToUpdate = new User(androidId);
        userToUpdate.getNotificationList().addOnCompleteListener(task -> {
            Log.i(TAG, "Refreshing notification list");
            notificationDataList = task.getResult();
            notificationAdapter = new NotificationAdapter(getContext(), notificationDataList);
            Log.i(TAG, String.valueOf(notificationDataList.size()));
            notificationView.setAdapter(notificationAdapter);
        });

        // Navigation buttons
        Button eventButton = view.findViewById(R.id.btn_events);
        Button createEventButton = view.findViewById(R.id.btn_addEvent);

        eventButton.setOnClickListener(v ->
                NavHostFragment.findNavController(NotificationFragment.this)
                        .navigate(R.id.action_NotificationFragment_to_EventFragment)
        );

        createEventButton.setOnClickListener(v ->
                NavHostFragment.findNavController(NotificationFragment.this)
                        .navigate(R.id.action_NotificationFragment_to_CreateEventFragment)
        );
    }

    /**
     * Example helper for creating a mock event (disabled by default).
     */
    private void createMockEvent() {
        User userToUpdate = new User(androidId);
        Event event = new Event(
                userToUpdate.getUserId(),
                "abc123",
                "Swimming Lessons",
                "Kids learn freestyle and backstroke",
                "2026-01-01",
                "2026-02-01",
                "2025-11-13",
                "2025-12-30",
                30,
                null,
                "sports kids swimming"
        );

        event.createEvent();
    }

    /**
     * Releases binding resources when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
