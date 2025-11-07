package com.example.chicksevent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that displays a list of {@link EntrantDisplay} objects
 * in a RecyclerView. Each item shows the entrant's ID, status, and includes a delete button.
 * Clicking the delete button notifies the registered {@link OnDeleteClickListener}.
 */
public class EntrantListAdapter extends RecyclerView.Adapter<EntrantListAdapter.ViewHolder> {

    /**
     * Callback interface for handling delete button clicks on entrant items.
     */
    public interface OnDeleteClickListener {
        /**
         * Called when the delete button is clicked for a specific entrant.
         *
         * @param entrant The {@link EntrantDisplay} object associated with the clicked item.
         */
        void onDeleteClicked(EntrantDisplay entrant);
    }

    /** List of entrant data to be displayed. */
    private List<EntrantDisplay> entrants;

    /** Listener to handle delete actions. */
    private OnDeleteClickListener listener;

    /**
     * Constructs a new adapter with the given entrant list and delete listener.
     *
     * @param entrants The list of {@link EntrantDisplay} objects to display.
     * @param listener The {@link OnDeleteClickListener} to notify on delete clicks.
     */
    public EntrantListAdapter(List<EntrantDisplay> entrants, OnDeleteClickListener listener) {
        this.entrants = entrants;
        this.listener = listener;
    }

    /**
     * Inflates the item layout and creates a new {@link ViewHolder}.
     *
     * @param parent   The parent view group into which the new view will be added.
     * @param viewType The view type of the new view (not used here).
     * @return A new {@link ViewHolder} that holds the view for each list item.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chosen_user, parent, false);
        return new ViewHolder(v);
    }

    /**
     * Binds data from the specified position to the {@link ViewHolder}.
     * Sets the user name, status text, and attaches the delete click listener.
     *
     * @param holder   The {@link ViewHolder} to bind data to.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EntrantDisplay entrant = entrants.get(position);
        holder.tvUserName.setText(entrant.getEntrantId());
        holder.tvStatus.setText("Status: " + entrant.getStatus());

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClicked(entrant));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return Size of the {@link #entrants} list.
     */
    @Override
    public int getItemCount() {
        return entrants.size();
    }

    /**
     * {@link RecyclerView.ViewHolder} subclass that represents a single list item view.
     * Holds references to the user name, status text, and delete button.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /** TextView displaying the entrant's ID. */
        TextView tvUserName;

        /** TextView displaying the entrant's status. */
        TextView tvStatus;

        /** ImageButton to trigger deletion of the entrant. */
        ImageButton btnDelete;

        /**
         * Initializes the ViewHolder by finding and storing references to child views.
         *
         * @param itemView The root view of the list item.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}