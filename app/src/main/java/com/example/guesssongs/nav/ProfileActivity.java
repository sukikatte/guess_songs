package com.example.guesssongs.nav;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.guesssongs.R;
import com.example.guesssongs.log.LoginActivity;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {
    private Button logout;
    private ImageView paymentQrImage;
    private TextView welcomeMessageTextView;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;
    private BarcodeDetector barcodeDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference("user");
        Log.d("Firebase", "Firebase connected: " + (mDatabase != null));
        findViews();

        // Retrieve user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String userKey = sharedPreferences.getString("id", null); // Get the user ID

        Log.d("ProfileActivity", "User Key: " + userKey);  // Log user key for debugging

        if (userKey != null) {
            // Fetch username from Firebase Database using the user key
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(userKey);
            userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String username = dataSnapshot.getValue(String.class);
                    Log.d("ProfileActivity", "Fetched username: " + username);  // Log the fetched username

                    if (username != null) {
                        // Username successfully fetched
                        welcomeMessageTextView.setText("Welcome, " + username);
                    } else {
                        // Username is null
                        Log.d("ProfileActivity", "Username is null");
                        welcomeMessageTextView.setText("Welcome, Guest");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ProfileActivity.this, "Failed to load username.", Toast.LENGTH_SHORT).show();
                    Log.e("ProfileActivity", "Error fetching username: " + databaseError.getMessage());
                }
            });
        } else {
            // Handle case where user key is missing
            Toast.makeText(this, "User key is missing. Please log in again.", Toast.LENGTH_SHORT).show();
            Log.d("ProfileActivity", "User key is missing. Redirecting to Login.");
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        // Set up logout button
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();  // Log out and finish ProfileActivity
        });

        // Set up the Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_game) {
                openGameActivity();
                return true;
            } else if (itemId == R.id.nav_music) {
                openListActivity();
                return true;
            } else if (itemId == R.id.nav_my) {
                return true;  // Stay in ProfileActivity
            }
            return false;
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_my);

        // Set up QR code image
        paymentQrImage.setImageResource(R.drawable.payment_qr_image);  // Set your QR code image here
        paymentQrImage.setOnLongClickListener(v -> {
            Bitmap bitmap = ((BitmapDrawable) paymentQrImage.getDrawable()).getBitmap();
            saveImageToGallery(bitmap);
            return true; // Long press handled
        });
    }

    private void openGameActivity() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void openListActivity() {
        Intent intent = new Intent(ProfileActivity.this, MusicActivity.class);
        startActivity(intent);
    }

    private void findViews() {
        logout = findViewById(R.id.Logout);
        welcomeMessageTextView = findViewById(R.id.welcome_message);
        paymentQrImage = findViewById(R.id.payment_qr_image);
    }

    private void saveImageToGallery(Bitmap bitmap) {
        String savedImageURL = null;

        // Define the image file name and location
        String imageFileName = "QR_code_" + System.currentTimeMillis() + ".jpg";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyApp");

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File imageFile = new File(storageDir, imageFileName);
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            savedImageURL = imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Add the image to the MediaStore to make it appear in the gallery
        if (savedImageURL != null) {
            MediaScannerConnection.scanFile(this,
                    new String[]{savedImageURL}, null,
                    (path, uri) -> Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }


    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
}
