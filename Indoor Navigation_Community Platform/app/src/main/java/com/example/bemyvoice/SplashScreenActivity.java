package com.example.bemyvoice;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int SPLASH_TIMEOUT = 2000; // Splash screen timeout duration in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // Find views
        ImageView appLogoImageView = findViewById(R.id.appLogoImageView);
        TextView appNameTextView = findViewById(R.id.appNameTextView);

        // Load animation
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Apply animation to views
        appLogoImageView.startAnimation(animation);
        appNameTextView.startAnimation(animation);

        // Delayed execution to start MainActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, SPLASH_TIMEOUT);
    }

}