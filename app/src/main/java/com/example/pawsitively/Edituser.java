package com.example.pawsitively;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Edituser extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference db;

    private TextInputEditText nameEditText, ageEditText, genderEditText, emailEditText, contactNumberEditText, addressEditText;
    private Button saveButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edituser);

        // Initialize Firebase Auth and Realtime Database
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            Log.d("Edituser", "User ID: " + user.getUid());
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if user is not logged in
        }

        // Initialize UI elements
        nameEditText = findViewById(R.id.nameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        genderEditText = findViewById(R.id.genderEditText);
        emailEditText = findViewById(R.id.emailEditText);
        contactNumberEditText = findViewById(R.id.contactNumberEditText);
        addressEditText = findViewById(R.id.addressEditText);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.imageButton); // Assuming this is your back button

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load current user data
        loadUserData();

        // Set up save button click listener
        saveButton.setOnClickListener(v -> saveUserData());

        // Set up back button click listener
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void loadUserData() {
        if (db != null) {
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Populate EditText fields
                        User userData = dataSnapshot.getValue(User.class);
                        if (userData != null) {
                            nameEditText.setText(userData.getName());
                            ageEditText.setText(userData.getAge());
                            genderEditText.setText(userData.getGender());
                            emailEditText.setText(userData.getEmail());
                            contactNumberEditText.setText(userData.getContactNumber());
                            addressEditText.setText(userData.getAddress());
                        }
                    } else {
                        Toast.makeText(Edituser.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Edituser", "Error loading user data: " + databaseError.getMessage());
                    if (databaseError.getCode() == DatabaseError.PERMISSION_DENIED) {
                        Toast.makeText(Edituser.this, "Permission Denied: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Edituser.this, "Error loading user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void saveUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String email = emailEditText.getText().toString().trim();
            String name = nameEditText.getText().toString().trim();
            String age = ageEditText.getText().toString().trim();
            String gender = genderEditText.getText().toString().trim();
            String contactNumber = contactNumberEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();

            // Input validation
            if (name.isEmpty() || age.isEmpty() || gender.isEmpty() || contactNumber.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a map to hold the updates
            HashMap<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("age", age);
            userData.put("gender", gender);
            userData.put("email", email);
            userData.put("contactNumber", contactNumber);
            userData.put("address", address);

            // Attempt to update user data without deleting pets data
            db.updateChildren(userData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(Edituser.this, "User information updated successfully", Toast.LENGTH_SHORT).show();
                        loadUserData();  // Reload user data after saving
                        clearFields();   // Clear fields after saving
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Edituser", "Error updating user data", e);
                        Toast.makeText(Edituser.this, "Error updating user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    // Clear input fields
    private void clearFields() {
        nameEditText.setText("");
        ageEditText.setText("");
        genderEditText.setText("");
        emailEditText.setText("");
        contactNumberEditText.setText("");
        addressEditText.setText("");
    }

    // User class to model the user data
    public static class User {
        private String name;
        private String age;
        private String gender;
        private String email;
        private String contactNumber;
        private String address;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String name, String age, String gender, String email, String contactNumber, String address) {
            this.name = name;
            this.age = age;
            this.gender = gender;
            this.email = email;
            this.contactNumber = contactNumber;
            this.address = address;
        }

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAge() {
            return age;
        }

        public void setAge(String age) {
            this.age = age;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getContactNumber() {
            return contactNumber;
        }

        public void setContactNumber(String contactNumber) {
            this.contactNumber = contactNumber;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }
}
