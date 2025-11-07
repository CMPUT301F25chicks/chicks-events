package com.example.chicksevent;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.databinding.FragmentCreateEventBinding;
import com.example.chicksevent.databinding.FragmentUpdateEventDetailBinding;

/**
 * Fragment that displays and allows navigation from an event update screen.
 * <p>
 * This fragment serves as a placeholder for future event editing functionality.
 * It currently supports navigation to related views such as Notifications, Events,
 * and Create Event screens.
 * </p>
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *     <li>Display the event name passed as a fragment argument.</li>
 *     <li>Provide navigation to other fragments in the app flow.</li>
 *     <li>Serve as a structural base for upcoming event modification features.</li>
 * </ul>
 * </p>
 *
 * @author Jordan Kwan
 */
public class UpdateEventFragment extends Fragment {

    /** View binding for the Update Event layout. */
    private FragmentUpdateEventDetailBinding binding;

    /**
     * Inflates the fragment layout using ViewBinding.
     *
     * @param inflater the layout inflater
     * @param container the parent view container
     * @param savedInstanceState the saved instance state
     * @return the inflated view hierarchy
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUpdateEventDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Initializes view components, displays the event name (if provided),
     * and wires navigation buttons for related fragments.
     *
     * @param view the root view returned by {@link #onCreateView}
     * @param savedInstanceState previously saved state, or {@code null} for a fresh instance
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView eventName = view.findViewById(R.id.tv_event_name);

        Bundle args = getArguments();
        if (args != null) {
            eventName.setText(args.getString("eventName"));
            // Use it to populate UI
        }

        Button eventButton = view.findViewById(R.id.btn_events);
        Button createEventButton = view.findViewById(R.id.btn_addEvent);
        Button notificationButton = view.findViewById(R.id.btn_notification);


        notificationButton.setOnClickListener(v -> {
                    NavHostFragment.findNavController(UpdateEventFragment.this)
                            .navigate(R.id.action_UpdateEventFragment_to_NotificationFragment);
                }
//
        );

        eventButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(UpdateEventFragment.this).navigate(R.id.action_UpdateEventFragment_to_EventFragment);
        });

        createEventButton.setOnClickListener(v -> {
//            NavHostFragment.findNavController(UpdateEventFragment.this).navigate(R.id.action_SecondFragment_to_CreateEventFragment);

            NavHostFragment.findNavController(UpdateEventFragment.this).navigate(R.id.action_UpdateEventFragment_to_CreateEventFragment);
        });

    }

    /** Clears binding references when the view is destroyed. */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
