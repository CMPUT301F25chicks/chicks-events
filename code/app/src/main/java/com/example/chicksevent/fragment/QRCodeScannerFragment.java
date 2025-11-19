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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chicksevent.R;
import com.example.chicksevent.misc.FirebaseService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * Fragment for scanning QR codes to view event details.
 * <p>
 * Uses ZXing library to scan QR codes containing deep links (chicksevent://event/{eventId}).
 * Automatically starts scanning when the fragment opens (if camera permission is granted).
 * </p>
 *
 * @author Jinn Kasai
 */
public class QRCodeScannerFragment extends Fragment {

    private static final String TAG = "QRCodeScannerFragment";
    private static final String DEEP_LINK_SCHEME = "chicksevent";
    private static final String DEEP_LINK_HOST = "event";

    private ActivityResultLauncher<ScanOptions> scanLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private FirebaseService eventService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        eventService = new FirebaseService("Event");
        
        // Initialize scan launcher
        scanLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() == null) {
                // User cancelled scanning or camera error
                // If back camera failed, try front camera next time
                if (cameraAttempt == 0) {
                    cameraAttempt = 1;
                } else {
                    cameraAttempt = 0; // Reset for next attempt
                }
                return;
            }
            
            // Reset camera attempt on success
            cameraAttempt = 0;
            
            String scannedData = result.getContents();
            handleQRCodeResult(scannedData);
        });
        
        // Initialize permission launcher
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    startScanning();
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        // User denied, but can ask again
                        showPermissionDeniedDialog();
                    } else {
                        // Permanently denied, redirect to settings
                        showPermissionPermanentlyDeniedDialog();
                    }
                }
            }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_code_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupNavigationButtons(view);
        
        // Manual start button
        Button startScanButton = view.findViewById(R.id.btn_start_scan);
        if (startScanButton != null) {
            startScanButton.setOnClickListener(v -> {
                if (hasCameraPermission()) {
                    startScanning();
                } else {
                    requestCameraPermission();
                }
            });
        }
    }

    /**
     * Checks if camera permission is granted.
     */
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests camera permission.
     */
    private void requestCameraPermission() {
        permissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private int cameraAttempt = 0; // Track which camera to try
    
    /**
     * Starts the QR code scanner.
     */
    private void startScanning() {
        try {
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            options.setPrompt("Point camera at QR code");
            
            // Try back camera first (0), then front camera (1) if back fails
            int cameraId = cameraAttempt % 2;
            options.setCameraId(cameraId);
            
            options.setBeepEnabled(false);
            options.setBarcodeImageEnabled(false);
            
            scanLauncher.launch(options);
        } catch (Exception e) {
            Log.e(TAG, "Error launching scanner", e);
            Toast.makeText(requireContext(), "Error starting scanner: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Handles the scanned QR code result.
     *
     * @param scannedData the raw data from the QR code
     */
    private void handleQRCodeResult(String scannedData) {
        if (scannedData == null || scannedData.isEmpty()) {
            showError("Invalid QR code");
            return;
        }

        // Parse deep link
        Uri uri = Uri.parse(scannedData);
        if (!DEEP_LINK_SCHEME.equals(uri.getScheme()) || !DEEP_LINK_HOST.equals(uri.getHost())) {
            showError("Invalid QR code format. Please scan an event QR code.");
            return;
        }

        // Extract event ID from path
        if (uri.getPathSegments().isEmpty()) {
            showError("Invalid QR code format. Event ID not found.");
            return;
        }

        String eventId = uri.getPathSegments().get(0);
        if (eventId == null || eventId.isEmpty()) {
            showError("Invalid QR code format. Event ID is empty.");
            return;
        }

        // Verify event exists in Firebase
        verifyAndNavigateToEvent(eventId);
    }

    /**
     * Verifies that the event exists in Firebase and navigates to EventDetailFragment.
     *
     * @param eventId the event ID to verify
     */
    private void verifyAndNavigateToEvent(String eventId) {
        eventService.getReference().child(eventId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                // Event exists, navigate to event details
                Bundle bundle = new Bundle();
                bundle.putString("eventId", eventId);
                
                try {
                    NavHostFragment.findNavController(this)
                        .navigate(R.id.action_QRCodeScannerFragment_to_EventDetailFragment, bundle);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to event details", e);
                    showError("Error opening event details");
                }
            } else {
                // Event not found
                showError("Event not found. The QR code may be invalid or the event may have been deleted.");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error checking event existence", e);
            showError("Network error. Please check your connection and try again.");
        });
    }

    /**
     * Shows an error message to the user.
     *
     * @param message the error message
     */
    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        // Optionally restart scanning after showing error
        if (hasCameraPermission()) {
            startScanning();
        }
    }

    /**
     * Shows a dialog when camera permission is denied.
     */
    private void showPermissionDeniedDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Camera Permission Required")
            .setMessage("Camera permission is required to scan QR codes. Please grant permission to continue.")
            .setPositiveButton("Grant Permission", (dialog, which) -> requestCameraPermission())
            .setNegativeButton("Cancel", (dialog, which) -> {
                // Navigate back
                NavHostFragment.findNavController(this).navigateUp();
            })
            .show();
    }

    /**
     * Shows a dialog when camera permission is permanently denied, with option to open settings.
     */
    private void showPermissionPermanentlyDeniedDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Camera Permission Required")
            .setMessage("Camera permission is required to scan QR codes. Please enable it in app settings.")
            .setPositiveButton("Open Settings", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                // Navigate back
                NavHostFragment.findNavController(this).navigateUp();
            })
            .show();
    }


    /**
     * Sets up navigation button click listeners.
     */
    private void setupNavigationButtons(View view) {
        Button scanButton = view.findViewById(R.id.btn_start_scan);

        scanButton.setOnClickListener(v -> {
            // Restart scanning
            if (hasCameraPermission()) {
                startScanning();
            } else {
                requestCameraPermission();
            }
        });
    }
}

