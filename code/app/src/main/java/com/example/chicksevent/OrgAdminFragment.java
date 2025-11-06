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
    private FirebaseService organizerService;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        organizerService = new FirebaseService("Organizer");
        recyclerView = view.findViewById(R.id.recycler_chosenUser);

        adapter = new OrganizerAdapter(organizerList, (Organizer organizer) -> {
            organizerService.deleteEntry(organizer.getOrganizerId());
            organizerList.remove(organizer); // works because both are typed as Organizer
            adapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Organizer removed", Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);


        loadOrganizers();
    }

    private void loadOrganizers() {
        organizerList.clear();

        organizerService.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Organizer org = child.getValue(Organizer.class);
                    organizerList.add(org);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("OrgAdmin", "Failed to load organizers", error.toException());
            }
        });
    }
}
