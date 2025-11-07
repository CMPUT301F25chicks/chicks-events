package com.example.chicksevent;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OrgAdminFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<Organizer> organizerList = new ArrayList<>();
    private OrganizerAdapter adapter;
    private Admin admin;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        admin = new Admin("ADMIN_DEFAULT");
        recyclerView = view.findViewById(R.id.recycler_chosenUser);

        // Adapter with delete click callback
        adapter = new OrganizerAdapter(organizerList, this::confirmDeleteOrganizer);
        recyclerView.setAdapter(adapter);

        loadOrganizers();
    }

    private void loadOrganizers() {
        organizerList.clear();

        admin.browseOrganizers()
                .addOnSuccessListener(organizers -> {
                    organizerList.addAll(organizers);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load organizers", Toast.LENGTH_SHORT).show();
                    Log.e("OrgAdmin", "Error loading organizers", e);
                });
    }


    private void confirmDeleteOrganizer(Organizer organizer) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Organizer")
                .setMessage("Are you sure you want to delete \"" + organizer.getUserName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteOrganizer(organizer))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteOrganizer(Organizer organizer) {
        admin.deleteOrganizerProfile(organizer.getOrganizerId());
        organizerList.remove(organizer);
        adapter.notifyDataSetChanged();
        Toast.makeText(requireContext(), "Organizer deleted", Toast.LENGTH_SHORT).show();
    }
}
