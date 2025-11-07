package com.example.chicksevent;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class LauncherFragment extends Fragment {

    private FirebaseService userService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_launcher, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            // No user logged in — navigate to regular home (or login if you add it later)
            NavHostFragment.findNavController(this)
                    .navigate(R.id.FirstFragment);
            return;
        }

        String uid = firebaseUser.getUid();
        userService = new FirebaseService("User"); // "User" node in DB

        userService.getReference().child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);

                        user.isAdmin(isAdmin -> {
                            if (isAdmin) {
                                NavHostFragment.findNavController(LauncherFragment.this)
                                        .navigate(R.id.adminHomeFragment);
                            } else {
                                NavHostFragment.findNavController(LauncherFragment.this)
                                        .navigate(R.id.FirstFragment);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("LauncherFragment", "Failed to fetch user info", error.toException());
                        // Fallback to regular home
                        NavHostFragment.findNavController(LauncherFragment.this)
                                .navigate(R.id.FirstFragment);
                    }
                });
    }
}
