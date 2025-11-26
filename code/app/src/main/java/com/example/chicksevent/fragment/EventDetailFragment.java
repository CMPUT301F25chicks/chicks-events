package com.example.chicksevent.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.R;
import com.example.chicksevent.databinding.FragmentEventDetailBinding;
import com.example.chicksevent.misc.Entrant;
import com.example.chicksevent.misc.FirebaseService;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Fragment displaying detailed information about a specific event.
 * <p>
 * This screen allows users to view the event name, description, and other details.
 * It provides navigation to related fragments (Notification, Events, Create Event)
 * and enables users to join the event's waiting list as an {@link Entrant}.
 * </p>
 *
 * <p><b>Navigation:</b>
 * <ul>
 *   <li>Navigate to {@code NotificationFragment}</li>
 *   <li>Navigate to {@code EventFragment}</li>
 *   <li>Navigate to {@code CreateEventFragment}</li>
 * </ul>
 * </p>
 *
 * <p>
 * Joining the waiting list uses the device's Android ID as the entrant ID and calls
 * {@link Entrant#joinWaitingList()}. Users must have a profile in Firebase to join.
 * </p>
 *
 * @author Jordan Kwan
 */
public class EventDetailFragment extends Fragment {

    /** View binding for the event detail layout. */
    private FragmentEventDetailBinding binding;

    /** Firebase service wrapper for accessing user data. */
    private FirebaseService userService;

    /** Firebase service wrapper for accessing event data. */
    private FirebaseService eventService;

    /** Unique identifier for the current user, derived from device Android ID. */
    String userId;
    String eventId;

    String eventIdString;

    private FirebaseService waitingListService;

    private Integer waitingListCount;
    private boolean geolocationRequired = false;
    private boolean eventOnHold = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final long LOCATION_TIMEOUT_MS = 30000; // 30 seconds
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Handler locationTimeoutHandler;
    private Runnable locationTimeoutRunnable;
    private ProgressBar locationProgressBar;
    private FirebaseService imageService;


    /**
     * Default constructor required for Fragment instantiation.
     */
    public EventDetailFragment() {
        // You can keep the constructor-empty and inflate via binding below
    }

    /**
     * Inflates the fragment layout using View Binding.
     *
     * @param inflater           the LayoutInflater to inflate the view
     * @param container          parent view that the fragment UI should attach to
     * @param savedInstanceState previous saved state (not used)
     * @return the root view of the fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the view is created. Initializes Firebase services, loads event data,
     * sets up navigation and join button listeners, and retrieves the current user ID.
     *
     * @param view               the root view returned by {@link #onCreateView}
     * @param savedInstanceState previous saved state (not used)
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userService = new FirebaseService("User");
        eventService = new FirebaseService("Event");
        waitingListService = new FirebaseService("WaitingList");
        imageService = new FirebaseService("Image");

//        eventNameReal = view.findViewById(R.id.tv_event_name);
        

        Bundle args = getArguments();
        if (args != null) {
            eventIdString = args.getString("eventId");
            loadEventInfo(eventIdString);
//            eventName.setText(eventIdString);
        }

        userId = Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        Button joinButton = view.findViewById(R.id.btn_waiting_list);
        Button leaveButton = view.findViewById(R.id.btn_leave_waiting_list);
        LinearLayout waitingStatus = view.findViewById(R.id.layout_waiting_status);
        TextView waitingCount = view.findViewById(R.id.tv_waiting_count);
        locationProgressBar = view.findViewById(R.id.progress_location);
        Button acceptButton = view.findViewById(R.id.btn_accept);
        Button declineButton = view.findViewById(R.id.btn_decline);
        LinearLayout invitedStatus = view.findViewById(R.id.layout_chosen_status);
        Button rejoinButton = view.findViewById(R.id.btn_rejoin_waiting_list);
        LinearLayout uninvitedStatus = view.findViewById(R.id.layout_not_chosen_status);
        LinearLayout acceptedStatus = view.findViewById(R.id.layout_accepted_status);
        LinearLayout declinedStatus = view.findViewById(R.id.layout_declined_status);
        ImageView posterImageView = view.findViewById(R.id.img_event);
        LinearLayout cancelledStatus = view.findViewById(R.id.layout_cancelled_status);

        imageService.getReference().child(eventIdString).get().addOnSuccessListener(task -> {
//            if (task.getResult().getValue() == null || !event.getId().equals(task.getResult().getKey())) return;
//            if (!eventIdString.equals(holder.eventId) || task.getValue() == null) return;
            try {
                String base64Image = ((HashMap<String, String>) task.getValue()).get("url");
                byte[] bytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                posterImageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.i("image error", ":(");
            }

//            imageCache.put(event.getId(), bitmap);
        });
        
        if (locationProgressBar != null) {
            locationProgressBar.setVisibility(View.GONE);
        }

        // QR scanner button
        Button scanButton = view.findViewById(R.id.btn_scan);
        if (scanButton != null) {
            scanButton.setOnClickListener(v -> {
                NavHostFragment.findNavController(EventDetailFragment.this)
                        .navigate(R.id.action_EventDetailFragment_to_QRCodeScannerFragment);
            });
        }

        // QR code button (for viewing QR code if user is organizer)
        Button qrCodeButton = view.findViewById(R.id.btn_qr_code);
        if (qrCodeButton != null) {
//            Log.i("checking event");
            qrCodeButton.setOnClickListener(v -> {
                // Get eventId from Firebase
                eventService.getReference().get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DataSnapshot ds : task.getResult().getChildren()) {
                            if (ds.getKey().equals(eventIdString)) {
                                Object idObj = ds.child("id").getValue();
                                Object nameObj = ds.child("name").getValue();

                                String eventId = idObj != null ? idObj.toString() : eventIdString;
                                String eventNameValue = nameObj != null ? nameObj.toString() : eventIdString;

                                Bundle bundle = new Bundle();
                                bundle.putString("eventId", eventId);
                                bundle.putString("eventName", eventNameValue);

                                NavHostFragment.findNavController(EventDetailFragment.this)
                                        .navigate(R.id.action_EventDetailFragment_to_QRCodeDisplayFragment, bundle);
                                break;
                            }
                        }
                    }
                });
            });
        }

        getEventDetail().addOnCompleteListener(t -> {
//            Log.i("browaiting", t.getResult().toString());
//            if (!t.isSuccessful()) return;
            if (t.getResult()==1) {
                waitingStatus.setVisibility(View.VISIBLE);
                waitingCount.setText("Number of Entrants: " + waitingListCount);
                joinButton.setVisibility(View.INVISIBLE);
            }
            if (t.getResult()==2) {
                invitedStatus.setVisibility(View.VISIBLE);
                joinButton.setVisibility(View.INVISIBLE);
            }
            if (t.getResult()==3) {
                uninvitedStatus.setVisibility(View.VISIBLE);
                joinButton.setVisibility(View.INVISIBLE);
            }
            if (t.getResult()==4) {
                acceptedStatus.setVisibility(View.VISIBLE);
                joinButton.setVisibility(View.INVISIBLE);
            }
            if (t.getResult()==5) {
                declinedStatus.setVisibility(View.VISIBLE);
                joinButton.setVisibility(View.INVISIBLE);
            }
            if (t.getResult()==6) {
                cancelledStatus.setVisibility(View.VISIBLE);
                joinButton.setVisibility(View.INVISIBLE);
            }
            waitingCount.setText("Number of Entrants: " + waitingListCount);
        });

        joinButton.setOnClickListener(v -> {
            // Check if event is on hold
            if (eventOnHold) {
                Toast.makeText(getContext(),
                        "This event is currently on hold. You cannot join the waiting list.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            
            userExists().continueWithTask(boole -> {
                if (boole.getResult()) {
                    // Check if geolocation is required
                    if (geolocationRequired) {
                        requestLocationAndJoin();
                    } else {
                        // No geolocation required, join normally
                        Entrant e = new Entrant(userId, args.getString("eventId"));
                        e.joinWaitingList();
                        Toast.makeText(getContext(),
                                "Joined waiting list :)",
                                Toast.LENGTH_SHORT).show();
                        waitingStatus.setVisibility(View.VISIBLE);
                        joinButton.setVisibility(View.INVISIBLE);
                    }
                } else {
                    Toast.makeText(getContext(),
                            "You need to a create profile to join the waiting list.",
                            Toast.LENGTH_SHORT).show();
                }

                return getWaitingCount();
            }).addOnCompleteListener(t -> {
                Log.i("RTD9", "" + t.getResult());
                waitingCount.setText("Number of Entrants: " + t.getResult());
            });
        });

        leaveButton.setOnClickListener(v -> {
            // Check if event is on hold
            if (eventOnHold) {
                Toast.makeText(getContext(),
                        "This event is currently on hold. You cannot leave the waiting list.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            
            Entrant e = new Entrant(userId, args.getString("eventId"));

            e.leaveWaitingList();
            Toast.makeText(getContext(),
                    "You left the waiting list.",
                    Toast.LENGTH_SHORT).show();

            joinButton.setVisibility(View.VISIBLE);
            waitingStatus.setVisibility(View.INVISIBLE);

        });

        acceptButton.setOnClickListener(v -> {
            // Check if event is on hold
            if (eventOnHold) {
                Toast.makeText(getContext(),
                        "This event is currently on hold. You cannot accept the invitation.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            
            Entrant e = new Entrant(userId, args.getString("eventId"));

            e.acceptWaitingList();
            Toast.makeText(getContext(),
                    "You accept the invitation. Yah!!!.",
                    Toast.LENGTH_SHORT).show();
            invitedStatus.setVisibility(View.INVISIBLE);
            acceptedStatus.setVisibility(View.VISIBLE);
        });

        declineButton.setOnClickListener(v -> {
            // Check if event is on hold
            if (eventOnHold) {
                Toast.makeText(getContext(),
                        "This event is currently on hold. You cannot decline the invitation.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            
            Entrant e = new Entrant(userId, args.getString("eventId"));

            e.declineWaitingList();
            Toast.makeText(getContext(),
                    "You decline the invitation :(((",
                    Toast.LENGTH_SHORT).show();
            invitedStatus.setVisibility(View.INVISIBLE);
            declinedStatus.setVisibility(View.VISIBLE);
        });

        rejoinButton.setOnClickListener(v -> {
            Entrant e = new Entrant(userId, args.getString("eventId"));

            e.joinWaitingList();
            waitingListService.deleteSubCollectionEntry(eventId, "UNINVITED", e.getEntrantId());
            Toast.makeText(getContext(),
                    "You rejoin the waiting list.",
                    Toast.LENGTH_SHORT).show();
            uninvitedStatus.setVisibility(View.INVISIBLE);
            waitingStatus.setVisibility(View.VISIBLE);
            joinButton.setVisibility(View.INVISIBLE);
        });
    }

    private void loadEventInfo(String eventId) {
        if (eventId == null) return;

        eventService.getReference().child(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String details = snapshot.child("eventDetails").getValue(String.class);
                        String startTime = snapshot.child("eventStartTime").getValue(String.class);
                        String endTime = snapshot.child("eventEndTime").getValue(String.class);
                        String startDateStr = snapshot.child("eventStartDate").getValue(String.class);
                        String endDateStr = snapshot.child("eventEndDate").getValue(String.class);
                        String startReg = snapshot.child("registrationStartDate").getValue(String.class);
                        String endReg = snapshot.child("registrationEndDate").getValue(String.class);

                        Long limitLong = snapshot.child("entrantLimit").getValue(Long.class);
                        String limit = limitLong != null ? String.valueOf(limitLong) : "0";

                        // Populate UI
                        binding.tvDate.setText(startDateStr);
                        binding.tvEventName.setText(name);
                        binding.tvEventDetails.setText(details);
                        binding.tvStartTime.setText(startTime);
                        binding.tvEndTime.setText(endTime);
                        binding.tvStartDate.setText(formatDatePretty(startDateStr));
                        binding.tvEndDate.setText(formatDatePretty(endDateStr));
                        binding.tvRegistrationOpen.setText(formatDatePretty(startReg));
                        binding.tvRegistrationDeadline.setText(formatDatePretty(endReg));

                        if (startDateStr != null) {
                            try {
                                // Parse the date string
                                SimpleDateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
                                Date date = inputFormat.parse(startDateStr);

                                // Format month abbreviation
                                SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.ENGLISH);
                                String month = monthFormat.format(date).toUpperCase(); // e.g., "OCT"

                                // Get day
                                SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.ENGLISH);
                                String day = dayFormat.format(date); // e.g., "30"

                                // Combine
                                String display = month + "\n" + day;

                                // Set TextView
                                binding.tvDate.setText(display);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                binding.tvDate.setText(startDateStr); // fallback
                            }
                        }


                        // Waiting count
                        getWaitingCount().addOnSuccessListener(count -> {
                            binding.tvEntrantsCount.setText(count + " / " + limit);
                        });
                    } else {
                        Log.e("EventDetail", "Event not found for id: " + eventId);
                    }
                })
                .addOnFailureListener(e -> Log.e("EventDetail", "Failed to load event: " + e.getMessage()));
    }




    public Task<Integer> getWaitingCount() {
        if (eventId == null) {
            return Tasks.forResult(0);
        }

        return waitingListService.getReference().child(eventId).get()
                .continueWith(task -> {
                    int total = 0;
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DataSnapshot obj : task.getResult().getChildren()) {
                            if ("WAITING".equals(obj.getKey())) {
                                total++;
                            }
                        }
                    }
                    return total;
                });
    }

    public Task<Integer> getEventDetail() {
        return eventService.getReference().get().continueWithTask(task -> {
            for (DataSnapshot ds : task.getResult().getChildren()) {

                Log.i("browaiting", ds.getKey() + " : " + eventIdString + " ");
                if (ds.getKey().equals(eventIdString)) {
                    HashMap<String, Object> hash = (HashMap<String, Object>) ds.getValue();
                    eventId = (String) hash.get("id");

                    // Check if geolocation is required
                    Object geoRequired = hash.get("geolocationRequired");
                    if (geoRequired instanceof Boolean) {
                        geolocationRequired = (Boolean) geoRequired;
                    } else {
                        geolocationRequired = false; // Default to false if not set
                    }

                    // Check if event is on hold
                    Object onHoldObj = hash.get("onHold");
                    if (onHoldObj instanceof Boolean) {
                        eventOnHold = (Boolean) onHoldObj;
                    } else {
                        eventOnHold = false; // Default to false if not set
                    }

                    getWaitingCount();

                    // Return Task<Boolean> directly (no extra wrapping)
                    return lookWaitingList();
                }
            }

            // No matching event found, return a completed Task with 'false'
            return Tasks.forResult(0);
        });
    }

    public Task<Integer> lookWaitingList() {
        return waitingListService.getReference().child(eventId).get().continueWith(task -> {
            DataSnapshot root = task.getResult();

            if (root.child("WAITING").hasChild(userId)) return 1;
            if (root.child("INVITED").hasChild(userId)) return 2;
            if (root.child("UNINVITED").hasChild(userId)) return 3;
            if (root.child("ACCEPTED").hasChild(userId)) return 4;
            if (root.child("DECLINED").hasChild(userId)) return 5;
            if (root.child("CANCELLED").hasChild(userId)) return 6;

            return 0;
        });
    }

    /**
     * Checks whether a user profile exists in Firebase for the current {@link #userId}.
     * <p>
     * Reads all children under the "User" node and checks if any key matches {@code userId}.
     * Returns {@code true} if found, {@code false} otherwise.
     * </p>
     *
     * @return a {@link Task} that resolves to {@code true} if the user exists,
     *         {@code false} if not
     */
    public Task<Boolean> userExists() {
        return userService.getReference().get().continueWith(ds -> {
            boolean userExists = false;
            for (DataSnapshot d : ds.getResult().getChildren()) {
                Log.i("TAGwerw", d.getKey());
                try {
                    HashMap<String, Object> userHash = (HashMap<String, Object>) d.getValue();
                    if (userId.equals(d.getKey())) {
                        return true;
                    }
                } catch(Exception e) {
                    Log.e("ERROR", "weird error " + e);
                }
            }
            return false;
        });
    }

    /**
     * Requests location permission and gets location, then joins the waiting list.
     * If permission is denied or location cannot be obtained, joining is blocked.
     */
    private void requestLocationAndJoin() {
        // Check if permission is already granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, get location
            getLocationAndJoin();
        } else {
            // Check if user permanently denied permission
            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Permission was permanently denied, redirect to settings
                Toast.makeText(getContext(),
                        "Location permission is required. Please enable it in app settings.",
                        Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(android.net.Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
            } else {
                // Request permission
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    /**
     * Handles the result of the location permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                getLocationAndJoin();
            } else {
                // Permission denied
                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Permanently denied, redirect to settings
                    Toast.makeText(getContext(),
                            "Location permission is required. Please enable it in app settings.",
                            Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.parse("package:" + requireContext().getPackageName()));
                    startActivity(intent);
                } else {
                    // Denied but can ask again
                    Toast.makeText(getContext(),
                            "Location permission is required to join this event",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Gets the current location and joins the waiting list with location data.
     * Requests a fresh location update instead of using cached location.
     * Includes timeout mechanism and location validation.
     */
    private void getLocationAndJoin() {
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        // Check if location services are enabled
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(getContext(),
                    "Location services are disabled. Please enable location services to join this event",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Show loading indicator
        if (locationProgressBar != null) {
            locationProgressBar.setVisibility(View.VISIBLE);
        }
        Toast.makeText(getContext(), "Getting your location...", Toast.LENGTH_SHORT).show();

        // Initialize timeout handler
        locationTimeoutHandler = new Handler(Looper.getMainLooper());
        locationTimeoutRunnable = () -> {
            // Timeout reached, stop location updates and show error
            if (locationManager != null && locationListener != null) {
                try {
                    locationManager.removeUpdates(locationListener);
                } catch (SecurityException e) {
                    Log.e("EventDetail", "Security exception removing location updates", e);
                }
            }

            if (locationProgressBar != null) {
                locationProgressBar.setVisibility(View.GONE);
            }

            Toast.makeText(getContext(),
                    "Location request timed out. Please check your location settings and try again.",
                    Toast.LENGTH_LONG).show();

            Log.w("EventDetail", "Location request timed out after " + LOCATION_TIMEOUT_MS + "ms");
        };

        // Start timeout timer
        locationTimeoutHandler.postDelayed(locationTimeoutRunnable, LOCATION_TIMEOUT_MS);

        // Request fresh location update
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // Cancel timeout since we got a location
                if (locationTimeoutHandler != null && locationTimeoutRunnable != null) {
                    locationTimeoutHandler.removeCallbacks(locationTimeoutRunnable);
                }

                // Validate location
                if (!isValidLocation(location)) {
                    Log.w("EventDetail", "Invalid location received: " + location.getLatitude() + ", " + location.getLongitude());
                    Toast.makeText(getContext(),
                            "Invalid location received. Please try again.",
                            Toast.LENGTH_LONG).show();

                    if (locationProgressBar != null) {
                        locationProgressBar.setVisibility(View.GONE);
                    }

                    // Remove location listener
                    if (locationManager != null && locationListener != null) {
                        try {
                            locationManager.removeUpdates(locationListener);
                        } catch (SecurityException e) {
                            Log.e("EventDetail", "Security exception removing location updates", e);
                        }
                    }
                    return;
                }

                // Log location for debugging
                Log.i("EventDetail", "Location obtained: " + location.getLatitude() + ", " + location.getLongitude() +
                        " (Accuracy: " + location.getAccuracy() + "m, Provider: " + location.getProvider() + ")");

                // Got valid location, join with it
                Bundle args = getArguments();
                Log.i("printing stuff", args.getString("eventId"));
                Entrant e = new Entrant(userId, args != null ? args.getString("eventId") : eventId);
                e.joinWaitingList(location.getLatitude(), location.getLongitude());
                Toast.makeText(getContext(),
                        "Joined waiting list with location :)",
                        Toast.LENGTH_SHORT).show();

                getWaitingCount().addOnCompleteListener(t -> {
                    Log.i("RTD9", "" + t.getResult());
                    ((TextView) getView().findViewById(R.id.tv_waiting_count)).setText("Number of Entrants: " + t.getResult());
                });

                // Update UI
                LinearLayout waitingStatus = getView().findViewById(R.id.layout_waiting_status);
                if (waitingStatus != null) {
                    waitingStatus.setVisibility(View.VISIBLE);
                }

                // Hide loading indicator
                if (locationProgressBar != null) {
                    locationProgressBar.setVisibility(View.GONE);
                }

                // Remove location listener to stop updates
                if (locationManager != null && locationListener != null) {
                    try {
                        locationManager.removeUpdates(locationListener);
                    } catch (SecurityException e2) {
                        Log.e("EventDetail", "Security exception removing location updates", e2);
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                Log.i("EventDetail", "Location provider enabled: " + provider);
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Log.w("EventDetail", "Location provider disabled: " + provider);
            }
        };

        // Try GPS first (more accurate)
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.i("EventDetail", "Requesting location from GPS provider");
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            } catch (SecurityException e) {
                Log.e("EventDetail", "Security exception requesting GPS location", e);
                handleLocationError();
            }
        }
        // Fallback to network if GPS not available
        else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Log.i("EventDetail", "Requesting location from Network provider");
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            } catch (SecurityException e) {
                Log.e("EventDetail", "Security exception requesting Network location", e);
                handleLocationError();
            }
        } else {
            handleLocationError();
        }
    }

    /**
     * Validates that a location is reasonable (not 0,0 and within valid ranges).
     *
     * @param location the location to validate
     * @return true if location is valid, false otherwise
     */
    private boolean isValidLocation(Location location) {
        if (location == null) {
            return false;
        }

        double lat = location.getLatitude();
        double lon = location.getLongitude();

        // Check if coordinates are 0,0 (likely invalid)
        if (lat == 0.0 && lon == 0.0) {
            return false;
        }

        // Check valid ranges: latitude -90 to 90, longitude -180 to 180
        if (lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
            return false;
        }

        return true;
    }

    /**
     * Handles location errors by showing appropriate messages and cleaning up.
     */
    private void handleLocationError() {
        if (locationProgressBar != null) {
            locationProgressBar.setVisibility(View.GONE);
        }

        Toast.makeText(getContext(),
                "Could not obtain location. Please enable location services and try again",
                Toast.LENGTH_LONG).show();
    }
    private String formatDatePretty(String dateStr) {
        if (dateStr == null) return "";

        try {
            // Input format from Firebase: "MM-dd-yyyy"
            SimpleDateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
            Date date = inputFormat.parse(dateStr);

            // Desired output format: "MMM d, yyyy" (e.g., "Oct 30, 2025")
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
            return outputFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
            return dateStr; // fallback
        }
    }

    /**
     * Cleans up the View Binding reference and location listener to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cancel timeout if still active
        if (locationTimeoutHandler != null && locationTimeoutRunnable != null) {
            locationTimeoutHandler.removeCallbacks(locationTimeoutRunnable);
        }

        // Remove location listener if still active
        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException e) {
                // Permission might have been revoked
                Log.e("EventDetail", "Security exception removing location updates in onDestroyView", e);
            }
        }
        binding = null;
    }
}