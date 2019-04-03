package com.strawberryjulats.roomtips.audio;

import android.app.Activity;
import android.util.Log;

import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.strawberryjulats.roomtips.env.IBMServices;

import java.io.InputStream;

import static android.support.constraint.Constraints.TAG;

class RecordConversation {
    private MicrophoneHelper microphoneHelper;
    private Activity activity;
    private boolean recording;

    RecordConversation(Activity currentActivity) {
        activity = currentActivity;
        microphoneHelper = new MicrophoneHelper(activity);
        recording = false;
    }

    void record() {
        if (!recording) {
            Log.d(TAG, "Starting record conversation");
            InputStream inputStream = microphoneHelper.getInputStream(true);
            final RecognizeOptions recognizeOptions = new RecognizeOptions.Builder()
                    .audio(inputStream)
                    .contentType(ContentType.OPUS.toString())
                    .model("en-US_BroadbandModel")
                    .interimResults(false)
                    .inactivityTimeout(1)
                    .build();
            final BaseRecognizeCallback baseRecognizeCallback = new BaseRecognizeCallback() {
                @Override
                public void onTranscription(SpeechRecognitionResults speechRecognitionResults) {
                    Log.d(TAG, "TRANSCRIBING AUDIO");
                    new WatsonTask(activity).execute(speechRecognitionResults.getResults().get(0).getAlternatives().get(0).getTranscript());
                    record();
                }
            };
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "DOING SPEECH TO TEXT");
                        IBMServices.getSpeechToText().recognizeUsingWebSocket(recognizeOptions, baseRecognizeCallback);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }).start();
            recording = true;
        } else {
            Log.d(TAG, "Stopping RecordConversation");
            try {
                microphoneHelper.closeInputStream();
                recording = false;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}