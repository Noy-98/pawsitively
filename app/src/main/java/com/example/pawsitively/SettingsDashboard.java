package com.example.pawsitively;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsDashboard extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private CircleImageView profileImage; // Change to CircleImageView
    private Button uploadPhotoButton, saveProfileButton;
    private TextView nameTextView;

    private Uri profileImageUri;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;

    private TextView logoutTextView;
    private TextView editUserInfoLabel;
    private TextView aboutAppTextView;
    private TextView termsConditionsTextView;
    private TextView changePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.settings);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home) {
                    startActivity(new Intent(getApplicationContext(), HomeDashboard.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                } else if (item.getItemId() == R.id.maps) {
                    startActivity(new Intent(getApplicationContext(), MapsDashboard.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                } else if (item.getItemId() == R.id.notification) {
                    startActivity(new Intent(getApplicationContext(), NotificationDashboard.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                } else if (item.getItemId() == R.id.settings) {
                    return true;
                }
                return false;
            }
        });

        profileImage = findViewById(R.id.profileImage);
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        saveProfileButton = findViewById(R.id.saveProfileButton);
        nameTextView = findViewById(R.id.nameTextView);

        // Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        String userId = firebaseAuth.getCurrentUser().getUid();


        logoutTextView = findViewById(R.id.logoutTextView);
        logoutTextView.setOnClickListener(view -> logoutUser());

        editUserInfoLabel = findViewById(R.id.editUserInfoLabel);
        editUserInfoLabel.setOnClickListener(view -> startActivity(new Intent(SettingsDashboard.this, Edituser.class)));

        aboutAppTextView = findViewById(R.id.aboutAppTextView);
        aboutAppTextView.setOnClickListener(view -> startActivity(new Intent(SettingsDashboard.this, Aboutus.class)));

        termsConditionsTextView = findViewById(R.id.termsConditionsTextView);
        termsConditionsTextView.setOnClickListener(view -> startActivity(new Intent(SettingsDashboard.this, Terms.class)));

        changePassword = findViewById(R.id.changePassword);
        changePassword.setOnClickListener(view -> startActivity(new Intent(SettingsDashboard.this, Changepassword.class)));

        // Fetch and display the user's name and profile image
        fetchUserData(userId);

        // Set up click listeners
        uploadPhotoButton.setOnClickListener(v -> openImageChooser());
        saveProfileButton.setOnClickListener(v -> checkNetworkAndUploadImage());
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(SettingsDashboard.this, "Successful Logout", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(SettingsDashboard.this, Login.class));
        finish();
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
            Toast.makeText(SettingsDashboard.this, "Failed to fetch user name", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(SettingsDashboard.this, "Failed to fetch profile image", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(SettingsDashboard.this, "Profile image updated successfully!", Toast.LENGTH_SHORT).show();
                                    loadImageIntoImageView(uri.toString()); // Load the new image immediately
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(SettingsDashboard.this, "Failed to save image URL to database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }).addOnFailureListener(e -> {
                        Toast.makeText(SettingsDashboard.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })).addOnFailureListener(e -> {
                        Toast.makeText(SettingsDashboard.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
        }
    }
}