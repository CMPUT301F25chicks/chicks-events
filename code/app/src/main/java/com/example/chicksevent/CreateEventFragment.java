package com.example.chicksevent;

import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.databinding.FragmentCreateEventBinding;


public class CreateEventFragment extends Fragment {

    private FragmentCreateEventBinding binding;

    public CreateEventFragment() {
        // You can keep the constructor-empty and inflate via binding below
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreateEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button notificationButton = view.findViewById(R.id.btn_notification);
        notificationButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(CreateEventFragment.this).navigate(R.id.action_CreateEventFragment_to_FirstFragment);
        });

        // Show/hide "max entrants" field when checkbox changes
        binding.cbLimitWaitingList.setOnCheckedChangeListener((btn, checked) -> {
            binding.etMaxEntrants.setVisibility(checked ? View.VISIBLE : View.GONE);
        });

        // Hook up CREATE button
        binding.btnCreateEvent.setOnClickListener(v -> {
            createEventFromForm();
        });

        // Optional: Cancel just pops back
        binding.btnCancel.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void createEventFromForm() {
        // Read inputs
        String name  = s(binding.etEventName.getText());
        String desc  = s(binding.etEventDescription.getText());
        String time  = s(binding.etEventTime.getText()); // currently not stored in Event model
        String regStart = s(binding.etStartDate.getText()); // Registration Start (from your UI)
        String regEnd   = s(binding.etEndDate.getText());   // Registration End (from your UI)

        // Optional max entrants
        int entrantLimit = 0;
        if (binding.cbLimitWaitingList.isChecked()) {
            String max = s(binding.etMaxEntrants.getText());
            if (!TextUtils.isEmpty(max)) {
                try { entrantLimit = Integer.parseInt(max); }
                catch (NumberFormatException ignore) {}
            }
        }

        // Basic validation
        if (TextUtils.isEmpty(name)) {
            toast("Please enter an event name");
            return;
        }
        if (TextUtils.isEmpty(desc)) {
            toast("Please enter an event description");
            return;
        }
        // You can also enforce regStart/regEnd if required

        // Organizer/entrant id — using device id like you did in FirstFragment
        String entrantId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Your Event model also has eventStartDate / eventEndDate.
        // If you don’t have those fields on this screen yet, pass nulls (Firebase will omit).
        String eventStartDate = null; // TODO: add UI if needed
        String eventEndDate   = null; // TODO: add UI if needed

        // Poster/tag are optional for now
        String poster = null;
        String tag    = null;

        // id will be generated in createEvent(), pass a placeholder for constructor param
        String placeholderId = null;

        Event e = new Event(
                entrantId,
                placeholderId,
                name,
                desc,
                eventStartDate,
                eventEndDate,
                regStart,
                regEnd,
                entrantLimit,
                poster,
                tag
        );

        // Push to Firebase
        e.createEvent();
        toast("Event created 🎉");
        // Optionally navigate back:
        requireActivity().onBackPressed();
    }

    private static String s(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
