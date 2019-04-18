package com.strawberryjulats.roomtips.audio;

import android.app.Activity;
import android.graphics.Camera;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.ibm.watson.developer_cloud.assistant.v2.model.CreateSessionOptions;
import com.ibm.watson.developer_cloud.assistant.v2.model.MessageInput;
import com.ibm.watson.developer_cloud.assistant.v2.model.MessageOptions;
import com.ibm.watson.developer_cloud.assistant.v2.model.MessageResponse;
import com.ibm.watson.developer_cloud.assistant.v2.model.RuntimeEntity;
import com.strawberryjulats.roomtips.CameraActivity;
import com.strawberryjulats.roomtips.CameraConnectionFragment;
import com.strawberryjulats.roomtips.DetectorActivity;
import com.strawberryjulats.roomtips.FurnitureAdapter;
import com.strawberryjulats.roomtips.R;
import com.strawberryjulats.roomtips.env.IBMServices;
import com.strawberryjulats.roomtips.ikea.IkeaAPIAccessTask;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class WatsonTask extends AsyncTask<String, Void, MessageResponse> {
    private final String TAG = "SessionTestTask";
    private WeakReference<CameraActivity> activity;

    WatsonTask(CameraActivity a) {
        activity = new WeakReference<>(a);
    }

    @Override
    protected MessageResponse doInBackground(String... params) {
            /*
                Take text from parameters and send into service as a message
             */
        String textToSend = params[0].replace("$", "");
        Log.d(TAG, "Sending text: " + textToSend);

        if (IBMServices.getSessionResponse() == null) {
            CreateSessionOptions watsonSessionOptions = new CreateSessionOptions.Builder(activity.get().getString(R.string.assistant_id)).build();
            IBMServices.setSessionResponse(IBMServices.getWatsonAssistant().createSession(watsonSessionOptions).execute());
            Log.d(TAG, "Session: " + IBMServices.getSessionResponse());
        }

        MessageInput input = new MessageInput.Builder().messageType("text").text(textToSend).build();
        MessageOptions messageOptions = new MessageOptions.Builder(activity.get().getString(R.string.assistant_id), IBMServices.getSessionResponse().getSessionId()).input(input).build();
            /*
                Get response from service
             */
        MessageResponse response = IBMServices.getWatsonAssistant().message(messageOptions).execute();
        Log.d(TAG, response.toString());

        return response;
    }

    @Override
    protected void onPostExecute(MessageResponse response) {
        super.onPostExecute(response);
        String text = "";
        String action = "";
        String[] params = new String[2];
        int count = 0;
        if (response.getOutput() != null) {
            if (!response.getOutput().getGeneric().isEmpty() &&
                    response.getOutput().getGeneric().get(0).getResponseType().equals("text")) {
                text = response.getOutput().getGeneric().get(0).getText();
            }
            if (response.getOutput().getActions() != null && !response.getOutput().getActions().isEmpty()) {
                action = response.getOutput().getActions().get(0).getName();
            }
            if (response.getOutput().getEntities() != null && !response.getOutput().getEntities().isEmpty()) {
                for(RuntimeEntity rt : response.getOutput().getEntities()) {
                    if(Character.isDigit(rt.getValue().charAt(0)) && count < 2) {
                        params[count++] = rt.getValue();
                    }
                }
            }
        }
        Log.d(TAG, "Text to be read: " + text);
        Log.d(TAG, "Action to take: " + action);

        Log.d(TAG, "SWITCHING");

        if(DetectorActivity.queryWords == null){
            text = "Please select an object before trying to adjust the price.";
        }
        if(action.equals("priceBetween") && params[1] == null){
            text = "I didn't catch the second number.";
            action = "";
        }
        runAction(action, params);
        new TextToSpeechTask(activity.get()).execute(text);
    }

    private void runAction(String action, String[] params) {
        if (action.isEmpty() || DetectorActivity.queryWords == null) {
            return;
        }
        String lower = "0";
        String upper = "9999";
        switch (action) {
            case "priceAbove":
                lower = params[0];
                break;
            case "priceBelow":
                upper = params[0];
                break;
            case "priceBetween":
                if (params.length > 1) {
                    if (Integer.parseInt(params[0]) < Integer.parseInt(params[1])) {
                        lower = params[0];
                        upper = params[1];
                    } else {
                        lower = params[1];
                        upper = params[0];
                    }
                }
                break;
        }
        Log.d("TESTTEST", "PASSING IN MIN: " + lower + " AND MAX: " + upper);
        new IkeaAPIAccessTask(activity.get().findViewById(R.id.frame)).execute(DetectorActivity.queryWords, lower, upper);
        if(FurnitureAdapter.products.size() > 0){
            CameraConnectionFragment.recyclerAdapter.notifyDataSetChanged();
            CameraConnectionFragment.recyclerView.scrollToPosition(0);
        }
    }
}
