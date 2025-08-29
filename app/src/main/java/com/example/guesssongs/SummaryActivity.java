package com.example.guesssongs;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.guesssongs.nav.MainActivity;

public class SummaryActivity extends AppCompatActivity {

    private TextView summaryTextView;
    private Button returnToGameButton, returnToMainButton;
    private int totalScore; // Store the passed score

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        // Get the total score passed from the GameActivity
        totalScore = getIntent().getIntExtra("total_score", 0); // Default value is 0 if no score is passed

        findViews();
        displayScore();

        // Set the OnClickListener for the "Try Again" button
        returnToGameButton.setOnClickListener(v -> restartGame());

        // Set the OnClickListener for the "Return to Main" button
        returnToMainButton.setOnClickListener(v -> returnToMainPage());
    }

    private void displayScore() {
        // Display the total score of the current game session
        summaryTextView.setText("Your Total Score: " + totalScore);
    }

    private void restartGame() {
        // Navigate back to the GameActivity to restart the game
        Intent intent = new Intent(SummaryActivity.this, GameActivity.class);
        startActivity(intent);
        finish(); // Finish the current activity
    }

    private void returnToMainPage() {
        // Navigate back to the main activity (e.g., MainActivity)
        Intent intent = new Intent(SummaryActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Finish the current activity
    }

    private void findViews() {
        summaryTextView = findViewById(R.id.game_total_score);
        returnToGameButton = findViewById(R.id.returntoGame);
        returnToMainButton = findViewById(R.id.returntoMain);
    }
}
