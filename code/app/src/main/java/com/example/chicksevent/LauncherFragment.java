package com.example.chicksevent;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.databinding.FragmentFirstBinding;
import com.example.chicksevent.databinding.FragmentLauncherBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class LauncherFragment extends Fragment {



    private FragmentLauncherBinding binding;
    private FirebaseService service;
    private FirebaseService userService;
    ArrayList<Notification> notificationDataList = new ArrayList<Notification>();
    NotificationAdapter notificationAdapter;

    private final String TAG = "RTD8";

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentLauncherBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        service = new FirebaseService("bruhmoment");
        userService = new FirebaseService("User");
        HashMap<String, Object> data = new HashMap<>();

        String androidId = Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        Log.i(TAG, "Android ID used for test: " + androidId);

        // ======================= TEST FOR 01.02.O1 =======================
        // As an entrant, I want to provide my personal information such as
        //  name, email and optional phone number in the app

        // Create User object identified by device ID
        User userToUpdate = new User(androidId);
//        userToUpdate.getNotificationList();

        // Define personal information to be saved
//        String testName = "Jinn Gay";
//        String testEmail = "jinn.gay@example.com";
//        String testPhone = "555-867-5309";
//
//        // update firebase
//        userToUpdate.updateProfile(testName, testEmail, testPhone);
//
//        Log.d("RTD8", "Test initiated: updateProfile for user " + androidId);
//        // ===================================================================



        Log.i("admin", "ew");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

//        if (firebaseUser == null) {
//            // No user logged in — navigate to regular home (or login if you add it later)
//            NavHostFragment.findNavController(this)
//                    .navigate(R.id.FirstFragment);
//            return;
//        }

        String uid = firebaseUser.getUid();
        userService = new FirebaseService("User"); // "User" node in DB

        User user = new User(Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        ));

        user.isAdmin().addOnCompleteListener(v -> {
            if (v.getResult()) {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
