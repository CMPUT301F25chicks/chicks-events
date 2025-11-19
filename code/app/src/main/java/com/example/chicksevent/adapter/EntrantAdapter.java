package com.example.chicksevent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.chicksevent.R;
import com.example.chicksevent.misc.Entrant;
import com.example.chicksevent.misc.User;

import java.util.ArrayList;

/**
 * Adapter that binds {@link Entrant} objects to a ListView for display.
 * <p>
 * Provides a simple textual representation of each entrant, currently displaying
 * the entrant ID associated with each {@link Entrant} instance.
 * </p>
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *     <li>Inflate the {@code item_entrant} layout for each list entry.</li>
 *     <li>Populate the layout with data from a {@link Entrant} object.</li>
 *     <li>Reuse views efficiently through view recycling.</li>
 * </ul>
 * </p>
 *
 * <p>This adapter can be extended to include additional entrant details (e.g.,
 * name, email, phone number) as the application evolves.</p>
 *
 * @author Jordan and Hanh </3
 */
public class EntrantAdapter extends ArrayAdapter<Entrant> {
    /**
     * Constructs a new adapter for displaying user information.
     *
     * @param context the current context
     * @param userArray the list of {@link User} objects to display
     */
    public EntrantAdapter(Context context, ArrayList<Entrant> userArray) {
        super(context, 0, userArray);
    }

    /**
     * Returns a populated list item view for a given position.
     *
     * @param position the position of the item within the list
     * @param convertView an existing view to reuse if possible
     * @param parent the parent view group that this view will be attached to
     * @return a populated view representing the {@link User} at the given position
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false);
        } else {
            view = convertView;
        }

        Entrant entrant = getItem(position);
        TextView userName = view.findViewById(R.id.tv_user_name);

        entrant.getName().addOnCompleteListener(name -> {
            userName.setText(name.getResult());
        });

        return view;
    }
}