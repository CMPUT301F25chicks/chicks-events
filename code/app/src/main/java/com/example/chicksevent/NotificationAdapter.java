//package com.example.chicksevent;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.TextView;
//
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.ArrayList;
//
//public abstract class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
//    public NotificationAdapter(Context context, ArrayList<Notification> notifArray) {
//        super(context, 0, notifArray);
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//
//        View view;
//        if (convertView == null) {
//            view = LayoutInflater.from(getContext()).inflate(R.layout.item_notification, parent, false);
//        } else {
//            view = convertView;
//        }
//
//        Notification notification = getItem(position);
//
//        TextView status = view.findViewById(R.id.tv_status);
//        TextView event_name = view.findViewById(R.id.tv_event_name);
//        TextView tv_time = view.findViewById(R.id.tv_time);
//
//        return view;
//    }
////    TextView cityName = view.findViewById(R.id.city_text);
////    TextView provinceName = view.findViewById(R.id.province_text);
////     cityName.setText(city.getName());
////     provinceName.setText(city.getProvince())
//}


package com.example.chicksevent;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.List;
//
//public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
//
//    // The data source
//    private List<Notification> notificationList;
//
//    // Constructor
//    public NotificationAdapter(List<Notification> notificationList) {
//        this.notificationList = notificationList;
//    }
//
//    // Create new views (invoked by the layout manager)
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        // Inflate your item layout
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_notification, parent, false);
//        return new ViewHolder(view);
//    }
//
//    // Replace the contents of a view (invoked by the layout manager)
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Notification notification = notificationList.get(position);
//        holder.status.setText(notification.getNotificationType().toString());
//        holder.event_name.setText(notification.getEventId());
//        holder.time.setText("no time");
//
//    }
//
//    // Return the size of your dataset
//    @Override
//    public int getItemCount() {
//        return notificationList.size();
//    }
//
//    // ViewHolder class holds references to your item’s views
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        TextView status;
//        TextView event_name;
//        TextView time;
//
//        public ViewHolder(@NonNull View view) {
//            super(view);
////            titleText = itemView.findViewById(R.id.notificationTitle);
////            messageText = itemView.findViewById(R.id.notificationMessage);
//            TextView status = view.findViewById(R.id.tv_status);
//            TextView event_name = view.findViewById(R.id.tv_event_name);
//            TextView time = view.findViewById(R.id.tv_time);
//        }
//    }
//}

//import android.content.Context;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageButton;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.List;
//
//public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
//
//    private List<Notification> notifications;
//    private Context context;
//
//    public NotificationAdapter(Context context, List<Notification> notifications) {
//        Log.i("RTD8", "calling notif constructor");
//
//        this.context = context;
//        this.notifications = notifications;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        Log.i("RTD8", "crate view holder");
//        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Log.i("RTD8", "making notif appear");
//        Notification item = notifications.get(position);
//
//        holder.tvDate.setText("no date");
//        holder.tvStatus.setText(item.getNotificationType().toString());
//        holder.tvEventName.setText(item.getEventId());
//        holder.tvTime.setText("no time");
//
//        holder.btnDelete.setOnClickListener(v -> {
//            notifications.remove(position);
//            notifyItemRemoved(position);
//        });
//
//        holder.btnArrow.setOnClickListener(v ->
//                Toast.makeText(context, "Clicked: " + item.getEventId(), Toast.LENGTH_SHORT).show()
//        );
//    }
//
//    @Override
//    public int getItemCount() {
//        return notifications.size();
//    }
//
//    public void addNotification(Notification notification) {
//        notifications.add(0, notification);
//        notifyItemInserted(0);
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        TextView tvDate, tvStatus, tvEventName, tvTime;
//        ImageButton btnArrow, btnDelete;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvDate = itemView.findViewById(R.id.tv_date);
//            tvStatus = itemView.findViewById(R.id.tv_status);
//            tvEventName = itemView.findViewById(R.id.tv_event_name);
//            tvTime = itemView.findViewById(R.id.tv_time);
//            btnArrow = itemView.findViewById(R.id.btn_arrow);
//            btnDelete = itemView.findViewById(R.id.btn_delete);
//        }
//    }
//}

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class NotificationAdapter extends ArrayAdapter<Notification> {

    private final Context context;
    private final List<Notification> notifications;

    public NotificationAdapter(@NonNull Context context, @NonNull List<Notification> notifications) {
        super(context, R.layout.item_notification, notifications);
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
            holder = new ViewHolder();
            holder.tvDate = convertView.findViewById(R.id.tv_date);
            holder.tvStatus = convertView.findViewById(R.id.tv_status);
            holder.tvEventName = convertView.findViewById(R.id.tv_event_name);
            holder.tvTime = convertView.findViewById(R.id.tv_time);
            holder.btnArrow = convertView.findViewById(R.id.btn_arrow);
            holder.btnDelete = convertView.findViewById(R.id.btn_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Notification item = notifications.get(position);

        holder.tvDate.setText("no date");
        holder.tvStatus.setText(item.getNotificationType().toString());
        holder.tvEventName.setText(item.getEventId());
        holder.tvTime.setText("no time");

        // Arrow button click
        holder.btnArrow.setOnClickListener(v ->
                Toast.makeText(context, "Clicked: " + item.getEventId(), Toast.LENGTH_SHORT).show()
        );

        // Delete button click
        holder.btnDelete.setOnClickListener(v -> {
            notifications.remove(position);
            notifyDataSetChanged();
        });

        return convertView;
    }

    public void addNotification(Notification notification) {
        notifications.add(0, notification);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView tvDate, tvStatus, tvEventName, tvTime;
        ImageButton btnArrow, btnDelete;
    }
}

