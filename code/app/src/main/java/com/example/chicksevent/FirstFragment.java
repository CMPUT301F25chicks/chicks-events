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
        String eventId = "-OdGXYckhMz90btdhkgm"; // Id of event ill delete

        admin.deleteEvent(eventId)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("DeleteEvent", "Event deleted successfully");
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("DeleteEvent", "Failed to delete event: " + e.getMessage());
                });
    }





    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}