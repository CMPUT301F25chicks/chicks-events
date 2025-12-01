package com.example.chicksevent.fragment_org;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chicksevent.R;
import com.example.chicksevent.databinding.FragmentEntrantLocationMapBinding;
import com.example.chicksevent.misc.FirebaseService;
import com.google.firebase.database.DataSnapshot;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays a map showing locations where entrants joined the waiting list.
 * This fragment allows organizers to:
 * <ul>
 *   <li>View locations of entrants who joined with geolocation (WAITING and INVITED status only)</li>
 *   <li>Filter entrants by status (All, WAITING, INVITED)</li>
 *   <li>Search entrants by name or ID</li>
 *   <li>Click markers to see entrant details</li>
 * </ul>
 * Uses OpenStreetMap (OSMDroid) - free and no API key required.
 *
 * @author Jinn Kasai
 */
public class EntrantLocationMapFragment extends Fragment {

    private static final String TAG = "EntrantLocationMap";
    
    private FragmentEntrantLocationMapBinding binding;
    private MapView mapView;
    private IMapController mapController;
    private FirebaseService waitingListService;
    private FirebaseService userService;
    private ProgressBar mapProgressBar;
    
    private String eventId;
    private List<EntrantMarkerData> allEntrants = new ArrayList<>();
    private List<EntrantMarkerData> filteredEntrants = new ArrayList<>();
    
    // Filter state
    private String currentStatusFilter = "ALL"; // ALL, WAITING, INVITED
    private String currentSearchQuery = "";

    /**
     * Data class to hold entrant information for map markers.
     */
    private static class EntrantMarkerData {
        String entrantId;
        String status;
        double latitude;
        double longitude;
        String userName;
        Marker marker;

        EntrantMarkerData(String entrantId, String status, double latitude, double longitude, String userName) {
            this.entrantId = entrantId;
            this.status = status;
            this.latitude = latitude;
            this.longitude = longitude;
            this.userName = userName;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize OSMDroid configuration
        Configuration.getInstance().load(getContext(), getContext().getSharedPreferences("osmdroid", 0));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEntrantLocationMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get event ID from arguments
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId");
        }

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(requireContext(), "Event ID not found", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }

        waitingListService = new FirebaseService("WaitingList");
        userService = new FirebaseService("User");

        // Initialize map
        mapView = view.findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapController = mapView.getController();
        mapController.setZoom(8.0); // Start more zoomed out to show broader area
        
        // Initialize progress bar
        mapProgressBar = view.findViewById(R.id.progress_map);
        if (mapProgressBar != null) {
            mapProgressBar.setVisibility(View.VISIBLE);
        }

