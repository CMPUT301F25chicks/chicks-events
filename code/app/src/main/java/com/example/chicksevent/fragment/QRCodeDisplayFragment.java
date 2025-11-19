package com.example.chicksevent.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.R;
import com.example.chicksevent.databinding.FragmentQrCodeDisplayBinding;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.util.FirebaseStorageHelper;
import com.example.chicksevent.util.QRCodeGenerator;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

/**
 * Fragment that displays the QR code for an event.
 * <p>
 * Allows organizers to view, share, and regenerate QR codes for their events.
 * </p>
 *
 * @author Jinn Kasai
 */
public class QRCodeDisplayFragment extends Fragment {

    private static final String TAG = "QRCodeDisplay";
    private FragmentQrCodeDisplayBinding binding;
    private FirebaseService eventService;
    private String eventId;
    private String eventName;
    private Bitmap qrBitmap;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQrCodeDisplayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventService = new FirebaseService("Event");

        // Get event ID and name from arguments
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId");
            eventName = args.getString("eventName");
        }

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(requireContext(), "Event ID not found", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }

        // Set event name
        TextView tvEventName = view.findViewById(R.id.tv_event_name);
        if (eventName != null) {
            tvEventName.setText(eventName);
        } else {
            loadEventName();
        }

        // Load and display QR code
        loadQRCode();

        // Setup buttons
        Button shareButton = view.findViewById(R.id.btn_share_qr);
        shareButton.setOnClickListener(v -> shareQRCode());

        Button regenerateButton = view.findViewById(R.id.btn_regenerate_qr);
        regenerateButton.setOnClickListener(v -> regenerateQRCode());

        // Setup navigation buttons
        setupNavigationButtons(view);
    }

    /**
     * Loads the event name from Firebase if not provided.
     */
    private void loadEventName() {
        eventService.getReference().child(eventId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot snapshot = task.getResult();
                Object nameObj = snapshot.child("name").getValue();
                if (nameObj != null) {
                    eventName = nameObj.toString();
                    TextView tvEventName = getView().findViewById(R.id.tv_event_name);
                    if (tvEventName != null) {
                        tvEventName.setText(eventName);
                    }
                }
            }
        });
    }

    /**
     * Loads the QR code from local storage or Firebase Storage.
     */
    private void loadQRCode() {
        // Try loading from local storage first
        File qrCodeDir = new File(requireContext().getFilesDir(), "qr_codes");
        File[] qrFiles = qrCodeDir.listFiles((dir, name) -> name.startsWith("QR_" + eventId + "_"));
        
        if (qrFiles != null && qrFiles.length > 0) {
            // Load from local storage
            Bitmap bitmap = BitmapFactory.decodeFile(qrFiles[0].getAbsolutePath());
            if (bitmap != null) {
                displayQRCode(bitmap);
                return;
            }
        }

        // If not found locally, try loading from Firebase Storage
        loadQRCodeFromFirebase();
    }

    /**
     * Loads QR code from Firebase Storage.
     */
    private void loadQRCodeFromFirebase() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference qrCodeRef = storage.getReference().child("qr_codes").child(eventId + ".png");

        qrCodeRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap != null) {
                displayQRCode(bitmap);
                // Also save to local storage for future use
                saveQRCodeToLocal(bitmap);
            } else {
                // QR code not found, generate a new one
                generateQRCode();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to load QR code from Firebase Storage", e);
            // QR code not found, generate a new one
            generateQRCode();
        });
    }

    /**
     * Generates a new QR code for the event.
     */
    private void generateQRCode() {
        String deepLink = QRCodeGenerator.generateEventDeepLink(eventId);
        Bitmap bitmap = QRCodeGenerator.generateQRCode(deepLink);
        
        if (bitmap != null) {
            displayQRCode(bitmap);
            saveQRCodeToLocal(bitmap);
            uploadQRCodeToFirebase(bitmap);
        } else {
            Toast.makeText(requireContext(), "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays the QR code bitmap in the ImageView.
     */
    private void displayQRCode(Bitmap bitmap) {
        qrBitmap = bitmap;
        ImageView qrImageView = getView().findViewById(R.id.iv_qr_code);
        if (qrImageView != null) {
            qrImageView.setImageBitmap(bitmap);
        }
    }

    /**
     * Saves QR code to local storage.
     */
    private void saveQRCodeToLocal(Bitmap bitmap) {
        File qrCodeDir = new File(requireContext().getFilesDir(), "qr_codes");
        if (!qrCodeDir.exists()) {
            qrCodeDir.mkdirs();
        }

        String sanitizedName = eventName != null ? eventName.replaceAll("[^a-zA-Z0-9]", "_") : eventId;
        File qrCodeFile = new File(qrCodeDir, "QR_" + eventId + "_" + sanitizedName + ".png");
        
        QRCodeGenerator.saveQRCodeToFile(bitmap, qrCodeFile);
    }

    /**
     * Uploads QR code to Firebase Storage.
     */
    private void uploadQRCodeToFirebase(Bitmap bitmap) {
        FirebaseStorageHelper.uploadQRCode(
            bitmap,
            eventId,
            uri -> Log.d(TAG, "QR code uploaded to Firebase Storage"),
            e -> Log.e(TAG, "Failed to upload QR code to Firebase Storage", e)
        );
    }

    /**
     * Shares the QR code image via Android's share intent.
     */
    private void shareQRCode() {
        if (qrBitmap == null) {
            Toast.makeText(requireContext(), "QR code not available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Save to temporary file for sharing
            File qrCodeDir = new File(requireContext().getCacheDir(), "qr_codes_share");
            if (!qrCodeDir.exists()) {
                boolean created = qrCodeDir.mkdirs();
                if (!created) {
                    Log.e(TAG, "Failed to create qr_codes_share directory");
                    Toast.makeText(requireContext(), "Failed to prepare QR code for sharing", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            File shareFile = new File(qrCodeDir, "QR_" + eventId + ".png");
            if (!QRCodeGenerator.saveQRCodeToFile(qrBitmap, shareFile)) {
                Toast.makeText(requireContext(), "Failed to save QR code for sharing", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Use FileProvider to get a content URI (required for Android 7.0+)
            String authority = requireContext().getPackageName() + ".fileprovider";
            Uri shareUri = FileProvider.getUriForFile(requireContext(), authority, shareFile);
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, shareUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "QR Code for: " + (eventName != null ? eventName : "Event"));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "FileProvider error: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error sharing QR code: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error sharing QR code", e);
            Toast.makeText(requireContext(), "Failed to share QR code", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Regenerates the QR code for the event.
     */
    private void regenerateQRCode() {
        generateQRCode();
        Toast.makeText(requireContext(), "QR code regenerated", Toast.LENGTH_SHORT).show();
    }

    /**
     * Sets up navigation button click listeners.
     */
    private void setupNavigationButtons(View view) {
        Button notificationButton = view.findViewById(R.id.btn_notification);
        Button eventButton = view.findViewById(R.id.btn_events);
        Button scanButton = view.findViewById(R.id.btn_scan);
        Button createEventButton = view.findViewById(R.id.btn_addEvent);
        Button profileButton = view.findViewById(R.id.btn_profile);

        notificationButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_QRCodeDisplayFragment_to_NotificationFragment));

        eventButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_QRCodeDisplayFragment_to_EventFragment));

        // QR scanner button
        scanButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_QRCodeDisplayFragment_to_QRCodeScannerFragment));

        createEventButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_QRCodeDisplayFragment_to_CreateEventFragment));

        profileButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_QRCodeDisplayFragment_to_ProfileFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

