package com.example.chicksevent;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class FirebaseService {
    private FirebaseDatabase database;
    private DatabaseReference reference;

    FirebaseService(String refString) {
        database = FirebaseDatabase.getInstance("https://listycity-friedchicken-default-rtdb.firebaseio.com/");
        reference = database.getReference(refString);
    }

    public String addEntry(HashMap<String, Object> data){

        String id = reference.push().getKey();
        reference.child(id).setValue(data)
                .addOnSuccessListener(a -> Log.d("FirestoreTest", "Success"))
                .addOnFailureListener(e -> Log.e("FirestoreTest", "Failed", e));;
        return id;
    }

    public void deleteEntry(String id){
        HashMap<String, String> data = new HashMap<>();
        reference.child(id).removeValue()
                .addOnSuccessListener(a -> Log.d("FirestoreTest", "Success"))
                .addOnFailureListener(e -> Log.e("FirestoreTest", "Failed", e));;
    }

    public String editEntry(String id, HashMap<String, Object> data){
        reference.child(id).updateChildren(data)
                .addOnSuccessListener(a -> Log.d("FirestoreTest", "Success"))
                .addOnFailureListener(e -> Log.e("FirestoreTest", "Failed", e));
        return id;
    }


    public DatabaseReference getReference() {
        return reference;
    }
}


