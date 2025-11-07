package com.example.chicksevent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chicksevent.misc.Event;
import com.example.chicksevent.R;

import java.util.ArrayList;

/**
 * Custom {@link ../ArrayAdapter} for displaying {@link Event} objects within a ListView or GridView.
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
public class EventAdminAdapter extends RecyclerView.Adapter<EventAdminAdapter.ViewHolder> {

    private ArrayList<Event> events;
    private OnDeleteClickListener listener;
    private Context context;

    public interface OnDeleteClickListener {
        void onDeleteClick(Event event);
    }

    public EventAdminAdapter(Context context, ArrayList<Event> events, OnDeleteClickListener listener) {
        this.context = context;
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event_deletable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.eventName.setText(event.getName());

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(event);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventName;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.tv_event_name);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}