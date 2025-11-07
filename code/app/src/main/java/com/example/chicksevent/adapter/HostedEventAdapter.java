package com.example.chicksevent.adapter;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.chicksevent.misc.Event;
import com.example.chicksevent.R;

import java.util.ArrayList;

/**
 * Custom {@link ArrayAdapter} subclass for displaying events hosted by the current organizer.
 * <p>
 * Each list item is inflated from {@code item_hosted_event.xml} and provides UI elements for
 * displaying the event name and interacting with two buttons:
 * <ul>
 *     <li>An arrow button to view event details.</li>
 *     <li>An update button to modify event information.</li>
 * </ul>
 * </p>
 *
 * <p><b>Usage:</b>
 * <pre>
 * HostedEventAdapter adapter = new HostedEventAdapter(context, hostedEvents, (event, type) -> {
 *     if (type == 0) {
 *         // Open event detail view
 *     } else if (type == 1) {
 *         // Open update form
 *     }
 * });
 * listView.setAdapter(adapter);
 * </pre>
 * </p>
 *
 * <p>The callback interface {@link OnItemButtonClickListener} allows the hosting fragment or
 * activity to differentiate which button was clicked for a given event.</p>
 *
 * @author Jordan Kwan
 */
public class HostedEventAdapter extends ArrayAdapter<Event> {
    /** Listener interface for responding to per-item button clicks. */
    OnItemButtonClickListener listener;

    /**
     * Callback interface to handle button interactions within each hosted event row.
     */
    public interface OnItemButtonClickListener {
        /**
         * Invoked when a button associated with an event item is clicked.
         *
         * @param item the {@link Event} that was clicked.
         * @param type integer flag representing the button type — typically 0 for arrow/view and 1 for update.
         */
        void onItemButtonClick(Event item, int type);
    }

    /**
     * Constructs a {@code HostedEventAdapter} to display the organizer's hosted events.
     *
     * @param context the activity or fragment context.
     * @param eventArray the list of hosted events to display.
     * @param listener a callback listener for button click events.
     */
    public HostedEventAdapter(Context context, ArrayList<Event> eventArray, OnItemButtonClickListener listener) {
        super(context, 0, eventArray);
        this.listener = listener;
    }

    /**
     * Inflates or reuses a view for each list item and binds event data to its visual components.
     * Also wires up click handlers for the arrow and update buttons.
     *
     * @param position the position of the current item in the list.
     * @param convertView the old view to reuse, if available.
     * @param parent the parent view group that this view will eventually be attached to.
     * @return the populated view representing the event.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i("sigma", "sigma");
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_hosted_event, parent, false);
        } else {
            view = convertView;
        }
//
        Event event = getItem(position);

        TextView status = view.findViewById(R.id.tv_status);
        TextView event_name = view.findViewById(R.id.tv_event_name);
        TextView tv_time = view.findViewById(R.id.tv_time);
        ImageButton btn_arrow = view.findViewById(R.id.btn_arrow);

        Button update_button = view.findViewById(R.id.update_button);

        event_name.setText(event.getName());
        tv_time.setText(event.getTag());


        btn_arrow.setOnClickListener(l -> {
            if (listener != null) listener.onItemButtonClick(event, 0);
        });

        update_button.setOnClickListener(l -> {
            if (listener != null) listener.onItemButtonClick(event, 1);
        });


        return view;
    }
//    TextView cityName = view.findViewById(R.id.city_text);
//    TextView provinceName = view.findViewById(R.id.province_text);
//     cityName.setText(city.getName());
//     provinceName.setText(city.getProvince())
}
