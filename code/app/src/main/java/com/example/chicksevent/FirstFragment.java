package com.example.chicksevent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.databinding.FragmentFirstBinding;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private FirebaseService service;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        service = new FirebaseService("bruhmoment");
        HashMap<String, Object> data = new HashMap<>();

//        data.put("username", "jim");
//        data.put("age", 43);
//        String id = service.addEntry(data);
//        data.put("phoneNumber", "403-420-6767");
//        id = service.editEntry(id, data);

        String androidId = Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        Log.i("FirestoreTest", "Android ID: " + androidId);
//        return androidId;

        User u = new User(androidId);
//        u.listEvents();


//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            Log.i("RTD8", "ww" + String.format("%d", e._waitingListSize));
//        }, 1000);

        ArrayList<String> filterList = new ArrayList<>();
        filterList.add("sportswer");
        u.filterEvents(filterList).addOnCompleteListener(task -> {
            Log.i("RTD8", String.format("good sh %b", task.getResult()));
        });


        Event event = new Event(
                u.getUserId(),
                "abc123",                           // id
                "Swimming Lessons",                 // name
                "Kids learn freestyle and backstroke", // eventDetails
                "2026-01-01",                       // eventStartDate
                "2026-02-01",                       // eventEndDate
                "2025-11-13",                       // registrationStartDate
                "2025-12-30",                       // registrationEndDate
                30,                                 // entrantLimit
                null,                               // poster
                "sports kids swimming"              // tag
        );

        event.createEvent();

        Log.d("RTD8", "even id: " + event.getId());

        Entrant e = new Entrant(androidId, event.getId());
        e.joinWaitingList();

        Entrant e2 = new Entrant(androidId + "poop", event.getId());
        e2.joinWaitingList();

        Organizer o = new Organizer(u.getUserId(), "jinn-event");
        o.listEntrants(EntrantStatus.INVITED);


//        e.swapStatus(EntrantStatus.INVITED);


//        e.updateWaitingListSize(EntrantStatus.INVITED).addOnCompleteListener(task -> {
//            Log.i("RTD8", String.format("amazing num %d", task.getResult()));
//        });
//        e.joinWaitingList();

//        o.listEntrants();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            Log.i("RTD8", "ww" + String.format("%d", e._waitingListSize));
            Log.i("RTD8", "what are in here here");
//            event.getOrganizer().getMatchingEvent(EntrantStatus.WAITING).addOnSuccessListener(task -> {
//                new Handler(Looper.getMainLooper()).postDelayed(() -> {
            event.getOrganizer().listEntrants();
//                }, 1000);
//            });

        }, 1000);

        event.getOrganizer().sendWaitingListNotification(EntrantStatus.WAITING, "this is a message to all waiting list people");

        String eventId = "jinn-event";

        Lottery lottery = new Lottery(eventId);
        lottery.runLottery();
//        Notification n = new Notification(u.getUserId(), event.getId(), NotificationType.WAITING, "this is a notification to people on waitaing list");
//        n.createNotification();



//        Log.i("RTD8", "wtf " + e.getWaitingListSize(EntrantStatus.INVITED));

//        service.deleteEntry(id);

//        binding.buttonFirst.setOnClickListener(v ->
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment)
//        );
        // VVV TEST CODE TO OPT-OUT OF NOTIFICATIONS VVV
        Log.d("NotificationTest", "User " + androidId + " is opting out of notifications.");
        User userToUpdate = new User(androidId);
        userToUpdate.updateNotificationPreference(false); // Set notifications to false
// ^^^ END OF TEST CODE ^^^
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}