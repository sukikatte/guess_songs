package com.example.guesssongs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.guesssongs.R;
import com.example.guesssongs.nav.MusicActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UploadSongActivity extends AppCompatActivity {

    private EditText songTitleEditText, songArtistEditText, musicUrlEditText;
    private Button uploadSongButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_song);

        // Initialize the views
        songTitleEditText = findViewById(R.id.song_title);
        songArtistEditText = findViewById(R.id.song_artist);
        musicUrlEditText = findViewById(R.id.music_url);
        uploadSongButton = findViewById(R.id.upload_song_button);

        // Set up the upload button click listener
        uploadSongButton.setOnClickListener(v -> uploadSong());
    }

    private void uploadSong() {
        String title = songTitleEditText.getText().toString().trim();
        String artist = songArtistEditText.getText().toString().trim();
        String musicUrl = musicUrlEditText.getText().toString().trim();

        // Validate input
        if (title.isEmpty() || artist.isEmpty() || musicUrl.isEmpty()) {
            Toast.makeText(UploadSongActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get reference to the songs node in the database
        DatabaseReference songsRef = FirebaseDatabase.getInstance().getReference("songs");

        // Create a unique ID for each song
        String songId = songsRef.push().getKey();

        // Create a Song object to store in the database
        Song song = new Song(title, artist, musicUrl);

        // Upload the song data
        if (songId != null) {
            songsRef.child(songId).setValue(song).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(UploadSongActivity.this, "Song uploaded successfully", Toast.LENGTH_SHORT).show();
                    // Optionally navigate back to MusicActivity
                    Intent intent = new Intent(UploadSongActivity.this, MusicActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(UploadSongActivity.this, "Failed to upload song", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Song model class
    public static class Song {
        public String title;
        public String artist;
        public String musicUri;

        public Song(String title, String artist, String musicUri) {
            this.title = title;
            this.artist = artist;
            this.musicUri = musicUri;
        }
    }
}
