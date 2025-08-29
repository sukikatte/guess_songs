package com.example.guesssongs.competitiveMode;
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
import com.example.guesssongs.R;
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

import androidx.annotation.NonNull;


public class CompetitiveActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private TextView player1Name, player1Score, player2Name, player2Score, player3Name, player3Score;
    private TextView questionText, timeCountingTextView, progressText;
    private Button optionA, optionB, optionC, optionD;
    private List<Song> songs = new ArrayList<>();
    private final Handler handler = new Handler();
    private Random random;
    private Song currentSong;
    private int seconds = 30; // 30 second countdown timer
    private int score = 0;
    private int completedQuestions = 0;
    private int currentQuestion = 1;
    private boolean player1Answered = false, player2Answered = false, player3Answered = false;
    private DatabaseReference roomRef, songsRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference gameRef;
    DatabaseReference playersRef;

    private String roomId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competitive);

        // Initialize views
        findViews();

        // Get data from Intent
        roomId = getIntent().getStringExtra("roomId");
        userId = getIntent().getStringExtra("userId");


        Log.d("CompetitiveActivity", "Room ID: " + roomId);
        Log.d("CompetitiveActivity", "User ID: " + userId);

        // Initialize Firebase references
        roomRef = FirebaseDatabase.getInstance().getReference("room").child(roomId);
        songsRef = FirebaseDatabase.getInstance().getReference("songs");
        gameRef = FirebaseDatabase.getInstance().getReference("game").child(roomId);

        // Load songs from Firebase
        loadSongsFromFirebase();

        random = new Random();
        startTimer();

        // Set up option buttons
        setupOptionButtons();

        // Set up real-time listeners for player scores
        setupScoreListeners();
    }

    private void setupScoreListeners() {
        roomRef.child("players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot playersSnapshot) {
                // Update player names and scores in real-time
                updatePlayerInfo(playersSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("CompetitiveActivity", "Failed to read player data", databaseError.toException());
            }
        });

        // Listening to the total score changes for each player
        roomRef.child("players").child("player1").child("id").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String player1Id = snapshot.getValue(String.class);
                if (player1Id != null) {
                    roomRef.child("users").child(player1Id).child("totalscore").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Integer totalScore1 = dataSnapshot.getValue(Integer.class);
                            roomRef.child("players").child("player1").child("score").setValue(totalScore1);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("CompetitiveActivity", "Error reading total score for player1", error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("CompetitiveActivity", "Failed to retrieve player1 ID", databaseError.toException());
            }
        });

        roomRef.child("players").child("player2").child("id").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String player2Id = snapshot.getValue(String.class);
                if (player2Id != null) {
                    roomRef.child("users").child(player2Id).child("totalscore").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Integer totalScore2 = dataSnapshot.getValue(Integer.class);
                            roomRef.child("players").child("player2").child("score").setValue(totalScore2);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("CompetitiveActivity", "Error reading total score for player2", error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("CompetitiveActivity", "Failed to retrieve player2 ID", databaseError.toException());
            }
        });

        roomRef.child("players").child("player3").child("id").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String player3Id = snapshot.getValue(String.class);
                if (player3Id != null) {
                    roomRef.child("users").child(player3Id).child("totalscore").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Integer totalScore3 = dataSnapshot.getValue(Integer.class);
                            roomRef.child("players").child("player3").child("score").setValue(totalScore3);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("CompetitiveActivity", "Error reading total score for player3", error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("CompetitiveActivity", "Failed to retrieve player3 ID", databaseError.toException());
            }
        });
    }

    private void updatePlayerInfo(DataSnapshot playersSnapshot) {
        // Update player names and scores in real-time
        String player1NameValue = playersSnapshot.child("player1").child("username").getValue(String.class);
        Integer player1ScoreValue = playersSnapshot.child("player1").child("score").getValue(Integer.class);

        String player2NameValue = playersSnapshot.child("player2").child("username").getValue(String.class);
        Integer player2ScoreValue = playersSnapshot.child("player2").child("score").getValue(Integer.class);

        String player3NameValue = playersSnapshot.child("player3").child("username").getValue(String.class);
        Integer player3ScoreValue = playersSnapshot.child("player3").child("score").getValue(Integer.class);

        // Set player names and scores
        player1Name.setText("Player 1: " + (player1NameValue != null ? player1NameValue : "No Name"));
        player1Score.setText("Score: " + (player1ScoreValue != null ? player1ScoreValue : 0));

        player2Name.setText("Player 2: " + (player2NameValue != null ? player2NameValue : "No Name"));
        player2Score.setText("Score: " + (player2ScoreValue != null ? player2ScoreValue : 0));

        player3Name.setText("Player 3: " + (player3NameValue != null ? player3NameValue : "No Name"));
        player3Score.setText("Score: " + (player3ScoreValue != null ? player3ScoreValue : 0));
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

    private void setupOptionButtons() {
        optionA.setOnClickListener(v -> checkAnswer(optionA.getText().toString()));
        optionB.setOnClickListener(v -> checkAnswer(optionB.getText().toString()));
        optionC.setOnClickListener(v -> checkAnswer(optionC.getText().toString()));
        optionD.setOnClickListener(v -> checkAnswer(optionD.getText().toString()));
    }

    private void startTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                seconds--;
                timeCountingTextView.setText("Time remaining: " + seconds + "s");

                // If time is up or all players have answered, move to next question
                if (seconds <= 0 || allPlayersAnswered()) {
                    loadNextQuestion();  // Transition to the next question
                } else {
                    handler.postDelayed(this, 1000);  // Continue countdown
                }
            }
        }, 1000);
    }

    private void disableAnswerButtons() {
        // Disable all answer buttons after the player has answered
        optionA.setEnabled(false);
        optionB.setEnabled(false);
        optionC.setEnabled(false);
        optionD.setEnabled(false);
    }

    // Enable all buttons when moving to the next question
    private void enableAnswerButtons() {
        optionA.setEnabled(true);
        optionB.setEnabled(true);
        optionC.setEnabled(true);
        optionD.setEnabled(true);
    }

    private void checkAnswer(String selectedAnswer) {
        // Check if the player has already answered
        if (hasPlayerAnswered()) {
            // Show a toast message instead of updating button color
            Toast.makeText(this, "You have already answered, please wait for others.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine the correct answer based on the question type (title or artist)
        boolean isTitleQuestion = questionText.getText().toString().contains("title");
        String correctAnswer = isTitleQuestion ? currentSong.getTitle() : currentSong.getArtist();
        int points = 0;

        // Check if the selected answer is correct
        if (selectedAnswer.equals(correctAnswer)) {
            points = 10;  // Correct answer earns 10 points
            Toast.makeText(this, "Correct! You earned 10 points.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Incorrect answer.", Toast.LENGTH_SHORT).show();
        }

        // Update the score and mark player as answered
        markPlayerAsAnswered(points);

        // Disable all buttons after the player has answered
        disableAnswerButtons();

        // Immediately check if all players have answered, if true, load the next question
        if (allPlayersAnswered()) {
            loadNextQuestion();  // Transition to the next question
        }
    }

    private boolean hasPlayerAnswered() {
        // Check if the current player has already answered for the current question
        String playerNode = userId;  // Use userId to reference the current player
        String questionId = String.valueOf(currentQuestion);  // Use current question number as ID

        // Retrieve the "answered" status for the current player
        DatabaseReference answeredRef = roomRef.child("users").child(playerNode).child(questionId).child("answered");
        final boolean[] hasAnswered = {false};

        answeredRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean answered = snapshot.getValue(Boolean.class);
                hasAnswered[0] = answered != null && answered;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CompetitiveActivity", "Failed to check if player has answered", error.toException());
            }
        });

        return hasAnswered[0];  // Return whether the player has already answered
    }

    private boolean allPlayersAnswered() {
        // Check if all players have answered the current question
        if (player1Answered == true && player2Answered == true && player3Answered == true){
            return true;
        }
        return false;
    }

    private void markPlayerAsAnswered(int points) {
        String playerNode = userId;  // Use userId to reference the current player
        String questionId = String.valueOf(currentQuestion);  // Assume the question ID is the current question number

        // Mark the player as answered for this question
        roomRef.child("users").child(playerNode).child("questions").child(questionId).child("answered").setValue(true);
        // Mark the player as answered for this question
        roomRef.child("users").child(playerNode).child("questions").child(questionId).child("score").setValue(points);

        // Update the player's total score
        updateTotalScore(playerNode);

        // Update the local answered flag for the current player
        if (playerNode.equals("player1")) {
            player1Answered = true;
        } else if (playerNode.equals("player2")) {
            player2Answered = true;
        } else if (playerNode.equals("player3")) {
            player3Answered = true;
        }

        // Check if all players have answered the question, and move to the next question
        if (allPlayersAnswered()) {
            loadNextQuestion();  // Transition to the next question
        }
    }

    private void updateTotalScore(String playerNode) {
        DatabaseReference playerRef = roomRef.child("users").child(playerNode);
        playerRef.child("questions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalScore = 0;

                // Iterate through the player's answered questions and calculate the total score
                for (DataSnapshot questionSnapshot : dataSnapshot.getChildren()) {
                    Integer score = questionSnapshot.child("score").getValue(Integer.class);
                    if (score != null) {
                        totalScore += score;
                    }
                }

                // Update the player's total score in the database
                playerRef.child("totalscore").setValue(totalScore);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CompetitiveActivity", "Failed to calculate total score", error.toException());
            }
        });
    }

    private void resetTimer() {
        seconds = 30;
        timeCountingTextView.setText("Time remaining: " + seconds + "s");

        // Restart the timer
        handler.removeCallbacksAndMessages(null);
        startTimer();
    }

    private void loadNextQuestion() {
        if (completedQuestions >= 10) {
            navigateToSummary();
            return;
        }

        // Reset answers for all players
        player1Answered = player2Answered = player3Answered = false;
        resetTimer();

        // Reset the timer and update UI
        seconds = 30;
        timeCountingTextView.setText("Time remaining: " + seconds);
        completedQuestions++;
        progressText.setText(completedQuestions + "/10");

        if (songs.isEmpty()) {
            Log.e("GameActivity", "Song list is empty.");
            return;
        }

        currentSong = songs.get(random.nextInt(songs.size()));
        playSong(currentSong.getMusicUri());

        // Randomly select a question type (title or artist)
        boolean askForTitle = random.nextBoolean();
        String questionTextValue;
        String correctAnswer;

        if (askForTitle) {
            questionTextValue = "What is the title of this song?";
            correctAnswer = currentSong.getTitle();
        } else {
            questionTextValue = "What is the artist of this song?";
            correctAnswer = currentSong.getArtist();
        }

        questionText.setText(questionTextValue);
        replaceRandomOption(correctAnswer);

        // Update Firebase with current question details
        roomRef.child("currentQuestion").setValue(currentQuestion++);
        roomRef.child("currentSongId").setValue(currentSong.getId());
        roomRef.child("currentQuestionText").setValue(questionTextValue);
        roomRef.child("correctAnswer").setValue(correctAnswer);

        // Start countdown timer
        resetTimer();

        // Enable answer buttons for the next question
        enableAnswerButtons();
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

    // Code to navigate to summary screen after completing 10 questions
    private void navigateToSummary() {
        Intent intent = new Intent(CompetitiveActivity.this, SummaryCompetitiveActivity.class);
        intent.putExtra("roomId", roomId);  // Pass roomId to the summary screen
        startActivity(intent);
        finish();
    }

    private void findViews() {
        player1Name = findViewById(R.id.player1Name);
        player1Score = findViewById(R.id.player1Score);
        player2Name = findViewById(R.id.player2Name);
        player2Score = findViewById(R.id.player2Score);
        player3Name = findViewById(R.id.player3Name);
        player3Score = findViewById(R.id.player3Score);
        progressText = findViewById(R.id.progress_text);
        timeCountingTextView = findViewById(R.id.time_couting);
        questionText = findViewById(R.id.question_text);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
    }

}
