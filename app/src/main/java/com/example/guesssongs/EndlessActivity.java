package com.example.guesssongs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.guesssongs.Class.Song;
import com.example.guesssongs.log.LoginActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EndlessActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private Button optionA, optionB, optionC, optionD;
    private TextView timeCountingTextView, questionText, scoreTotal, questionNumberTextView;
    private List<Song> songs = new ArrayList<>();
    private final Handler handler = new Handler();
    private Random random;
    private Song currentSong;
    private int remainingTime = 60; // 3 minutes in seconds
    private int score = 0;
    private int wrongAttempts;
    private String lastWrongOption = "";
    private int currentQuestionNumber = 1;

    private DatabaseReference userRef, scoreRef;

    // Firebase Database Reference for songs
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference songsRef = database.getReference("songs");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_endless);

        // 获取当前用户 ID
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String id = sharedPreferences.getString("id", null);
        if (id == null) {
            Toast.makeText(this, "User not logged in. Redirecting to login.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Firebase references
        userRef = FirebaseDatabase.getInstance().getReference("user").child(id);
        scoreRef = FirebaseDatabase.getInstance().getReference("scores").child(id);

        Log.d("EndlessActivity", "onCreate called");
        loadSongsFromFirebase();
        random = new Random();
        startTimer();
        findViews();
        setupOptionButtons();
        loadNextQuestion();
    }

    private void setupOptionButtons() {
        optionA.setOnClickListener(v -> checkAnswer(optionA.getText().toString()));
        optionB.setOnClickListener(v -> checkAnswer(optionB.getText().toString()));
        optionC.setOnClickListener(v -> checkAnswer(optionC.getText().toString()));
        optionD.setOnClickListener(v -> checkAnswer(optionD.getText().toString()));
    }

    private void loadSongsFromFirebase() {
        songsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                songs.clear();

                // Loop through the songs node and retrieve each song
                for (DataSnapshot songSnapshot : dataSnapshot.getChildren()) {
                    String title = songSnapshot.child("title").getValue(String.class);
                    String artist = songSnapshot.child("artist").getValue(String.class);
                    String musicUri = songSnapshot.child("musicUri").getValue(String.class);

                    if (title == null || artist == null || musicUri == null) {
                        continue; // Skip if any of the required fields are missing
                    }

                    // Create a Song object and set its ID to the key of the current song node
                    Song song = new Song(title, artist, musicUri, songSnapshot.getKey());

                    songs.add(song);
                }

                if (!songs.isEmpty()) {
                    loadNextQuestion();
                } else {
                    Log.e("GameActivity", "Song list is empty.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error reading songs data", databaseError.toException());
            }
        });
    }

    private void playSong(String audioUrl) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
        } catch (IOException e) {
            Log.e("EndlessActivity", "Error setting data source", e);
            Toast.makeText(this, "Cannot load the mp3", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void startTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (remainingTime > 0) {
                    remainingTime--;
                    int minutes = remainingTime / 60;
                    int seconds = remainingTime % 60;
                    timeCountingTextView.setText(String.format("Time Remaining: %02d:%02d", minutes, seconds));
                    handler.postDelayed(this, 1000);
                } else {
                    navigateToSummary();
                }
            }
        }, 1000);
    }

    private void checkAnswer(String selectedAnswer) {
        boolean isTitleQuestion = questionText.getText().toString().contains("title");
        String correctAnswer = isTitleQuestion ? currentSong.getTitle() : currentSong.getArtist();

        if (selectedAnswer.equals(correctAnswer)) {
            if (remainingTime >= 178) {
                score += 2;
                Toast.makeText(EndlessActivity.this, "Perfect! Plus two more points!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EndlessActivity.this, "You are Correct!", Toast.LENGTH_SHORT).show();
            }

            score += 10 - (wrongAttempts * 2);
            wrongAttempts = 0;
            updateScoreDisplay();
            loadNextQuestion();
        } else {
            if (!selectedAnswer.equals(lastWrongOption)) {
                Toast.makeText(EndlessActivity.this, "The answer is incorrect", Toast.LENGTH_SHORT).show();
                wrongAttempts++;
                score -= 2;
                updateScoreDisplay();
                lastWrongOption = selectedAnswer;
            } else {
                Toast.makeText(EndlessActivity.this, "You've already selected this option, try another one", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadNextQuestion() {
        if (songs.isEmpty()) {
            Log.e("EndlessActivity", "Song list is empty.");
            return;
        }

        currentSong = songs.get(random.nextInt(songs.size()));
        playSong(currentSong.getMusicUri());

        boolean askForTitle = random.nextBoolean();
        if (askForTitle) {
            questionText.setText("What is the title of this song?");
            replaceRandomOption(currentSong.getTitle());
        } else {
            questionText.setText("What is the singer of this song?");
            replaceRandomOption(currentSong.getArtist());
        }

        wrongAttempts = 0;
        lastWrongOption = "";
        questionNumberTextView.setText("Question: " + currentQuestionNumber);
        currentQuestionNumber++;
    }

    private void replaceRandomOption(String correctAnswer) {
        Button[] options = {optionA, optionB, optionC, optionD};
        int randomIndex = random.nextInt(options.length);
        options[randomIndex].setText(correctAnswer);

        List<String> wrongOptions = new ArrayList<>();
        for (Song song : songs) {
            String songTitle = song.getTitle();
            String songArtist = song.getArtist();
            if (!songTitle.equals(correctAnswer) && !songArtist.equals(correctAnswer)) {
                wrongOptions.add(songTitle);
                wrongOptions.add(songArtist);
            }
        }

        Collections.shuffle(wrongOptions);
        for (int i = 0; i < options.length; i++) {
            if (i != randomIndex) {
                options[i].setText(wrongOptions.isEmpty() ? "Option" : wrongOptions.remove(0));
            }
        }
    }

    private void updateScoreDisplay() {
        scoreTotal.setText("Total Score: " + score);
    }

    private void navigateToSummary() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String id = sharedPreferences.getString("id", null);

        if (id == null) {
            Toast.makeText(this, "User not logged in. Redirecting to login.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        scoreRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer bestScore = dataSnapshot.child("bestScore").getValue(Integer.class);

                if (bestScore == null || score > bestScore) {
                    scoreRef.child("bestScore").setValue(score).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(EndlessActivity.this, "New High Score!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                // Navigate to summary screen
                Intent intent = new Intent(EndlessActivity.this, SummaryActivity.class);
                intent.putExtra("total_score", score);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("EndlessActivity", "Failed to retrieve bestScore", databaseError.toException());
            }
        });
    }

    private void findViews() {
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        timeCountingTextView = findViewById(R.id.time_couting);
        questionText = findViewById(R.id.question_text);
        scoreTotal = findViewById(R.id.score_total);
        questionNumberTextView = findViewById(R.id.progress_text);
    }
}