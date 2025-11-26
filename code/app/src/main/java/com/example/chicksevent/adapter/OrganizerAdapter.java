package com.example.chicksevent.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chicksevent.R;
import com.example.chicksevent.misc.Organizer;
import com.example.chicksevent.misc.User;

import java.util.ArrayList;

/**
 * A {@link RecyclerView.Adapter} that displays a list of {@link Organizer} objects
 * in a {@link RecyclerView}, with support for deleting individual organizers via a button.
 * <p>
 * Each item is inflated from {@code R.layout.item_facility} and shows the organizer's ID.
 * Clicking the delete button triggers the registered {@link OnBanToggleClickListener}.
 * </p>
 *
 * <p>
 * This adapter is used in administrative interfaces to manage organizer accounts.
 * </p>
 *
 * @see Organizer
 * @see OnBanToggleClickListener
 * @see RecyclerView
 * @see ViewHolder
 */
public class OrganizerAdapter extends RecyclerView.Adapter<OrganizerAdapter.ViewHolder> {


    /**
     * Callback interface for handling ban/unban toggle switch changes on organizer items.
     */
    public interface OnBanToggleClickListener {
        /**
         * Called when the ban/unban toggle switch is changed for a specific organizer.
         *
         * @param organizer the {@link Organizer} object associated with the clicked item
         * @param willBeBanned whether the organizer will be banned (true) or unbanned (false) after this action
         */
        void onBanToggleClick(Organizer organizer, boolean willBeBanned);
    }

    /**
     * List of {@link Organizer} objects to be displayed in the RecyclerView.
     * <p>
     * This list is mutable and should be updated externally (e.g., after deletion)
     * followed by a call to {@link #notifyDataSetChanged()} or similar.
     * </p>
     */
    private ArrayList<Organizer> organizers;


    /**
     * Listener to be notified when the ban/unban toggle button is clicked on an item.
     */
    private OnBanToggleClickListener banToggleListener;

    /**
     * Constructs a new {@code OrganizerAdapter} with ban/unban toggle functionality.
     *
     * @param organizers the list of organizers to display
     * @param banToggleListener the listener to notify on ban/unban toggle clicks, or {@code null}
     */
    public OrganizerAdapter(ArrayList<Organizer> organizers,
                           OnBanToggleClickListener banToggleListener) {
        this.organizers = organizers;
        this.banToggleListener = banToggleListener;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} to represent an item.
     * <p>
     * Inflates the item layout {@code R.layout.item_facility} and returns a new
     * {@link ViewHolder} instance.
     * </p>
     *
     * @param parent   the ViewGroup into which the new View will be added
     * @param viewType the view type of the new View
     * @return a new {@link ViewHolder} that holds the inflated item view
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_facility, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * <p>
     * Binds the {@link Organizer} at the given position to the {@link ViewHolder},
     * setting the name text and configuring the delete button click listener.
     * </p>
     *
     * @param holder   the ViewHolder which should be updated to represent the contents
     * @param position the position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Organizer organizer = organizers.get(position);
        String organizerId = organizer.getOrganizerId();
        
        // Get user name instead of ID
        User user = new User(organizerId);
        user.getName().addOnCompleteListener(nameTask -> {
            String displayName = nameTask.getResult();
            // If name is null, empty, or error message, fallback to ID
            if (displayName == null || displayName.isEmpty() || displayName.equals("couldn't find name")) {
                holder.name.setText(organizerId);
            } else {
                holder.name.setText(displayName);
            }
        });
        
        // Check if organizer is banned and update switch state
        user.isBannedFromOrganizer().addOnCompleteListener(task -> {
            boolean isBanned = task.isSuccessful() && task.getResult();
            
            // Update label text based on ban status
            if (holder.banLabel != null) {
                if (isBanned) {
                    holder.banLabel.setText("Banned");
                } else {
                    holder.banLabel.setText("Can Create Events");
                }
            }
            
            // Update switch state: OFF = banned, ON = not banned
            if (holder.banToggleSwitch != null) {
                // Set switch state (inverted: banned = false/off, not banned = true/on)
                holder.banToggleSwitch.setChecked(!isBanned);
                
                // Remove any existing listeners to avoid conflicts
                holder.banToggleSwitch.setOnCheckedChangeListener(null);
                
                // Set click listener for toggle
                holder.banToggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    // isChecked = true means NOT banned, false means BANNED
                    boolean willBeBanned = !isChecked;
                    if (banToggleListener != null) {
                        banToggleListener.onBanToggleClick(organizers.get(position), willBeBanned);
                    }
                });
            }
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return the size of {@link #organizers}
     */
    @Override
    public int getItemCount() {
        return organizers.size();
    }

    /**
     * A {@link RecyclerView.ViewHolder} that represents a single organizer item
     * in the list.
     * <p>
     * Holds references to the name {@link TextView} and delete button
     * defined in {@code item_facility.xml}.
     * </p>
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * TextView displaying the organizer's name or ID.
         */
        TextView name;

        /**
         * TextView displaying the label for the ban toggle switch.
         */
        TextView banLabel;

        /**
         * Switch that toggles ban/unban status of the organizer.
         * ON = not banned, OFF = banned
         */
        Switch banToggleSwitch;

        /**
         * Constructs a new ViewHolder from the provided item view.
         * <p>
         * Initializes {@link #name} and {@link #banToggleSwitch} by finding views
         * using their resource IDs.
         * </p>
         *
         * @param itemView the inflated item view
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_user_name);
            banLabel = itemView.findViewById(R.id.tv_ban_label);
            banToggleSwitch = itemView.findViewById(R.id.switch_ban_toggle);
        }
    }
}