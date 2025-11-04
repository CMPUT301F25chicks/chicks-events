package com.example.chicksevent;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;

public class CreateEventFragment extends Fragment {

    private EditText nameEt, detailsEt, startEt, endEt, regStartEt, regEndEt, limitEt, tagsEt;
    private Button publishBtn;
    private ImageView qrImg;

    private FirebaseService service;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_event, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        nameEt     = v.findViewById(R.id.inputName);
        detailsEt  = v.findViewById(R.id.inputDetails);
        startEt    = v.findViewById(R.id.inputStartDate);
        endEt      = v.findViewById(R.id.inputEndDate);
        regStartEt = v.findViewById(R.id.inputRegStart);
        regEndEt   = v.findViewById(R.id.inputRegEnd);
        limitEt    = v.findViewById(R.id.inputLimit);
        tagsEt     = v.findViewById(R.id.inputTags);
        publishBtn = v.findViewById(R.id.btnPublish);
        qrImg      = v.findViewById(R.id.qrImage);

        // Write under /events
        service = new FirebaseService("events");

        publishBtn.setOnClickListener(view -> publish());
    }

    private void publish() {
        publishBtn.setEnabled(false);

        // Validate quick
        if (!valid()) {
            Toast.makeText(requireContext(), "Fill required fields and use YYYY-MM-DD.", Toast.LENGTH_LONG).show();
            publishBtn.setEnabled(true);
            return;
        }

        // Build the map exactly like your DB screenshot (strings + int)
        HashMap<String, Object> e = new HashMap<>();
        e.put("name", text(nameEt));
        e.put("eventDetails", text(detailsEt));
        e.put("eventStartDate", text(startEt));
        e.put("eventEndDate", text(endEt));
        e.put("registrationStartDate", text(regStartEt));
        e.put("registrationEndDate", text(regEndEt));
        e.put("entrantLimit", parseInt(text(limitEt)));
        e.put("organizer", "dev-organizer"); // TODO replace with auth uid
        e.put("poster", null);
        e.put("tag", text(tagsEt)); // space-separated tags

        // Optional placeholders (until you back them with real lists)

        // Write: addEntry returns the generated push key immediately
        String eventId = service.addEntry(e);


    }

    // helpers
    private String text(EditText et) { return et.getText().toString().trim(); }
    private int parseInt(String s){ try { return Integer.parseInt(s); } catch(Exception e){ return 0; } }
    private boolean isIso(String s){ return !TextUtils.isEmpty(s) && s.matches("\\d{4}-\\d{2}-\\d{2}"); }
    private boolean valid(){
        return !TextUtils.isEmpty(text(nameEt))
                && parseInt(text(limitEt)) > 0
                && isIso(text(startEt))
                && isIso(text(endEt))
                && isIso(text(regStartEt))
                && isIso(text(regEndEt));
    }
}
