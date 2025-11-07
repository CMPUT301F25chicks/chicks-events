package com.example.chicksevent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EntrantListAdapter extends RecyclerView.Adapter<EntrantListAdapter.ViewHolder>{
    public interface OnDeleteClickListener {
        void onDeleteClicked(EntrantDisplay entrant);
    }

    private List<EntrantDisplay> entrants;
    private OnDeleteClickListener listener;

    public EntrantListAdapter(List<EntrantDisplay> entrants, OnDeleteClickListener listener) {
        this.entrants = entrants;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chosen_user, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EntrantDisplay entrant = entrants.get(position);
        holder.tvUserName.setText(entrant.getEntrantId());
        holder.tvStatus.setText("Status: " + entrant.getStatus());

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClicked(entrant));
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvStatus;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}