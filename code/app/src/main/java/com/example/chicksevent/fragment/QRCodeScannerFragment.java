package com.example.chicksevent.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.R;
import com.example.chicksevent.databinding.FragmentQrCodeScannerBinding;
import com.example.chicksevent.misc.FirebaseService;
import com.google.firebase.database.DataSnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import androidx.activity.result.ActivityResultLauncher;

/**
 * Fragment that allows users to scan QR codes to view event details.
 * <p>
 * Uses the device camera to scan QR codes and navigates to event details
 * when a valid event QR code is scanned.
 * </p>
 *
 * @author Jinn Kasai
 */
public class QRCodeScannerFragment extends Fragment {

    private static final String TAG = "QRCodeScanner";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 2001;
    
    private FragmentQrCodeScannerBinding binding;
    private FirebaseService eventService;
    private ActivityResultLauncher<ScanOptions> barcodeLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQrCodeScannerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventService = new FirebaseService("Event");

        // Setup barcode scanner launcher
        barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() == null) {
                // Scan was cancelled
                Toast.makeText(requireContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
            } else {
                // Handle scanned result
                handleQRCodeResult(result.getContents());
            }
        });

        // Setup navigation buttons
        setupNavigationButtons(view);

        // Setup manual scan button
        Button manualScanButton = view.findViewById(R.id.btn_manual_scan);
        if (manualScanButton != null) {
            manualScanButton.setOnClickListener(v -> {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    startScanning();
                } else {
                    requestCameraPermission();
                }
            });
        }

        // Auto-start scanning when fragment opens (if permission granted)
        // Use post to ensure the fragment is fully attached
        view.post(() -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                requestCameraPermission();
            }
        });
    }

    /**
     * Requests camera permission from the user.
     */
    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            Toast.makeText(requireContext(),
                    "Camera permission is required to scan QR codes",
                    Toast.LENGTH_LONG).show();
        }
        
        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                // Permission denied
                if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    // Permanently denied, redirect to settings
                    Toast.makeText(requireContext(),
                            "Camera permission is required. Please enable it in app settings.",
                            Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                    startActivity(intent);
                } else {
                    Toast.makeText(requireContext(),
                            "Camera permission is required to scan QR codes",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Starts the QR code scanner using ZXing IntentIntegrator.
     */
    private void startScanning() {
        try {
            if (barcodeLauncher == null) {
                Log.e(TAG, "Barcode launcher is null! Cannot start scanning.");
                Toast.makeText(requireContext(), "Scanner not ready. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            options.setPrompt("Scan a QR code");
            options.setCameraId(0);
            options.setBeepEnabled(false);
            options.setBarcodeImageEnabled(true);
            options.setOrientationLocked(false);
            
            Log.d(TAG, "Launching QR code scanner...");
            barcodeLauncher.launch(options);
        } catch (Exception e) {
            Log.e(TAG, "Error starting scanner", e);
            Toast.makeText(requireContext(), "Error starting scanner: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Handles the scanned QR code result.
     *
     * @param scannedText the text content of the scanned QR code
     */
    private void handleQRCodeResult(String scannedText) {
        Log.d(TAG, "Scanned QR code: " + scannedText);

        // Parse deep link
        if (scannedText == null || scannedText.isEmpty()) {
            showError("Invalid QR code format");
            return;
        }

        // Check if it's a deep link
        if (scannedText.startsWith("chicksevent://event/")) {
            String eventId = scannedText.substring("chicksevent://event/".length());
            if (eventId.isEmpty()) {
                showError("Invalid QR code format");
                return;
            }
            
            // Verify event exists and navigate
            verifyAndNavigateToEvent(eventId);
        } else {
            // Not a valid event QR code
            showError("Invalid QR code format");
        }
    }

    /**
     * Verifies that the event exists and navigates to event details.
     *
     * @param eventId the event ID from the QR code
     */
    private void verifyAndNavigateToEvent(String eventId) {
        // Search for event by ID in Firebase
        eventService.getReference().get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                boolean eventFound = false;
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    Object idObj = ds.child("id").getValue();
                    if (idObj != null && idObj.toString().equals(eventId)) {
                        // Event found
                        eventFound = true;
                        Object nameObj = ds.child("name").getValue();
                        String eventName = nameObj != null ? nameObj.toString() : eventId;
                        
                        // Navigate to event details
                        Bundle bundle = new Bundle();
                        bundle.putString("eventName", eventName);
                        
                        NavHostFragment.findNavController(QRCodeScannerFragment.this)
                                .navigate(R.id.action_QRCodeScannerFragment_to_EventDetailFragment, bundle);
                        break;
                    }
                }
                
                if (!eventFound) {
                    showError("Event not found");
                }
            } else {
                // Network error or Firebase error
                showError("Network error. Please check your connection and try again.");
            }
        });
    }

    /**
     * Shows an error message to the user.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "QR code scan error: " + message);
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
                        .navigate(R.id.action_QRCodeScannerFragment_to_NotificationFragment));

        eventButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_QRCodeScannerFragment_to_EventFragment));

        scanButton.setOnClickListener(v -> {
            // Restart scanning
            startScanning();
        });

        createEventButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_QRCodeScannerFragment_to_CreateEventFragment));

        profileButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_QRCodeScannerFragment_to_ProfileFragment));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Scanner is launched via Intent, so no need to resume here
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

