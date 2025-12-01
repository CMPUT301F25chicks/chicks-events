package com.example.chicksevent.adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chicksevent.R;
import com.example.chicksevent.misc.Event;
import com.example.chicksevent.misc.FirebaseService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Custom {@link ArrayAdapter} subclass for displaying events hosted by the current organizer.
 * Each list item is inflated from {@code item_hosted_event.xml} and provides UI elements for
 * displaying the event name and interacting with two buttons:
 * <ul>
 *     <li>An arrow button to view event details.</li>
 *     <li>An update button to modify event information.</li>
 * </ul>
 *
 * <b>Usage:</b>
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
 *
 * The callback interface {@link OnItemButtonClickListener} allows the hosting fragment or
 * activity to differentiate which button was clicked for a given event.
 *
 * @author Jordan Kwan
 */
public class HostedEventAdapter extends ArrayAdapter<Event> {
    /** Listener interface for responding to per-item button clicks. */
    OnItemButtonClickListener listener;
    private final HashMap<String, Bitmap> imageCache = new HashMap<>();



    FirebaseService imageService = new FirebaseService("Image");

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

    static class ViewHolder {
        ImageView posterImageView;
        String eventId; // track which event this view belongs to
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

        ViewHolder holder;

        Log.i("sigmaerror", "sigma");
//        View view;
//        if (convertView == null) {
//            view = LayoutInflater.from(getContext()).inflate(R.layout.item_hosted_event, parent, false);
//        } else {
//            view = convertView;
//        }

        View view;


        if (convertView == null) {
//            if (getContext() == null) {
//                return view;
//            }
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_hosted_event, parent, false);
            holder = new HostedEventAdapter.ViewHolder();
            holder.posterImageView = view.findViewById(R.id.img_event);
            view.setTag(holder);
        } else {
            holder = (HostedEventAdapter.ViewHolder) convertView.getTag();
            view = convertView;
        }
//
        Event event = getItem(position);

        TextView event_name = view.findViewById(R.id.tv_event_name);
        TextView tv_startTime = view.findViewById(R.id.tv_startTime);
        TextView tv_endTime = view.findViewById(R.id.tv_endTime);
        ImageButton btn_arrow = view.findViewById(R.id.btn_arrow);
        TextView tv_date = view.findViewById(R.id.tv_date);

        Button update_button = view.findViewById(R.id.update_button);

        event_name.setText(event.getName());
        tv_startTime.setText(event.getEventStartTime());
        tv_endTime.setText(event.getEventEndTime());

        String startDateStr = event.getEventStartDate(); // e.g., "03-15-2025"

        if (startDateStr != null) {
            try {
                // Parse incoming date
                SimpleDateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
                Date date = inputFormat.parse(startDateStr);

                // Format month abbreviation (uppercase)
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.ENGLISH);
                String month = monthFormat.format(date).toUpperCase(); // "MAR"

                // Format day
                SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.ENGLISH);
                String day = dayFormat.format(date); // "15"

                // Combine exactly like your fragment
                String display = month + "\n" + day;

                tv_date.setText(display);

            } catch (ParseException e) {
                e.printStackTrace();
                tv_date.setText(startDateStr); // fallback
            }
        } else {
            tv_date.setText(""); // or "N/A"
        }


        btn_arrow.setOnClickListener(l -> {
            if (listener != null) listener.onItemButtonClick(event, 0);
        });

        update_button.setOnClickListener(l -> {
            if (listener != null) listener.onItemButtonClick(event, 1);
        });

        holder.posterImageView.setImageResource(R.drawable.sample_image);
        holder.eventId = event.getId();
//        Log.i("what event", event.getId() + " | " + holder.eventId);

        if (imageCache.containsKey(event.getId())) {
            Glide.with(holder.posterImageView.getContext())
                    .load(imageCache.get(event.getId()))
                    .into(holder.posterImageView);
        } else {


            try {
                imageService.getReference()
                        .child(event.getId())
                        .child("poster")
                        .get()
                        .addOnSuccessListener(snapshot -> {

                            if (!event.getId().equals(holder.eventId)) return;

                            String imageUrl = snapshot.getValue(String.class);
                            if (imageUrl == null) return;

                            Glide.with(holder.posterImageView.getContext())
                                    .load(imageUrl)
                                    .into(holder.posterImageView);

//                    imageCache.put(event.getId(), imageUrl); // optional
                        });
            } catch (Exception e) {
                Log.i("errorthingprintthis", ""+e);
                holder.posterImageView.setImageResource(R.drawable.sample_image);

            }
        }


        return view;
    }
//    TextView cityName = view.findViewById(R.id.city_text);
//    TextView provinceName = view.findViewById(R.id.province_text);
//     cityName.setText(city.getName());
//     provinceName.setText(city.getProvince())
}
