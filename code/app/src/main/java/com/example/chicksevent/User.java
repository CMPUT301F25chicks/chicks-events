package com.example.chicksevent;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class User {

    private ArrayList<Event> eventList;
    private FirebaseService userService;
    private FirebaseService eventService;
    private String userId;
    private String username;
    private String phoneNumber;
    private String email;
    String TAG = "RTD8";

    User(String userId) {
        userId = userId;
        userService = new FirebaseService("User");
        eventService = new FirebaseService("Event");
    }

    public Task<Boolean> filterEvents(ArrayList<String> filterList) {
        Log.i(TAG, "what");
        Log.i(TAG, "e" + eventService);
        return eventService.getReference().get().continueWith(task -> {
            Log.d(TAG, "=== All Children at Root filter ===");

            // Iterate through all children
            for (DataSnapshot childSnapshot : task.getResult().getChildren()) {
                String key = childSnapshot.getKey();
                String[] value = ((Map<String, String>) childSnapshot.getValue()).get("tag").split(" ");


                Log.d(TAG, "Key: " + key);
                for (String val : value) {
                    if (filterList.contains(val)) {
                        return true;
                    }
                }
                Log.d(TAG, "Value: " + value);
                Log.d(TAG, "---");
            }

//                Log.d(TAG, "Total children: " + dataSnapshot.getChildrenCount());
            return false;
        });
    }

    public void listEvents() {
        Log.i(TAG, "what");
        Log.i(TAG, "e" + eventService);
        eventService.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "=== All Children at Root ===");

                // Iterate through all children
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String key = childSnapshot.getKey();
                    Object value = childSnapshot.getValue();

                    Log.d(TAG, "Key: " + key);
                    Log.d(TAG, "Value: " + value);
                    Log.d(TAG, "---");
                }

                Log.d(TAG, "Total children: " + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error reading data: " + databaseError.getMessage());
            }
        });
    }

//    public void listEvents() {
//        Log.i("RTD8", "hi");
////        Log.i("RTD8", String.format(eventService.getReference().get().getResult()));
//        eventService.getReference().get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
//            @Override
//            public void onSuccess(DataSnapshot snapshot) {
//                Log.i("RTD8", "in here");
//                Log.i("RTD8", String.format("%d", snapshot.getChildrenCount()));
//
//                Log.i("RTD8", String.valueOf(snapshot.getChildren()));
////                List<String> childrenKeys = new ArrayList<>();
////                List<Object> childrenValues = new ArrayList<>();
////                if (snapshot.getValue() != null) {
////                Log.i("RTDB", "Raw value: " + snapshot.getValue());
//////                }
//////                if (snapshot.getValue() != null) {
////                Log.i("RTDB", "poop");
////                }
////                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
//////                    String key = childSnapshot.getKey();                // e.g., "-Nabc123"
//////                    Object value = childSnapshot.getValue();            // Full child data
////                    // Or typed: String msg = childSnapshot.child("message").getValue(String.class);
////
////
////                    Log.i("RTDB", "Key: ");
////                }
//
//                Log.i("RTD8", "bruh");
//            }
//        }).addOnFailureListener(e -> {
//            Log.i("RTDB", "Error: " + e.getMessage());
//        });
//
//
//
//    }

    public Boolean isAdmin() {
        return false;
    }

    public Boolean isOrganizer() {
        return false;
    }
}
