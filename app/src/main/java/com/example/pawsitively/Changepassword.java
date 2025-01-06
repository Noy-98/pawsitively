package com.example.pawsitively;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Changepassword extends AppCompatActivity {

    private FloatingActionButton backButton;
    private TextInputEditText oldPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    private AppCompatButton saveButton;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_changepassword);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "No user logged in!", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if no user is logged in
        }

        backButton = findViewById(R.id.go_back_bttn);
        oldPasswordEditText = findViewById(R.id.oldPassword);
        newPasswordEditText = findViewById(R.id.newPassword);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        saveButton = findViewById(R.id.saveButton);

        backButton.setOnClickListener(view -> startActivity(new Intent(Changepassword.this, SettingsDashboard.class)));

        saveButton.setOnClickListener(view -> {
            String oldPassword = oldPasswordEditText.getText().toString().trim();
            String newPassword = newPasswordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (validateInputs(oldPassword, newPassword, confirmPassword)) {
                updatePassword(currentUser, oldPassword, newPassword);
            }
        });
    }

    private boolean validateInputs(String oldPassword, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(oldPassword)) {
            oldPasswordEditText.setError("Old password is required");
            oldPasswordEditText.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(newPassword)) {
            newPasswordEditText.setError("New password is required");
            newPasswordEditText.requestFocus();
            return false;
        }
        if (newPassword.length() < 6) {
            newPasswordEditText.setError("Password must be at least 6 characters");
            newPasswordEditText.requestFocus();
            return false;
        }
        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return false;
        }
        return true;
    }

    private void updatePassword(FirebaseUser currentUser, String oldPassword, String newPassword) {
        String email = currentUser.getEmail();

        if (email == null) {
            Toast.makeText(this, "Email not found for the current user!", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);

        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentUser.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Changepassword.this, SettingsDashboard.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update password: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Reauthentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}