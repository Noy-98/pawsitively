package com.example.pawsitively;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide; // Add this import for Glide
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView; // Import CircleImageView

public class UserAccount extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private CircleImageView profileImage; // Change to CircleImageView
    private Button uploadPhotoButton, saveProfileButton;
    private TextView nameTextView;

    private Uri profileImageUri;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        // Initialize views
        profileImage = findViewById(R.id.profileImage);
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        saveProfileButton = findViewById(R.id.saveProfileButton);
        nameTextView = findViewById(R.id.nameTextView);

        // Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        String userId = firebaseAuth.getCurrentUser().getUid();

        // Fetch and display the user's name and profile image
        fetchUserData(userId);

        // Set up click listeners
        uploadPhotoButton.setOnClickListener(v -> openImageChooser());
        saveProfileButton.setOnClickListener(v -> checkNetworkAndUploadImage());
    }

    private void fetchUserData(String userId) {
        // Fetch and display the user's name
        databaseReference.child(userId).child("name").get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                String name = dataSnapshot.getValue(String.class);
                nameTextView.setText(name);
            } else {
                nameTextView.setText("Name not available");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(UserAccount.this, "Failed to fetch user name", Toast.LENGTH_SHORT).show();
        });

        // Load the user's profile image if it exists
        databaseReference.child(userId).child("profileImageUrl").get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                String imageUrl = dataSnapshot.getValue(String.class);
                loadImageIntoImageView(imageUrl);
            } else {
                Toast.makeText(this, "No profile image found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(UserAccount.this, "Failed to fetch profile image", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadImageIntoImageView(String imageUrl) {
        // Use Glide to load the image into the CircleImageView
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.profile) // Optional placeholder image
                .error(R.drawable.profile) // Optional error image
                .into(profileImage);
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            profileImage.setImageURI(profileImageUri);
        }
    }

    private void checkNetworkAndUploadImage() {
        if (isNetworkConnected()) {
            uploadImageToFirebase();
        } else {
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void uploadImageToFirebase() {
        if (profileImageUri != null) {
            String userId = firebaseAuth.getCurrentUser().getUid();
            StorageReference fileReference = storageReference.child(userId + ".jpg");

            fileReference.putFile(profileImageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Save the image URL in the Realtime Database
                        databaseReference.child(userId).child("profileImageUrl").setValue(uri.toString())
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(UserAccount.this, "Profile image updated successfully!", Toast.LENGTH_SHORT).show();
                                    loadImageIntoImageView(uri.toString()); // Load the new image immediately
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(UserAccount.this, "Failed to save image URL to database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }).addOnFailureListener(e -> {
                        Toast.makeText(UserAccount.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })).addOnFailureListener(e -> {
                        Toast.makeText(UserAccount.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
        }
    }
}
