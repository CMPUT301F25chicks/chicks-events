package com.example.chicksevent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.databinding.FragmentSecondBinding;
//import androidx.navigation.fragment.navArgs;

import java.util.Random;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    public TextView random_text;
    public TextView header_text;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        int currentCount =
//                Integer.pa    rseInt(random_text.getText().toString());

        //        Integer count =
//                SecondFragmentArgs.fromBundle(getArguments()).getMyArg();
//        String countText = getString(R.string.random_text, count);
//        TextView headerView =
//                view.getRootView().findViewById(R.id.textview_header);
//        view.setBackgroundColor(Color.argb(255, 255, 0, 255));

        Integer count = chicksevent.SecondFragmentArgs.fromBundle(getArguments()).getMyArg();

        header_text = view.findViewById(R.id.header_text);
        header_text.setText("Here is a random number between 0 and " + count + ".");
        random_text = view.findViewById(R.id.random_text);


        Random random = new java.util.Random();

        int randomNum = random.nextInt(count + 1);
        random_text.setText(String.valueOf(randomNum));

//        if (count > 0) {
//            randomNumber = random.nextInt(count + 1);
//        }

        binding.buttonSecond.setOnClickListener(v ->
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}