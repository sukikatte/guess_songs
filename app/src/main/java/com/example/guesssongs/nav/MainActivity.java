package com.example.guesssongs.nav;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.guesssongs.EndlessActivity;
import com.example.guesssongs.GameActivity;
import com.example.guesssongs.HistoryActivity;
import com.example.guesssongs.ManualActivity;
import com.example.guesssongs.MusicListActivity;
import com.example.guesssongs.R;
import com.example.guesssongs.competitiveMode.RoomStateActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private Button start_game, hardmode, manual, history, competitive, music_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        hideSystemUI();
        start_game.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });
        hardmode.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EndlessActivity.class);
            startActivity(intent);
        });
        manual.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ManualActivity.class);
            startActivity(intent);
        });
        history.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
        competitive.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RoomStateActivity.class);
            startActivity(intent);
        });

        // Set up the Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set a listener to handle item selection in the Bottom Navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_game) {// Stay in MainActivity or switch to Game Fragment if needed
                    return true;
                } else if (itemId == R.id.nav_music) {// Open the Music List screen
                    openMusicActivity();
                    return true;
                } else if (itemId == R.id.nav_my) {// Open the Profile Activity
                    openProfileActivity();
                    return true;
                }
                return false;
            }
        });

        // Default item selected
        bottomNavigationView.setSelectedItemId(R.id.nav_game);
    }

    // Method to open the Music List Activity
    private void openMusicActivity() {
        Intent intent = new Intent(MainActivity.this, MusicActivity.class);
        startActivity(intent);
    }

    private void openProfileActivity() {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void findViews() {
        start_game = findViewById(R.id.start_game);
        hardmode = findViewById(R.id.hardmode);
        manual = findViewById(R.id.manual);
        history = findViewById(R.id.history);
        competitive = findViewById(R.id.mc);
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