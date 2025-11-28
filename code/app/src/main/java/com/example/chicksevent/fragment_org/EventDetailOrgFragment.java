package com.example.chicksevent.fragment_org;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.R;
import com.example.chicksevent.databinding.FragmentEventDetailOrgBinding;
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
 * Fragment displaying detailed information about an event from the organizer's perspective.
 * <p>
 * This screen enables organizers to view event details, navigate to related fragments
 * (such as the waiting list, notifications, or the event list), and create new events.
 * It passes the selected event name between fragments using a {@link Bundle}.
 * </p>
 *
 * <p><b>Navigation:</b>
 * <ul>
 *   <li>Navigate to {@code NotificationFragment}</li>
 *   <li>Navigate to {@code EventFragment}</li>
 *   <li>Navigate to {@code CreateEventFragment}</li>
 *   <li>Navigate to {@code WaitingListFragment} (with event name argument)</li>
 * </ul>
 * </p>
 *
 * <p><b>Usage:</b> Typically accessed when an organizer selects an event they manage.
 * It retrieves the event name from fragment arguments and binds it to the view.
 * </p>
 *
 * @author Jordan Kwan
 */
public class EventDetailOrgFragment extends Fragment {

    /** View binding for the organizer event detail layout. */
    private FragmentEventDetailOrgBinding binding;

    private FirebaseService eventService;
    private FirebaseService imageService;

    private FirebaseService waitingListService = new FirebaseService("WaitingList");


    private String eventId;


    /**
     * Inflates the layout for the organizer event detail fragment.
     *
     * @param inflater LayoutInflater used to inflate the fragment's views.
     * @param container Parent view that the fragment's UI should attach to.
     * @param savedInstanceState Saved state from previous instance, if any.
     * @return the inflated root view for this fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventDetailOrgBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the fragment view hierarchy has been created.
     * Initializes event detail display, sets up navigation and button interactions.
     *
     * @param view the root view returned by {@link #onCreateView}.
     * @param savedInstanceState Previously saved state, if available.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventService = new FirebaseService("Event");
        imageService = new FirebaseService("Image");

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId");
            loadEventDetails(eventId);
        } else {
            Log.e("EventDetail", "No eventId passed to fragment!");
        }


        Button viewWaitingListButton = view.findViewById(R.id.btn_waiting_list);
        Button viewChosenListButton = view.findViewById(R.id.btn_chosen_entrants);
        Button viewCancelledListButton = view.findViewById(R.id.btn_cancelled_entrants);
        Button viewFinalListButton = view.findViewById(R.id.btn_finalist);

        Button viewMapButton = view.findViewById(R.id.btn_map);
        TextView eventDetails = view.findViewById(R.id.tv_event_details);
        TextView eventNameReal = view.findViewById(R.id.tv_event_name);

        ImageView posterImageView = view.findViewById(R.id.img_event);

        imageService.getReference().child(args.getString("eventId")).get().addOnSuccessListener(task -> {
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

        viewWaitingListButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailOrgFragment.this);

            Bundle bundle = new Bundle();
            bundle.putString("eventId", args.getString("eventId"));


            navController.navigate(R.id.action_EventDetailOrgFragment_to_WaitingListFragment, bundle);
        });

        viewCancelledListButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailOrgFragment.this);

            Bundle bundle = new Bundle();
            bundle.putString("eventId", args.getString("eventId"));


            navController.navigate(R.id.action_EventDetailOrgFragment_to_CancelledListFragment, bundle);
        });

        viewFinalListButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailOrgFragment.this);

            Bundle bundle = new Bundle();
            bundle.putString("eventId", args.getString("eventId"));


            navController.navigate(R.id.action_EventDetailOrgFragment_to_FinalListFragment, bundle);
        });

        Button exportCsvButton = view.findViewById(R.id.btn_export_csv);
        /**
         * Sets up a click listener for the 'Export to CSV' button.
         * <p>* When clicked, this listener retrieves the current event's ID from the fragment arguments.
         * It then constructs a URL by appending the event ID as a query parameter
         * to a predefined Firebase Cloud Function URL.
         * </p>
         * <p>
         * An {@link Intent#ACTION_VIEW} is created with this URL, which opens a web browser.
         * The Cloud Function is responsible for generating a CSV file and setting the
         * appropriate HTTP headers to trigger a file download in the browser.
         * </p>
         * <p>
         * Includes error handling for missing event data or if no web browser is installed
         * on the device.
         * </p>
         **/
        exportCsvButton.setOnClickListener(v -> {
            // 1. Get the eventId from the fragment arguments
            if (args == null) {
                Toast.makeText(getContext(), "Error: Event data not found.", Toast.LENGTH_SHORT).show();
                return;
            }
            String eventId = args.getString("eventId");
            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(getContext(), "Error: Event ID is missing.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Use the CORRECT Deployed Cloud Function URL
            String functionUrl = "https://us-central1-listycity-friedchicken.cloudfunctions.net/exportFinalEntrants";

            // 3. Build the final URL with the eventId as a query parameter
            String downloadUrl = functionUrl + "?eventId=" + eventId;

            // 4. Create an Intent to open the URL in a web browser.
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(downloadUrl));

            // 5. Start the activity and handle potential errors
            try {
                startActivity(intent);
            } catch (android.content.ActivityNotFoundException e) {
                // This error occurs if no web browser is installed on the device.
                Toast.makeText(getContext(), "Error: No web browser found.", Toast.LENGTH_SHORT).show();
            }
        });


        viewChosenListButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailOrgFragment.this);

            Bundle bundle = new Bundle();
            bundle.putString("eventId", args.getString("eventId"));

            navController.navigate(R.id.action_EventDetailOrgFragment_to_ChosenListFragment, bundle);
        });

        Button viewQRCodeButton = view.findViewById(R.id.btn_qr_code);

        viewQRCodeButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailOrgFragment.this);

            // Get eventId from Firebase (eventName is the Firebase key, but we need the id field)
            String eventIdKey = args.getString("eventId");
            eventService.getReference().child(eventIdKey).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DataSnapshot snapshot = task.getResult();
                    Object idObj = snapshot.child("id").getValue();
                    Object nameObj = snapshot.child("name").getValue();

                    String eventId = idObj != null ? idObj.toString() : eventIdKey;
                    String eventNameValue = nameObj != null ? nameObj.toString() : eventIdKey;

                    Bundle bundle = new Bundle();
                    bundle.putString("eventId", eventId);
                    bundle.putString("eventName", eventNameValue);

                    navController.navigate(R.id.action_EventDetailOrgFragment_to_QRCodeDisplayFragment, bundle);
                } else {
                    // Fallback: use eventName as eventId
                    Bundle bundle = new Bundle();
                    bundle.putString("eventId", eventIdKey);
                    bundle.putString("eventName", eventIdKey);
                    navController.navigate(R.id.action_EventDetailOrgFragment_to_QRCodeDisplayFragment, bundle);
                }
            });
        });


        viewMapButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailOrgFragment.this);

            Bundle bundle = new Bundle();
            bundle.putString("eventId", args.getString("eventId"));
            bundle.putString("eventId", args.getString("eventId")); // Using eventName as eventId

            navController.navigate(R.id.action_EventDetailOrgFragment_to_EntrantLocationMapFragment, bundle);
        });
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


    private void loadEventDetails(String eventId) {
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
                        String tag = snapshot.child("tag").getValue(String.class);

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
                        binding.etEventTag.setText(tag);

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
                        getFinalCount().addOnSuccessListener(count -> {
                            binding.tvEntrantsCount.setText(count + " / " + limit);
                        });
                    } else {
                        Log.e("EventDetail", "Event not found for id: " + eventId);
                    }
                })
                .addOnFailureListener(e -> Log.e("EventDetail", "Failed to load event: " + e.getMessage()));
    }

    public Task<Integer> getFinalCount() {
        if (eventId == null) {
            return Tasks.forResult(0);
        }

        return waitingListService.getReference().child(eventId).get()
                .continueWith(task -> {
                    int total = 0;
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DataSnapshot obj : task.getResult().getChildren()) {
                            if ("ACCEPTED".equals(obj.getKey())) {
                                total++;
                            }
                        }
                    }
//                    waitingListCount = total;
                    return total;
                });
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
