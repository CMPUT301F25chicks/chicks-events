package com.example.chicksevent;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.databinding.FragmentFirstBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    public Button toast_button;
    public Button count_button;
    public Button random_button;

    public TextView showCountTextView;

    public int count = 1823750;

    private FirebaseService dbService;
    private DatabaseReference citiesRef;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        view.setBackgroundColor(Color.argb(255, 255, 0, 255));
        dbService = new FirebaseService("bruhmoment");



        showCountTextView = view.findViewById(R.id.count_text);
        showCountTextView.setText(String.valueOf(count));


        toast_button = view.findViewById(R.id.toast_button);
        count_button = view.findViewById(R.id.count_button);
        random_button = view.findViewById(R.id.random_button);

        toast_button.setOnClickListener(v -> {
            Toast myToast = Toast.makeText(getActivity(), "Hello toast!",
                    Toast.LENGTH_SHORT);
            myToast.show();
        });

        count_button.setOnClickListener(v -> {
            countMe(v);
        });

        random_button.setOnClickListener(v -> {
            int currentCount =
                    Integer.parseInt(showCountTextView.getText().toString());

            chicksevent.FirstFragmentDirections.ActionFirstFragmentToSecondFragment action = chicksevent.FirstFragmentDirections.actionFirstFragmentToSecondFragment(currentCount);
            NavHostFragment.findNavController(FirstFragment.this).navigate(action);
//                    NavHostFragment.findNavController(FirstFragment.this).navigate(R.id.action_FirstFragment_to_SecondFragment)
        }



        );



//        FirstFragmentDirections action = FirstFragmentDirections.actionFirstFragmentToSecondFragment(count);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void countMe(View view) {
// Get the value of the text view
        String countString = showCountTextView.getText().toString();
// Convert value to a number and increment it
        Integer count = Integer.parseInt(countString);
        count++;
// Display the new value in the text view.
        showCountTextView.setText(count.toString());
    }

    //    @Override
//    public void updateCity(City city, String title, String year) {
//        getReference().child(city.getName()).removeValue()
//                .addOnSuccessListener(a -> Log.d("FirestoreTest", "Success remove"))
//                .addOnFailureListener(e -> Log.e("FirestoreTest", "Failed remove", e));;;
//        city.setName(title);
//        city.setProvince(year);
//        cityArrayAdapter.notifyDataSetChanged();
//
//        HashMap<String, String> data = new HashMap<>();
//        data.put("Province", city.getProvince());
//
//        getReference().child(city.getName()).setValue(data)
//                .addOnSuccessListener(a -> Log.d("FirestoreTest", "Success"))
//                .addOnFailureListener(e -> Log.e("FirestoreTest", "Failed", e));;
//        // Updating the database using delete + addition
//    }
//
//    @Override
//    public void addCity(City city){
//        cityArrayList.add(city);
//        cityArrayAdapter.notifyDataSetChanged();
//        HashMap<String, String> data = new HashMap<>();
//        data.put("Province", city.getProvince());
//        Log.e("Firestore", "what");
//        getReference().child(city.getName()).setValue(data)
//                .addOnSuccessListener(a -> Log.d("FirestoreTest", "Success"))
//                .addOnFailureListener(e -> Log.e("FirestoreTest", "Failed", e));;
//    }

//    private void deleteCity(City city) {
//        int index = cityArrayList.indexOf(city);
//        cityArrayList.remove(city);
//        cityArrayAdapter.notifyDataSetChanged();
//
//        String cityName = city.getName();
//        getReference().child(cityName).removeValue()
//                .addOnSuccessListener(aVoid -> Log.d("FirestoreTest", "City " + cityName + " deleted"))
//                .addOnFailureListener(e -> {
//                    Log.e("FirestoreTest", "Delete failed for " + cityName, e);
//                    cityArrayList.add(index, city);
//                    cityArrayAdapter.notifyDataSetChanged();
//                });
//    }

}