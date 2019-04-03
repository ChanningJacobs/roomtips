package com.strawberryjulats.roomtips.env;

import com.ibm.watson.developer_cloud.assistant.v2.Assistant;
import com.ibm.watson.developer_cloud.assistant.v2.model.SessionResponse;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;

public class IBMServices {
    private static Assistant watsonAssistant;
    private static SpeechToText speechToText;
    private static SessionResponse watsonSession;

    public static Assistant getWatsonAssistant() {
        return watsonAssistant;
    }

    public static SpeechToText getSpeechToText() {
        return speechToText;
    }

    public static void setWatsonSession(SessionResponse r) {
        watsonSession = r;
    }

    public static SessionResponse getWatsonSession() {
        return watsonSession;
    }

    public static void setWatsonAssistant(Assistant a) {
        watsonAssistant = a;
    }

    public static void setSpeechToText(SpeechToText s) {
        speechToText = s;
    }
}
