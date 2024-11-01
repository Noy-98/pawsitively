package com.example.pawsitively;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Mainpage extends AppCompatActivity {
    private Button logoutButton;
    private boolean doubleBackToExitPressedOnce = false;
    private RecyclerView petRecyclerView;
    private PetAdapter petAdapter;
    private List<ManagePets.Pet> petList;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mainpage);

        // Check if user is logged in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // User is not logged in, redirect to login activity
            startActivity(new Intent(Mainpage.this, MainActivity.class));
            finish();
            return;
        }

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("pets");

        petRecyclerView = findViewById(R.id.petRecyclerView);
        petRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        petList = new ArrayList<>();
        petAdapter = new PetAdapter(petList);
        petRecyclerView.setAdapter(petAdapter);

        loadPets();

        // Initialize logout button and set its click listener
        logoutButton = findViewById(R.id.Logout);
        logoutButton.setOnClickListener(view -> logoutUser());

        // Initialize image view buttons and set click listeners
        initializeNavigationButtons();
    }

    private void loadPets() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                petList.clear();
                for (DataSnapshot petSnapshot : dataSnapshot.getChildren()) {
                    ManagePets.Pet pet = petSnapshot.getValue(ManagePets.Pet.class);
                    if (pet != null) {
                        petList.add(pet);
                    }
                }
                petAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Mainpage.this, "Failed to load pets: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(Mainpage.this, "Successful Logout", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Mainpage.this, MainActivity.class));
        finish();
    }

    private void initializeNavigationButtons() {
        ImageView petsButton = findViewById(R.id.imageView2);
        petsButton.setOnClickListener(v -> startActivity(new Intent(Mainpage.this, ManagePets.class)));

        ImageView userAccountButton = findViewById(R.id.imageView);
        userAccountButton.setOnClickListener(v -> startActivity(new Intent(Mainpage.this, UserAccount.class)));

        ImageView messageButton = findViewById(R.id.imageView1);
        messageButton.setOnClickListener(v -> {
            // Replace with actual logic to get recipient user ID
            String recipientUserId = "someUserId"; // Get the recipient user ID based on your logic
            Intent intent = new Intent(Mainpage.this, Message.class);
            intent.putExtra("recipientUserId", recipientUserId); // Pass the recipient ID
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }
}
