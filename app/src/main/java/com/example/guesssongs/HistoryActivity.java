package com.example.guesssongs;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HistoryActivity extends AppCompatActivity {
    private TextView historyTextView;
    private DatabaseReference scoreRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyTextView = findViewById(R.id.history_text);
        scoreRef = FirebaseDatabase.getInstance().getReference("scores");

        loadScoreData();
    }

    private void loadScoreData() {
        scoreRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                StringBuilder historyData = new StringBuilder();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String username = userSnapshot.child("username").getValue(String.class);
                    Integer bestScore = userSnapshot.child("bestScore").getValue(Integer.class);

                    if (username != null) {
                        if (bestScore == null) {
                            historyData.append(username).append(": 该用户还未参加本模式游戏\n");
                        } else {
                            historyData.append(username).append(": Best Score = ").append(bestScore).append("\n");
                        }
                    }
                }

                historyTextView.setText(historyData.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HistoryActivity", "Failed to load score data", databaseError.toException());
            }
        });
    }
}
