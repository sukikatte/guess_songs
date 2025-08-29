// RegisterActivity.java
package com.example.guesssongs.log;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.guesssongs.Class.User;
import com.example.guesssongs.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText mEtUsername, mEtPassword;
    private Button mBtnRegister, returnL;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserRef, mScoreRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        findViews();
        hideSystemUI();
        mDatabase = FirebaseDatabase.getInstance();
        mUserRef = mDatabase.getReference("user");
        mScoreRef = mDatabase.getReference("scores");

        returnL.setOnClickListener(v -> finish());
        mBtnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = mEtUsername.getText().toString().trim();
        String password = mEtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the username is unique
        mUserRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(RegisterActivity.this, "This username is already in use. Please try a different one.", Toast.LENGTH_SHORT).show();
                } else {
                    saveUserToDatabase(username, password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RegisterActivity.this, "Registration Failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserToDatabase(String username, String password) {
        mUserRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int newId = (int) dataSnapshot.getChildrenCount() + 1; // 自增主键逻辑
                User newUser = new User(newId, username, password);

                mUserRef.child(String.valueOf(newId)).setValue(newUser).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 初始化成绩表
                        mScoreRef.child(String.valueOf(newId)).child("bestScore").setValue(0);
                        mScoreRef.child(String.valueOf(newId)).child("username").setValue(username);

                        Toast.makeText(RegisterActivity.this, "Registration Success", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RegisterActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void findViews() {
        mEtUsername = findViewById(R.id.username);
        mEtPassword = findViewById(R.id.password);
        mBtnRegister = findViewById(R.id.newUser);
        returnL = findViewById(R.id.BacktoMain);
    }

    private void hideSystemUI() {
        // 设置系统UI标志
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION      // 隐藏导航栏
                        | View.SYSTEM_UI_FLAG_FULLSCREEN         // 隐藏状态栏
        );
    }

    // 保持隐藏系统UI
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
}
