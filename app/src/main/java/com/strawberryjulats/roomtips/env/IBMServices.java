package com.strawberryjulats.roomtips.env;

import com.ibm.watson.developer_cloud.assistant.v2.Assistant;
import com.ibm.watson.developer_cloud.assistant.v2.model.SessionResponse;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;

public class IBMServices {
    private static Assistant watsonAssistant;
    private static SpeechToText speechToText;
    private static TextToSpeech textToSpeech;
    private static SessionResponse sessionResponse;

    public static Assistant getWatsonAssistant() {
        return watsonAssistant;
    }

    public static void setWatsonAssistant(Assistant w) {
        watsonAssistant = w;
    }

    public static SpeechToText getSpeechToText() {
        return speechToText;
    }

    public static void setSpeechToText(SpeechToText s) {
        speechToText = s;
    }

    public static TextToSpeech getTextToSpeech() {
        return textToSpeech;
    }

    public static void setTextToSpeech(TextToSpeech textToSpeech) {
        IBMServices.textToSpeech = textToSpeech;
    }

    public static SessionResponse getSessionResponse() {
        return sessionResponse;
    }

    public static void setSessionResponse(SessionResponse r) {
        sessionResponse = r;
    }
}
