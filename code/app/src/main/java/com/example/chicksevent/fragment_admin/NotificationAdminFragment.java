package com.example.chicksevent.fragment_admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chicksevent.R;
import com.example.chicksevent.adapter.NotificationAdapter;
import com.example.chicksevent.enums.NotificationType;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.Notification;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Fragment that displays notification log
 */
public class NotificationAdminFragment extends Fragment {

    /**
     * ListView that displays the list of notification.
     */
    private ListView notificationView;

    private FirebaseService notificationService = new FirebaseService("Notification");

    /**
     * Adapter responsible for binding Notification data to RecyclerView items.
     * Configured with a delete click listener.
     */
    private NotificationAdapter adapter;

    /**
     * List holding all {@link Notification} objects loaded from Firebase.
     * Serves as the backing data for the adapter.
     */
    private ArrayList<Notification> notificationList;

    /**
     * Inflates the fragment layout and initializes the RecyclerView, adapter,
     * and admin instance. Begins loading event data from Firebase.
     *
     * @param inflater           the LayoutInflater to inflate the view
     * @param container          parent view that the fragment UI should attach to
     * @param savedInstanceState previous saved state (not used)
     * @return the root view of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_notification, container, false);

        notificationView = view.findViewById(R.id.recycler_notifications);
        notificationList = new ArrayList<>();

//        adapter = new NotificationAdapter(requireContext(), notificationList, item -> {}, item ->{});
//        notificationView.setAdapter(adapter);

        getNotificationList().addOnCompleteListener(task -> {
//            Log.i(TAG, "should i change");

            notificationList = task.getResult();
            ArrayList<Notification> notifNewList = new ArrayList<>();


            adapter = new NotificationAdapter(getContext(), notificationList, item -> {
//                Log.i("WATTHE", notificationList.size() + " : " + item.getEventId() + " : " + item.getNotificationType().toString());
//                for (Notification notif : notificationList) {
//                    Log.i("WATTHE", item.getEventId() + " : " + item.getNotificationType().toString());
//                    notifNewList.add(notif);
//                }
//                Log.i("WATTHE", "hi : " + notifNewList.size());
//                notificationList = notifNewList;
//                // DON'T DELETE THIS CUZ WE NEED TO RESET NOTIF
//                adapter = new NotificationAdapter(requireContext(), notificationList, item1 -> {}, item2 ->{});
//                notificationView.setAdapter(adapter);
            }, i -> {});



            notificationView.setAdapter(adapter);


//            Log.i(TAG, String.valueOf(notificationDataList.size()));

        });
        return view;
    }

    public Task<ArrayList<Notification>> getNotificationList() {
        return notificationService.getReference().get().continueWith(task -> {
            ArrayList<Notification> notificationList = new ArrayList<Notification>();

//            Log.d(TAG, "=== All Children at Root filter ===");
            for (DataSnapshot eventSnapshot : task.getResult().getChildren()) {

                for (DataSnapshot childSnapshot : eventSnapshot.getChildren()) {
                    String eventId = childSnapshot.getKey();
                    HashMap<String, HashMap<String, String>> value = (HashMap<String, HashMap<String, String>>) childSnapshot.getValue();
                    //                value.get("WAITING").

                    //                Log.d(TAG, "Key: " + eventId);
                    for (Map.Entry<String, HashMap<String, String>> entry : value.entrySet()) {
                        //                    Log.i(TAG, "Key: " + entry.getKey() + ", Value: " + entry.getValue());
                        //                    Log.d(TAG, "KKK: " + entry.getKey());

                        for (Map.Entry<String, String> entry2 : entry.getValue().entrySet()) {
                            //                        Log.i(TAG, "kkk2: " + entry.getKey() + ", Value: " + entry.getValue().get("message"));
                            NotificationType notificationType;
                            switch (entry.getKey()) {
                                case "WAITING":
                                    notificationType = NotificationType.WAITING;
                                    break;
                                case "INVITED":
                                    notificationType = NotificationType.INVITED;
                                    break;
                                case "UNINVITED":
                                    notificationType = NotificationType.UNINVITED;
                                    break;
                                default:
                                    notificationType = NotificationType.WAITING;
                                    break;
                            }

                            notificationList.add(new Notification(task.getResult().getKey(), eventId, notificationType, entry.getValue().get("message")));
                        }

                    }
                }

//

            }
            return notificationList;
        });
    }
}