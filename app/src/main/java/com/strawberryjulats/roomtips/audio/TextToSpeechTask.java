package com.strawberryjulats.roomtips.audio;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.SynthesizeOptions;
import com.strawberryjulats.roomtips.CameraActivity;
import com.strawberryjulats.roomtips.env.IBMServices;

import java.io.InputStream;
import java.lang.ref.WeakReference;

public class TextToSpeechTask extends AsyncTask<String, Void, String> {
    private final String TAG = "TextToSpeechTask";
    private WeakReference<CameraActivity> activity;

    TextToSpeechTask(CameraActivity a) {
        activity = new WeakReference<>(a);
    }
    @Override
    protected String doInBackground(String... params) {
        String textToRead = params[0];
        Log.d(TAG, textToRead);
        StreamPlayer streamPlayer = new StreamPlayer();
        try {
            SynthesizeOptions synthesizeOptions = new SynthesizeOptions.Builder().text(textToRead).accept(SynthesizeOptions.Accept.AUDIO_WAV).voice(SynthesizeOptions.Voice.EN_US_ALLISONVOICE).build();
            InputStream inputStream = IBMServices.getTextToSpeech().synthesize(synthesizeOptions).execute();
            streamPlayer.playStream(inputStream);
            inputStream.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return "";
    }

    @Override
    protected void onPostExecute(String action) {
    }
}