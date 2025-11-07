package com.example.chicksevent;

import android.os.Bundle;
import android.provider.Settings;
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
        Log.i("admin", "ew");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            // No user logged in — navigate to regular home (or login if you add it later)
            NavHostFragment.findNavController(this)
                    .navigate(R.id.FirstFragment);
            return;
        }

        String uid = firebaseUser.getUid();
        userService = new FirebaseService("User"); // "User" node in DB

        User user = new User(Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        ));

        user.isAdmin(isAdmin -> {
            if (isAdmin) {
                Log.i("im admin", "yay");
                NavHostFragment.findNavController(LauncherFragment.this)
                        .navigate(R.id.adminHomeFragment);
            } else {
                Log.i("im admin", "no");

                NavHostFragment.findNavController(LauncherFragment.this)
                        .navigate(R.id.FirstFragment);
            }
        });
    }
}
