package com.example.pawsitively;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    private List<ManagePets.Pet> petList;

    public PetAdapter(List<ManagePets.Pet> petList) {
        this.petList = petList;
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

        // Load pet image using Glide
        Glide.with(holder.petImageView.getContext())
                .load(pet.imageUrl) // imageUrl from the database
                .placeholder(R.drawable.default_pet_image) // Placeholder while loading
                .error(R.drawable.default_pet_image) // Error image if loading fails
                .into(holder.petImageView);
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    public static class PetViewHolder extends RecyclerView.ViewHolder {
        TextView petNameTextView, petBreedTextView, petBirthdayTextView, petGenderTextView;
        CircleImageView petImageView; // Changed to CircleImageView

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            petNameTextView = itemView.findViewById(R.id.petNameTextView);
            petBreedTextView = itemView.findViewById(R.id.petBreedTextView);
            petBirthdayTextView = itemView.findViewById(R.id.petBirthdayTextView);
            petGenderTextView = itemView.findViewById(R.id.petGenderTextView);
            petImageView = itemView.findViewById(R.id.petImageView); // Ensure this matches the CircleImageView in the layout
        }
    }
}
