package com.example.chicksevent.fragment_admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chicksevent.adapter.OrganizerAdapter;
import com.example.chicksevent.databinding.FragmentAdminOrgBinding;
import com.example.chicksevent.misc.Admin;
import com.example.chicksevent.misc.Organizer;

import java.util.ArrayList;

/**
 * Fragment that displays a list of all organizers in the system for administrative management.
 * <p>
 * Allows an admin to view all registered organizers and delete them via a confirmation dialog.
 * Data is loaded from Firebase using {@link Admin#browseOrganizers()} and displayed using
 * {@link OrganizerAdapter}.
 * </p>
 *
 * <p>
 * Deletion is performed via {@link Admin#deleteOrganizerProfile(String)} and reflected
 * immediately in the UI.
 * </p>
 *
 * @see Admin
 * @see Organizer
 * @see OrganizerAdapter
 * @see FragmentAdminOrgBinding
 */
public class OrgAdminFragment extends Fragment {

    /**
     * RecyclerView that displays the list of organizers.
     * Uses a {@link LinearLayoutManager} and {@link OrganizerAdapter}.
     */
    private RecyclerView recyclerView;

    /**
     * List holding all {@link Organizer} objects loaded from Firebase.
     * Backing data for the adapter.
     */
    private ArrayList<Organizer> organizerList = new ArrayList<>();

    /**
     * Adapter responsible for binding {@link Organizer} data to RecyclerView items.
     * Configured with a delete click listener.
     */
    private OrganizerAdapter adapter;

    /**
     * Admin instance used to perform privileged operations such as
     * browsing and deleting organizers.
     */
    private Admin admin;

    /**
     * View binding for the {@code fragment_admin_org.xml} layout.
     * Provides type-safe access to all views.
     */
    private FragmentAdminOrgBinding binding;

    /**
     * Inflates the fragment layout using View Binding.
     *
     * @param inflater           the LayoutInflater to inflate the view
     * @param container          parent view that the fragment UI should attach to
     * @param savedInstanceState previous saved state (not used)
     * @return the root view of the fragment
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAdminOrgBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the view is created. Initializes the UI components,
     * sets up the RecyclerView and adapter, and loads organizer data.
     *
     * @param view               the root view returned by {@link #onCreateView}
     * @param savedInstanceState previous saved state (not used)
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        admin = new Admin("ADMIN_DEFAULT");
        recyclerView = binding.recyclerChosenUser;

        // Adapter with delete click callback
        adapter = new OrganizerAdapter(organizerList, this::confirmDeleteOrganizer);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        loadOrganizers();
    }

    /**
     * Loads all organizers from Firebase using {@link Admin#browseOrganizers()}.
     * <p>
     * Clears the current list, adds all fetched organizers, and notifies the adapter
     * of data changes. Shows a toast and logs on failure.
     * </p>
     */
    private void loadOrganizers() {
        organizerList.clear();

        admin.browseOrganizers()
                .addOnSuccessListener(organizers -> {
                    organizerList.addAll(organizers);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load organizers", Toast.LENGTH_SHORT).show();
                    Log.e("OrgAdmin", "Error loading organizers", e);
                });
    }

    /**
     * Shows a confirmation dialog before deleting an organizer.
     * <p>
     * Called by {@link OrganizerAdapter} when the delete button is clicked.
     * </p>
     *
     * @param organizer the {@link Organizer} to be deleted
     */
    private void confirmDeleteOrganizer(Organizer organizer) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Organizer")
                .setMessage("Are you sure you want to delete \"" + organizer.getOrganizerId() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteOrganizer(organizer))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes the specified organizer from Firebase and updates the UI.
     * <p>
     * Calls {@link Admin#deleteOrganizerProfile(String)} to remove from database,
     * removes from {@link #organizerList}, and notifies the adapter.
     * Shows a success toast.
     * </p>
     *
     * @param organizer the {@link Organizer} to delete
     */
    private void deleteOrganizer(Organizer organizer) {
        admin.deleteOrganizerProfile(organizer.getOrganizerId());
        organizerList.remove(organizer);
        adapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Organizer deleted", Toast.LENGTH_SHORT).show();
    }
}