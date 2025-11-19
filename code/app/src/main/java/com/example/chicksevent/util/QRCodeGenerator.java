package com.example.chicksevent.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for generating QR codes.
 * <p>
 * Generates QR code bitmaps from text/data and saves them as image files.
 * </p>
 *
 * @author Jinn Kasai
 */
public class QRCodeGenerator {

    private static final int QR_CODE_SIZE = 512; // Size in pixels
    private static final int QR_CODE_MARGIN = 4;

    /**
     * Generates a QR code bitmap from the given text/data.
     *
     * @param data the data to encode in the QR code
     * @return a Bitmap containing the QR code, or null if generation fails
     */
    public static Bitmap generateQRCode(String data) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, QR_CODE_MARGIN);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Saves a QR code bitmap to a file in the app's internal storage.
     *
     * @param bitmap the QR code bitmap to save
     * @param file the file to save to
     * @return true if successful, false otherwise
     */
    public static boolean saveQRCodeToFile(Bitmap bitmap, File file) {
        if (bitmap == null || file == null) {
            return false;
        }

        try {
            // Create parent directories if they don't exist
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generates a deep link URL for an event.
     *
     * @param eventId the event ID to encode
     * @return the deep link URL (e.g., "chicksevent://event/E123")
     */
    public static String generateEventDeepLink(String eventId) {
        return "chicksevent://event/" + eventId;
    }
}

