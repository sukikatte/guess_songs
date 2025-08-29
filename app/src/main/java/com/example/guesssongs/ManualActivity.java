package com.example.guesssongs;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
public class ManualActivity extends AppCompatActivity{
    private Button returnto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
        findViews();
        returnto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回到登录活动
                finish(); // 或者可以使用 Intent
            }
        });
    }
    private void findViews() {
        returnto = findViewById(R.id.BacktoMain);
    }
}
