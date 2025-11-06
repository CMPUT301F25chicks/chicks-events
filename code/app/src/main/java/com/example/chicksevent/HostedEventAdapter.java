package com.example.chicksevent;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;

public class HostedEventAdapter extends ArrayAdapter<Event> {
    OnItemButtonClickListener listener;

    public interface OnItemButtonClickListener {
        void onItemButtonClick(Event item, int type);
//        void onUpdateButtonClick(Event item);
    }
    public HostedEventAdapter(Context context, ArrayList<Event> eventArray, OnItemButtonClickListener listener) {
        super(context, 0, eventArray);
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i("sigma", "sigma");
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_hosted_event, parent, false);
        } else {
            view = convertView;
        }
//
        Event event = getItem(position);

        TextView status = view.findViewById(R.id.tv_status);
        TextView event_name = view.findViewById(R.id.tv_event_name);
        TextView tv_time = view.findViewById(R.id.tv_time);
        ImageButton btn_arrow = view.findViewById(R.id.btn_arrow);

        Button update_button = view.findViewById(R.id.update_button);

        event_name.setText(event.getName());


        btn_arrow.setOnClickListener(l -> {
            if (listener != null) listener.onItemButtonClick(event, 0);
        });

        update_button.setOnClickListener(l -> {
            if (listener != null) listener.onItemButtonClick(event, 1);
        });


        return view;
    }
//    TextView cityName = view.findViewById(R.id.city_text);
//    TextView provinceName = view.findViewById(R.id.province_text);
//     cityName.setText(city.getName());
//     provinceName.setText(city.getProvince())
}
