package com.example.pawsitively;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

public class AddLostPet extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 2;

    private TextInputEditText petNameEditText, breedEditText, birthdayEditText, dateLostEditText, descriptionEditText, petId;
    private Spinner genderSpinner, petTypeSpinner, tagTypeSpinner;
    private Button uploadImageButton, uploadImageButton2;

    private AppCompatButton addPetButton;
    private CircleImageView petImageView, vaccineImage;
    private FloatingActionButton backButton;

    private Uri petImageUri, vaccineImageUri;

    private DatabaseReference databaseReference;

    private StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_lost_pet);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("Lost&Found");
        storageReference = FirebaseStorage.getInstance().getReference("Lost&Found");

        petNameEditText = findViewById(R.id.PetName);
        breedEditText = findViewById(R.id.Breed);
        birthdayEditText = findViewById(R.id.Birthday);
        dateLostEditText = findViewById(R.id.dateLost);
        descriptionEditText = findViewById(R.id.description);
        genderSpinner = findViewById(R.id.Gender);
        petTypeSpinner = findViewById(R.id.PetType);
        tagTypeSpinner = findViewById(R.id.typeTag);
        addPetButton = findViewById(R.id.Signup);
        backButton = findViewById(R.id.go_back_bttn);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        uploadImageButton2 = findViewById(R.id.uploadImageButton2);
        petImageView = findViewById(R.id.petImageView);
        vaccineImage = findViewById(R.id.vaccineImage);
        petId = findViewById(R.id.petId);

        setupSpinners();

        birthdayEditText.setOnClickListener(v -> showDatePickerDialog(birthdayEditText));
        dateLostEditText.setOnClickListener(v -> showDatePickerDialog2(dateLostEditText));
        uploadImageButton.setOnClickListener(v -> openImageChooser());
        uploadImageButton2.setOnClickListener(v -> openImageChooser2());
        addPetButton.setOnClickListener(v -> addPet());
        backButton.setOnClickListener(view -> startActivity(new Intent(AddLostPet.this, PetFoundDashboard.class)));

        checkStoragePermission();
    }

    private void openImageChooser2() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Vaccine Image"), PICK_IMAGE_REQUEST + 1);
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    private void addPet() {
        // Disable the button to prevent duplicate submissions
        addPetButton.setEnabled(false);

        String petName = petNameEditText.getText().toString().trim();
        String breed = breedEditText.getText().toString().trim();
        String birthday = birthdayEditText.getText().toString().trim();
        String dateLost = dateLostEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String ptId = petId.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString();
        String petType = petTypeSpinner.getSelectedItem().toString();
        String tagType = tagTypeSpinner.getSelectedItem().toString();

        if (petName.isEmpty() || breed.isEmpty() || birthday.isEmpty() || dateLost.isEmpty() || description.isEmpty() || ptId.isEmpty() || petImageUri == null || vaccineImageUri == null) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            addPetButton.setEnabled(true); // Re-enable the button if validation fails
            return;
        }

        String petId = ptId;

        if ("RFID Tag".equals(tagType)) {
            // Show RFID Alert Dialog and save the selected key/tagId
            showRFIDAlertDialog(selectedTagId -> {
                Pets newPet = new Pets(petId, petName, breed, birthday, dateLost, description, gender, petType, tagType, selectedTagId);
                savePetDataToFirebase(petId, newPet, tagType);
            });
        } else {
            // Default tagId to null for other tag types
            Pets newPet = new Pets(petId, petName, breed, birthday, dateLost, description, gender, petType, tagType, null);
            savePetDataToFirebase(petId, newPet, tagType);
        }
    }



    private void showRFIDAlertDialog(OnTagIdSelectedListener listener) {
        DatabaseReference tagsReference = FirebaseDatabase.getInstance().getReference("GeneratedTags");
        tagsReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<String> keys = new ArrayList<>();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    keys.add(snapshot.getKey());
                }

                // Show the keys in a dialog
                String[] keysArray = keys.toArray(new String[0]);
                new AlertDialog.Builder(this)
                        .setTitle("Select RFID Tag")
                        .setItems(keysArray, (dialog, which) -> {
                            String selectedTagId = keysArray[which];
                            listener.onTagIdSelected(selectedTagId); // Pass selected key back
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                Toast.makeText(this, "Failed to load tags", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Callback interface for handling tagId selection
    private interface OnTagIdSelectedListener {
        void onTagIdSelected(String tagId);
    }

    private void savePetDataToFirebase(String petId, Pets newPet, String tagType) {
        databaseReference.child("Pets").child(petId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Toast.makeText(this, "Pet already exists in database!", Toast.LENGTH_SHORT).show();
            } else {
                databaseReference.child("Pets").child(petId).setValue(newPet)
                        .addOnSuccessListener(aVoid -> {
                            uploadImageToFirebase(petId);
                            if ("QR Code".equals(tagType)) {
                                generateAndSaveQRCode(petId);
                            }
                            Toast.makeText(this, "Pet added successfully!", Toast.LENGTH_SHORT).show();
                            clearFields();
                        })
                        .addOnFailureListener(e -> Toast.makeText(AddLostPet.this, "Failed to add pet: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }


    private void generateAndSaveQRCode(String petId) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            int qrCodeSize = 500;
            Bitmap bitmap = Bitmap.createBitmap(qrCodeSize, qrCodeSize, Bitmap.Config.RGB_565);
            com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(petId, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize);

            for (int x = 0; x < qrCodeSize; x++) {
                for (int y = 0; y < qrCodeSize; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            saveQRCodeToStorage(bitmap, petId);

        } catch (WriterException e) {
            Toast.makeText(this, "Error generating QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveQRCodeToStorage(Bitmap bitmap, String petId) {
        String picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File qrCodeFile = new File(picturesDir, "QRCode_" + petId + ".png");

        try (FileOutputStream out = new FileOutputStream(qrCodeFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Toast.makeText(this, "QR Code saved to " + qrCodeFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToFirebase(String petId) {
        if (petImageUri != null) {
            StorageReference petImageReference = storageReference.child(petId + "_pet.jpg");

            petImageReference.putFile(petImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        petImageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            databaseReference
                                    .child("Pets").child(petId).child("imageUrl").setValue(uri.toString())
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(AddLostPet.this, "Pet image uploaded successfully!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(AddLostPet.this, "Failed to save pet image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddLostPet.this, "Pet image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

        if (vaccineImageUri != null) {
            StorageReference vaccineImageReference = storageReference.child(petId + "_vaccine.jpg");

            vaccineImageReference.putFile(vaccineImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        vaccineImageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            databaseReference
                                    .child("Pets").child(petId).child("vaccineUrl").setValue(uri.toString())
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(AddLostPet.this, "Vaccine image uploaded successfully!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(AddLostPet.this, "Failed to save vaccine image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddLostPet.this, "Vaccine image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void clearFields() {
        petNameEditText.setText("");
        breedEditText.setText("");
        birthdayEditText.setText("");
        dateLostEditText.setText("");
        descriptionEditText.setText("");
        petId.setText("");
        genderSpinner.setSelection(0);
        petTypeSpinner.setSelection(0);
        tagTypeSpinner.setSelection(0);
        petImageView.setImageResource(0);
        vaccineImage.setImageResource(0);
        petImageUri = null;
        vaccineImageUri = null;
        addPetButton.setEnabled(true);
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pet Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();

            if (requestCode == PICK_IMAGE_REQUEST) {
                // For pet image upload
                petImageUri = selectedImageUri;
                petImageView.setImageURI(petImageUri);
            } else if (requestCode == PICK_IMAGE_REQUEST + 1) {
                // For vaccine image upload
                vaccineImageUri = selectedImageUri;
                vaccineImage.setImageURI(vaccineImageUri);
            }
        }
    }

    private void showDatePickerDialog(TextInputEditText birthdayEditText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> birthdayEditText.setText(day + "/" + (month + 1) + "/" + year),
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showDatePickerDialog2(TextInputEditText dateLostEditText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> dateLostEditText.setText(day + "/" + (month + 1) + "/" + year),
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }


    private void setupSpinners() {
        ArrayAdapter<String> petTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Dog", "Cat"});
        petTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        petTypeSpinner.setAdapter(petTypeAdapter);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Male", "Female"});
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        ArrayAdapter<String> tagTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"QR Code", "RFID Tag"});
        tagTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagTypeSpinner.setAdapter(tagTypeAdapter);
    }

    public static class Pets {
        public String petId;
        public String name, breed, birthday, dateLost, description, gender, type, tagType, imageUrl, vaccineUrl;
        public String tagId; // Added tagId field

        public Pets() {}

        public Pets(String petId, String name, String breed, String birthday, String dateLost, String description,String gender, String type, String tagType, String tagId) {
            this.petId = petId;
            this.name = name;
            this.breed = breed;
            this.birthday = birthday;
            this.dateLost = dateLost;
            this.description = description;
            this.gender = gender;
            this.type = type;
            this.tagType = tagType;
            this.tagId = tagId; // Initialize tagId
        }
    }
}