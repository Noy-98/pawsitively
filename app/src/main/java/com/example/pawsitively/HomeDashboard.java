package com.example.pawsitively;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeDashboard extends AppCompatActivity {

    private CardView addPets, communityChat, lostFound;
    private static final String CHANNEL_ID = "pet_location_channel";
    private DatabaseReference petsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize Firebase Database reference
        petsRef = FirebaseDatabase.getInstance().getReference("Pets");


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Redirect to login activity if no user is logged in
            startActivity(new Intent(HomeDashboard.this, Login.class));
            finish();
            return;
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home) {
                    return true;
                } else if (item.getItemId() == R.id.maps) {
                    startActivity(new Intent(getApplicationContext(), MapsDashboard.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                } else if (item.getItemId() == R.id.notification) {
                    return true;
                } else if (item.getItemId() == R.id.settings) {
                    startActivity(new Intent(getApplicationContext(), SettingsDashboard.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                }
                return false;
            }
        });

        addPets = findViewById(R.id.addPets);
        communityChat = findViewById(R.id.communityChat);
        lostFound = findViewById(R.id.lostAndFound);

        addPets.setOnClickListener(view -> startActivity(new Intent(HomeDashboard.this, Mainpage.class)));
        communityChat.setOnClickListener(view -> startActivity(new Intent(HomeDashboard.this, CommunityChatActivity.class)));
        lostFound.setOnClickListener(view -> startActivity(new Intent(HomeDashboard.this, PetFoundDashboard.class)));

        verifyAndListenForPetLocation(bottomNavigationView);
        createNotificationChannel();
    }

    private void verifyAndListenForPetLocation(BottomNavigationView bottomNavigationView) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String currentUserId = currentUser.getUid();
        DatabaseReference userPetsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(currentUserId).child("pets");

        userPetsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot petSnapshot : dataSnapshot.getChildren()) {
                        String petId = petSnapshot.child("petId").getValue(String.class);
                        if (petId != null) {
                            listenForPetLocation(petId, bottomNavigationView);
                        }
                    }
                } else {
                    Toast.makeText(HomeDashboard.this, "No pets linked to this account.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeDashboard.this, "Failed to verify pet ownership: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Pet Location Alerts";
            String description = "Notifications for pet location updates";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void listenForPetLocation(String petId, BottomNavigationView bottomNavigationView) {
        petsRef.child(petId).child("gpsTrack").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    String location = snapshot.getValue(String.class);
                    updateNotificationIconColor(bottomNavigationView, true);
                    sendNotification("Your Pet is in their Location", location);
                } else {
                    updateNotificationIconColor(bottomNavigationView, false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeDashboard.this, "Failed to check pet location: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNotificationIconColor(BottomNavigationView bottomNavigationView, boolean isActive) {
        MenuItem notificationItem = bottomNavigationView.getMenu().findItem(R.id.notification);
        if (isActive) {
            notificationItem.setIcon(R.drawable.baseline_notifications_active_24); // Use a red icon
            notificationItem.getIcon().setTint(ContextCompat.getColor(this, R.color.red));
        } else {
            notificationItem.setIcon(R.drawable.baseline_notifications_24); // Use a default icon
            notificationItem.getIcon().setTint(ContextCompat.getColor(this, R.color.white));
        }

        bottomNavigationView.invalidate();
    }

    private void sendNotification(String title, String location) {
        Intent intent = new Intent(this, MapsDashboard.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE // Ensures compatibility for API level 31 and above
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_notifications_active_24)
                .setContentTitle(title)
                .setContentText(location)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }


}