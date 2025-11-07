package com.example.chicksevent;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;

/**
 * Fragment that allows users to search and filter events by interests and availability.
 * <p>
 * This screen collects user-defined search filters and invokes
 * {@link User#filterEvents(ArrayList)} to retrieve matching event IDs
 * from Firebase. Results are passed to {@link EventFragment} for display.
 * </p>
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *     <li>Collect filter input (interests and availability) from the user interface.</li>
 *     <li>Delegate filtering logic to the current {@link User} instance.</li>
 *     <li>Navigate to the event list screen with the filtered results.</li>
 *     <li>Provide an option to reset search filters.</li>
 * </ul>
 * </p>
 *
 * @author Jinn Kasai
 * @author Jordan Kwan
 */
public class SearchEventFragment extends Fragment {

    /** Default constructor inflating the search event layout. */
    public SearchEventFragment() {
        super(R.layout.fragment_search_event);
    }

    /**
     * Called after the fragment’s view has been created.
     * <p>
     * Sets up UI listeners, handles filter application, and triggers navigation upon completion.
     * </p>
     *
     * @param view the root view of the fragment
     * @param savedInstanceState saved instance state, or {@code null} for a fresh creation
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Back arrow
        ImageButton back = view.findViewById(R.id.btn_back);
        if (back != null) back.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        // ---- UI references from XML ----
        EditText etInterest       = view.findViewById(R.id.search_interest);
        Spinner  spAvailability   = view.findViewById(R.id.spinner_availability);
        Button   btnApply         = view.findViewById(R.id.btn_apply_filter);
        Button   btnClear         = view.findViewById(R.id.btn_clear_filter);

        // Create a User using device id (same pattern you used elsewhere)
        String androidId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        User user = new User(androidId);

        // Apply filter -> call your User.filterEvents(ArrayList<String>)
        btnApply.setOnClickListener(v -> {
            ArrayList<String> filters = new ArrayList<>();

            // interests: split by spaces or commas
            String interest = etInterest.getText().toString().trim();
            if (!interest.isEmpty()) {
                for (String token : interest.split("[,\\s]+")) {
                    if (!token.isEmpty()) filters.add(token);
                }
            }

            // availability (optional): if not "Any", add as a tag
//            if (spAvailability != null && spAvailability.getSelectedItem() != null) {
//                String opt = spAvailability.getSelectedItem().toString().trim();
//                if (!opt.equalsIgnoreCase("Any") && !opt.isEmpty()) {
//                    filters.add(opt);
//                }
//            }

            Log.i("filteringthing", "what is this filter " + filters);

            user.filterEvents(filters).addOnCompleteListener(task -> {
                if (!isAdded()) return;
//                boolean ok = task.isSuccessful() && Boolean.TRUE.equals(task.getResult());

                ArrayList<String> eventList = task.getResult();
//
                Toast.makeText(getContext(),
                        !eventList.isEmpty() ? "Found matching events" : "No matching events",
                        Toast.LENGTH_SHORT).show();
////
                NavController navController = NavHostFragment.findNavController(SearchEventFragment.this);
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("eventList", eventList);
                navController.navigate(R.id.action_SearchEventFragment_to_EventFragment, bundle);

                // TODO: if ok, you can now refresh your RecyclerView with matching events
            }).addOnFailureListener(e -> {
                if (!isAdded()) return;
                Toast.makeText(getContext(),
                        "Filter error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });

        // Clear fields
        btnClear.setOnClickListener(v -> {
            etInterest.setText("");
            if (spAvailability != null && spAvailability.getAdapter() != null) {
                spAvailability.setSelection(0); // usually "Any"
            }
        });
    }
}
