package com.example.chicksevent.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chicksevent.R;
import com.example.chicksevent.enums.EntrantStatus;
import com.example.chicksevent.misc.Entrant;
import com.example.chicksevent.misc.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Adapter that binds {@link Entrant} objects to a ListView for display.
 * <p>
 * Provides a simple textual representation of each entrant, currently displaying
 * the entrant ID associated with each {@link Entrant} instance.
 * </p>
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *     <li>Inflate the {@code item_entrant} layout for each list entry.</li>
 *     <li>Populate the layout with data from a {@link Entrant} object.</li>
 *     <li>Reuse views efficiently through view recycling.</li>
 * </ul>
 * </p>
 *
 * <p>This adapter can be extended to include additional entrant details (e.g.,
 * name, email, phone number) as the application evolves.</p>
 *
 * @author Jordan and Hanh </3
 */
public class EntrantAdapter extends ArrayAdapter<Entrant> {
    /**
     * Constructs a new adapter for displaying user information.
     *
     * @param context the current context
     * @param userArray the list of {@link User} objects to display
     */
    public EntrantAdapter(Context context, ArrayList<Entrant> userArray) {
        super(context, 0, userArray);
    }

    /**
     * Returns a populated list item view for a given position.
     *
     * @param position the position of the item within the list
     * @param convertView an existing view to reuse if possible
     * @param parent the parent view group that this view will be attached to
     * @return a populated view representing the {@link User} at the given position
     */
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_chosen_user, parent, false);
        } else {
            view = convertView;
        }

        Entrant entrant = getItem(position);
        TextView userName = view.findViewById(R.id.tv_user_name);
        TextView statusView = view.findViewById(R.id.tv_status);
        ImageButton deleteBtn = view.findViewById(R.id.btn_delete);

        // Load name as usual
        entrant.getName().addOnCompleteListener(name -> {
            userName.setText(name.getResult());
        });

        String uid = entrant.getEntrantId();
        String eventId = entrant.getEventId();

        // Load live status from database
        DatabaseReference statusRef = FirebaseDatabase.getInstance()
                .getReference("WaitingList")
                .child(eventId);

        statusRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) return;

            DataSnapshot snapshot = task.getResult();
            EntrantStatus status = EntrantStatus.INVITED; // default fallback

            if (snapshot.child("ACCEPTED").hasChild(uid)) {
                status = EntrantStatus.ACCEPTED;
            } else if (snapshot.child("INVITED").hasChild(uid)) {
                status = EntrantStatus.INVITED;
            } else if (snapshot.child("CANCELLED").hasChild(uid)) {
                status = EntrantStatus.CANCELLED;
            }

            statusView.setText(status.name());

            // Update the entrant object so it stays consistent
            entrant.setStatus(status);

            // Set up delete button using updated status
            setupDeleteButton(deleteBtn, entrant, userName, status);
        });

        return view;
    }

    private void setupDeleteButton(ImageButton deleteBtn, Entrant entrant,
                                   TextView userName, EntrantStatus status) {

        deleteBtn.setOnClickListener(v -> {
            String uid = entrant.getEntrantId();
            String eventId = entrant.getEventId();

            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(getContext(), "Missing eventId!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (status != EntrantStatus.INVITED) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Cannot Cancel")
                        .setMessage("This entrant is already signed up, so they cannot be cancelled.")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
                return;
            }

            new AlertDialog.Builder(getContext())
                    .setTitle("Cancel Entrant")
                    .setMessage("Are you sure you want to cancel " + userName.getText() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        DatabaseReference root = FirebaseDatabase.getInstance()
                                .getReference("WaitingList")
                                .child(eventId);

                        root.child("INVITED").child(uid).removeValue();
                        root.child("CANCELLED").child(uid).setValue(true);

                        Toast.makeText(getContext(),
                                "Cancelled " + uid,
                                Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    // Test helper (ignored by app)
    public void cancelEntrantForTest(Entrant e) {
        DatabaseReference root = FirebaseDatabase.getInstance()
                .getReference("WaitingList")
                .child(e.getEventId());

        root.child("INVITED").child(e.getEntrantId()).removeValue();
        root.child("CANCELLED").child(e.getEntrantId()).setValue(true);
    }

}
