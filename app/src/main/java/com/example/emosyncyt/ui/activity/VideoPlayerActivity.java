package com.example.emosyncyt.ui.activity;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.emosyncyt.R;

public class VideoPlayerActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());

        // Enable JavaScript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Get video ID from intent
        String videoId = getIntent().getStringExtra("VIDEO_ID");
        String videoUrl = "https://www.youtube.com/embed/" + videoId;

        // Load the video URL
        webView.loadUrl(videoUrl);
    }
}