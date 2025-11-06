package com.example.chicksevent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;

public class EventAdapter extends ArrayAdapter<Event> {
    public EventAdapter(Context context, ArrayList<Event> eventArray) {
        super(context, 0, eventArray);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_event, parent, false);
        } else {
            view = convertView;
        }
//
        Event event = getItem(position);

        TextView status = view.findViewById(R.id.tv_status);
        TextView event_name = view.findViewById(R.id.tv_event_name);
        TextView tv_time = view.findViewById(R.id.tv_time);
//        Button btn_arrow = view.findViewById(R.id.btn_arrow);
//        btn_arrow.setOnClickListener(l -> {
//            createEventButton.setOnClickListener(v -> {
//                NavHostFragment.findNavController(FirstFragment.this).navigate(R.id.action_FirstFragment_to_CreateEventFragment);
//            });
////            event_name;
//        });
        return view;
    }
//    TextView cityName = view.findViewById(R.id.city_text);
//    TextView provinceName = view.findViewById(R.id.province_text);
//     cityName.setText(city.getName());
//     provinceName.setText(city.getProvince())
}
