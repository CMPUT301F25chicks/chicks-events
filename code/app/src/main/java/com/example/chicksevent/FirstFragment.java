package com.example.chicksevent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

        admin.browseProfiles()
                .addOnSuccessListener(users -> {
                    StringBuilder sb = new StringBuilder();
                    if (users.isEmpty()) {
                        sb.append("No profiles found.\n");
                    } else {
                        for (User user : users) {
                            sb.append("Uid: ").append(user.getUid())
                                    .append(" | Name: ").append(user.getName())
                                    .append(" | Email: ").append(user.getEmail())
                                    .append(" | Phone: ").append(user.getPhoneNumber())
                                    .append("\n");
                        }
                    }
                        // log to logcat to see
                    android.util.Log.d("BrowseProfiles", sb.toString());

                })
                .addOnFailureListener(err -> {
                    android.util.Log.e("BrowseProfiles", "Failed to fetch profiles", err);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}