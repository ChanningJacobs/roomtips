package com.strawberryjulats.roomtips;

import android.content.Intent;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

public class TutorialActivity extends AppCompatActivity {

    private Button actionButton;
    private Button visual;
    private TextView tutorialMainText;
    private String[] tutorialStrings = new String[] {
            "This tutorial will walk you through the basics",
            "Roomtips helps you redecorate interior spaces using artificial intelligence",
            "When roomtips finds furniture it will draw a shape around it", // show the box
            "If you click inside of the shape roomtips will suggest replacement products", // wait for the box to be clicked
            "Roomtips also comes with a virtual assistant named Visual to help you refine product searches",
            "To refine a product search you can talk to Visual by clicking on this button",
            "Let Visual know you're done by clicking on the button again",
            "That's enough to get you started. Have fun!"
    };
    private int curStringIndex = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        // Find the appropriate elements in the UI
        visual = findViewById(R.id.visualButton);
        actionButton = findViewById(R.id.nextButton);
        tutorialMainText = findViewById(R.id.mainTutorialText);
    }

    public void nextAction(View v) {
        if(curStringIndex < tutorialStrings.length) {
            replaceTextWithNext();
        } else {
            Intent intent = new Intent(this, DetectorActivity.class);
            startActivity(intent);
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

    private void replaceTextWithNext() {
        if(curStringIndex == 1) {
            tutorialMainText.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        }
        tutorialMainText.setText(tutorialStrings[curStringIndex]);
        curStringIndex++;
        if(curStringIndex == tutorialStrings.length) {
            actionButton.setText("done");
            visual.setVisibility(View.INVISIBLE);
        }
        else if(curStringIndex == tutorialStrings.length-2) {
            visual.setVisibility(View.VISIBLE);
        } else if(curStringIndex == tutorialStrings.length-1) {
            visual.setText("stop");
        }
    }

}
