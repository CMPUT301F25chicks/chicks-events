package com.example.chicksevent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * A {@link RecyclerView.Adapter} that displays a list of {@link Organizer} objects
 * in a {@link RecyclerView}, with support for deleting individual organizers via a button.
 * <p>
 * Each item is inflated from {@code R.layout.item_facility} and shows the organizer's ID.
 * Clicking the delete button triggers the registered {@link OnDeleteClickListener}.
 * </p>
 *
 * <p>
 * This adapter is used in administrative interfaces to manage organizer accounts.
 * </p>
 *
 * @see Organizer
 * @see OnDeleteClickListener
 * @see RecyclerView
 * @see ViewHolder
 */
public class OrganizerAdapter extends RecyclerView.Adapter<OrganizerAdapter.ViewHolder> {

    /**
     * Callback interface for handling delete button clicks on organizer items.
     * <p>
     * Implementations should typically delete the organizer from Firebase
     * and remove it from the underlying data list.
     * </p>
     */
    public interface OnDeleteClickListener {
        /**
         * Called when the delete button is clicked for a specific organizer.
         *
         * @param organizer the {@link Organizer} object associated with the clicked item
         */
        void onDeleteClick(Organizer organizer);
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
     * Listener to be notified when the delete button is clicked on an item.
     * <p>
     * May be {@code null} if no delete action is required.
     * </p>
     */
    private OnDeleteClickListener listener;

    /**
     * Constructs a new {@code OrganizerAdapter} with the given data and listener.
     *
     * @param organizers the list of organizers to display
     * @param listener   the listener to notify on delete clicks, or {@code null}
     */
    public OrganizerAdapter(ArrayList<Organizer> organizers, OnDeleteClickListener listener) {
        this.organizers = organizers;
        this.listener = listener;
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
        holder.name.setText(organizer.getOrganizerId());
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(organizers.get(position));
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
     * Holds references to the name {@link TextView} and delete {@link ImageButton}
     * defined in {@code item_facility.xml}.
     * </p>
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * TextView displaying the organizer's ID or name.
         */
        TextView name;

        /**
         * ImageButton that triggers deletion of the organizer when clicked.
         */
        ImageButton deleteButton;

        /**
         * Constructs a new ViewHolder from the provided item view.
         * <p>
         * Initializes {@link #name} and {@link #deleteButton} by finding views
         * using their resource IDs.
         * </p>
         *
         * @param itemView the inflated item view
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_user_name);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }
}