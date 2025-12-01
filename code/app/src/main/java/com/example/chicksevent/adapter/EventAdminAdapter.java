package com.example.chicksevent.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chicksevent.R;
import com.example.chicksevent.misc.Event;
import com.example.chicksevent.misc.FirebaseService;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Custom ArrayAdapter for displaying {@link Event} objects within a ListView or GridView.
 * <p>
 * The adapter inflates the {@code item_event.xml} layout for each event, binding event details such as
 * name and time (if available) and providing a clickable arrow button to trigger callback actions.
 * </p>
 *
 * <b>Usage:</b>
 * <pre>
 * EventAdapter adapter = new EventAdapter(context, events, event -> {
 *     // handle click on event item
 * });
 * listView.setAdapter(adapter);
 * </pre>
 *
 * @author Jordan Kwan
 */
public class EventAdminAdapter extends RecyclerView.Adapter<EventAdminAdapter.ViewHolder> {

    private ArrayList<Event> events;
    private OnDeleteClickListener listener;
    private OnDeleteClickEventListener listenerEvent;
    private Context context;
    private View view;

    private final HashMap<String, Bitmap> imageCache = new HashMap<>();


    private FirebaseService imageService = new FirebaseService("Image");

    public interface OnDeleteClickListener {
        void onArrowClick(Event event);

    }

    public interface OnDeleteClickEventListener {
        void onDeleteEventClick(Event event);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventName;
        ImageButton btnDelete;

        ImageView posterImageView;
        ImageButton btnArrow;
        String eventId; // track which event this view belongs to

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.tv_event_name);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            posterImageView = itemView.findViewById(R.id.img_event);
            btnArrow = itemView.findViewById(R.id.btn_arrow);

        }
    }

    public EventAdminAdapter(Context context, ArrayList<Event> events, OnDeleteClickListener listener, OnDeleteClickEventListener listenerEvent) {
        this.context = context;
        this.events = events;
        this.listener = listener;
        this.listenerEvent = listenerEvent;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event_deletable, parent, false);
        this.view = view;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Event event = events.get(position);
        holder.eventName.setText(event.getName());
        TextView tv_startTime = view.findViewById(R.id.tv_startTime);
        TextView tv_endTime = view.findViewById(R.id.tv_endTime);
        tv_startTime.setText(event.getEventStartTime());
        tv_endTime.setText(event.getEventEndTime());

        holder.eventId = event.getId();

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
//
        holder.btnArrow.setOnClickListener(v -> {
            if (listener != null) listener.onArrowClick(event);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listenerEvent.onDeleteEventClick(event);

        });

    }

    @Override
    public int getItemCount() {
        return events.size();
    }


}