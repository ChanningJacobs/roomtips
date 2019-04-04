package com.strawberryjulats.roomtips;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

public class LandingActivity extends AppCompatActivity {

    TextView landingTitle;
    Button takeTutorial;
    Button skipTutorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);


        // Find all of the UI elements
        landingTitle = findViewById(R.id.landingTitle);
        landingTitle.setVisibility(View.INVISIBLE);
        takeTutorial = findViewById(R.id.tutorialPrompt);
        skipTutorial = findViewById(R.id.skipTutorialPrompt);

        startTitleAnimation();

        if(isNewUser()) {
            startTutorialAnimation();
        }

    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        findViewById(R.id.top_layout).setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public void takeTutorial(View v) {
        disableNewUserStatus();
        Intent intent = new Intent(this, TutorialActivity.class);
        startActivity(intent);
    }

    public void skipTutorial(View v) {
        disableNewUserStatus();
        Intent intent = new Intent(this, DetectorActivity.class);
        startActivity(intent);
    }

    private boolean isNewUser() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return prefs.getBoolean("new_user", true);
    }

    private void disableNewUserStatus() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("new_user", false);
        edit.commit();
    }

    private void startTitleAnimation() {
        Handler handler = new Handler();
        handler.postDelayed(this::titleAnimation, 1000);
    }

    private void titleAnimation() {
        landingTitle.setVisibility(View.VISIBLE);
        Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_animation);
        landingTitle.startAnimation(fadeIn);
    }

    private void startTutorialAnimation() {
        Handler handler = new Handler();
        handler.postDelayed(this::tutorialAnimation, 1500);
    }

    private void tutorialAnimation() {
        takeTutorial.setVisibility(View.VISIBLE);
        skipTutorial.setVisibility(View.VISIBLE);
        Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_animation);
        takeTutorial.startAnimation(fadeIn);
        skipTutorial.startAnimation(fadeIn);
    }
}
