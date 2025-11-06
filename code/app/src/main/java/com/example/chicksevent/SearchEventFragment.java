package com.example.chicksevent;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class SearchEventFragment extends Fragment {

    public SearchEventFragment() {
        super(R.layout.fragment_search_event);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton back = view.findViewById(R.id.btn_back);
        if (back != null) {
            back.setOnClickListener(v ->
                    NavHostFragment.findNavController(this).popBackStack()
            );
        }
    }
}
