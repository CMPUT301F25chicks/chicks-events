package com.example.chicksevent;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileAdminFragment extends Fragment {
    private RecyclerView recyclerView;
    private EntrantListAdapter adapter;
    private List<EntrantDisplay> entrantList;
    private Admin admin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_profile, container, false);

        recyclerView = view.findViewById(R.id.recycler_chosenUser);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        entrantList = new ArrayList<>();
        adapter = new EntrantListAdapter(entrantList, this::deleteEntrant);
        recyclerView.setAdapter(adapter);

        admin = new Admin("ADMIN_DEFAULT");

        loadEntrants();

        return view;
    }

    /** Loads all entrants using Admin.browseEntrants() */
    private void loadEntrants() {
        admin.browseEntrants()
                .addOnSuccessListener(entrants -> {
                    entrantList.clear();
                    for (User user: entrants) {
                        if (user instanceof Entrant) {
                            Entrant e = (Entrant) user;
                            entrantList.add(new EntrantDisplay(e.getEntrantId(), "Active"));
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e("ProfileAdminFragment", "Failed to load entrants", e));
    }

    /** Deletes an entrant using Admin.deleteEntrantProfile() */
    private void deleteEntrant(EntrantDisplay entrant) {
        admin.deleteEntrantProfile(entrant.getEntrantId())
                .addOnSuccessListener(aVoid -> {
                    Log.d("ProfileAdminFragment", "Deleted entrant: " + entrant.getEntrantId());
                    loadEntrants(); // refresh list
                })
                .addOnFailureListener(e ->
                        Log.e("ProfileAdminFragment", "Failed to delete entrant: " + entrant.getEntrantId(), e));
    }
}
