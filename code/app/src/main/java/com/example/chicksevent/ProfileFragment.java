package com.example.chicksevent;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.databinding.FragmentCreateEventBinding;
import com.example.chicksevent.databinding.FragmentProfileEntrantBinding;
import com.example.chicksevent.databinding.FragmentUpdateEventDetailBinding;
import com.example.chicksevent.databinding.FragmentWaitingListBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileFragment extends Fragment {

    private FragmentProfileEntrantBinding binding;
    private FirebaseService userService = new FirebaseService("User");
    private String TAG = "RTD8";
    EditText editName;
    EditText editPhone;
    EditText editEmail;
    String eventId;
    Button saveInfoButton;
    String userId;
    public ProfileFragment() {
        // You can keep the constructor-empty and inflate via binding below
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileEntrantBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventName");
            // Use it to populate UI
        }

        userId = Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        Button eventButton = view.findViewById(R.id.btn_events);
        Button createEventButton = view.findViewById(R.id.btn_addEvent);
        Button notificationButton = view.findViewById(R.id.btn_notification);;

        editName = view.findViewById(R.id.edit_name);
        editPhone = view.findViewById(R.id.edit_phone);

        editEmail = view.findViewById(R.id.edit_email);
        saveInfoButton = view.findViewById(R.id.btn_save_info);


        notificationButton.setOnClickListener(v -> {
                    NavHostFragment.findNavController(ProfileFragment.this)
                            .navigate(R.id.action_ProfileFragment_to_NotificationFragment);
                }
//
        );

        eventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(ProfileFragment.this).navigate(R.id.action_ProfileFragment_to_EventFragment);
        });

        createEventButton.setOnClickListener(v -> {
//            NavHostFragment.findNavController(UpdateEventFragment.this).navigate(R.id.action_SecondFragment_to_CreateEventFragment);

            NavHostFragment.findNavController(ProfileFragment.this).navigate(R.id.action_ProfileFragment_to_CreateEventFragment);
        });

        saveInfoButton.setOnClickListener(v -> updateProfile());


        renderProfile();



//        eventName
    }

    private void renderProfile() {
        userService.getReference().get().continueWith(ds -> {
            for (DataSnapshot d : ds.getResult().getChildren()) {
                Log.i("TAGwerw", d.getKey());
                try {
                    HashMap<String, String> userHash = (HashMap<String, String> ) d.getValue();
                    if (userId.equals(d.getKey())) {
                        editName.setText(userHash.get("name"));
                        editEmail.setText(userHash.get("email"));
                        editPhone.setText(userHash.get("phoneNumber"));
                    }
                } catch(Exception e) {
                    Log.e("ERROR", "weird error " + e);
                }

            }

            return null;
        });
    }

    private void updateProfile() {

        HashMap<String, Object> data = new HashMap<>();

        data.put("name", editName.getText().toString());
        data.put("email", editEmail.getText().toString());
        data.put("phone",editPhone.getText().toString());

        userService.editEntry(userId, data);
        Toast.makeText(requireContext(), "Updated Profile", Toast.LENGTH_SHORT).show();


    }

    private static String s(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
