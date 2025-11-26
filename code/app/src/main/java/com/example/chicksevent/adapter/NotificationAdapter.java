package com.example.chicksevent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.chicksevent.misc.Notification;
import com.example.chicksevent.enums.NotificationType;
import com.example.chicksevent.R;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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
    OnItemButtonClickListener listener;
    OnItemButtonClickListener listener2;

    public interface OnItemButtonClickListener {
        void onItemButtonClick(Notification notification);
    }
    /**
     * Constructs a new adapter for displaying a list of notifications.
     *
     * @param context the current context used to inflate the layout
     * @param notifArray the list of {@link Notification} objects to display
     */
    public NotificationAdapter(Context context, ArrayList<Notification> notifArray, OnItemButtonClickListener listener, OnItemButtonClickListener listener2) {
        super(context, 0, notifArray);
        this.listener = listener;
        this.listener2 = listener2;
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
        ImageButton btnDelete = view.findViewById(R.id.btn_delete);
        ImageButton btnArrow = view.findViewById(R.id.btn_arrow);
        TextView tv_date = view.findViewById(R.id.tv_date);

        notification.getEventName().addOnCompleteListener(t -> {
            eventName.setText(t.getResult());
        });

        time.setText(notification.getMessage());

        String eventId = notification.getEventId();

        FirebaseDatabase.getInstance()
                .getReference("Event")
                .child(eventId)
                .child("eventStartDate")
                .get()
                .addOnCompleteListener(task -> {
                    String startDateStr = task.getResult().getValue(String.class);
                    bindDate(startDateStr, tv_date);
                });




        status.setText(notification.getNotificationType() == NotificationType.WAITING ? "WAITING" : notification.getNotificationType() == NotificationType.INVITED ? "INVITED" : notification.getNotificationType() == NotificationType.CANCELLED ? "CANCELLED" : notification.getNotificationType() == NotificationType.SYSTEM ? "SYSTEM" : "NOT CHOSEN");
        btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onItemButtonClick(notification);
        });

        btnArrow.setOnClickListener(v -> {
            if (listener != null) listener2.onItemButtonClick(notification);
        });

        return view;
    }

    private void bindDate(String startDateStr, TextView tv_date) {
        if (startDateStr != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
                Date date = inputFormat.parse(startDateStr);

                SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.ENGLISH);
                SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.ENGLISH);

                String display = monthFormat.format(date).toUpperCase() + "\n" + dayFormat.format(date);
                tv_date.setText(display);

            } catch (ParseException e) {
                e.printStackTrace();
                tv_date.setText(startDateStr);
            }
        } else {
            tv_date.setText("");
        }
    }

}