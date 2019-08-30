package com.example.myexamplepipexoplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private String videoUrl = "https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8";
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStartVideo();
            }
        });
    }

    private void onStartVideo() {
        Intent intent = new  Intent(MainActivity.this, VideoActivity.class);
        intent.putExtra(VideoActivity.ARG_VIDEO_URL, videoUrl);
        startActivity(intent);
    }
}
