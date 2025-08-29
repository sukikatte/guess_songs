package com.example.guesssongs;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.guesssongs.Class.Song;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicListActivity extends AppCompatActivity {

    private ListView musicListView;
    private EditText searchEditText;
    private Button searchButton;
    private MediaPlayer mediaPlayer;
    private List<Song> songs = new ArrayList<>();
    private List<Song> filteredSongs = new ArrayList<>();
    private List<String> songTitles;

    // Firebase database reference
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference songsRef = database.getReference("songs");

    // UI elements for bottom player
    private TextView musicPlayerSongName;
    private TextView musicPlayerArtistName;
    private ImageView musicPlayerPlayPause;
    private ImageView musicPlayerPrevious;
    private ImageView musicPlayerNext;

    private int currentSongIndex = -1;  // Keep track of the current song index

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activtiy_music_list);

        // Initialize UI elements
        musicListView = findViewById(R.id.music_info);
        searchEditText = findViewById(R.id.search_edit_text);
        searchButton = findViewById(R.id.search_button);

        // Bottom player UI elements
        musicPlayerSongName = findViewById(R.id.musicPlayer_songName);
        musicPlayerArtistName = findViewById(R.id.musicPlayer_artistName);
        musicPlayerPlayPause = findViewById(R.id.musicPlayer_playPause);
        musicPlayerPrevious = findViewById(R.id.musicPlayer_previous);
        musicPlayerNext = findViewById(R.id.musicPlayer_next);

        songTitles = new ArrayList<>();

        loadSongsFromFirebase();

        // Handle song click in the list
        musicListView.setOnItemClickListener((parent, view, position, id) -> {
            // Use the filteredSongs list for item clicks
            Song selectedSong = filteredSongs.get(position);
            String musicUri = selectedSong.getMusicUri();
            if (musicUri != null && !musicUri.isEmpty()) {
                playSong(musicUri, selectedSong, position);  // Pass the song and its index to play
            } else {
                Toast.makeText(MusicListActivity.this, "Invalid song URI", Toast.LENGTH_SHORT).show();
            }
        });

        // Search button click listener
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().toLowerCase();
            filterSongs(query);  // Trigger search when search button is clicked
        });

        // Handle "Done" or "Enter" key press on the keyboard
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchEditText.getText().toString().toLowerCase();
                filterSongs(query);  // Trigger search on "Done" or "Enter"
                return true;
            }
            return false;
        });

        // Play/Pause button listener
        musicPlayerPlayPause.setOnClickListener(v -> togglePlayPause());

        // Previous button listener
        musicPlayerPrevious.setOnClickListener(v -> playPreviousSong());

        // Next button listener
        musicPlayerNext.setOnClickListener(v -> playNextSong());
    }

    private void loadSongsFromFirebase() {
        songsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                songs.clear();
                songTitles.clear();

                for (DataSnapshot songSnapshot : dataSnapshot.getChildren()) {
                    String title = songSnapshot.child("title").getValue(String.class);
                    String artist = songSnapshot.child("artist").getValue(String.class);
                    String musicUri = songSnapshot.child("musicUri").getValue(String.class);
                    if (title == null || artist == null || musicUri == null) {
                        continue;
                    }

                    Song song = new Song(title, artist, musicUri, songSnapshot.getKey());
                    songs.add(song);
                    songTitles.add(title + " - " + artist);
                }

                filteredSongs.addAll(songs);
                updateListView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MusicListActivity.this, "Failed to load songs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterSongs(String query) {
        filteredSongs.clear();
        for (Song song : songs) {
            if (song.getTitle().toLowerCase().contains(query) || song.getArtist().toLowerCase().contains(query)) {
                filteredSongs.add(song);
            }
        }

        updateListView();
    }

    private void updateListView() {
        List<String> filteredTitles = new ArrayList<>();
        for (Song song : filteredSongs) {
            filteredTitles.add(song.getTitle() + " - " + song.getArtist());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(MusicListActivity.this, android.R.layout.simple_list_item_1, filteredTitles);
        musicListView.setAdapter(adapter);
    }

    private void playSong(String audioUrl, Song song, int index) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                currentSongIndex = index;  // Update the current song index
                updateBottomPlayer(song);
            });
        } catch (IOException e) {
            Log.e("MusicListActivity", "Error setting data source", e);
            Toast.makeText(this, "Cannot load the mp3", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBottomPlayer(Song song) {
        musicPlayerSongName.setText(song.getTitle());
        musicPlayerArtistName.setText(song.getArtist());
        musicPlayerPlayPause.setImageResource(R.drawable.pause);  // Change icon to pause
    }

    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                musicPlayerPlayPause.setImageResource(R.drawable.play);  // Change icon to play
            } else {
                mediaPlayer.start();
                musicPlayerPlayPause.setImageResource(R.drawable.pause);  // Change icon to pause
            }
        }
    }

    private void playPreviousSong() {
        if (currentSongIndex > 0) {
            currentSongIndex--;
            Song previousSong = filteredSongs.get(currentSongIndex);
            playSong(previousSong.getMusicUri(), previousSong, currentSongIndex);
        }
    }

    private void playNextSong() {
        if (currentSongIndex < filteredSongs.size() - 1) {
            currentSongIndex++;
            Song nextSong = filteredSongs.get(currentSongIndex);
            playSong(nextSong.getMusicUri(), nextSong, currentSongIndex);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}

