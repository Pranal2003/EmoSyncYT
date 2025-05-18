package com.example.emosyncyt.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.emosyncyt.R;

import org.opencv.android.OpenCVLoader;

public class SplashActivity extends AppCompatActivity {

    private static final int DELAY_MILLIS = 5000; // Duration of the splash screen
    Animation topAnim, bottomAnim;
    ImageView image;
    TextView logo, slogan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Set to the splash layout

        // Initialize animations
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        // Hooks
        image = findViewById(R.id.imageView2);
        logo = findViewById(R.id.textView);
        slogan = findViewById(R.id.textView2);

        // Set animations
        image.setAnimation(topAnim);
        logo.setAnimation(bottomAnim);
        slogan.setAnimation(bottomAnim);

        // Handler to delay the transition to LoginActivity
        new Handler().postDelayed(() -> {
            Intent dashboardIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(dashboardIntent);
            finish(); // Finish the splash activity
        }, DELAY_MILLIS);
    }
}
