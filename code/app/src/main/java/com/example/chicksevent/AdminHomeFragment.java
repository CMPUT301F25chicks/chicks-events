package com.example.chicksevent;

import android.app.Activity;
import android.view.View;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminHomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        Button btnEvents = view.findViewById(R.id.btn_admin_event);
        Button btnOrganizers = view.findViewById(R.id.btn_admin_org);
        Button btnProfiles = view.findViewById(R.id.btn_admin_profile);

        btnEvents.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminHomeFragment.this)
                        .navigate(R.id.action_adminHome_to_eventAdminFragment));

        btnOrganizers.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminHomeFragment.this)
                        .navigate(R.id.action_adminHome_to_orgAdminFragment));

        btnProfiles.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminHomeFragment.this)
                        .navigate(R.id.action_adminHome_to_profileAdminFragment));

        return view;
    }
}

