package com.example.pawsitively;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference db;

    private EditText emailEditText, passwordEditText, nameEditText, ageEditText, genderEditText, contactNumberEditText, addressEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        nameEditText = findViewById(R.id.nameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        genderEditText = findViewById(R.id.genderEditText);
        contactNumberEditText = findViewById(R.id.contactNumberEditText);
        addressEditText = findViewById(R.id.addressEditText);
        registerButton = findViewById(R.id.registerButton);

        // Set up register button click listener
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String age = ageEditText.getText().toString().trim();
        String gender = genderEditText.getText().toString().trim();
        String contactNumber = contactNumberEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        // Input validation
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || age.isEmpty() || gender.isEmpty() || contactNumber.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User registration successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Initialize database reference
                            db = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

                            // Create User object to save to database
                            User userData = new User(name, age, gender, email, contactNumber, address);

                            // Save user data to Realtime Database
                            db.setValue(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, "User registered and data saved successfully", Toast.LENGTH_SHORT).show();
                                        // Optionally navigate to another activity
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterActivity.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // User registration failed
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
