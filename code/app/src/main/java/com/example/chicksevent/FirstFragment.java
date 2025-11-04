package com.example.chicksevent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.databinding.FragmentFirstBinding;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private FirebaseService service;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Admin admin = new Admin();

        // shows on logcat
        admin.browseEvents()
                .addOnSuccessListener(events -> {
                    StringBuilder sb = new StringBuilder();
                    if (events.isEmpty()) {
                        sb.append("No events found.\n");
                    } else {
                        for (Event e : events) {
                            sb.append("ID: ").append(e.getId())
                                    .append(" | Name: ").append(e.getName())
                                    .append(" | Start: ").append(e.getEventStartDate())
                                    .append(" | End: ").append(e.getEventEndDate())
                                    .append("\n");
                        }
                    }

                    android.util.Log.d("BrowseEvents", sb.toString());

                })
                .addOnFailureListener(err -> {
                    android.util.Log.e("BrowseEvents", "Failed to fetch events", err);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}