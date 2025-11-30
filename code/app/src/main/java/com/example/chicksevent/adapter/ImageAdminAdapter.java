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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chicksevent.R;
import com.example.chicksevent.misc.Event;
import com.example.chicksevent.misc.FirebaseService;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Custom {@link ../ArrayAdapter} for displaying Event Image objects within a ListView or GridView.
 * <p>
 * The adapter inflates the {@code item_event.xml} layout for each event, binding event details such as
 * name and time (if available) and providing a clickable arrow button to trigger callback actions.
 * </p>
 *
 * <p><b>Usage:</b>
 * <pre>
 * ImageAdapter adapter = new EventAdapter(context, events, event -> {
 *     // handle click on event item
 * });
 * listView.setAdapter(adapter);
 * </pre>
 * </p>
 *
 * @author Jordan Kwan
 */
public class ImageAdminAdapter extends RecyclerView.Adapter<ImageAdminAdapter.ViewHolder> {

    private ArrayList<Event> events;
    private OnDeleteClickListener listener;
    private OnDeleteClickPosterListener listenerPoster;
    private Context context;
    private View view;

    private final HashMap<String, Bitmap> imageCache = new HashMap<>();


    private FirebaseService imageService = new FirebaseService("Image");

    public interface OnDeleteClickListener {
        void onArrowClick(Event event);

    }

    public interface OnDeleteClickPosterListener {
        void onDeletePosterClick(Event event);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventName;
        ImageButton btnDelete;
        ImageButton btnArrow;

        ImageView posterImageView;
        String eventId; // track which event this view belongs to

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.tv_event_name);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            posterImageView = itemView.findViewById(R.id.img_event);
        }
    }

    public ImageAdminAdapter(Context context, ArrayList<Event> events, OnDeleteClickListener listener, OnDeleteClickPosterListener listenerPoster) {
        this.context = context;
        this.events = events;
        this.listener = listener;
        this.listenerPoster = listenerPoster;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        this.view = view;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Event event = events.get(position);
        holder.eventName.setText(event.getName());


        holder.eventId = event.getId();

        if (imageCache.containsKey(event.getId())) {
            holder.posterImageView.setImageBitmap(imageCache.get(event.getId()));

        } else {


            imageService.getReference().child(event.getId()).get().addOnSuccessListener(task -> {

                Log.i("what event", event.getId() + " | " + holder.eventId);
                //            if (task.getResult().getValue() == null || !event.getId().equals(task.getResult().getKey())) return;
                if (!event.getId().equals(holder.eventId)) return;

                if (task.getValue() == null) {
                    // clearn image if does not exist
                    holder.posterImageView.setImageResource(R.drawable.sample_image);
                    return;
                }

                String base64Image = ((HashMap<String, String>) task.getValue()).get("url");
                byte[] bytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.posterImageView.setImageBitmap(bitmap);
                imageCache.put(event.getId(), bitmap);
            }).addOnFailureListener(e -> {
                Log.i("errorerror", e.getMessage());
                holder.posterImageView.setImageResource(R.drawable.sample_image);
            });
        }
//
//        holder.btnDelete.setOnClickListener(v -> {
//            if (listener != null) listener.onDeleteClick(event);
//        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listenerPoster.onDeletePosterClick(event);

        });


    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void removeCache(String eventId) {
        imageCache.remove(eventId);
    }


}