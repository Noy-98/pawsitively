package com.example.pawsitively;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.barcode.BarcodeScanner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;

public class ScanPet extends AppCompatActivity {

    private PreviewView previewView;
    private FloatingActionButton scanQrCodeFab;
    private FloatingActionButton backButton;

    private DatabaseReference databaseReference;
    private ExecutorService cameraExecutor;
    private boolean isScanning = false; // Prevent multiple scans simultaneously

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan_pet);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        previewView = findViewById(R.id.previewView);
        scanQrCodeFab = findViewById(R.id.scan_qr_code_fab);
        backButton = findViewById(R.id.go_back_bttn);

        databaseReference = FirebaseDatabase.getInstance().getReference("Lost&Found").child("Pets");
        cameraExecutor = Executors.newSingleThreadExecutor();

        backButton.setOnClickListener(view -> startActivity(new Intent(ScanPet.this, PetFoundDashboard.class)));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE
            );
        }

        scanQrCodeFab.setOnClickListener(v -> isScanning = true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // Bind the preview use case to the lifecycle
                Preview preview = new Preview.Builder().build();

                // Attach the preview to the PreviewView
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImage);

                // Bind to lifecycle
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,  // Attach the preview
                        imageAnalysis // Attach image analysis
                );

            } catch (Exception e) {
                Toast.makeText(this, "Failed to start camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("CameraError", "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void analyzeImage(ImageProxy image) {
        @SuppressLint("UnsafeOptInUsageError")
        android.media.Image mediaImage = image.getImage();
        if (mediaImage != null) {
            InputImage inputImage = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
            BarcodeScanner scanner = BarcodeScanning.getClient();
            scanner.process(inputImage)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String qrCodeValue = barcode.getRawValue();
                            if (isScanning && qrCodeValue != null) {
                                isScanning = false;
                                showQrCodeDialog(qrCodeValue);
                                break;
                            }
                        }
                        image.close();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "QR code scan failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        image.close();
                    });
        } else {
            image.close();
        }
    }

    private void showQrCodeDialog(String qrCodeValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("QR Code Scanned");
        builder.setMessage("Value: " + qrCodeValue);

        builder.setPositiveButton("View", (dialog, which) -> verifyQrCode(qrCodeValue));
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void verifyQrCode(String qrCodeValue) {
        databaseReference.orderByKey().equalTo(qrCodeValue).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Copy data to ScannedQRCode table
                    for (DataSnapshot petSnapshot : dataSnapshot.getChildren()) {
                        DatabaseReference scannedQrCodeRef = FirebaseDatabase.getInstance().getReference("ScannedList");
                        scannedQrCodeRef.child(petSnapshot.getKey()).setValue(petSnapshot.getValue())
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ScanPet.this, "Data copied to ScannedQRCode successfully!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(ScanPet.this, PetFoundDashboard.class);
                                        intent.putExtra("petId", qrCodeValue);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(ScanPet.this, "Failed to copy data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    // QR Code does not match
                    Toast.makeText(ScanPet.this, "QR Code not registered!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ScanPet.this, "Verification failed: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
