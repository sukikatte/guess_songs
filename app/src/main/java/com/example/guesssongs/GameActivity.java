package com.example.guesssongs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
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
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private Button optionA, optionB, optionC, optionD, nextButton;
    private TextView timeCountingTextView, questionText, scoreTotal, progressText;
    private List<Song> songs = new ArrayList<>();
    private final Handler handler = new Handler();
    private Random random;
    private Song currentSong;
    private int seconds = 0;
    private int score = 0;
    private int completedQuestions = 0;
    private int currentQuestion = 1;
    private int wrongAttempts;
    private String lastWrongOption = "";

    // Firebase Database Reference for songs
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference songsRef = database.getReference("songs");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_classic);
        // Replace with actual user ID
        String userId = "userId";

        //initial game state
        completedQuestions = 0;
        score = 0;
        seconds = 0;
        Log.d("GameActivity", "onCreate called");
        loadSongsFromFirebase();
        random = new Random();
        startTimer();
        findViews();
        setupOptionButtons();
        loadNextQuestion();
        nextButton.setOnClickListener(v -> loadNextQuestion());
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
            //user URL from Firebase Database
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> mp.start());
        } catch (IOException e) {
            Log.e("GameActivity", "Error setting data source", e);
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

    @SuppressLint("SetTextI18n")
    private void startTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                seconds++;
                timeCountingTextView.setText("time counting: " + seconds + " s");
                //refresh per second
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void updateScoreDisplay() {
        scoreTotal.setText("Total Score: " + score);
    }

    private void updateProgress() {
        progressText.setText(currentQuestion + "/" + "10");
    }

    private void checkAnswer(String selectedAnswer) {
        // Determine the correct answer
        boolean isTitleQuestion = questionText.getText().toString().contains("title");
        String correctAnswer = isTitleQuestion ? currentSong.getTitle() : currentSong.getArtist();
        // Check if the selected answer is correct
        if (selectedAnswer.equals(correctAnswer)) {
            completedQuestions++;
            Log.d("GameActivity", "Completed Questions: " + completedQuestions);
            if (seconds < 2) {
                score += 2;
                Toast.makeText(GameActivity.this, "Perfect! Plus two more points!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(GameActivity.this, "You are Correct!", Toast.LENGTH_SHORT).show();
            }
            // Update score based on wrong attempts
            score += 10 - (wrongAttempts * 2);
            // Reset wrong attempts count
            wrongAttempts = 0;
            updateScoreDisplay();

            if (currentQuestion == 10) {
                navigateToSummary();
            } else {
                // Load the next question automatically
                handler.postDelayed(this::loadNextQuestion, 1000);
                currentQuestion++;
                updateProgress();
            }
        } else {
            // If the user selects the same wrong option, don't deduct the score again
            if (!selectedAnswer.equals(lastWrongOption)) {
                Toast.makeText(GameActivity.this, "The answer is incorrect", Toast.LENGTH_SHORT).show();
                wrongAttempts++;
                score -= 2;
                updateScoreDisplay();
                lastWrongOption = selectedAnswer;
            } else {
                // If the same wrong option is selected again, show a brief message
                Toast.makeText(GameActivity.this, "You've already selected this option, try another one", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void loadNextQuestion() {
        if (songs.isEmpty()) {
            Log.e("GameActivity", "Song list is empty.");
            return;
        }
        // Check if 10 questions have been completed
        if (completedQuestions >= 10) {
            navigateToSummary();
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

        // Reset the timer and prepare for the next question
        seconds = 0;
        wrongAttempts = 0;
        lastWrongOption = "";
        timeCountingTextView.setText("time counting: " + seconds + " s");
        handler.removeCallbacksAndMessages(null);
        startTimer();
    }


    private void replaceRandomOption(String correctAnswer) {
        if (correctAnswer == null || correctAnswer.isEmpty()) {
            // 如果 correctAnswer 为 null 或空字符串，则返回，避免继续处理
            Log.e("GameActivity", "Correct answer is null or empty.");
            return;
        }

        // Randomly select one of the options to replace with the correct answer
        Button[] options = {optionA, optionB, optionC, optionD};
        int randomIndex = random.nextInt(options.length);

        // Replace the selected option with the correct answer
        options[randomIndex].setText(correctAnswer);

        // Create a temporary list to store wrong options
        List<String> wrongOptions = new ArrayList<>();
        // Populate the wrongOptions list with other song titles and artists
        for (Song song : songs) {
            if (song != null) {  // Ensure song is not null
                String songTitle = song.getTitle();
                String songArtist = song.getArtist();
                if (songTitle != null && songArtist != null && !songTitle.equals(correctAnswer) && !songArtist.equals(correctAnswer)) {
                    wrongOptions.add(songTitle);
                    wrongOptions.add(songArtist);
                }
            }
        }

        // Shuffle the wrong options to randomize selection
        Collections.shuffle(wrongOptions);

        // Set other options as wrong answers (up to 3 options)
        for (int i = 0; i < options.length; i++) {
            if (i != randomIndex) {
                if (wrongOptions.isEmpty()) {
                    options[i].setText("Option " + (char) ('A' + i));
                } else {
                    options[i].setText(wrongOptions.remove(0));
                }
            }
        }
    }


    private void navigateToSummary() {
        Intent intent = new Intent(GameActivity.this, SummaryActivity.class);
        intent.putExtra("total_score", score);
        startActivity(intent);
        finish();
    }

    private void findViews() {
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        nextButton = findViewById(R.id.next_button);
        timeCountingTextView = findViewById(R.id.time_couting);
        questionText = findViewById(R.id.question_text);
        scoreTotal = findViewById(R.id.score_total);
        progressText = findViewById(R.id.progress_text);
    }
}








