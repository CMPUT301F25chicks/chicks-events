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

        // Adapter with ban toggle callback
        adapter = new OrganizerAdapter(
                organizerList,
                this::handleBanToggle
        );
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
     * Handles the ban/unban toggle switch change.
     * Shows appropriate confirmation dialog based on the action.
     *
     * @param organizer the {@link Organizer} to toggle ban status for
     * @param willBeBanned true if switching to banned, false if switching to unbanned
     */
    private void handleBanToggle(Organizer organizer, boolean willBeBanned) {
        if (willBeBanned) {
            // Switching to banned - show ban confirmation with reason
            String reason = "Organizer violated policy";
            String message = "Are you sure you want to ban \"" + organizer.getOrganizerId() + "\" from creating events?\n\n" +
                           "Reason: " + reason + "\n\n" +
                           "All their future events will be put on hold.";
            new AlertDialog.Builder(requireContext())
                    .setTitle("Ban Organizer")
                    .setMessage(message)
                    .setPositiveButton("Confirm Ban", (dialog, which) -> banOrganizer(organizer, reason))
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // Revert the switch if cancelled
                        adapter.notifyItemChanged(organizerList.indexOf(organizer));
                    })
                    .show();
        } else {
            // Switching to unbanned - show unban confirmation
            new AlertDialog.Builder(requireContext())
                    .setTitle("Unban Organizer")
                    .setMessage("Are you sure you want to unban \"" + organizer.getOrganizerId() + "\"? They will be able to create events again.")
                    .setPositiveButton("Unban", (dialog, which) -> unbanOrganizer(organizer))
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // Revert the switch if cancelled
                        adapter.notifyItemChanged(organizerList.indexOf(organizer));
                    })
                    .show();
        }
    }

    /**
     * Bans the specified organizer from creating events.
     * <p>
     * Calls {@link Admin#banUserFromOrganizer(String, String)} to ban the user,
     * put their events on hold, and notify them. Updates the UI after completion.
     * </p>
     *
     * @param organizer the {@link Organizer} to ban
     * @param reason the reason for banning the organizer
     */
    private void banOrganizer(Organizer organizer, String reason) {
        admin.banUserFromOrganizer(organizer.getOrganizerId(), reason)
                .addOnSuccessListener(aVoid -> {
                    // Refresh the specific item to update the label and switch
                    int position = organizerList.indexOf(organizer);
                    if (position >= 0) {
                        adapter.notifyItemChanged(position);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                    Toast.makeText(getContext(), "Organizer banned", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("OrgAdmin", "Error banning organizer", e);
                    // Revert the switch if failed
                    int position = organizerList.indexOf(organizer);
                    if (position >= 0) {
                        adapter.notifyItemChanged(position);
                    }
                    Toast.makeText(getContext(), "Failed to ban organizer", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Unbans the specified organizer, allowing them to create events again.
     * <p>
     * Calls {@link Admin#unbanUserFromOrganizer(String)} to unban the user
     * and notify them. Updates the UI after completion.
     * </p>
     *
     * @param organizer the {@link Organizer} to unban
     */
    private void unbanOrganizer(Organizer organizer) {
        admin.unbanUserFromOrganizer(organizer.getOrganizerId())
                .addOnSuccessListener(aVoid -> {
                    // Refresh the specific item to update the label and switch
                    int position = organizerList.indexOf(organizer);
                    if (position >= 0) {
                        adapter.notifyItemChanged(position);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                    Toast.makeText(getContext(), "Organizer unbanned", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("OrgAdmin", "Error unbanning organizer", e);
                    // Revert the switch if failed
                    int position = organizerList.indexOf(organizer);
                    if (position >= 0) {
                        adapter.notifyItemChanged(position);
                    }
                    Toast.makeText(getContext(), "Failed to unban organizer", Toast.LENGTH_SHORT).show();
                });
    }
}