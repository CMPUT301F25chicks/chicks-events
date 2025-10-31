package com.example.chicksevent;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class FirebaseService implements EntryStore {
    private final FirebaseDatabase database;
    private final DatabaseReference reference;

    public FirebaseService(String refString) {
        database = FirebaseDatabase.getInstance("https://listycity-friedchicken-default-rtdb.firebaseio.com/");
        reference = database.getReference(refString);
    }

    @Override
    public String addEntry(HashMap<String, Object> data) {
        String id = reference.push().getKey();
        reference.child(id).setValue(data)
                .addOnSuccessListener(a -> Log.d("RTDB", "addEntry: success"))
                .addOnFailureListener(e -> Log.e("RTDB", "addEntry: failed", e));
        return id;
    }

    public void deleteEntry(String id) {
        reference.child(id).removeValue()
                .addOnSuccessListener(a -> Log.d("RTDB", "deleteEntry: success"))
                .addOnFailureListener(e -> Log.e("RTDB", "deleteEntry: failed", e));
    }

    public String editEntry(String id, HashMap<String, Object> data) {
        deleteEntry(id);
        return addEntry(data);
    }

    public DatabaseReference getReference() {
        return reference;
    }
}
