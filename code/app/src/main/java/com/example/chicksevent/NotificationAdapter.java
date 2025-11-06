package com.example.chicksevent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Adapter class for displaying {@link Notification} objects in a {@link android.widget.ListView}.
 * <p>
 * This adapter inflates the {@code item_notification.xml} layout for each list element and binds
 * notification data to the corresponding view components.
 * </p>
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *     <li>Inflate and recycle views for efficient list rendering.</li>
 *     <li>Bind notification data to text views within each item layout.</li>
 * </ul>
 * </p>
 *
 * @author Jordan Kwan
 */
public class NotificationAdapter extends ArrayAdapter<Notification> {

    /**
     * Constructs a new adapter for displaying a list of notifications.
     *
     * @param context the current context used to inflate the layout
     * @param notifArray the list of {@link Notification} objects to display
     */
    public NotificationAdapter(Context context, ArrayList<Notification> notifArray) {
        super(context, 0, notifArray);
    }

    /**
     * Returns a view representing a single {@link Notification} in the list.
     * <p>
     * This method reuses recycled views where possible for performance efficiency.
     * </p>
     *
     * @param position the position of the item within the adapter’s data set
     * @param convertView a potentially recycled view
     * @param parent the parent view that this view will be attached to
     * @return the populated list item view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_notification, parent, false);
        } else {
            view = convertView;
        }

        Notification notification = getItem(position);

        TextView status = view.findViewById(R.id.tv_status);
        TextView eventName = view.findViewById(R.id.tv_event_name);
        TextView time = view.findViewById(R.id.tv_time);

        // TODO: Bind actual data once Notification properties are finalized
        // e.g., eventName.setText(notification.getEventId());

        return view;
    }
}