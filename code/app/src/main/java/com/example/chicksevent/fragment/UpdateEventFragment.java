package com.example.chicksevent.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.R;
import com.example.chicksevent.databinding.FragmentUpdateEventDetailBinding;
import com.example.chicksevent.misc.FirebaseService;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;

/**
 * Fragment that displays and allows navigation from an event update screen.
 * <p>
 * This fragment serves as a placeholder for future event editing functionality.
 * It currently supports navigation to related views such as Notifications, Events,
 * and Create Event screens.
 * </p>
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *     <li>Display the event name passed as a fragment argument.</li>
 *     <li>Provide navigation to other fragments in the app flow.</li>
 *     <li>Serve as a structural base for upcoming event modification features.</li>
 * </ul>
 * </p>
 *
 * @author Jordan Kwan
 */
public class UpdateEventFragment extends Fragment {

    /** View binding for the Update Event layout. */
    private FragmentUpdateEventDetailBinding binding;
    
    /** Firebase service for event operations. */
    private FirebaseService eventService;
    
    /** Event ID for the event being updated. */
    private String eventId;
    
    private static final String TAG = "UpdateEventFragment";

    /**
     * Inflates the fragment layout using ViewBinding.
     *
     * @param inflater the layout inflater
     * @param container the parent view container
     * @param savedInstanceState the saved instance state
     * @return the inflated view hierarchy
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUpdateEventDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Initializes view components, displays the event name (if provided),
     * and wires navigation buttons for related fragments.
     *
     * @param view the root view returned by {@link #onCreateView}
     * @param savedInstanceState previously saved state, or {@code null} for a fresh instance
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventService = new FirebaseService("Event");
        
        TextView eventName = view.findViewById(R.id.tv_event_name);
        Switch geoSwitch = view.findViewById(R.id.switch_geo);
        Button saveButton = view.findViewById(R.id.btn_save_event);

        Bundle args = getArguments();
        if (args != null) {
            String eventNameString = args.getString("eventName");
            eventName.setText(eventNameString);
            
            // Load event data from Firebase
            loadEventData(eventNameString, geoSwitch);
        }

        // Wire up Save button
        saveButton.setOnClickListener(v -> {
            saveEventChanges(geoSwitch);
        });

        Button eventButton = view.findViewById(R.id.btn_events);
        Button createEventButton = view.findViewById(R.id.btn_addEvent);
        Button notificationButton = view.findViewById(R.id.btn_notification);


        notificationButton.setOnClickListener(v -> {
                    NavHostFragment.findNavController(UpdateEventFragment.this)
                            .navigate(R.id.action_UpdateEventFragment_to_NotificationFragment);
                }
//
        );

        eventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(UpdateEventFragment.this).navigate(R.id.action_UpdateEventFragment_to_EventFragment);
        });

        createEventButton.setOnClickListener(v -> {
//            NavHostFragment.findNavController(UpdateEventFragment.this).navigate(R.id.action_SecondFragment_to_CreateEventFragment);

            NavHostFragment.findNavController(UpdateEventFragment.this).navigate(R.id.action_UpdateEventFragment_to_CreateEventFragment);
        });

    }

    /**
     * Loads event data from Firebase and populates the UI.
     *
     * @param eventNameString the event name/key to look up
     * @param geoSwitch the geolocation toggle switch to update
     */
    private void loadEventData(String eventNameString, Switch geoSwitch) {
        eventService.getReference().get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    if (ds.getKey().equals(eventNameString)) {
                        HashMap<String, Object> hash = (HashMap<String, Object>) ds.getValue();
                        eventId = (String) hash.get("id");
                        
                        // Set geolocation toggle
                        Object geoRequired = hash.get("geolocationRequired");
                        if (geoRequired instanceof Boolean) {
                            geoSwitch.setChecked((Boolean) geoRequired);
                        } else {
                            geoSwitch.setChecked(false); // Default to false
                        }
                        break;
                    }
                }
            }
        });
    }

    /**
     * Saves the geolocation requirement change to Firebase.
     *
     * @param geoSwitch the geolocation toggle switch
     */
    private void saveEventChanges(Switch geoSwitch) {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(requireContext(), "Event ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean geolocationRequired = geoSwitch.isChecked();
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("geolocationRequired", geolocationRequired);

        eventService.editEntry(eventId, updates);
        Toast.makeText(requireContext(), "Event updated successfully", Toast.LENGTH_SHORT).show();
    }

    /** Clears binding references when the view is destroyed. */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
