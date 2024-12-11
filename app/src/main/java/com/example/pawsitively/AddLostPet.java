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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

public class AddLostPet extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 2;

    private TextInputEditText petNameEditText, breedEditText, birthdayEditText, dateLostEditText, descriptionEditText;
    private Spinner genderSpinner, petTypeSpinner;
    private Button uploadImageButton;

    private AppCompatButton addPetButton;
    private ImageView petImageView;
    private FloatingActionButton backButton;

    private Uri petImageUri;

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
        addPetButton = findViewById(R.id.Signup);
        backButton = findViewById(R.id.go_back_bttn);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        petImageView = findViewById(R.id.petImageView);

        setupSpinners();

        birthdayEditText.setOnClickListener(v -> showDatePickerDialog());
        dateLostEditText.setOnClickListener(v -> showDatePickerDialog2());
        uploadImageButton.setOnClickListener(v -> openImageChooser());
        addPetButton.setOnClickListener(v -> addPet());
        backButton.setOnClickListener(view -> startActivity(new Intent(AddLostPet.this, PetFoundDashboard.class)));

        checkStoragePermission();
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    private void addPet() {
        String petName = petNameEditText.getText().toString().trim();
        String breed = breedEditText.getText().toString().trim();
        String birthday = birthdayEditText.getText().toString().trim();
        String dateLost = dateLostEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString();
        String petType = petTypeSpinner.getSelectedItem().toString();

        if (petName.isEmpty() || breed.isEmpty() || birthday.isEmpty() || dateLost.isEmpty() || description.isEmpty() || petImageUri == null) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

            String petId = databaseReference.child("Pets").push().getKey();

            AddLostPet.Pets newPet = new AddLostPet.Pets(petId, petName, breed, birthday, dateLost, description, gender, petType);

            databaseReference.child("Pets").child(petId).setValue(newPet)
                    .addOnSuccessListener(aVoid -> uploadImageToFirebase(petId))
                    .addOnFailureListener(e -> Toast.makeText(AddLostPet.this, "Failed to add pet: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void uploadImageToFirebase(String petId) {
        if (petImageUri != null) {
            StorageReference fileReference = storageReference.child(petId + ".jpg");

            fileReference.putFile(petImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            databaseReference
                                    .child("Pets").child(petId).child("imageUrl").setValue(uri.toString())
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(AddLostPet.this, "Pet and image added successfully!", Toast.LENGTH_SHORT).show();
                                        clearFields();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(AddLostPet.this, "Failed to save image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddLostPet.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void clearFields() {
        petNameEditText.setText("");
        breedEditText.setText("");
        birthdayEditText.setText("");
        dateLostEditText.setText("");
        descriptionEditText.setText("");
        genderSpinner.setSelection(0);
        petTypeSpinner.setSelection(0);
        petImageView.setImageResource(0);
        petImageUri = null;
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            petImageUri = data.getData();
            petImageView.setImageURI(petImageUri);
        }
    }

    private void showDatePickerDialog2() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> birthdayEditText.setText(day + "/" + (month + 1) + "/" + year),
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
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

    private void setupSpinners() {
        ArrayAdapter<String> petTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Dog", "Cat"});
        petTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        petTypeSpinner.setAdapter(petTypeAdapter);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Male", "Female"});
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);
    }

    public static class Pets {
        public String petId;
        public String name, breed, birthday, dateLost, description, gender, type, imageUrl;

        public Pets() {}

        public Pets(String petId, String name, String breed, String birthday, String dateLost, String description,String gender, String type) {
            this.petId = petId;
            this.name = name;
            this.breed = breed;
            this.birthday = birthday;
            this.dateLost = dateLost;
            this.description = description;
            this.gender = gender;
            this.type = type;
        }
    }
}