package com.example.chicksevent;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/**
 * Utility service class that wraps Firebase Realtime Database operations for a specific root node.
 * <p>
 * Each instance of {@code FirebaseService} is scoped to a single database path, defined by
 * {@code refString} in the constructor. The service provides convenience methods to add, edit,
 * and delete entries or subcollection entries, while automatically logging success and failure
 * outcomes to Logcat.
 * </p>
 *
 * <h3>Example usage:</h3>
 * <pre>
 * FirebaseService eventService = new FirebaseService("Event");
 * HashMap<String, Object> data = new HashMap<>();
 * data.put("name", "Hackathon 2025");
 * data.put("location", "Edmonton");
 * String id = eventService.addEntry(data);
 * </pre>
 *
 * <p>All write operations are asynchronous; success or failure is reported via Logcat with the tag
 * <code>FirestoreTest</code>.</p>
 *
 * @author Jordan Kwan
 */
public class FirebaseService {

    /** Firebase Realtime Database instance bound to the project URL. */
    private FirebaseDatabase database;

    /** Database reference pointing to the specified root node. */
    private DatabaseReference reference;

    /**
     * Constructs a FirebaseService for the given database root.
     *
     * @param refString the root path within Firebase Realtime Database (e.g., "Event").
     */
    FirebaseService(String refString) {
        database = FirebaseDatabase.getInstance("https://listycity-friedchicken-default-rtdb.firebaseio.com/");
        reference = database.getReference(refString);
    }

    /**
     * Adds a new entry with a generated push key under the current reference.
     *
     * @param data key-value pairs representing the new entry.
     * @return the generated Firebase key.
     */
    public String addEntry(HashMap<String, Object> data) {
        String id = reference.push().getKey();
        reference.child(id).setValue(data)
                .addOnSuccessListener(a -> Log.d("FirestoreTest", "Success"))
                .addOnFailureListener(e -> Log.e("FirestoreTest", "Failed", e));
        return id;
    }

    /**
     * Adds or overwrites an entry with the given id under the current reference.
     *
     * @param data key-value pairs representing the entry data.
     * @param id explicit id under which to store the entry.
     * @return the id that was written.
     */
    public String addEntry(HashMap<String, Object> data, String id) {
        reference.child(id).setValue(data)
                .addOnSuccessListener(a -> Log.d("FirestoreTest", "Success"))
                .addOnFailureListener(e -> Log.e("FirestoreTest", "Failed", e));
        return id;
    }

    /**
     * Deletes the entry with the specified id under the current reference.
     *
     * @param id the key of the entry to remove.
     */
    public void deleteEntry(String id) {
        reference.child(id).removeValue()
                .addOnSuccessListener(a -> Log.d("FirestoreTest", "Success"))
                .addOnFailureListener(e -> Log.e("FirestoreTest", "Failed", e));
    }

    /**
     * Updates an existing entry with the provided data map.
     *
     * @param id the id of the entry to update.
     * @param data key-value pairs containing updated fields.
     * @return the id that was updated.
     */
    public String editEntry(String id, HashMap<String, Object> data) {
        reference.child(id).updateChildren(data)
                .addOnSuccessListener(a -> Log.d("FirestoreTest", "Success"))
                .addOnFailureListener(e -> Log.e("FirestoreTest", "Failed", e));
        return id;
    }

    /**
     * Updates a nested subcollection entry within a given parent node.
     *
     * @param parentId the parent node id.
     * @param subCollectionName the name of the subcollection node.
     * @param subId the id of the child within the subcollection to update.
     * @param updates the key-value pairs to update.
     */
    public void updateSubCollectionEntry(String parentId, String subCollectionName, String subId, HashMap<String, Object> updates) {
        reference.child(parentId).child(subCollectionName).child(subId)
                .updateChildren(updates)
                .addOnSuccessListener(a -> Log.d("FirestoreTest", "SubCollection Update Success"))
                .addOnFailureListener(e -> Log.e("FirestoreTest", "SubCollection Update Failed", e));
    }

    /**
     * Deletes a nested subcollection entry within a given parent node.
     *
     * @param parentId the parent node id.
     * @param subCollectionName the name of the subcollection node.
     * @param subId the id of the child within the subcollection to delete.
     */
    public void deleteSubCollectionEntry(String parentId, String subCollectionName, String subId) {
        reference.child(parentId).child(subCollectionName).child(subId).removeValue()
                .addOnSuccessListener(a -> Log.d("FirestoreTest", "SubCollection Delete Success"))
                .addOnFailureListener(e -> Log.e("FirestoreTest", "SubCollection Delete Failed", e));
    }

    /**
     * Retrieves the underlying {@link DatabaseReference} for direct Firebase operations.
     *
     * @return the database reference bound to this service.
     */
    public DatabaseReference getReference() {
        return reference;
    }
}