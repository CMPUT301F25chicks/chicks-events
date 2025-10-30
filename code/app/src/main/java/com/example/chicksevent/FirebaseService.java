package com.example.chicksevent;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseService {
    private FirebaseDatabase database;
    private DatabaseReference reference;

    FirebaseService(String refString) {
        database = FirebaseDatabase.getInstance("https://listycity-friedchicken-default-rtdb.firebaseio.com/");
        reference = database.getReference(refString);
    }

    public DatabaseReference getReference() {
        return reference;
    }
}
