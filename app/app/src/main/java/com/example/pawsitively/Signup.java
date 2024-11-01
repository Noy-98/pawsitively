package com.example.pawsitively;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Signup extends AppCompatActivity {
    private EditText Email;
    private EditText Password;
    private Button Signup;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Email = findViewById(R.id.Email);
        Password = findViewById(R.id.Password);
        Signup = findViewById(R.id.Signup);
        auth = FirebaseAuth.getInstance();

        Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_Email = Email.getText().toString();
                String txt_Password = Password.getText().toString();

                if (TextUtils.isEmpty(txt_Email) || TextUtils.isEmpty(txt_Password)) {
                    Toast.makeText(Signup.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(txt_Email, txt_Password);
                }
            }
        });

        CheckBox showPassword = findViewById(R.id.showPassword);
        showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Password.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    Password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });

        ImageButton backButton = findViewById(R.id.imageButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateBack();
            }
        });
    }

    private void navigateBack() {
        startActivity(new Intent(Signup.this, MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        navigateBack();
    }

    private void registerUser(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(Signup.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Signup.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                            // Optionally, you can redirect to another activity here
                        } else {
                            Toast.makeText(Signup.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
