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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private TextInputEditText Email;
    private TextInputEditText Password;
    private AppCompatButton Login,btnGoogleSignIn;
    private TextView Signup;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private DatabaseReference usersRef;


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
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))  // Replace with your actual client ID from Firebase
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize views
        Email = findViewById(R.id.Email);
        Password = findViewById(R.id.Password);
        Login = findViewById(R.id.login_bttn);
        Signup = findViewById(R.id.sign_up_bttn);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        Signup.setOnClickListener(view -> startActivity(new Intent(Login.this, Signup.class)));
        // Login button click listener
        Login.setOnClickListener(view -> {
            String txt_Email = Email.getText().toString().trim();
            String txt_Password = Password.getText().toString().trim();
            if (txt_Email.isEmpty() || txt_Password.isEmpty()) {
                Toast.makeText(Login.this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(txt_Email, txt_Password);
            }
        });

        // Google Sign-In button click listener
        btnGoogleSignIn.setOnClickListener(view -> signInWithGoogle());

        // Check if user is already logged in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            navigateToHome();
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(Exception.class);
                firebaseAuthWithGoogle(account);
            } catch (Exception e) {
                Toast.makeText(this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            checkIfUserExistsInDatabase(user.getUid());
                        }
                    } else {
                        Toast.makeText(Login.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfUserExistsInDatabase(String uid) {
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    navigateToHome();
                } else {
                    Toast.makeText(Login.this, "User not found in database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Toast.makeText(Login.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> navigateToHome())
                .addOnFailureListener(e -> Toast.makeText(Login.this, "Failed to login: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void navigateToHome() {
        Toast.makeText(Login.this, "Successful Login", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Login.this, HomeDashboard.class));
        finish();
    }
}
