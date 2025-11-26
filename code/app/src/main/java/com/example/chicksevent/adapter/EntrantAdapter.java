package com.example.chicksevent.adapter;

import android.content.Context;
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
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_chosen_user, parent, false);
        } else {
            view = convertView;
        }

        Entrant entrant = getItem(position);
        TextView userName = view.findViewById(R.id.tv_user_name);
        TextView statusView = view.findViewById(R.id.tv_status);
        ImageButton deleteBtn = view.findViewById(R.id.btn_delete);

        entrant.getName().addOnCompleteListener(name -> {
            userName.setText(name.getResult());
        });

        statusView.setText("TODO");

        deleteBtn.setOnClickListener(v -> {
            String uid = entrant.getEntrantId();
            String eventId = entrant.getEventId();
            EntrantStatus status = entrant.getStatus();

            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(getContext(), "Missing eventId!", Toast.LENGTH_SHORT).show();
                return;
            }

            // If NOT in INVITED, they cannot be cancelled
            if (!"INVITED".equals(status)) {
                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                        .setTitle("Cannot Cancel")
                        .setMessage("This entrant is already signed up, so they cannot be cancelled.")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
                return;
            }

            // Only INVITED entrants can be cancelled
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Cancel Entrant")
                    .setMessage("Are you sure you want to cancel " + userName.getText() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        DatabaseReference root = FirebaseDatabase.getInstance()
                                .getReference("WaitingList")
                                .child(eventId);

                        root.child("INVITED").child(uid).removeValue();
                        root.child("CANCELLED").child(uid).setValue(true);

                        Toast.makeText(getContext(),
                                "Cancelled entrant " + uid,
                                Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        return view;
    }
}
