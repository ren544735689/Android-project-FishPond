package com.example.android_forest_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FishDeadActivity extends AppCompatActivity {
    private int coinNum;
    private TextView coinSum;
    private ImageView backButton;
    private ImageView fish;
    private String choose;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishdead);

        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        coinNum = preferences.getInt("coinSum",0);

        coinSum = findViewById(R.id.coinSum);
        coinSum.setText(coinNum+"");
        fish = findViewById(R.id.fish);
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent intent = getIntent();
        choose = intent.getStringExtra("choose");

        switch (choose){
            case "starBurst": fish.setImageResource(R.drawable.starburstdead); break;
            case "time": fish.setImageResource(R.drawable.timedead); break;
            case "star": fish.setImageResource(R.drawable.stardead); break;
        }

        Log.d("debug:","fishDead onCreate");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("debug:","fishDead onDestroyed");
    }
}
