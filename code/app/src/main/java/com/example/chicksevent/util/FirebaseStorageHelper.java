package com.example.chicksevent.util;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

/**
 * Helper class for Firebase Storage operations.
 * <p>
 * Provides methods to upload QR code images to Firebase Storage.
 * </p>
 *
 * @author Jinn Kasai
 */
public class FirebaseStorageHelper {

    private static final String TAG = "FirebaseStorageHelper";
    private static final String QR_CODES_PATH = "qr_codes";

    /**
     * Uploads a QR code bitmap to Firebase Storage.
     *
     * @param bitmap the QR code bitmap to upload
     * @param eventId the event ID to use as the filename
     * @param onSuccess callback for successful upload (receives download URL)
     * @param onFailure callback for failed upload
     */
    public static void uploadQRCode(Bitmap bitmap, String eventId,
                                    OnSuccessListener<Uri> onSuccess,
                                    OnFailureListener onFailure) {
        if (bitmap == null || eventId == null || eventId.isEmpty()) {
            Log.e(TAG, "Invalid parameters for QR code upload");
            if (onFailure != null) {
                onFailure.onFailure(new IllegalArgumentException("Invalid bitmap or eventId"));
            }
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference qrCodesRef = storage.getReference().child(QR_CODES_PATH);
        StorageReference qrCodeFileRef = qrCodesRef.child(eventId + ".png");

        // Convert bitmap to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        // Upload to Firebase Storage
        UploadTask uploadTask = qrCodeFileRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Get download URL
            qrCodeFileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Log.d(TAG, "QR code uploaded successfully: " + uri.toString());
                if (onSuccess != null) {
                    onSuccess.onSuccess(uri);
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to get download URL", e);
                if (onFailure != null) {
                    onFailure.onFailure(e);
                }
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to upload QR code", e);
            if (onFailure != null) {
                onFailure.onFailure(e);
            }
        });
    }
}

