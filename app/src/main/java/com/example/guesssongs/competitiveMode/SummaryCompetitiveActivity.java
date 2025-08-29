package com.example.guesssongs.competitiveMode;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.guesssongs.nav.MainActivity;
import com.example.guesssongs.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SummaryCompetitiveActivity extends AppCompatActivity {

    private TextView player1Name, player1Score, player2Name, player2Score, player3Name, player3Score;
    private TextView questionText, timeCountingTextView, progressText;

    private String roomId;
    private DatabaseReference roomRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_competitive);

        findViews();

        // Get the room ID from the Intent
        roomId = getIntent().getStringExtra("roomId");

        if (roomId == null) {
            Toast.makeText(this, "Room ID is missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase reference
        roomRef = FirebaseDatabase.getInstance().getReference("room").child(roomId);

        // Get player info and display
        loadPlayerInfo();
    }

    private void findViews(){
        player1Name = findViewById(R.id.player1Name);
        player1Score = findViewById(R.id.player1Score);
        player2Name = findViewById(R.id.player2Name);
        player2Score = findViewById(R.id.player2Score);
        player3Name = findViewById(R.id.player3Name);
        player3Score = findViewById(R.id.player3Score);
        questionText = findViewById(R.id.question_text);
    }

    private void loadPlayerInfo() {
        roomRef.child("players").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Player> players = new ArrayList<>();

                // Player 1
                String player1NameValue = dataSnapshot.child("player1").child("username").getValue(String.class);
                Integer player1ScoreValue = dataSnapshot.child("player1").child("score").getValue(Integer.class);
                players.add(new Player(player1NameValue, player1ScoreValue));

                // Player 2
                String player2NameValue = dataSnapshot.child("player2").child("username").getValue(String.class);
                Integer player2ScoreValue = dataSnapshot.child("player2").child("score").getValue(Integer.class);
                players.add(new Player(player2NameValue, player2ScoreValue));

                // Player 3
                String player3NameValue = dataSnapshot.child("player3").child("username").getValue(String.class);
                Integer player3ScoreValue = dataSnapshot.child("player3").child("score").getValue(Integer.class);
                players.add(new Player(player3NameValue, player3ScoreValue));

                // Sort players by score in descending order
                Collections.sort(players, new Comparator<Player>() {
                    @Override
                    public int compare(Player p1, Player p2) {
                        return p2.getScore().compareTo(p1.getScore());
                    }
                });

                // Set player info on UI
                Player p1 = players.get(0);
                player1Name.setText(p1.getName());
                player1Score.setText("Score: " + p1.getScore());

                Player p2 = players.get(1);
                player2Name.setText(p2.getName());
                player2Score.setText("Score: " + p2.getScore());

                Player p3 = players.get(2);
                player3Name.setText(p3.getName());
                player3Score.setText("Score: " + p3.getScore());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SummaryCompetitiveActivity.this, "Failed to load player data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class Player {
        private String name;
        private Integer score;

        public Player(String name, Integer score) {
            this.name = name;
            this.score = score != null ? score : 0;
        }

        public String getName() {
            return name;
        }

        public Integer getScore() {
            return score;
        }
    }

    // Handle Restart Game button click
    public void onRestartGameClick() {
        // Restart the game, resetting everything
        Intent intent = new Intent(SummaryCompetitiveActivity.this, RoomActivity.class);
        intent.putExtra("roomId", roomId);
        startActivity(intent);
        finish();
    }

    // Handle Return to Menu button click
    public void onReturnToMenuClick() {
        // Return to the main menu
        Intent intent = new Intent(SummaryCompetitiveActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    
}
