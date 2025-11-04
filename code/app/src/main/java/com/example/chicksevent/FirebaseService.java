package com.example.chicksevent;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseService {
    private static final String TAG = "RTDB";
    private final FirebaseDatabase database;
    private final DatabaseReference reference;

    // Pass the path you want to work under, e.g., new FirebaseService("events")
    FirebaseService(String refString) {
        database = FirebaseDatabase.getInstance("https://listycity-friedchicken-default-rtdb.firebaseio.com/");
        reference = database.getReference(refString);
    }

    /** Simple callback for createEvent */
    public interface OnEventCreated {
        void onSuccess(String eventId);
        void onError(Exception e);
    }




    /** Generic helpers you already had (kept, but tag fixed) */
    public String addEntry(java.util.HashMap<String, Object> data){
        String id = reference.push().getKey();
        reference.child(id).setValue(data)
                .addOnSuccessListener(a -> Log.d(TAG, "addEntry success"))
                .addOnFailureListener(e -> Log.e(TAG, "addEntry failed", e));
        return id;
    }

    public void deleteEntry(String id){
        reference.child(id).removeValue()
                .addOnSuccessListener(a -> Log.d(TAG, "deleteEntry success"))
                .addOnFailureListener(e -> Log.e(TAG, "deleteEntry failed", e));
    }

    public String editEntry(String id, java.util.HashMap<String, Object> data){
        reference.child(id).updateChildren(data)
                .addOnSuccessListener(a -> Log.d(TAG, "editEntry success"))
                .addOnFailureListener(e -> Log.e(TAG, "editEntry failed", e));
        return id;
    }

    public DatabaseReference getReference() { return reference; }

    // NOTE: We intentionally removed Firestore & Storage methods for this story.
    // If/when you want to persist poster/QR images, we’ll add Firebase Storage back.
}
