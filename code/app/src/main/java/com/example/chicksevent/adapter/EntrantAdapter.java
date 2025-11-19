package com.example.chicksevent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chicksevent.R;
import com.example.chicksevent.misc.Entrant;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class EntrantAdapter extends ArrayAdapter<Entrant> {

    public EntrantAdapter(Context context, ArrayList<Entrant> userArray) {
        super(context, 0, userArray);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_chosen_user, parent, false);
        } else {
            view = convertView;
        }

        Entrant entrant = getItem(position);

        TextView userName = view.findViewById(R.id.tv_user_name);
        TextView statusView = view.findViewById(R.id.tv_status);
        ImageButton deleteBtn = view.findViewById(R.id.btn_delete);

        // Load user name async
        entrant.getName().addOnCompleteListener(name -> {
            userName.setText(name.getResult());
        });

        statusView.setText("INVITED");

        deleteBtn.setOnClickListener(v -> {
            String uid = entrant.getEntrantId();
            String eventId = entrant.getEventId();   // using your getter

            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(getContext(), "Missing eventId!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Cancel Entrant")
                    .setMessage("Are you sure you want to cancel " + userName.getText() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        DatabaseReference root = FirebaseDatabase.getInstance()
                                .getReference("WaitingList")
                                .child(eventId);

                        // Move user from INVITED to CANCELLED
                        root.child("INVITED").child(uid).removeValue();
                        root.child("CANCELLED").child(uid).setValue(true);

                        Toast.makeText(getContext(),
                                "Cancelled entrant " + uid,
                                Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });


        return view;
    }
}
