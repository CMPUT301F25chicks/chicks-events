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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // ← fixes your Nullable error
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TEMP: test write to Firebase
        FirebaseService service = new FirebaseService("bruhmoment");
        HashMap<String, Object> data = new HashMap<>();
        data.put("username", "jim");
        data.put("age", 43);
        String id = service.addEntry(data);
        data.put("phoneNumber", "403-420-6767");
        service.editEntry(id, data);





//        FirebaseService service = new FirebaseService("events");

        Event e = new Event();
        e.setId("abc123");                // you can give one, or let RTDB generate it
        e.setName("Swimming Lessons");
        e.setEventDetails("Kids learn freestyle and backstroke");
        e.setEventStartDate("2026-01-01");
        e.setEventEndDate("2026-02-01");
        e.setRegistrationStartDate("2025-11-13");
        e.setRegistrationEndDate("2025-12-30");
        e.setEntrantLimit(30);
        e.setOrganizer("org123");
        e.setPoster(null);
        e.setTag("sports kids swimming");
        e.createEvent();

// convert to map





        // Navigate to CreateEventFragment when FAB is tapped
        //com.google.android.material.floatingactionbutton.FloatingActionButton fab =
                //requireActivity().findViewById(R.id.fab);
        //fab.setOnClickListener(v ->
        // androidx.navigation.fragment.NavHostFragment.findNavController(FirstFragment.this)
                       // .navigate(R.id.action_FirstFragment_to_CreateEventFragment)
        //);
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



}