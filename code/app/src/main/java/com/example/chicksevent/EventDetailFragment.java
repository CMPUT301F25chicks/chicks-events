package com.example.chicksevent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EventDetailFragment extends Fragment {

    private TextView titleView, idView;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        titleView = view.findViewById(R.id.eventTitle);
        idView = view.findViewById(R.id.eventIdText);

        // Safe Args-style (arg name: eventId). If you’re not using Safe Args,
        // read from getArguments().getString("eventId") instead.
        Bundle args = getArguments();
        String eventId = args != null ? args.getString("eventId") : null;

        if (eventId == null) {
            Toast.makeText(requireContext(), "No eventId", Toast.LENGTH_SHORT).show();
            return;
        }

        idView.setText("eventId: " + eventId);
        // TODO: fetch Firestore: events/{eventId} and set title/desc/etc.
        // For now show a placeholder
        titleView.setText("Loading...");
    }
}
