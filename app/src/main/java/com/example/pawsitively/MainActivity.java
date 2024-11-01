package com.example.pawsitively;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private Button Login;
    private Button Signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Signup = findViewById(R.id.Signup);
        Login = findViewById(R.id.Login);

        Login.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, Login.class)));
        Signup.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, Signup.class)));

        // Check if user is already authenticated
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // User is signed in, navigate to Mainpage
            startActivity(new Intent(MainActivity.this, Mainpage.class));
            finish(); // Close MainActivity
        }
    }
}
