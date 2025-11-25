package com.example.chicksevent.fragment_org;

import android.content.Intent; // <-- Add this
import android.net.Uri;        // <-- Add this
import android.widget.Toast;// <-- Add this
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.R;
import com.example.chicksevent.databinding.FragmentEventDetailOrgBinding;
import com.example.chicksevent.misc.FirebaseService;
import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;

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

        TextView eventName = view.findViewById(R.id.tv_event_name);

        Bundle args = getArguments();
        if (args != null) {
            eventName.setText(args.getString("eventId"));
            // Use it to populate UI
        }

        
        
        

        Button viewWaitingListButton = view.findViewById(R.id.btn_waiting_list);
        Button viewChosenListButton = view.findViewById(R.id.btn_chosen_entrants);
        Button viewCancelledListButton = view.findViewById(R.id.btn_cancelled_entrants);
        Button viewFinalListButton = view.findViewById(R.id.btn_finalist);

        Button viewMapButton = view.findViewById(R.id.btn_map);
        TextView eventDetails = view.findViewById(R.id.tv_event_details);
        TextView eventNameReal = view.findViewById(R.id.tv_time);



        eventService.getReference().get().continueWith(task -> {
//            eventName =
            for (DataSnapshot ds : task.getResult().getChildren()) {
                if (ds.getKey().equals(args.getString("eventId"))) {
                    HashMap<String, String> hash = (HashMap<String, String>) ds.getValue();
                    eventNameReal.setText(hash.get("name"));
                    eventDetails.setText(hash.get("eventDetails"));
                }
            }
            return null;
        });

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
