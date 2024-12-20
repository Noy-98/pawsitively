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

public class LostPetAdapter extends RecyclerView.Adapter<LostPetAdapter.PetViewHolder> {

    private List<AddLostPet.Pets> petList;
    private Context context; // To display toast messages

    public LostPetAdapter(List<AddLostPet.Pets> petList, Context context) {
        this.petList = petList;
        this.context = context;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lost_pet, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        AddLostPet.Pets pet = petList.get(position);

        holder.petNameTextView.setText(pet.name);
        holder.petBreedTextView.setText("Breed: " + pet.breed);
        holder.petBirthdayTextView.setText("Birthday: " + pet.birthday);
        holder.petDateLostTextView.setText("Date Lost: " + pet.dateLost);
        holder.petDescriptionTextView.setText("Description: " + pet.description);
        holder.petGenderTextView.setText("Gender: " + pet.gender);
        holder.petTypeTextView.setText("Pet Type: " + pet.type);
        holder.tagTypeTextView.setText("Tag Type: " + pet.tagType);

        // Load pet image using Glide
        Glide.with(holder.petImageView.getContext())
                .load(pet.imageUrl) // imageUrl from the database
                .placeholder(R.drawable.default_pet_image) // Placeholder while loading
                .error(R.drawable.default_pet_image) // Error image if loading fails
                .into(holder.petImageView);

        Glide.with(holder.tagTypeView.getContext())
                .load(pet.vaccineUrl) // imageUrl from the database
                .placeholder(R.drawable.default_pet_image) // Placeholder while loading
                .error(R.drawable.default_pet_image) // Error image if loading fails
                .into(holder.tagTypeView);
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    public static class PetViewHolder extends RecyclerView.ViewHolder {
        TextView petNameTextView, petBreedTextView, petBirthdayTextView, petGenderTextView, petDateLostTextView, petDescriptionTextView, petTypeTextView, tagTypeTextView;
        CircleImageView petImageView; // Changed to CircleImageView
        ImageView tagTypeView;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            petNameTextView = itemView.findViewById(R.id.petNameTextView);
            petBreedTextView = itemView.findViewById(R.id.petBreedTextView);
            petBirthdayTextView = itemView.findViewById(R.id.petBirthdayTextView);
            petDateLostTextView = itemView.findViewById(R.id.petDateLostTextView);
            petDescriptionTextView = itemView.findViewById(R.id.petDescriptionTextView);
            petGenderTextView = itemView.findViewById(R.id.petGenderTextView);
            petTypeTextView = itemView.findViewById(R.id.petTypeTextView);
            tagTypeTextView = itemView.findViewById(R.id.tagTypeTextView);
            petImageView = itemView.findViewById(R.id.petImageView); // Ensure this matches the CircleImageView in the layout
            tagTypeView = itemView.findViewById(R.id.tagTypeView);
        }
    }
}
