package com.example.chicksevent.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Calendar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chicksevent.R;
import com.example.chicksevent.databinding.FragmentCreateEventBinding;
import com.example.chicksevent.misc.Event;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.util.FirebaseStorageHelper;
import com.example.chicksevent.util.QRCodeGenerator;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Fragment that provides the user interface for creating a new event in the ChicksEvent app.
 * <p>
 * This fragment allows users to input event details such as name, description, start and end
 * registration dates, and optionally specify a maximum number of entrants. The event is then
 * persisted to Firebase through the {@link Event#createEvent()} method.
 * </p>
 *
 * <p><b>Navigation:</b> Provides quick access to Notification and Event fragments through buttons.
 * </p>
 *
 * @author Jinn Kasai
 */
public class CreateEventFragment extends Fragment {

    /** View binding for accessing UI elements. */
    private FragmentCreateEventBinding binding;
    private FirebaseService imageService = new FirebaseService("Image");

    private ActivityResultLauncher<Intent> pickImageLauncher;

    private Uri imageUri;

    private HashMap<String, Object> urlData = new HashMap<>();


    /**
     * Inflates the layout for this fragment using ViewBinding.
     *
     * @param inflater  The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The root view for the fragment's layout.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreateEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the view hierarchy associated with the fragment has been created.
     * Initializes listeners and button click handlers.
     *
     * @param view The root view returned by {@link #onCreateView}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize AM/PM spinners
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.ampm_choices,
                R.layout.spinner_item
        );
        adapter.setDropDownViewResource(R.layout.spinner_item);
        binding.spinnerAmpm1.setAdapter(adapter);
        binding.spinnerAmpm2.setAdapter(adapter);

        // Show/hide "max entrants" field when checkbox changes
        binding.cbLimitWaitingList.setOnCheckedChangeListener((btn, checked) -> {
            binding.etMaxEntrants.setVisibility(checked ? View.VISIBLE : View.GONE);
        });

        // Add validation for max entrants field
        binding.etMaxEntrants.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (!TextUtils.isEmpty(input) && !input.matches("\\d+")) {
                    binding.etMaxEntrants.setError("Please enter numbers only");
                } else {
                    binding.etMaxEntrants.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set up event start date picker - will automatically show time picker after date selection
        binding.eventStartDateContainer.setOnClickListener(v -> showDatePickerWithTime(
                binding.etEventStartDate,
                binding.etStartTime,
                binding.spinnerAmpm1,
                "Event Start Date"
        ));

        // Set up event end date picker - will automatically show time picker after date selection
        binding.eventEndDateContainer.setOnClickListener(v -> showDatePickerWithTime(
                binding.etEventEndDate,
                binding.etEndTime,
                binding.spinnerAmpm2,
                "Event End Date"
        ));

        // Set up event start time - allow both typing and picker
        // User can type directly in the EditText (it's focusable and clickable)
        addTimeTextWatcher(binding.etStartTime);
        
        // Clicking the container shows the time picker (like iPhone timer)
        // Users can also click the EditText to type directly
        binding.eventStartTimeContainer.setOnClickListener(v -> {
            showTimePicker(binding.etStartTime, binding.spinnerAmpm1);
        });

        // Set up event end time - allow both typing and picker
        addTimeTextWatcher(binding.etEndTime);
        
        // Clicking the container shows the time picker
        binding.eventEndTimeContainer.setOnClickListener(v -> {
            showTimePicker(binding.etEndTime, binding.spinnerAmpm2);
        });

        // Set up registration start date picker
        binding.startDateContainer.setOnClickListener(v -> showDatePicker(
                binding.etStartDate,
                "Registration Start Date"
        ));

        // Set up registration end date picker
        binding.endDateContainer.setOnClickListener(v -> showDatePicker(
                binding.etEndDate,
                "Registration End Date"
        ));

        // Hook up CREATE button
        binding.btnCreateEvent.setOnClickListener(v -> {
            createEventFromForm();
        });

        // Optional: Cancel just pops back
        binding.btnCancel.setOnClickListener(v -> requireActivity().onBackPressed());

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            imageUri = data.getData();
                            binding.imgEventPoster.setImageURI(imageUri);
                        }
                    }
                }
        );

        binding.imgEventPoster.setOnClickListener(v -> {
            openImageChooser();
        });
    }

    public String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos); // compress if needed
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * Reads form data, validates it, creates an {@link Event} object, and uploads it to Firebase.
     * Displays appropriate toast messages on success or validation errors.
     */
    private void createEventFromForm() {
        // Read inputs
        String name  = s(binding.etEventName.getText());
        String desc  = s(binding.etEventDescription.getText());

        String startDateInput = s(binding.etEventStartDate.getText());
        String startTimeInput = s(binding.etStartTime.getText());
        String startAMPM = binding.spinnerAmpm1.getSelectedItem().toString();

        String endDateInput = s(binding.etEventEndDate.getText());
        String endTimeInput = s(binding.etEndTime.getText());
        String endAMPM = binding.spinnerAmpm2.getSelectedItem().toString();
        String regStart = s(binding.etStartDate.getText()); // Registration Start (from your UI)
        String regEnd = s(binding.etEndDate.getText()); // Registration End (from your UI)

        // Optional max entrants with validation
        int entrantLimit = 999;
        if (binding.cbLimitWaitingList.isChecked()) {
            String max = s(binding.etMaxEntrants.getText());
            if (!TextUtils.isEmpty(max)) {
                if (!max.matches("\\d+")) {
                    toast("Please enter numbers only for max entrants");
                    return;
                }
                try {
                    entrantLimit = Integer.parseInt(max);
                    if (entrantLimit <= 0) {
                        toast("Max entrants must be greater than 0");
                        return;
                    }
                } catch (NumberFormatException e) {
                    toast("Invalid number for max entrants");
                    return;
                }
            } else {
                toast("Please enter max number of entrants");
                return;
            }
        }

        // Validate required fields
        if (TextUtils.isEmpty(name)) {
            toast("Please enter an event name");
            return;
        }
        if (TextUtils.isEmpty(desc)) {
            toast("Please enter an event description");
            return;
        }
        if (TextUtils.isEmpty(startDateInput)) {
            toast("Please enter a start date");
            return;
        }
        if (TextUtils.isEmpty(startTimeInput)) {
            toast("Please enter a start time");
            return;
        }
        if (TextUtils.isEmpty(endDateInput)) {
            toast("Please enter an end date");
            return;
        }
        if (TextUtils.isEmpty(endTimeInput)) {
            toast("Please enter an end time");
            return;
        }

        // Validate date format MM-DD-YYYY
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        sdf.setLenient(false);
        Date startDate, endDate;
        try {
            startDate = sdf.parse(startDateInput);
        } catch (ParseException e) {
            toast("Please enter start date as MM-DD-YYYY");
            return;
        }
        try {
            endDate = sdf.parse(endDateInput);
        } catch (ParseException e) {
            toast("Please enter end date as MM-DD-YYYY");
            return;
        }
        
        // Validate that end date is >= start date
        if (endDate.before(startDate)) {
            toast("Event end date must be on or after start date");
            return;
        }

        // Validate time format HH:MM
        if (!startTimeInput.matches("\\d{2}:\\d{2}")) {
            toast("Please select a start time");
            return;
        }
        if (!endTimeInput.matches("\\d{2}:\\d{2}")) {
            toast("Please select an end time");
            return;
        }

        // Combine time + AM/PM
        String finalStartTime = startTimeInput + " " + startAMPM;
        String finalEndTime = endTimeInput + " " + endAMPM;

        // ✅ Now you have:
        // startDateInput, finalStartTime
        // endDateInput, finalEndTime
        // name, desc, entrantLimit
        // Use these to create the event



        // Validate registration dates
        if (TextUtils.isEmpty(regStart)) {
            toast("Please enter a registration start date");
            return;
        }
        if (TextUtils.isEmpty(regEnd)) {
            toast("Please enter a registration end date");
            return;
        }
        
        // Validate registration date format MM-DD-YYYY
        Date regStartDate, regEndDate;
        try {
            regStartDate = sdf.parse(regStart);
        } catch (ParseException e) {
            toast("Please enter registration start date as MM-DD-YYYY");
            return;
        }
        try {
            regEndDate = sdf.parse(regEnd);
        } catch (ParseException e) {
            toast("Please enter registration end date as MM-DD-YYYY");
            return;
        }
        
        // Validate that registration end date is >= registration start date
        if (regEndDate.before(regStartDate)) {
            toast("Registration end date must be on or after start date");
            return;
        }

        // You can also enforce regStart/regEnd if required

        // Organizer/entrant id — using device id like you did in NotificationFragment
        String entrantId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        boolean geolocationRequired = binding.switchGeo.isChecked();

        // Your Event model also has eventStartDate / eventEndDate.
        // If you don’t have those fields on this screen yet, pass nulls (Firebase will omit).
        String eventStartDate = null; // TODO: add UI if needed
        String eventEndDate   = null; // TODO: add UI if needed

        // Poster/tag are optional for now
        String poster = null;
        String tag    = null;

        // id will be generated in createEvent(), pass a placeholder for constructor param
        String placeholderId = null;

        Event e = new Event(
                entrantId,
                placeholderId,
                name,
                desc,
                finalStartTime,
                finalEndTime,
                startDateInput,
                endDateInput,
                regStart,
                regEnd,
                entrantLimit,
                poster,
                tag,
                geolocationRequired
        );

        toast("Event created 🎉");

        // Push to Firebase
        String id = e.createEvent();
        String eventId = e.getId();
        String eventName = e.getName();

        // Generate and save QR code
        generateAndSaveQRCode(eventId, eventName);
        Bitmap bitmap = null;

        if (imageUri == null) return;
//
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), imageUri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                // fallback for older versions
                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
            }
        } catch (IOException err) {
            err.printStackTrace();
        }
