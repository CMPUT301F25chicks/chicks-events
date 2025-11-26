package com.example.chicksevent.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

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
 * Custom {@link EventAdapter} subclass for displaying events
 * <p>
 */
public class EventAdapter extends ArrayAdapter<Event> {
    /** Listener interface for responding to item button clicks. */
    OnItemButtonClickListener listener;
    FirebaseService imageService = new FirebaseService("Image");

    HostedEventAdapter.ViewHolder holder;
    private final HashMap<String, Bitmap> imageCache = new HashMap<>();


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
        Log.i("sigma", "old one");
        View view;
        HostedEventAdapter.ViewHolder holder;
//        if (convertView == null) {
//            view = LayoutInflater.from(getContext()).inflate(R.layout.item_event, parent, false);
//        } else {
//            view = convertView;
//        }


        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_event, parent, false);
            holder = new HostedEventAdapter.ViewHolder();
            holder.posterImageView = view.findViewById(R.id.img_event);
            view.setTag(holder);
        } else {
            holder = (HostedEventAdapter.ViewHolder) convertView.getTag();
            view = convertView;
        }


        Event event = getItem(position);

        TextView status = view.findViewById(R.id.tv_status);
        TextView event_name = view.findViewById(R.id.tv_event_name);
        TextView tv_startTime = view.findViewById(R.id.tv_startTime);
        TextView tv_endTime = view.findViewById(R.id.tv_endTime);
        TextView tv_date = view.findViewById(R.id.tv_date);
        ImageButton btn_arrow = view.findViewById(R.id.btn_arrow);
//        ImageView posterImageView = view.findViewById(R.id.img_event);

        Log.i("what id ", event.getId());

        holder.posterImageView.setImageResource(R.drawable.sample_image);
        holder.eventId = event.getId();

        if (imageCache.containsKey(event.getId())) {
            holder.posterImageView.setImageBitmap(imageCache.get(event.getId()));

        } else {
            imageService.getReference().child(event.getId()).get().addOnSuccessListener(task -> {
//            if (task.getResult().getValue() == null || !event.getId().equals(task.getResult().getKey())) return;
                if (!event.getId().equals(holder.eventId) || task.getValue() == null) return;

                String base64Image = ((HashMap<String, String>) task.getValue()).get("url");
                byte[] bytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.posterImageView.setImageBitmap(bitmap);
                imageCache.put(event.getId(), bitmap);
            });
        }
        // ----- Format tv_date as "MMM\ndd" -----
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





        event_name.setText(event.getName());
        tv_startTime.setText(event.getEventStartTime());
        tv_endTime.setText(event.getEventEndTime());

        btn_arrow.setOnClickListener(l -> {
            if (listener != null) listener.onItemButtonClick(event);
        });
        return view;
    }
}