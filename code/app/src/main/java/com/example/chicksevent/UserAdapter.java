package com.example.chicksevent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Adapter that binds {@link User} objects to a ListView for display.
 * <p>
 * Provides a simple textual representation of each user, currently displaying
 * the user ID associated with each {@link User} instance.
 * </p>
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *     <li>Inflate the {@code item_user} layout for each list entry.</li>
 *     <li>Populate the layout with data from a {@link User} object.</li>
 *     <li>Reuse views efficiently through view recycling.</li>
 * </ul>
 * </p>
 *
 * <p>This adapter can be extended to include additional user details (e.g.,
 * name, email, phone number) as the application evolves.</p>
 *
 * @author Jordan Kwan
 */
public class UserAdapter extends ArrayAdapter<User> {
    /**
     * Constructs a new adapter for displaying user information.
     *
     * @param context the current context
     * @param userArray the list of {@link User} objects to display
     */
    public UserAdapter(Context context, ArrayList<User> userArray) {
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

        User user = getItem(position);
        TextView userName = view.findViewById(R.id.tv_user_name);

        userName.setText(user.getUserId());

        return view;
    }
}
