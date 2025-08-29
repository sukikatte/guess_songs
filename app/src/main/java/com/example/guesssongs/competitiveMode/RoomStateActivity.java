package com.example.guesssongs.competitiveMode;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.guesssongs.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RoomStateActivity extends AppCompatActivity {
    private EditText roomNameEditText, roomPasswordEditText;
    private Button createRoomButton, joinRoomButton;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRoomRef, mUserRef;
    private String currentUserId, currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_state);

        // Firebase references
        mDatabase = FirebaseDatabase.getInstance();
        mRoomRef = mDatabase.getReference("room");
        mUserRef = mDatabase.getReference("user");

        // Find views
        findViews();

        // Retrieve current user session data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sharedPreferences.getString("id", null);
        currentUserName = sharedPreferences.getString("username", null);

        // Ensure user is logged in
        if (currentUserId == null || currentUserName == null) {
            Toast.makeText(this, "You need to be logged in to create or join a room", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if user is not logged in
        }

        // Create room button
        createRoomButton.setOnClickListener(v -> {
            String roomName = roomNameEditText.getText().toString().trim();
            String roomPassword = roomPasswordEditText.getText().toString().trim();

            if (roomName.isEmpty() || roomPassword.isEmpty()) {
                Toast.makeText(this, "Please enter room name and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if room already exists
            mRoomRef.orderByChild("roomname").equalTo(roomName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Toast.makeText(RoomStateActivity.this, "Room already exists, try a different name.", Toast.LENGTH_SHORT).show();
                    } else {
                        createRoom(roomName, roomPassword);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(RoomStateActivity.this, "Error checking room existence.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Join room button
        joinRoomButton.setOnClickListener(v -> {
            String roomName = roomNameEditText.getText().toString().trim();
            String roomPassword = roomPasswordEditText.getText().toString().trim();

            if (roomName.isEmpty() || roomPassword.isEmpty()) {
                Toast.makeText(this, "Please enter room name and password", Toast.LENGTH_SHORT).show();
                return;
            }

            joinRoom(roomName, roomPassword);
        });
    }

    private void createRoom(String roomName, String roomPassword) {
        mRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int newRoomId = (int) dataSnapshot.getChildrenCount() + 1;
                String roomId = String.valueOf(newRoomId);

                // Initialize room data structure
                Map<String, Object> roomData = new HashMap<>();
                roomData.put("roomname", roomName);
                roomData.put("roompassword", roomPassword);

                // Initialize players structure with score set to 0
                Map<String, Object> playersData = new HashMap<>();
                Map<String, Object> player1 = new HashMap<>();
                player1.put("id", currentUserId);
                player1.put("username", currentUserName);
                player1.put("score", 0);  // Initializing player1's score to 0
                playersData.put("player1", player1);

                playersData.put("player2", new HashMap<>()); // Placeholder for player2
                playersData.put("player3", new HashMap<>()); // Placeholder for player3

                roomData.put("players", playersData);

                // Write to Firebase
                mRoomRef.child(roomId).setValue(roomData).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Save roomId and roomName in SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("RoomSession", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("roomId", roomId);
                        editor.putString("roomName", roomName);
                        editor.apply();

                        // Navigate to RoomActivity
                        Intent intent = new Intent(RoomStateActivity.this, RoomActivity.class);
                        intent.putExtra("roomId", roomId);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RoomStateActivity.this, "Failed to create room, please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RoomStateActivity.this, "Error creating room.", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void joinRoom(String roomName, String roomPassword) {
        mRoomRef.orderByChild("roomname").equalTo(roomName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot roomSnapshot : dataSnapshot.getChildren()) {
                        String roomId = roomSnapshot.getKey();
                        String storedPassword = roomSnapshot.child("roompassword").getValue(String.class);

                        if (roomId != null && storedPassword != null && storedPassword.equals(roomPassword)) {
                            addPlayerToRoom(roomId);
                        } else {
                            Toast.makeText(RoomStateActivity.this, "Incorrect password.", Toast.LENGTH_SHORT).show();
                        }

                        return;
                    }
                } else {
                    Toast.makeText(RoomStateActivity.this, "Room not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RoomStateActivity.this, "Error joining room.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addPlayerToRoom(String roomId) {
        mRoomRef.child(roomId).child("players").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot playersSnapshot) {
                if (!playersSnapshot.hasChild("player2")) {
                    // Add player2 with score initialized to 0
                    Map<String, Object> player2 = new HashMap<>();
                    player2.put("id", currentUserId);
                    player2.put("username", currentUserName);
                    player2.put("score", 0); // Initialize player2's score to 0

                    mRoomRef.child(roomId).child("players").child("player2").setValue(player2)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    navigateToRoomActivity(roomId); // Navigate immediately
                                    checkIfRoomIsFull(roomId);
                                } else {
                                    Toast.makeText(RoomStateActivity.this, "Failed to join room.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else if (!playersSnapshot.hasChild("player3")) {
                    // Add player3 with score initialized to 0
                    Map<String, Object> player3 = new HashMap<>();
                    player3.put("id", currentUserId);
                    player3.put("username", currentUserName);
                    player3.put("score", 0); // Initialize player3's score to 0

                    mRoomRef.child(roomId).child("players").child("player3").setValue(player3)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    navigateToRoomActivity(roomId); // Navigate immediately
                                    checkIfRoomIsFull(roomId);
                                } else {
                                    Toast.makeText(RoomStateActivity.this, "Failed to join room.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(RoomStateActivity.this, "Room is full.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RoomStateActivity.this, "Error joining room.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void checkIfRoomIsFull(String roomId) {
        mRoomRef.child(roomId).child("players").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot playersSnapshot) {
                if (playersSnapshot.hasChild("player1") && playersSnapshot.hasChild("player2") && playersSnapshot.hasChild("player3")) {
                    startGame(roomId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RoomStateActivity.this, "Error checking room status.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startGame(String roomId) {
        Map<String, Object> gameData = new HashMap<>();
        gameData.put("currentQuestion", 1); // 开始第一题
        gameData.put("currentSongId", "song123"); // 示例歌曲ID

        mRoomRef.child(roomId).updateChildren(gameData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RoomStateActivity.this, "Game is starting!", Toast.LENGTH_SHORT).show();

                // 跳转到 CompetitiveActivity
                Intent intent = new Intent(RoomStateActivity.this, CompetitiveActivity.class);
                intent.putExtra("roomId", roomId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(RoomStateActivity.this, "Failed to start game.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void navigateToRoomActivity(String roomId) {
        Intent intent = new Intent(RoomStateActivity.this, RoomActivity.class);
        intent.putExtra("roomId", roomId); // Pass roomId to RoomNewActivity
        startActivity(intent);
        finish();
    }

    private void findViews() {
        roomNameEditText = findViewById(R.id.etRoomName);
        roomPasswordEditText = findViewById(R.id.etRoomPassword);
        createRoomButton = findViewById(R.id.createRoomButton);
        joinRoomButton = findViewById(R.id.joinRoomButton);
    }
}

