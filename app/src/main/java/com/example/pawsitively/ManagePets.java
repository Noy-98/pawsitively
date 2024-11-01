package com.example.pawsitively;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

public class ManagePets extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 2;

    private EditText petNameEditText, breedEditText, birthdayEditText;
    private Spinner genderSpinner, petTypeSpinner;
    private Button addPetButton, uploadImageButton;
    private ImageView petImageView;
    private ImageButton backButton;

    private Uri petImageUri;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_pets);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference("pet_images");

        petNameEditText = findViewById(R.id.PetName);
        breedEditText = findViewById(R.id.Breed);
        birthdayEditText = findViewById(R.id.Birthday);
        genderSpinner = findViewById(R.id.Gender);
        petTypeSpinner = findViewById(R.id.PetType);
        addPetButton = findViewById(R.id.Signup);
        backButton = findViewById(R.id.imageButton);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        petImageView = findViewById(R.id.petImageView);

        setupSpinners();

        birthdayEditText.setOnClickListener(v -> showDatePickerDialog());
        uploadImageButton.setOnClickListener(v -> openImageChooser());
        addPetButton.setOnClickListener(v -> addPet());
        backButton.setOnClickListener(v -> finish());

        checkStoragePermission();
    }

    private void setupSpinners() {
        ArrayAdapter<String> petTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Dog", "Cat"});
        petTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        petTypeSpinner.setAdapter(petTypeAdapter);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Male", "Female"});
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            petImageUri = data.getData();
            petImageView.setImageURI(petImageUri);
        }
    }

    private void addPet() {
        String petName = petNameEditText.getText().toString().trim();
        String breed = breedEditText.getText().toString().trim();
        String birthday = birthdayEditText.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString();
        String petType = petTypeSpinner.getSelectedItem().toString();

        // Validate if all fields are filled
        if (petName.isEmpty()) {
            petNameEditText.setError("Pet name is required");
            petNameEditText.requestFocus();
            return;
        }

        if (breed.isEmpty()) {
            breedEditText.setError("Breed is required");
            breedEditText.requestFocus();
            return;
        }

        if (birthday.isEmpty()) {
            birthdayEditText.setError("Birthday is required");
            birthdayEditText.requestFocus();
            return;
        }

        if (petImageUri == null) {
            Toast.makeText(this, "Please upload an image of your pet", Toast.LENGTH_SHORT).show();
            return;
        }

        if (firebaseAuth.getCurrentUser() != null) {
            String userId = firebaseAuth.getCurrentUser().getUid();

            String petId = databaseReference.child(userId).child("pets").push().getKey();
            Pet newPet = new Pet(petName, breed, birthday, gender, petType);

            addPetButton.setEnabled(false); // Disable the button to prevent multiple clicks

            // Save pet data in Realtime Database
            databaseReference.child(userId).child("pets").child(petId).setValue(newPet)
                    .addOnSuccessListener(aVoid -> {
                        // Upload image to Firebase Storage if all data is valid
                        uploadImageToFirebase(petId);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ManagePets.this, "Failed to add pet: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        addPetButton.setEnabled(true); // Re-enable the button on failure
                    });
        }
    }


    private void uploadImageToFirebase(String petId) {
        if (petImageUri != null) {
            StorageReference fileReference = storageReference.child(petId + ".jpg");

            fileReference.putFile(petImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            databaseReference.child(firebaseAuth.getCurrentUser().getUid())
                                    .child("pets").child(petId).child("imageUrl").setValue(uri.toString())
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(ManagePets.this, "Pet and image added successfully!", Toast.LENGTH_SHORT).show();
                                        clearFields();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(ManagePets.this, "Failed to save image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ManagePets.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void clearFields() {
        petNameEditText.setText("");
        breedEditText.setText("");
        birthdayEditText.setText("");
        genderSpinner.setSelection(0);
        petTypeSpinner.setSelection(0);
        petImageView.setImageResource(0);
        petImageUri = null;
        addPetButton.setEnabled(true);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> birthdayEditText.setText(day + "/" + (month + 1) + "/" + year),
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    public static class Pet {
        public String name, breed, birthday, gender, type, imageUrl;

        public Pet() {}

        public Pet(String name, String breed, String birthday, String gender, String type) {
            this.name = name;
            this.breed = breed;
            this.birthday = birthday;
            this.gender = gender;
            this.type = type;
        }
    }
}
