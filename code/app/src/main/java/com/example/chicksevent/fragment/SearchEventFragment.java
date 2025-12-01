package com.example.chicksevent.fragment;

import static android.content.ContentValues.TAG;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.R;
import com.example.chicksevent.adapter.EventAdapter;
import com.example.chicksevent.misc.Event;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.User;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchEventFragment extends Fragment {

    private ArrayList<Event> eventDataList = new ArrayList<>();
    private ArrayList<String> filters = new ArrayList<>();
    private String filterAvailability = null;

    private ListView eventView;
    private User user;
    private FirebaseService eventService;

    public SearchEventFragment() {
        super(R.layout.fragment_search_event);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventService = new FirebaseService("Event");

        // UI references
        EditText etInterest = view.findViewById(R.id.search_interest);
        EditText searchBar = view.findViewById(R.id.search_bar);
        Spinner spAvailability = view.findViewById(R.id.spinner_availability);
        Button btnApply = view.findViewById(R.id.btn_apply_filter);
        Button btnClear = view.findViewById(R.id.btn_clear_filter);
        Button btnSave = view.findViewById(R.id.btn_save);
        Button btnFilter = view.findViewById(R.id.btn_filter);
        LinearLayout filterPanel = view.findViewById(R.id.filter_panel);
        eventView = view.findViewById(R.id.recycler_notifications);


        // Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.availability_options,
                R.layout.spinner_item
        );
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spAvailability.setAdapter(adapter);

        // User object
        String androidId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        user = new User(androidId);

        // Filter panel toggle
        btnFilter.setOnClickListener(v -> filterPanel.setVisibility(
                filterPanel.getVisibility() == VISIBLE ? INVISIBLE : VISIBLE
        ));

        // Initial load: show all events
        listEvents(null);

        // Clear fields
        btnClear.setOnClickListener(v -> {
            etInterest.setText("");
            searchBar.setText("");
            if (spAvailability.getAdapter() != null) spAvailability.setSelection(0);
        });

        // Apply interest/availability filter
        btnApply.setOnClickListener(v -> {
            filters.clear();

            // Interests
            String interest = etInterest.getText().toString().trim();
            if (!interest.isEmpty()) {
                for (String token : interest.split("[,\\s]+")) {
                    if (!token.isEmpty()) filters.add(token);
                }
            }

            // Availability
            if (spAvailability != null && spAvailability.getSelectedItem() != null) {
                filterAvailability = spAvailability.getSelectedItem().toString();
            }

            Toast.makeText(getContext(), "Filter applied", Toast.LENGTH_SHORT).show();
            filterPanel.setVisibility(INVISIBLE);

            applyFilters();
        });

        // Search by name (respects current filters)
        btnSave.setOnClickListener(v -> {
            String searchText = searchBar.getText().toString().trim().toLowerCase();
            if (!searchText.isEmpty()) {
                ArrayList<Event> filtered = new ArrayList<>();
                for (Event e : eventDataList) {
                    if (e.getName().toLowerCase().contains(searchText)) {
                        filtered.add(e);
                    }
                }
                updateEventList(filtered);
            } else {
                // If search empty, re-apply current filters or show all
                applyFilters();
            }
        });
    }

    // ---------------- Helper Methods ----------------

    /** Show all events from Firebase or filtered IDs */
    public void listEvents(@Nullable ArrayList<String> filterIds) {
        Log.i(TAG, "Fetching events...");
        eventDataList = new ArrayList<>();

        eventService.getReference().addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                for (com.google.firebase.database.DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    HashMap<String, String> value = (HashMap<String, String>) childSnapshot.getValue();
                    String eventId = value.get("id");

                    Boolean onHold = (Boolean) ((HashMap<String, Object>) childSnapshot.getValue()).get("onHold");

                    if (onHold) continue;

                    if (filterIds != null && !filterIds.contains(eventId)) continue;

                    Event e = new Event(
                            "e",
                            eventId,
                            value.get("name"),
                            value.get("eventDetails"),
                            value.get("eventStartTime"),
                            value.get("eventEndTime"),
                            value.get("eventStartDate"),
                            "N/A",
                            value.get("registrationEndDate"),
                            value.get("registrationStartDate"),
                            32,
                            "N/A",
                            value.get("tag"),
                            false
                    );

                    eventDataList.add(e);
                }

                updateEventList(eventDataList);
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError databaseError) {
                Log.e(TAG, "Error reading data: " + databaseError.getMessage());
            }
        });
    }

    /** Update ListView */
    private void updateEventList(ArrayList<Event> list) {
        EventAdapter adapter = new EventAdapter(
                getContext(),
                list,
                item -> {
                    NavController nav = NavHostFragment.findNavController(SearchEventFragment.this);
                    Bundle b = new Bundle();
                    b.putString("eventId", item.getId());
                    nav.navigate(R.id.action_SearchEventFragment_to_EventDetailFragment, b);
                }
        );
        eventView.setAdapter(adapter);
    }

    /** Apply interest + availability filters */
    private void applyFilters() {
//        Log.i("RTD10", filters.toString());
//        Log.i("RTD10", filterAvailability.toString());
        user.filterEvents(filters, filterAvailability)
                .addOnCompleteListener(task -> {
                    if (!isAdded()) return;

                    ArrayList<String> filteredIds = task.getResult();
                    listEvents(filteredIds); // use updated listEvents()
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(),
                            "Filter error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
