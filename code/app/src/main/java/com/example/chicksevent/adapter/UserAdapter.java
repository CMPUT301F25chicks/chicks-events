package com.example.chicksevent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chicksevent.R;
import com.example.chicksevent.misc.Entrant;
import com.example.chicksevent.misc.FirebaseService;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter that binds {@link Entrant} objects to a list for the organizer UI.
 *
 * <p><b>Displays:</b></p>
 * <ul>
 *     <li>Entrant name</li>
 *     <li>Entrant status (WAITING, INVITED, ACCEPTED, CANCELLED, etc.)</li>
 *     <li>A delete/cancel button to remove an invited entrant</li>
 * </ul>
 *
 * <p>
 * Used when the organizer needs to manage the invited list and cancel or replace entrants.
 * </p>
 */
public class UserAdapter extends ArrayAdapter<Entrant> {

    private final String eventId;

    /**
     * Creates the adapter.
     *
     * @param context app context
     * @param entrantList list of entrants to display
     * @param eventId the event whose waiting-list is being modified
     */
    public UserAdapter(Context context, ArrayList<Entrant> entrantList, String eventId) {
        super(context, 0, entrantList);
        this.eventId = eventId;
    }

    /** Holder pattern for performance */
    private static class ViewHolder {
        TextView nameView;
        TextView statusView;
        ImageButton deleteButton;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_chosen_user, parent, false);

            holder = new ViewHolder();
            holder.nameView = convertView.findViewById(R.id.tv_user_name);
            holder.statusView = convertView.findViewById(R.id.tv_status);
            holder.deleteButton = convertView.findViewById(R.id.btn_delete);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Entrant entrant = getItem(position);

        if (entrant != null) {

            // Show entrant name + status
            holder.nameView.setText(entrant.getName());
            holder.statusView.setText(entrant.getStatus().name());

            // Delete / Cancel an invited entrant
            holder.deleteButton.setOnClickListener(v -> {

                String entrantId = entrant.getEntrantId();
                DatabaseReference ref =
                        new FirebaseService("WaitingList").getReference().child(eventId);

                Map<String, Object> updates = new HashMap<>();
                updates.put("INVITED/" + entrantId, null);       // remove from invited
                updates.put("CANCELLED/" + entrantId, true);     // move to cancelled

                ref.updateChildren(updates);
            });
        }

        return convertView;
    }
}