//
        String base64Image = bitmapToBase64(bitmap);
        urlData.put("url", base64Image);

        imageService.addEntry(urlData, id);



        // Optionally navigate back:
//        requireActivity().onBackPressed();
    }

    /**
     * Adds a TextWatcher to an EditText to format input as HH:MM automatically
     */
    private void addTimeTextWatcher(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;
                isUpdating = true;

                String str = s.toString().replaceAll("[^\\d]", ""); // remove non-digits

                // Format as HH:MM with proper padding
                if (str.length() == 0) {
                    str = "";
                } else if (str.length() <= 2) {
                    // Just hours entered
                    str = str;
                } else if (str.length() == 3) {
                    // Hours and one minute digit - pad minute with 0
                    str = str.substring(0, 2) + ":" + "0" + str.substring(2);
                } else if (str.length() >= 4) {
                    // Hours and minutes - ensure two digits for minutes
                    String hours = str.substring(0, 2);
                    String minutes = str.substring(2, Math.min(4, str.length()));
                    if (minutes.length() == 1) {
                        minutes = "0" + minutes;
                    }
                    str = hours + ":" + minutes;
                }

                if (str.length() > 5) {
                    str = str.substring(0, 5);
                }

                editText.setText(str);
                editText.setSelection(str.length());
                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Shows a DatePickerDialog and updates the EditText with the selected date in MM-DD-YYYY format.
     *
     * @param editText The EditText to update with the selected date
     * @param title The title for the date picker dialog
     */
    private void showDatePicker(EditText editText, String title) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format date as MM-DD-YYYY
                    String formattedDate = String.format(Locale.US, "%02d-%02d-%04d",
                            selectedMonth + 1, selectedDay, selectedYear);
                    editText.setText(formattedDate);
                },
                year, month, day
        );
        datePickerDialog.setTitle(title);
        datePickerDialog.show();
    }

    /**
     * Shows a DatePickerDialog and then automatically shows a TimePickerDialog after date selection.
     * This allows users to select both date and time in sequence from the calendar.
     *
     * @param dateEditText The EditText to update with the selected date
     * @param timeEditText The EditText to update with the selected time
     * @param amPmSpinner The spinner for AM/PM selection
     * @param title The title for the date picker dialog
     */
    private void showDatePickerWithTime(EditText dateEditText, EditText timeEditText, Spinner amPmSpinner, String title) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format date as MM-DD-YYYY
                    String formattedDate = String.format(Locale.US, "%02d-%02d-%04d",
                            selectedMonth + 1, selectedDay, selectedYear);
                    dateEditText.setText(formattedDate);
                    
                    // Automatically show time picker after date is selected
                    showTimePicker(timeEditText, amPmSpinner);
                },
                year, month, day
        );
        datePickerDialog.setTitle(title);
        datePickerDialog.show();
    }

    /**
     * Shows a TimePickerDialog and updates the EditText with the selected time in HH:MM format.
     *
     * @param editText The EditText to update with the selected time
     * @param amPmSpinner The spinner for AM/PM selection
     */
    private void showTimePicker(EditText editText, Spinner amPmSpinner) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        boolean isPM = calendar.get(Calendar.AM_PM) == Calendar.PM;
        
        // Try to parse existing time from EditText
        String existingTime = editText.getText().toString();
        if (!TextUtils.isEmpty(existingTime) && existingTime.matches("\\d{2}:\\d{2}")) {
            String[] parts = existingTime.split(":");
            int hour12 = Integer.parseInt(parts[0]);
            minute = Integer.parseInt(parts[1]);
            // Convert 12-hour display (1-12) to TimePicker format (0-11)
            if (hour12 == 12) {
                hour = 0;
            } else {
                hour = hour12;
            }
            // Get AM/PM from spinner
            isPM = amPmSpinner.getSelectedItemPosition() == 1;
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, selectedHour, selectedMinute) -> {
                    // TimePicker in 12-hour mode returns 0-11, convert to 1-12 for display
                    int displayHour = selectedHour;
                    if (selectedHour == 0) {
                        displayHour = 12;
                    }
                    
                    // Format time as HH:MM in 12-hour format
                    String formattedTime = String.format(Locale.US, "%02d:%02d", displayHour, selectedMinute);
                    editText.setText(formattedTime);
                    
                    // Try to get AM/PM from the TimePicker
                    // Access the TimePicker's AM/PM spinner
                    try {
                        java.lang.reflect.Field amPmField = view.getClass().getDeclaredField("mAmPmSpinner");
                        amPmField.setAccessible(true);
                        android.widget.Spinner amPmSpinnerView = (android.widget.Spinner) amPmField.get(view);
                        if (amPmSpinnerView != null) {
                            int amPmSelection = amPmSpinnerView.getSelectedItemPosition();
                            amPmSpinner.setSelection(amPmSelection);
                        }
                    } catch (Exception e) {
                        // Fallback: try alternative field name
                        try {
                            java.lang.reflect.Field isAmField = view.getClass().getDeclaredField("mIsAm");
                            isAmField.setAccessible(true);
                            boolean isAM = isAmField.getBoolean(view);
                            amPmSpinner.setSelection(isAM ? 0 : 1);
                        } catch (Exception ex) {
                            // If reflection fails, keep current selection
                            Log.d("TimePicker", "Could not determine AM/PM from picker");
                        }
                    }
                },
                hour, minute, true // true = 12-hour format
        );
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }


    /**
     * Generates a QR code for the event and saves it to local storage and Firebase Storage.
     *
     * @param eventId the event ID to encode in the QR code
     * @param eventName the event name to include in the filename
     */
    private void generateAndSaveQRCode(String eventId, String eventName) {
        if (eventId == null || eventId.isEmpty()) {
            Log.e("CreateEvent", "Cannot generate QR code: eventId is null or empty");
            return;
        }

        // Generate deep link URL
        String deepLink = QRCodeGenerator.generateEventDeepLink(eventId);

        // Generate QR code bitmap
        Bitmap qrBitmap = QRCodeGenerator.generateQRCode(deepLink);
        if (qrBitmap == null) {
            Log.e("CreateEvent", "Failed to generate QR code bitmap");
            return;
        }

        // Save to local storage
        File qrCodeDir = new File(requireContext().getFilesDir(), "qr_codes");
        if (!qrCodeDir.exists()) {
            qrCodeDir.mkdirs();
        }

        // Use event ID for filename (sanitize event name for filename)
        String sanitizedName = eventName != null ? eventName.replaceAll("[^a-zA-Z0-9]", "_") : eventId;
        File qrCodeFile = new File(qrCodeDir, "QR_" + eventId + "_" + sanitizedName + ".png");

        boolean saved = QRCodeGenerator.saveQRCodeToFile(qrBitmap, qrCodeFile);
        if (saved) {
            Log.d("CreateEvent", "QR code saved to local storage: " + qrCodeFile.getAbsolutePath());
        } else {
            Log.e("CreateEvent", "Failed to save QR code to local storage");
        }

        // Upload to Firebase Storage
        FirebaseStorageHelper.uploadQRCode(
                qrBitmap,
                eventId,
                new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("CreateEvent", "QR code uploaded to Firebase Storage: " + uri.toString());
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("CreateEvent", "Failed to upload QR code to Firebase Storage", e);
                    }
                }
        );
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    /**
     * Utility helper to safely trim CharSequence values.
     *
     * @param cs The CharSequence to trim.
     * @return The trimmed String or an empty string if null.
     */
    private static String s(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }

    /**
     * Displays a short {@link Toast} message.
     *
     * @param msg The message to display.
     */
    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Cleans up resources by nullifying the binding when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}