package com.strawberryjulats.roomtips.audio;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.strawberryjulats.roomtips.CameraActivity;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class HotwordDetect {
    private static final String TAG = "HotwordDetect";
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.DEFAULT;
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int SAMPLE_DURATION_MS = 1000;
    private static final String LABEL_FILENAME = "file:///android_asset/name_hotwords_labels.txt";
    private static final String MODEL_FILENAME = "file:///android_asset/name_hotwords.pb";
    private static final long AVERAGE_WINDOW_DURATION_MS = 1000;
    private static final float DETECTION_THRESHOLD = 0.50f;
    private static final int SUPPRESSION_MS = 1500;
    private static final int MINIMUM_COUNT = 3;
    private static final long MINIMUM_TIME_BETWEEN_SAMPLES_MS = 30;
    private static final int REQUEST_RECORD_AUDIO = 13;
    private static final String OUTPUT_SCORES_NAME = "labels_softmax";
    private static final String INPUT_DATA_NAME = "decoded_sample_data:0";
    private static final String SAMPLE_RATE_NAME = "decoded_sample_data:1";
    private int recordingOffset = 0;
    private boolean isRecording = true;
    private boolean isRecognizing = true;
    private final ReentrantLock recordingLock = new ReentrantLock();
    private Thread recordingThread;
    private Thread recognitionThread;

    private static final int RECORDING_LENGTH = (SAMPLE_RATE  * SAMPLE_DURATION_MS / 1000);
    private short[] recordingBuffer = new short[RECORDING_LENGTH];
    private List<String> labels = new ArrayList<>();
    private List<String> displayedLabels = new ArrayList<>();
    private TensorFlowInferenceInterface inferenceInterface;
    private RecognizeCommands recognizeCommands;
    private CameraActivity activity;
    private RecordAudio recordAudio;

    public HotwordDetect(CameraActivity a) {
        activity = a;
        String actualLabelFilename = LABEL_FILENAME.split("file:///android_asset/", -1)[1];
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(activity.getApplicationContext().getAssets().open(actualLabelFilename)));
            String line;
            while ((line = br.readLine()) != null) {
                labels.add(line);
                if (line.charAt(0) != '_') {
                    displayedLabels.add(line.substring(0, 1).toUpperCase() + line.substring(1));
                    Log.d(TAG, "Put in Displayed Labels: " + displayedLabels.get(displayedLabels.size() - 1));
                }
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!", e);
        }

        recognizeCommands =
                new RecognizeCommands(
                        labels,
                        AVERAGE_WINDOW_DURATION_MS,
                        DETECTION_THRESHOLD,
                        SUPPRESSION_MS,
                        MINIMUM_COUNT,
                        MINIMUM_TIME_BETWEEN_SAMPLES_MS);

        try {
            inferenceInterface = new TensorFlowInferenceInterface(activity.getApplicationContext().getAssets(), MODEL_FILENAME);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void startRecording() {
        Log.d(TAG, "Entered Start Recording");
        if (recordingThread != null) {
            Log.d(TAG, "Recording Thread not null, return");
            return;
        }
        recordAudio = new RecordAudio(activity);
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                record();
            }
        });
        recordingThread.start();
    }

    public synchronized void stopRecording() {
        if (recordingThread == null) {
            return;
        }
        isRecording = false;
        recordingThread = null;
    }

    private void record() {
        Log.d(TAG, "Entered Record");
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }

        short[] audioBuffer = new short[bufferSize / 2];
        AudioRecord record = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.d(TAG, "Audio Record state is not initialized, returning");
            return;
        }

        record.startRecording();

        while (isRecording) {
            int numberRead = record.read(audioBuffer, 0, audioBuffer.length);
            int maxLength = recordingBuffer.length;
            int newRecordingOffset = recordingOffset + numberRead;
            int secondCopyLength = Math.max(0, newRecordingOffset - maxLength);
            int firstCopyLength = numberRead - secondCopyLength;
            recordingLock.lock();

            try {
                System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, firstCopyLength);
                System.arraycopy(audioBuffer, firstCopyLength, recordingBuffer, 0, secondCopyLength);
                recordingOffset = newRecordingOffset % maxLength;
            } finally {
                recordingLock.unlock();
            }
        }

        record.stop();
        record.release();
    }

    public synchronized void startRecognition() {
        Log.d(TAG, "Entered StartRecognition");
        if (recognitionThread != null) {
            Log.d(TAG, "recognition thread not null, returning");
            return;
        }

        isRecognizing = true;
        recognitionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                recognize();
            }
        });
        recognitionThread.start();
    }

    public synchronized void stopRecognition() {
        if (recognitionThread == null) {
            return;
        }

        isRecognizing = false;
        recognitionThread = null;
    }

    private void recognize() {
        Log.d(TAG, "Entered Recognize");
        short[] inputBuffer = new short[RECORDING_LENGTH];
        float[] floatInputBuffer = new float[RECORDING_LENGTH];
        float[] outputScores = new float[labels.size()];
        String[] outputScoresNames = new String[]{OUTPUT_SCORES_NAME};
        int[] sampleRateList = new int[] {SAMPLE_RATE};

        while (isRecognizing) {
            recordingLock.lock();

            try {
                int maxLength = recordingBuffer.length;
                int firstCopyLength = maxLength - recordingOffset;
                int secondCopyLength = recordingOffset;
                System.arraycopy(recordingBuffer, recordingOffset, inputBuffer, 0, firstCopyLength);
                System.arraycopy(recordingBuffer, 0, inputBuffer, firstCopyLength, secondCopyLength);
            } finally {
                recordingLock.unlock();
            }

            for (int i = 0; i < RECORDING_LENGTH; ++i) {
                floatInputBuffer[i] = inputBuffer[i] / 32767.0f;
            }

            inferenceInterface.feed(SAMPLE_RATE_NAME, sampleRateList);
            inferenceInterface.feed(INPUT_DATA_NAME, floatInputBuffer, RECORDING_LENGTH, 1);
            inferenceInterface.run(outputScoresNames);
            inferenceInterface.fetch(OUTPUT_SCORES_NAME, outputScores);

            long currentTime = System.currentTimeMillis();
            final RecognizeCommands.RecognitionResult result =
                    recognizeCommands.processLatestResults(outputScores, currentTime);

            if (!result.foundCommand.startsWith("_") && result.isNewCommand) {
                int labelIndex = -1;
                for (int i = 0; i < labels.size(); ++i) {
                    if (labels.get(i).equals(result.foundCommand)) {
                        labelIndex = i;
                    }
                }

                switch (labelIndex - 2) {
                    case 0:
                        Log.d(TAG, "Sheila Found");
                        break;
                    case 1:
                        Log.d(TAG, "Marvin Found");
                        break;
                    case 2:
                        Log.d(TAG, "Visual Found");
                        Vibrator v = (Vibrator) activity.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(VibrationEffect.createOneShot(200, 200));
                        stopRecording();
                        stopRecognition();
                        //recordAudio.setRecord();
                        break;
                }
                Log.d(TAG, "Score: " + result.score);
            }

            try {
                // We don't need to run too frequently, so snooze for a bit.
                Thread.sleep(MINIMUM_TIME_BETWEEN_SAMPLES_MS);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }

    public void recordOn() {
        if (recordAudio != null) {
            recordAudio.record();
        }
    }
}
