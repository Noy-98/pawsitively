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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    private boolean doubleBackToExitPressedOnce = false;
    private RecyclerView petRecyclerView;
    private PetAdapter petAdapter;
    private List<ManagePets.Pet> petList;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private FloatingActionButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mainpage);

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
        petAdapter = new PetAdapter(petList, this);
        petRecyclerView.setAdapter(petAdapter);
        backButton = findViewById(R.id.go_back_bttn);

        loadPets();

        // Initialize image view buttons and set click listeners
        initializeNavigationButtons();

        backButton.setOnClickListener(view -> startActivity(new Intent(Mainpage.this, HomeDashboard.class)));
    }

    private void loadPets() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                petList.clear(); // Clear the list before adding new data
                for (DataSnapshot petSnapshot : dataSnapshot.getChildren()) {
                    ManagePets.Pet pet = petSnapshot.getValue(ManagePets.Pet.class);
                    if (pet != null) {
                        // Set the petId from the database key
                        pet.petId = petSnapshot.getKey();
                        petList.add(pet);
                    }
                }
                petAdapter.notifyDataSetChanged(); // Notify adapter to refresh the RecyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Mainpage.this, "Failed to load pets: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeNavigationButtons() {
        ImageView petsButton = findViewById(R.id.imageView2);
        petsButton.setOnClickListener(v -> startActivity(new Intent(Mainpage.this, ManagePets.class)));
    }
}
