package com.example.chicksevent;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;

/**
 * Custom {@link ArrayAdapter} for displaying {@link Event} objects within a ListView or GridView.
 * <p>
 * The adapter inflates the {@code item_event.xml} layout for each event, binding event details such as
 * name and time (if available) and providing a clickable arrow button to trigger callback actions.
 * </p>
 *
 * <p><b>Usage:</b>
 * <pre>
 * EventAdapter adapter = new EventAdapter(context, events, event -> {
 *     // handle click on event item
 * });
 * listView.setAdapter(adapter);
 * </pre>
 * </p>
 *
 * @author Jordan Kwan
 */
public class EventAdapter extends ArrayAdapter<Event> {

    /** Listener interface for responding to item button clicks. */
    OnItemButtonClickListener listener;

    /**
     * Interface defining a callback when a button inside a list item is clicked.
     */
    public interface OnItemButtonClickListener {
        /**
         * Invoked when a button associated with a given {@link Event} item is clicked.
         *
         * @param item the {@link Event} whose button was clicked.
         */
        void onItemButtonClick(Event item);
    }

    /**
     * Constructs a new {@code EventAdapter} for displaying events.
     *
     * @param context the activity or fragment context.
     * @param eventArray list of events to display.
     * @param listener callback interface to handle per-item button clicks.
     */
    public EventAdapter(Context context, ArrayList<Event> eventArray, OnItemButtonClickListener listener) {
        super(context, 0, eventArray);
        this.listener = listener;
    }

    /**
     * Provides a view for an adapter view (ListView, GridView, etc.) based on the event data.
     * Inflates {@code item_event.xml} if necessary and binds data to its views.
     *
     * @param position the position of the item within the dataset.
     * @param convertView the old view to reuse, if possible.
     * @param parent the parent view that this view will be attached to.
     * @return the view corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i("EventAdapter", "Inflating item view at position: " + position);
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_event, parent, false);
        } else {
            view = convertView;
        }

        // Retrieve event for this position
        Event event = getItem(position);

        // Bind UI elements
        TextView status = view.findViewById(R.id.tv_status);
        TextView event_name = view.findViewById(R.id.tv_event_name);
        TextView tv_time = view.findViewById(R.id.tv_time);
        ImageButton btn_arrow = view.findViewById(R.id.btn_arrow);

        // Populate UI fields
        if (event != null) {
            event_name.setText(event.getName());
            // Additional fields (e.g., tv_time) can be populated when data available
        }

        // Handle item button click
        btn_arrow.setOnClickListener(l -> {
            if (listener != null && event != null) listener.onItemButtonClick(event);
        });

        return view;
    }
}
