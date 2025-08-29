package com.example.guesssongs.competitiveMode;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.guesssongs.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                RoomActivity extends AppCompatActivity {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mRoomRef;
    private String roomId;
    private TextView player1, player2, player3;
    private Button startGameButton, leaveRoomButton;

    private ValueEventListener roomListener; // To listen for real-time updates on the room

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        mDatabase = FirebaseDatabase.getInstance();
        roomId = getIntent().getStringExtra("roomId");
        mRoomRef = mDatabase.getReference("room").child(roomId);

        findViews();

        // Start listening to room changes
        startRoomListener();

        leaveRoomButton.setOnClickListener(v -> onLeaveRoomClick());
    }

    private void findViews() {
        player1 = findViewById(R.id.player1);
        player2 = findViewById(R.id.player2);
        player3 = findViewById(R.id.player3);
        startGameButton = findViewById(R.id.startGameButton);
        leaveRoomButton = findViewById(R.id.leaveRoomButton);
    }

    private void startRoomListener() {
        roomListener = mRoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 获取玩家信息
                String player1Name = dataSnapshot.child("players/player1/username").getValue(String.class);
                String player2Name = dataSnapshot.child("players/player2/username").getValue(String.class);
                String player3Name = dataSnapshot.child("players/player3/username").getValue(String.class);

                // 更新 UI
                player1.setText("Player 1: " + (player1Name != null ? player1Name : "Waiting..."));
                player2.setText("Player 2: " + (player2Name != null ? player2Name : "Waiting..."));
                player3.setText("Player 3: " + (player3Name != null ? player3Name : "Waiting..."));

                // 检查房间是否已满
                if (player1Name != null && player2Name != null && player3Name != null) {
                    Log.d("RoomNewActivity", "All players are ready. Starting the game!");
                    mRoomRef.removeEventListener(roomListener);
                    // 跳转到 CompetitiveActivity
                    new Handler(getMainLooper()).postDelayed(() -> {
                        Intent intent = new Intent(RoomActivity.this, CompetitiveActivity.class);
                        intent.putExtra("roomId", roomId); // 传递房间ID
                        intent.putExtra("userId", getUserIdFromSession()); // 传递用户ID
                        startActivity(intent);
                        finish();
                    }, 3000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RoomActivity.this, "Failed to load room data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getUserIdFromSession() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        return sharedPreferences.getString("id", null);
    }



    private void onLeaveRoomClick() {
        // Get the current user name from SharedPreferences (assuming it was stored there)
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("id", null);

        if (currentUserId == null) {
            Toast.makeText(this, "No user found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Remove the current player from the room's player slots by setting values to null
        removePlayerFromRoom(currentUserId);
    }

    private void removePlayerFromRoom(String currentUserId) {
        // First, remove the player from the room
        mRoomRef.child("player1").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String player1Id = dataSnapshot.getValue(String.class);
                if (player1Id != null && player1Id.equals(currentUserId)) {
                    mRoomRef.child("player1").setValue(null); // Set to null instead of removing
                    mRoomRef.child("player1name").setValue(null); // Set name to null
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

        mRoomRef.child("player2").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String player2Id = dataSnapshot.getValue(String.class);
                if (player2Id != null && player2Id.equals(currentUserId)) {
                    mRoomRef.child("player2").setValue(null); // Set to null instead of removing
                    mRoomRef.child("player2name").setValue(null); // Set name to null
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

        mRoomRef.child("player3").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String player3Id = dataSnapshot.getValue(String.class);
                if (player3Id != null && player3Id.equals(currentUserId)) {
                    mRoomRef.child("player3").setValue(null); // Set to null instead of removing
                    mRoomRef.child("player3name").setValue(null); // Set name to null
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

        // After removing the player, check if the room is empty
        mRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String player1Id = dataSnapshot.child("player1").getValue(String.class);
                String player2Id = dataSnapshot.child("player2").getValue(String.class);
                String player3Id = dataSnapshot.child("player3").getValue(String.class);

                // If all players are null, delete the room
                if (player1Id == null && player2Id == null && player3Id == null) {
                    mRoomRef.removeValue();
                    Toast.makeText(RoomActivity.this, "Room deleted as all players left.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

        // Finish the current activity (exit room)
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the real-time listener when the activity is destroyed
        if (roomListener != null) {
            mRoomRef.removeEventListener(roomListener);
        }
    }
}



