package com.strawberryjulats.roomtips;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class TutorialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
    }

    public void fakeMethod(View v) {
        Intent intent = new Intent(this, DetectorActivity.class);
        startActivity(intent);
    }
}
