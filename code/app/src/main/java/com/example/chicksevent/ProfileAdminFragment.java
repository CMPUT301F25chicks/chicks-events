package com.example.chicksevent;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays and manages a list of entrants for an admin user.
 * <p>
 * This fragment allows the admin to view all entrants using {@link Admin#browseEntrants()},
 * and to delete entrants through {@link Admin#deleteEntrantProfile(String)}.
 * The data is displayed in a {@link RecyclerView} using {@link EntrantListAdapter}.
 */
public class ProfileAdminFragment extends Fragment {
    private RecyclerView recyclerView;
    private EntrantListAdapter adapter;
    private List<EntrantDisplay> entrantList;
    private Admin admin;

    /**
     * Inflates the fragment layout and initializes UI components.
     *
     * @param inflater  The LayoutInflater object that can be used to inflate any views.
     * @param container The parent view that this fragment’s UI should attach to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The root view of the inflated layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_profile, container, false);

        recyclerView = view.findViewById(R.id.recycler_chosenUser);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        entrantList = new ArrayList<>();
        adapter = new EntrantListAdapter(entrantList, this::deleteEntrant);
        recyclerView.setAdapter(adapter);

        admin = new Admin(Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        ));

        loadEntrants();

        return view;
    }

    /**
     * Loads all entrants associated with the admin account.
     * <p>
     * Retrieves entrants asynchronously using {@link Admin#browseEntrants()}.
     * When the data is loaded, it updates the {@link RecyclerView} adapter to display the entrants.
     */
    private void loadEntrants() {
        admin.browseEntrants()
                .addOnCompleteListener(entrants -> {
                    entrantList.clear();
                    Log.i("friedchick", entrants.getResult().toString());

                    for (User user : entrants.getResult()) {
                        entrantList.add(new EntrantDisplay(user.getUserId(), "Active"));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e("ProfileAdminFragment", "Failed to load entrants", e));
    }

    /**
     * Deletes an entrant profile from the database.
     * <p>
     * Called when the admin selects the delete option in the list.
     *
     * @param entrant The entrant to be deleted.
     */
    private void deleteEntrant(EntrantDisplay entrant) {
        Log.i("friedchicken", "Deleting entrant: " + entrant.getEntrantId());

        admin.deleteEntrantProfile(entrant.getEntrantId());
        loadEntrants();
    }
}
