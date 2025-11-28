package com.example.chicksevent.fragment_admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chicksevent.R;
import com.example.chicksevent.adapter.EventAdminAdapter;
import com.example.chicksevent.adapter.ImageAdminAdapter;
import com.example.chicksevent.misc.Admin;
import com.example.chicksevent.misc.Event;

import java.util.ArrayList;

/**
 * Fragment that displays a list of all event images for admin management.
 * <p>
 * Allows an admin to view all images in the system and delete them via a confirmation dialog.
 * Events are loaded from Firebase using {@link Admin#browseEvents()} and displayed using
 * {@link EventAdminAdapter}.
 * </p>
 *
 * <p>
 * Deletion is performed via {@link Admin#deleteEvent(String)} and immediately reflected
 * in the UI by removing the event from the list and notifying the adapter.
 * </p>
 *
 * @see Admin
 * @see Event
 * @see EventAdminAdapter
 */
public class ImageAdminFragment extends Fragment {


    /**
     * RecyclerView that displays the list of events.
     * Uses a {@link LinearLayoutManager} and {@link EventAdminAdapter}.
     */
    private RecyclerView recyclerView;

    /**
     * Adapter responsible for binding {@link Event} data to RecyclerView items.
     * Configured with a delete click listener.
     */
    private ImageAdminAdapter adapter;

    /**
     * List holding all {@link Event} objects loaded from Firebase.
     * Serves as the backing data for the adapter.
     */
    private ArrayList<Event> eventList;

    /**
     * Admin instance used to perform privileged operations such as
     * browsing and deleting events.
     */
    private Admin admin;

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
        View view = inflater.inflate(R.layout.fragment_admin_image, container, false);

        recyclerView = view.findViewById(R.id.recycler_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        eventList = new ArrayList<>();
        admin = new Admin("ADMIN_DEFAULT");

        adapter = new ImageAdminAdapter(requireContext(), eventList, this::goToEvent, this::confirmDeletePoster);
        recyclerView.setAdapter(adapter);

        loadEvents();
        return view;
    }

    /**
     * Loads all events from Firebase using {@link Admin#browseEvents()}.
     * <p>
     * Clears the current list, adds all fetched events, and notifies the adapter
     * of data changes. Shows a toast and logs on failure.
     * </p>
     */
    private void loadEvents() {
        eventList.clear();

        admin.browseEvents()
                .addOnSuccessListener(events -> {
                    eventList.addAll(events);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                    Log.e("EventAdmin", "Error loading events", e);
                });
    }

    /**
     * Shows a confirmation dialog before deleting an event.
     * <p>
     * Called by {@link EventAdminAdapter} when the delete button is clicked.
     * </p>
     *
     * @param event the {@link Event} to be deleted
     */
    private void confirmDeleteEvent(Event event) {
        Log.i("DEL", "back in time " + event.getId());

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete the image of \"" + event.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent(event))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void goToEvent(Event event) {
        NavController navController = NavHostFragment.findNavController(ImageAdminFragment.this);

        Bundle bundle = new Bundle();
        bundle.putString("eventId", event.getId());

        navController.navigate(R.id.action_ImageAdmin_to_EventDetailFragment, bundle);
    }

    /**
     * Shows a confirmation dialog before deleting a poster.
     * <p>
     * Called by {@link EventAdminAdapter} when the delete button is clicked.
     * </p>
     *
     * @param event the {@link Event} that the poster will be deleted from.
     */
    private void confirmDeletePoster(Event event) {
        Log.i("DEL", "back in time " + event.getId());

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Poster")
                .setMessage("Are you sure you want to delete poster for \"" + event.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deletePoster(event))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes the specified event from Firebase and updates the UI.
     * <p>
     * Calls {@link Admin#deleteEvent(String)} to remove from database,
     * removes from {@link #eventList}, and notifies the adapter.
     * Shows a success toast.
     * </p>
     *
     * @param event the {@link Event} to delete
     */
    private void deleteEvent(Event event) {
        Log.i("DEL", "back in time " + event.getId());
        admin.deleteEvent(event.getId());
        eventList.remove(event);
        adapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
    }

    private void deletePoster(Event event) {
        Log.i("DEL", "back in time " + event.getId());
        admin.deletePoster(event.getId());
//        admin.deleteEvent(event.getId());
//        eventList.remove(event);
        adapter.notifyDataSetChanged();
        adapter.removeCache(event.getId());
        loadEvents();
        Toast.makeText(getContext(), "Poster deleted", Toast.LENGTH_SHORT).show();
    }
}