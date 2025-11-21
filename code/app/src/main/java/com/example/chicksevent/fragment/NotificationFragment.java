package com.example.chicksevent.fragment;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.R;
import com.example.chicksevent.adapter.NotificationAdapter;
import com.example.chicksevent.databinding.FragmentNotificationBinding;
import com.example.chicksevent.misc.Event;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.Notification;
import com.example.chicksevent.misc.User;

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

    private FirebaseService notificationService;

    ListView notificationView;
    User userToUpdate;

    /**
     * Inflates the fragment layout using ViewBinding.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        notificationService = new FirebaseService("Notification");

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
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        Log.i("ANDROID-ID", "Android ID used for test: " + androidId);

//        createMockEvent();

        notificationView = view.findViewById(R.id.recycler_notifications);
        userToUpdate = new User(androidId);





        userToUpdate.isAdmin().addOnCompleteListener(v -> {
            if (v.getResult()) {
                Log.i("im admin", "yay");
                NavHostFragment.findNavController(NotificationFragment.this)
                        .navigate(R.id.action_NotificationFragment_to_AdminHomeFragment);
            } else {
                Log.i("im admin", "no");
                userToUpdate.getNotificationList().addOnCompleteListener(task -> {
//            Log.i(TAG, "should i change");

                    notificationDataList = task.getResult();



                    notificationAdapter = new NotificationAdapter(getContext(), notificationDataList, item -> {
                        notificationDeleteListener(item);
                    }, item -> notificationEventListener(item));



                    notificationView.setAdapter(notificationAdapter);


//            Log.i(TAG, String.valueOf(notificationDataList.size()));

                });
            }
        });




    }

    public void notificationEventListener(Notification item) {
        NavController navController = NavHostFragment.findNavController(this);

        Bundle bundle = new Bundle();
        bundle.putString("eventId", item.getEventId());

        navController.navigate(R.id.action_NotificationFragment_to_EventDetailFragment, bundle);
    }

    public void notificationDeleteListener(Notification item) {
//        ArrayList<Notification> notifNewList = new ArrayList<>();
        Log.i("WATTHE", notificationDataList.size() + " : " + item.getEventId() + " : " + item.getNotificationType().toString());
        for (Notification notif : notificationDataList) {
            Log.i("WATTHE", item.getEventId() + " : " + item.getNotificationType().toString());

            if (item.getNotificationType() == notif.getNotificationType() && item.getEventId().equals(notif.getEventId())) {
                notificationService.deleteSubCollectionEntry(userToUpdate.getUserId(), item.getEventId(), item.getNotificationType().toString());
                notificationAdapter.remove(notif);
                notificationAdapter.notifyDataSetChanged();
            } else {
//                notifNewList.add(notif);
            }
        }
//        Log.i("WATTHE", "hi : " + notifNewList.size());
//        notificationDataList = notifNewList;
//        // DON'T DELETE THIS CUZ WE NEED TO RESET NOTIF
//        notificationAdapter = new NotificationAdapter(getContext(), notificationDataList, b -> notificationDeleteListener(b));
//        notificationView.setAdapter(notificationAdapter);
    }

    /**
     * Example helper for creating a mock event (disabled by default).
     */
    private void createMockEvent() {
        User userToUpdate = new User(androidId);
        Event event = new Event(
                userToUpdate.getUserId(),
                "abc123",                           // id
                "Swimming Lessons",                 // name
                "Kids learn freestyle and backstroke", // eventDetails
                "OCT/n30",
                "2026-01-01",                       // eventStartDate
                "2026-02-01",                       // eventEndDate
                "2025-11-13",                       // registrationStartDate
                "2025-12-30",                       // registrationEndDate
                30,                                 // entrantLimit
                null,                               // poster
                "sports kids swimming",              // tag
                false
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
