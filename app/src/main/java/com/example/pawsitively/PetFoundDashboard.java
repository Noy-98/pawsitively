package com.example.pawsitively;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PetFoundDashboard extends AppCompatActivity {

    private String petId;
    private boolean doubleBackToExitPressedOnce = false;
    private RecyclerView petRecyclerView;
    private LostPetAdapter lostPetAdapter;
    private List<AddLostPet.Pets> petList;
    private DatabaseReference databaseReference;
    private FloatingActionButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pet_found_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("Lost&Found").child("Pets");

        // Get the petId passed from the ScanPet activity
        petId = getIntent().getStringExtra("petId");

        petRecyclerView = findViewById(R.id.petRecyclerView);
        petRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        petList = new ArrayList<>();
        lostPetAdapter = new LostPetAdapter(petList, this);
        petRecyclerView.setAdapter(lostPetAdapter);
        backButton = findViewById(R.id.go_back_bttn);

        if (petId != null) {
            loadPets(petId);  // Load data for the specific petId
        }
        initializeNavigationButtons();

        backButton.setOnClickListener(view -> startActivity(new Intent(PetFoundDashboard.this, HomeDashboard.class)));
    }

    private void loadPets(String petId) {
        // Fetch only the pet that matches the petId
        databaseReference.child(petId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    AddLostPet.Pets pet = dataSnapshot.getValue(AddLostPet.Pets.class);
                    if (pet != null) {
                        pet.petId = petId;  // Set the petId to ensure it's passed along
                        petList.clear();
                        petList.add(pet);
                        lostPetAdapter.notifyDataSetChanged();  // Update the RecyclerView
                    }
                } else {
                    Toast.makeText(PetFoundDashboard.this, "Pet not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PetFoundDashboard.this, "Failed to load pet data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeNavigationButtons() {
        ImageView petsButton = findViewById(R.id.imageView2);
        ImageView rfidTag = findViewById(R.id.imageView3);

        petsButton.setOnClickListener(v -> startActivity(new Intent(PetFoundDashboard.this, AddLostPet.class)));
        rfidTag.setOnClickListener(v -> startActivity(new Intent(PetFoundDashboard.this, ScanPet.class)));
    }
}