package com.example.chicksevent;

import android.app.AlertDialog;
import android.os.Bundle;
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

public class EventAdminFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdminAdapter adapter;
    private ArrayList<Event> eventList;
    private FirebaseService firebaseService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_event, container, false);

        recyclerView = view.findViewById(R.id.recycler_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        eventList = new ArrayList<>();
        firebaseService = new FirebaseService("Event");

        adapter = new EventAdminAdapter(requireContext(), eventList, this::confirmDeleteEvent);
        recyclerView.setAdapter(adapter);

        loadEvents();

        return view;
    }

    private void loadEvents() {
        firebaseService.getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Event event = child.getValue(Event.class);
                    if (event != null) eventList.add(event);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteEvent(Event event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete \"" + event.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent(event))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEvent(Event event) {
        firebaseService.deleteEntry(event.getId());
        Toast.makeText(requireContext(), "Event deleted", Toast.LENGTH_SHORT).show();
    }
}
