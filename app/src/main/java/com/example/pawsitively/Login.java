package com.example.pawsitively;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private TextInputEditText Email;
    private TextInputEditText Password;
    private AppCompatButton Login;
    private TextView Signup;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();

        // Initialize views
        Email = findViewById(R.id.Email);
        Password = findViewById(R.id.Password);
        Login = findViewById(R.id.login_bttn);
        Signup = findViewById(R.id.sign_up_bttn);

        Signup.setOnClickListener(view -> startActivity(new Intent(Login.this, Signup.class)));
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
            startActivity(new Intent(Login.this, HomeDashboard.class));
            finish(); // Optional: Close Login activity
        }
    }

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(Login.this, "Successful Login", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Login.this, HomeDashboard.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Login.this, "Failed to login: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
