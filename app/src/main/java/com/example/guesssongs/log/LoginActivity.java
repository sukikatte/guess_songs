package com.example.guesssongs.log;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.guesssongs.Class.User;
import com.example.guesssongs.nav.MainActivity;
import com.example.guesssongs.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private Button login, register;
    private EditText username, password;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference("user");

        Log.d("Firebase", "Firebase connected: " + (mDatabase != null));

        findViews();
        hideSystemUI();
        login.setOnClickListener(v -> {
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            authenticateUser(user, pass);
        });

        register.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void findViews() {
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
    }

    private void authenticateUser(String usernameInput, String passwordInput) {
        mDatabaseRef.orderByChild("username").equalTo(usernameInput).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User userFromDb = userSnapshot.getValue(User.class);

                        if (userFromDb != null
                                && userFromDb.getPassword().equals(passwordInput)) {
                            // 登录成功，获取用户 ID
                            String id = userSnapshot.getKey();

                            // 保存用户 ID 和用户名到 SharedPreferences
                            saveUserSession(id, userFromDb.getUsername());

                            Toast.makeText(LoginActivity.this, "Login success", Toast.LENGTH_SHORT).show();

                            // 跳转到主界面
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }
                    Toast.makeText(LoginActivity.this, "Invalid password. Please try again.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Username not found. Please register first.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Error occurred. Try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserSession(String id, String username) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("id", id);
        editor.putString("username", username);
        editor.apply();
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
