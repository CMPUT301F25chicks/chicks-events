package com.example.chicksevent;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class User {

    private ArrayList<Event> eventList;
    private FirebaseService userService;
    private FirebaseService eventService;
    private String userId;
    String TAG = "RTD8";

    User(String userId) {
        userId = userId;
        userService = new FirebaseService("User");
        eventService = new FirebaseService("Event");
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

    public void printAllChildrenDetailed() {
        eventService.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "=== Detailed Children at Root ===");

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    printSnapshot(childSnapshot, 0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error: " + databaseError.getMessage());
            }
        });
    }

    // Recursive method to print nested data
    private void printSnapshot(DataSnapshot snapshot, int level) {
        String indent = new String(new char[level * 2]).replace('\0', ' ');

        if (snapshot.hasChildren()) {
            Log.d(TAG, indent + snapshot.getKey() + ":");
            for (DataSnapshot child : snapshot.getChildren()) {
                printSnapshot(child, level + 1);
            }
        } else {
            Log.d(TAG, indent + snapshot.getKey() + ": " + snapshot.getValue());
        }
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
