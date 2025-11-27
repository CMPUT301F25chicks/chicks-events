package com.example.chicksevent.fragment;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.R;
import com.example.chicksevent.misc.User;

import java.util.ArrayList;

/**
 * Fragment that allows users to search and filter events by interests and availability.
 * <p>
 * This screen collects user-defined search filters and invokes
 * filterEvents to retrieve matching event IDs
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

    ArrayList<String> filters = new ArrayList<>();
    String filterAvailability = null;

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
        ImageButton back = view.findViewById(R.id.btn_check);
        if (back != null) back.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        // ---- UI references from XML ----
        EditText etInterest       = view.findViewById(R.id.search_interest);
        Spinner  spAvailability   = view.findViewById(R.id.spinner_availability);
        Button   btnApply         = view.findViewById(R.id.btn_apply_filter);
        Button   btnClear         = view.findViewById(R.id.btn_clear_filter);
        ImageButton   btnFilter        = view.findViewById(R.id.btn_filter);
        LinearLayout filterPanel = view.findViewById(R.id.filter_panel);
        Button btnSave = view.findViewById(R.id.btn_save);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.availability_options,
                R.layout.spinner_item
        );
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spAvailability.setAdapter(adapter);

        // Create a User using device id (same pattern you used elsewhere)
        String androidId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        User user = new User(androidId);

        btnFilter.setOnClickListener(v -> {
            if (filterPanel.getVisibility() == VISIBLE) {
                filterPanel.setVisibility(INVISIBLE);
            } else {
                filterPanel.setVisibility(VISIBLE);
            }

        });

        // Apply filter -> call your User.filterEvents(ArrayList<String>)
        btnApply.setOnClickListener(v -> {
            filters = new ArrayList<>();

            // interests: split by spaces or commas
            String interest = etInterest.getText().toString().trim();
            if (!interest.isEmpty()) {
                for (String token : interest.split("[,]+")) {
                    if (!token.isEmpty()) filters.add(token);
                }
            }


//            for (String z : filters) {
//                Log.i("what is filter", z);
//
//            }

            // availability (optional): if not "Any", add as a tag
            if (spAvailability != null && spAvailability.getSelectedItem() != null) {
                filterAvailability = spAvailability.getSelectedItem().toString();
            }

            Toast.makeText(getContext(),
                    "filter applied",
                    Toast.LENGTH_SHORT).show();

            filterPanel.setVisibility(INVISIBLE);
        });

        btnSave.setOnClickListener(v -> {
            EditText searchBar = view.findViewById(R.id.search_bar);
            if (!searchBar.getText().toString().equals("")) {
                filters.add(searchBar.getText().toString());
            }

            Log.i("filteringthing", "what is this filter " + filters);

            user.filterEvents(filters, filterAvailability).addOnCompleteListener(task -> {
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
                bundle.putString("filterAvailability", filterAvailability);
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
