package com.example.chicksevent.fragment_org;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chicksevent.R;
import com.example.chicksevent.databinding.FragmentUpdateEventDetailBinding;
import com.example.chicksevent.misc.FirebaseService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

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
    private ActivityResultLauncher<Intent> pickImageLauncher;

    private FirebaseService imageService = new FirebaseService("Image");

    private Uri imageUri;

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
        Button saveButton = view.findViewById(R.id.btn_save_event);

        Bundle args = getArguments();
        if (args != null) {
            eventName.setText(args.getString("eventId"));
            // Use it to populate UI
        }

        ImageButton imageButton = view.findViewById(R.id.img_event);
        imageService.getReference().child(args.getString("eventId")).get().addOnCompleteListener(task -> {
            if (task.getResult().getValue() == null) return;
            String base64Image = ((HashMap<String, String>) task.getResult().getValue()).get("url");
            byte[] bytes = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageButton.setImageBitmap(bitmap);
        });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            imageUri = data.getData();
                            imageButton.setImageURI(imageUri);


                        }
                    }
                }
        );



        imageButton.setOnClickListener(v -> {
            Log.i("pressed image", ""); openImageChooser();});

        saveButton.setOnClickListener(l -> {
            Bitmap bitmap = null;
//
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), imageUri);
                    bitmap = ImageDecoder.decodeBitmap(source);
                } else {
                    // fallback for older versions
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//
            String base64Image = bitmapToBase64(bitmap);
            HashMap<String, Object> hash = new HashMap<>();
            hash.put("url", base64Image);

            imageService.addEntry(hash, args.getString("eventId"));

            Toast.makeText(getContext(), "Updated Event Details", Toast.LENGTH_SHORT).show();
        });
    }

    public String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos); // compress if needed
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    /** Clears binding references when the view is destroyed. */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
