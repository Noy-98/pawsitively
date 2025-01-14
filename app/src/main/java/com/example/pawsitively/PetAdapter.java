package com.example.pawsitively;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    private List<ManagePets.Pet> petList;
    private Context context; // To display toast messages

    public PetAdapter(List<ManagePets.Pet> petList, Context context) {
        this.petList = petList;
        this.context = context;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pet, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        ManagePets.Pet pet = petList.get(position);

        holder.petNameTextView.setText(pet.name);
        holder.petBreedTextView.setText("Breed: " + pet.breed);
        holder.petBirthdayTextView.setText("Birthday: " + pet.birthday);
        holder.petGenderTextView.setText("Gender: " + pet.gender);
        holder.petGenderTextView.setText("Gender: " + pet.gender);
        holder.petIdTextView.setText("Pet ID: " + pet.petId);

        // Load pet image using Glide
        Glide.with(holder.petImageView.getContext())
                .load(pet.imageUrl) // imageUrl from the database
                .placeholder(R.drawable.default_pet_image) // Placeholder while loading
                .error(R.drawable.default_pet_image) // Error image if loading fails
                .into(holder.petImageView);

        if (pet.petId != null) {
            holder.delete.setOnClickListener(v -> deletePetFromFirebase(position, pet.petId));
        } else {
            holder.delete.setOnClickListener(v -> {
                Toast.makeText(holder.itemView.getContext(), "Error: Pet ID is null", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    public static class PetViewHolder extends RecyclerView.ViewHolder {
        TextView petNameTextView, petBreedTextView, petBirthdayTextView, petGenderTextView, petIdTextView;
        CircleImageView petImageView; // Changed to CircleImageView
        ImageView delete;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            petNameTextView = itemView.findViewById(R.id.petNameTextView);
            petBreedTextView = itemView.findViewById(R.id.petBreedTextView);
            petBirthdayTextView = itemView.findViewById(R.id.petBirthdayTextView);
            petGenderTextView = itemView.findViewById(R.id.petGenderTextView);
            petIdTextView = itemView.findViewById(R.id.petIdTextView);
            petImageView = itemView.findViewById(R.id.petImageView); // Ensure this matches the CircleImageView in the layout
            delete = itemView.findViewById(R.id.delete);
        }
    }

    public void deletePetFromFirebase(int position, String petId) {
        DatabaseReference userPetsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("pets");

        DatabaseReference globalPetsRef = FirebaseDatabase.getInstance()
                .getReference("Pets");

        // Delete from user-specific pets table
        userPetsRef.child(petId).removeValue().addOnSuccessListener(aVoid -> {
            // Delete from global pets table
            globalPetsRef.child(petId).removeValue().addOnSuccessListener(globalVoid -> {
                // Remove the pet object directly from the list
                if (position >= 0 && position < petList.size()) {
                    petList.remove(position);
                    notifyItemRemoved(position);
                    if (context != null) {
                        Toast.makeText(context, "Pet deleted successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("PetAdapter", "Context is null, cannot show toast.");
                    }
                }
            }).addOnFailureListener(globalError -> {
                Log.e("PetAdapter", "Failed to delete pet from global table: " + globalError.getMessage());
                if (context != null) {
                    Toast.makeText(context, "Failed to delete pet globally: " + globalError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }).addOnFailureListener(e -> {
            Log.e("PetAdapter", "Failed to delete pet: " + e.getMessage());
            if (context != null) {
                Toast.makeText(context, "Failed to delete pet: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
