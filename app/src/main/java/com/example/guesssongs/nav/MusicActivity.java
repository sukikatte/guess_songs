package com.example.guesssongs.nav;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.guesssongs.MusicListActivity;
import com.example.guesssongs.R;
import com.example.guesssongs.UploadSongActivity;
import com.example.guesssongs.log.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MusicActivity extends AppCompatActivity {
    private Button music_list, uploadSongButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        findViews();
        hideSystemUI();
        music_list.setOnClickListener(v -> {
            Intent intent = new Intent(MusicActivity.this, MusicListActivity.class);
            startActivity(intent);
        });
        uploadSongButton = findViewById(R.id.upload_song_button);
        uploadSongButton.setOnClickListener(v -> {
            Intent intent = new Intent(MusicActivity.this, UploadSongActivity.class);
            startActivity(intent);
        });


        // Set up the Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set a listener to handle item selection in the Bottom Navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_game) {
                    openGameActivity();  // Go to Game Activity
                    return true;
                } else if (itemId == R.id.nav_music) {
                    return true; // Stay in MusicActivity
                } else if (itemId == R.id.nav_my) {
                    openProfileActivity();
                    return true;
                }
                return false;
            }
        });

        // Default item selected
        bottomNavigationView.setSelectedItemId(R.id.nav_music);  // Set the default selected item to 'Music'
    }

    private void openGameActivity() {
        Intent intent = new Intent(MusicActivity.this, MainActivity.class);  // Or GameActivity.class if needed
        startActivity(intent);
    }

    private void openProfileActivity() {
        Intent intent = new Intent(MusicActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void findViews() {
        music_list = findViewById(R.id.music_list);
    }

    private void hideSystemUI() {
        // 设置系统UI标志
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION      // 隐藏导航栏
                        | View.SYSTEM_UI_FLAG_FULLSCREEN         // 隐藏状态栏
        );
    }

    // 保持隐藏系统UI
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
}
