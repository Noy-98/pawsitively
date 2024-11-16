package com.example.pawsitively;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MapsDashboard extends AppCompatActivity implements OnMapReadyCallback {

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private MapView mapView;
    private GoogleMap googleMap;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_maps_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.maps);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home) {
                    startActivity(new Intent(getApplicationContext(), HomeDashboard.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                } else if (item.getItemId() == R.id.maps) {
                    return true;
                } else if (item.getItemId() == R.id.notification) {
                    startActivity(new Intent(getApplicationContext(), NotificationDashboard.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
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

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize MapView
        mapView = findViewById(R.id.maps);
        Bundle mapViewBundle = savedInstanceState != null ? savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY) : null;
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        fetchAndVerifyPetLocations();
    }

    private void fetchAndVerifyPetLocations() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Fetch pets of the current user
            databaseReference.child("users").child(userId).child("pets").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot userPetsSnapshot) {
                    if (!userPetsSnapshot.exists()) {
                        showToast("No pets found for the current user.");
                        return;
                    }

                    // Check each petId in the user's pets
                    for (DataSnapshot petSnapshot : userPetsSnapshot.getChildren()) {
                        String petId = petSnapshot.getKey();
                        verifyAndPinPetLocation(petId);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showToast("Failed to fetch user's pets: " + error.getMessage());
                }
            });
        } else {
            showToast("No authenticated user found.");
        }
    }

    private void verifyAndPinPetLocation(String petId) {
        // Verify if petId exists in petLocation
        databaseReference.child("petLocation").child(petId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot petLocationSnapshot) {
                if (petLocationSnapshot.exists()) {
                    String locTracker = petLocationSnapshot.child("locTracker").getValue(String.class);

                    if (locTracker != null) {
                        LatLng coordinates = extractCoordinatesFromUrl(locTracker);

                        if (coordinates != null) {
                            // Pin the location on the map
                            googleMap.addMarker(new MarkerOptions()
                                    .position(coordinates)
                                    .title("Pet Location: " + petId));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15));
                        } else {
                            showToast("Invalid location link for petId: " + petId);
                        }
                    } else {
                        showToast("No location tracker found for petId: " + petId);
                    }
                } else {
                    showToast("Pet location not found for petId: " + petId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to fetch location: " + error.getMessage());
            }
        });
    }

    private LatLng extractCoordinatesFromUrl(String locTrackerUrl) {
        try {
            if (locTrackerUrl == null || locTrackerUrl.isEmpty()) {
                return null;
            }

            // Check if it's a Google Maps redirect URL
            if (locTrackerUrl.contains("https://maps.app.goo.gl/")) {
                // Open a connection to follow the redirect
                java.net.URL url = new java.net.URL(locTrackerUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(false);  // Disable automatic redirection
                connection.connect();

                // Get the redirect location (it will be the real Google Maps link)
                String redirectUrl = connection.getHeaderField("Location");
                connection.disconnect();

                // If the redirection URL is valid, extract the coordinates from the final URL
                if (redirectUrl != null && redirectUrl.contains("https://www.google.com/maps/place/")) {
                    String[] parts = redirectUrl.split("q=");
                    if (parts.length > 1) {
                        String[] coords = parts[1].split(",");
                        if (coords.length >= 2) {
                            double latitude = Double.parseDouble(coords[0]);
                            double longitude = Double.parseDouble(coords[1]);
                            return new LatLng(latitude, longitude);
                        }
                    }
                }
            } else {
                // If not a redirect URL, handle directly (e.g., Google Maps with 'q=latitude,longitude')
                String[] urlParts = locTrackerUrl.split("q=");
                if (urlParts.length > 1) {
                    String[] coords = urlParts[1].split(",");
                    if (coords.length >= 2) {
                        double latitude = Double.parseDouble(coords[0]);
                        double longitude = Double.parseDouble(coords[1]);
                        return new LatLng(latitude, longitude);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}