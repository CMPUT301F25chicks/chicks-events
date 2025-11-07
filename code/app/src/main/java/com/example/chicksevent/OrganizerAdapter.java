package com.example.chicksevent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class OrganizerAdapter extends RecyclerView.Adapter<OrganizerAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(Organizer organizer);
    }

    private ArrayList<Organizer> organizers;
    private OnDeleteClickListener listener;

    public OrganizerAdapter(ArrayList<Organizer> organizers, OnDeleteClickListener listener) {
        this.organizers = organizers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_facility, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Organizer organizer = organizers.get(position);
        holder.name.setText(organizer.getOrganizerId());
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(organizers.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return organizers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_user_name);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }
}
