package com.strawberryjulats.roomtips.audio;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.strawberryjulats.roomtips.CameraActivity;
import com.strawberryjulats.roomtips.R;
import com.strawberryjulats.roomtips.env.IBMServices;

import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public class RecordAudio {
    private static final String TAG = "RecordAudio";
    private MicrophoneHelper microphoneHelper;
    private CameraActivity activity;
    private boolean recording;

    public RecordAudio(CameraActivity currentActivity) {
        activity = currentActivity;
        microphoneHelper = new MicrophoneHelper(activity);
        recording = false;
    }

    public void setRecord() {
        FragmentManager fm = activity.getSupportFragmentManager();
        DialogFragment dialog = new SpeechDialogFragment();
        dialog.show(fm, "speech sheet");
    }

    public void record() {
        if (!recording) {
            Log.d(TAG, "Recording");
            InputStream inputStream = microphoneHelper.getInputStream(true);
            final RecognizeOptions recognizeOptions = new RecognizeOptions.Builder()
                    .audio(inputStream)
                    .contentType(ContentType.OPUS.toString())
                    .model("en-US_BroadbandModel")
                    .interimResults(false)
                    .inactivityTimeout(2)
                    .smartFormatting(true)
                    .build();
            final BaseRecognizeCallback baseRecognizeCallback = new BaseRecognizeCallback() {
                @Override
                public void onTranscription(SpeechRecognitionResults speechRecognitionResults) {
                    Log.d(TAG, "Recognized stuff: " + speechRecognitionResults.getResults().get(0).getAlternatives().get(0).getTranscript());
                    new WatsonTask(activity).execute(speechRecognitionResults.getResults().get(0).getAlternatives().get(0).getTranscript());
                }
            };
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        IBMServices.getSpeechToText().recognizeUsingWebSocket(recognizeOptions, baseRecognizeCallback);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }).start();
            recording = true;
        } else {
            Log.d(TAG, "Stopped Recording");
            try {
                microphoneHelper.closeInputStream();
                recording = false;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
