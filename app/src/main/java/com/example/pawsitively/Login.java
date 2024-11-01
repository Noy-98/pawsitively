package com.example.pawsitively;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private EditText Email;
    private EditText Password;
    private Button Login;
    private CheckBox showPassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        // Initialize views
        Email = findViewById(R.id.Email);
        Password = findViewById(R.id.Password);
        Login = findViewById(R.id.Login);
        showPassword = findViewById(R.id.showPassword);

        // Back button handling
        ImageButton backButton = findViewById(R.id.imageButton);
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
        });

        // Show/hide password checkbox handling
        showPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                Password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            Password.setSelection(Password.getText().length());
        });

        // Login button click listener
        Login.setOnClickListener(view -> {
            String txt_Email = Email.getText().toString().trim();
            String txt_Password = Password.getText().toString().trim();

            if (TextUtils.isEmpty(txt_Email) || TextUtils.isEmpty(txt_Password)) {
                Toast.makeText(Login.this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(txt_Email, txt_Password);
        });

        // Check if user is already logged in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // User is already logged in, navigate to Mainpage
            startActivity(new Intent(Login.this, Mainpage.class));
            finish(); // Optional: Close Login activity
        }
    }

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(Login.this, "Successful Login", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Login.this, Mainpage.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Login.this, "Failed to login: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