        // Setup filter radio buttons
        RadioGroup statusFilter = view.findViewById(R.id.radio_status_filter);
        statusFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_all) {
                currentStatusFilter = "ALL";
            } else if (checkedId == R.id.radio_waiting) {
                currentStatusFilter = "WAITING";
            } else if (checkedId == R.id.radio_invited) {
                currentStatusFilter = "INVITED";
            }
            applyFilters();
        });

        // Setup search
        EditText searchEditText = view.findViewById(R.id.et_search_name);
        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Load entrant data
        loadEntrantLocations();
    }

    /**
     * Loads entrant location data from Firebase for WAITING and INVITED statuses.
     */
    private void loadEntrantLocations() {
        // Show loading indicator
        if (mapProgressBar != null) {
            mapProgressBar.setVisibility(View.VISIBLE);
        }
        
        waitingListService.getReference().child(eventId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                Log.e(TAG, "Failed to load waiting list data");
                Toast.makeText(requireContext(), "Failed to load entrant data", Toast.LENGTH_SHORT).show();
                
                // Hide loading indicator on error
                if (mapProgressBar != null) {
                    mapProgressBar.setVisibility(View.GONE);
                }
                return;
            }

            allEntrants.clear();
            DataSnapshot waitingListSnapshot = task.getResult();

            // Process WAITING entrants
            DataSnapshot waitingSnapshot = waitingListSnapshot.child("WAITING");
            if (waitingSnapshot.exists()) {
                processEntrants(waitingSnapshot, "WAITING");
            }

            // Process INVITED entrants
            DataSnapshot invitedSnapshot = waitingListSnapshot.child("INVITED");
            if (invitedSnapshot.exists()) {
                processEntrants(invitedSnapshot, "INVITED");
            }

            // Load user names for all entrants
            loadUserNames();
        });
    }

    /**
     * Processes entrants from a status node and extracts location data.
     */
    private void processEntrants(DataSnapshot statusSnapshot, String status) {
        for (DataSnapshot entrantSnapshot : statusSnapshot.getChildren()) {
            String entrantId = entrantSnapshot.getKey();
            if (entrantId == null) continue;

            // Check if location data exists
            Object latObj = entrantSnapshot.child("latitude").getValue();
            Object lngObj = entrantSnapshot.child("longitude").getValue();

            if (latObj != null && lngObj != null) {
                try {
                    double latitude = latObj instanceof Number ? ((Number) latObj).doubleValue() : Double.parseDouble(latObj.toString());
                    double longitude = lngObj instanceof Number ? ((Number) lngObj).doubleValue() : Double.parseDouble(lngObj.toString());

                    EntrantMarkerData entrant = new EntrantMarkerData(entrantId, status, latitude, longitude, null);
                    allEntrants.add(entrant);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid location data for entrant: " + entrantId, e);
                }
            }
        }
    }

    /**
     * Loads user names for all entrants from the User root.
     */
    private void loadUserNames() {
        userService.getReference().get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot usersSnapshot = task.getResult();
                
                for (EntrantMarkerData entrant : allEntrants) {
                    DataSnapshot userSnapshot = usersSnapshot.child(entrant.entrantId);
                    if (userSnapshot.exists()) {
                        Object nameObj = userSnapshot.child("name").getValue();
                        if (nameObj != null) {
                            entrant.userName = nameObj.toString();
                        } else {
                            entrant.userName = entrant.entrantId; // Fallback to ID
                        }
                    } else {
                        entrant.userName = entrant.entrantId; // Fallback to ID
                    }
                }

                // Apply filters and update map
                applyFilters();
                
                // Hide loading indicator
                if (mapProgressBar != null) {
                    mapProgressBar.setVisibility(View.GONE);
                }
            } else {
                // If user names can't be loaded, use IDs
                for (EntrantMarkerData entrant : allEntrants) {
                    entrant.userName = entrant.entrantId;
                }
                applyFilters();
                
                // Hide loading indicator
                if (mapProgressBar != null) {
                    mapProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Applies status filter and search query, then updates the map.
     */
    private void applyFilters() {
        filteredEntrants.clear();

        for (EntrantMarkerData entrant : allEntrants) {
            // Apply status filter
            boolean statusMatches = currentStatusFilter.equals("ALL") || 
                                   entrant.status.equals(currentStatusFilter);

            // Apply search filter
            boolean searchMatches = currentSearchQuery.isEmpty() ||
                                   entrant.entrantId.toLowerCase().contains(currentSearchQuery) ||
                                   (entrant.userName != null && entrant.userName.toLowerCase().contains(currentSearchQuery));

            if (statusMatches && searchMatches) {
                filteredEntrants.add(entrant);
            }
        }

        updateMapMarkers();
    }

    /**
     * Updates map markers based on filtered entrants.
     */
    private void updateMapMarkers() {
        if (mapView == null) return;

        // Clear existing markers
        mapView.getOverlays().clear();
        for (EntrantMarkerData entrant : allEntrants) {
            entrant.marker = null;
        }

        if (filteredEntrants.isEmpty()) {
            Toast.makeText(requireContext(), "No entrants found with location data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add markers for filtered entrants
        List<GeoPoint> points = new ArrayList<>();
        
        for (EntrantMarkerData entrant : filteredEntrants) {
            GeoPoint location = new GeoPoint(entrant.latitude, entrant.longitude);
            points.add(location);
            
            String title = entrant.userName != null ? entrant.userName : entrant.entrantId;
            String snippet = "Status: " + entrant.status + "\nID: " + entrant.entrantId;
            
            Marker marker = new Marker(mapView);
            marker.setPosition(location);
            marker.setTitle(title);
            marker.setSnippet(snippet);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            
            // Set click listener to show info
            marker.setOnMarkerClickListener((marker1, mapView1) -> {
                Toast.makeText(requireContext(), 
                    marker1.getTitle() + "\n" + marker1.getSnippet(), 
                    Toast.LENGTH_LONG).show();
                return true;
            });
            
            mapView.getOverlays().add(marker);
            entrant.marker = marker;
        }

        // Auto-fit map to show all markers with broader view
        if (!points.isEmpty()) {
            // Calculate bounding box
            BoundingBox boundingBox = BoundingBox.fromGeoPoints(points);
            
            // Expand the bounding box by a factor to show more area
            double latCenter = (boundingBox.getLatNorth() + boundingBox.getLatSouth()) / 2.0;
            double lonCenter = (boundingBox.getLonEast() + boundingBox.getLonWest()) / 2.0;
            double latSpan = boundingBox.getLatNorth() - boundingBox.getLatSouth();
            double lonSpan = boundingBox.getLonEast() - boundingBox.getLonWest();
            
            // Determine expansion factor based on how close together entrants are
            // If entrants are close together (small span), expand more (4x)
            // If entrants are spread out (large span), expand less (2x)
            double maxSpan = Math.max(latSpan, lonSpan);
            double expansionFactor;
            
            if (maxSpan < 0.01) {
                // Very close together (< ~1km), expand by 4x
                expansionFactor = 4.0;
            } else if (maxSpan < 0.05) {
                // Close together (< ~5km), expand by 3x
                expansionFactor = 3.0;
            } else {
                // Spread out (>= ~5km), expand by 2x
                expansionFactor = 2.0;
            }
            
            // Expand the bounding box
            double expandedLatSpan = Math.max(latSpan * expansionFactor, 0.1); // Minimum 0.1 degrees
            double expandedLonSpan = Math.max(lonSpan * expansionFactor, 0.1); // Minimum 0.1 degrees
            
            BoundingBox expandedBox = new BoundingBox(
                latCenter + expandedLatSpan / 2.0,
                lonCenter + expandedLonSpan / 2.0,
                latCenter - expandedLatSpan / 2.0,
                lonCenter - expandedLonSpan / 2.0
            );
            
            // Zoom to expanded bounding box with padding
            mapView.zoomToBoundingBox(expandedBox, true, 200);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
