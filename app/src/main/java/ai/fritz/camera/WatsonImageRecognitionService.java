package ai.fritz.camera;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.ibm.watson.developer_cloud.service.exception.ServiceResponseException;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class WatsonImageRecognitionService extends Service {

    private final Binder binder = new localBinder();

    public class localBinder extends Binder{
        WatsonImageRecognitionService getService(){
            return WatsonImageRecognitionService.this;
        }
    }

    public WatsonImageRecognitionService() {
    }

    public String getCurrentTime(){
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.US);
        return df.format(new Date());
    }

    public String classifyImage(File imageFile){
        IamOptions options = new IamOptions.Builder()
                .apiKey(getString(R.string.visual_recognition_iam_apikey))
                .build();
        VisualRecognition visualRecognition = new VisualRecognition(getString(R.string.visual_recognition_version), options);

        if(imageFile == null){
            return "error, null file";
        } else {
            ClassifyOptions classifyOptions = null;
            ClassifiedImages result = null;
            try {
                classifyOptions = new ClassifyOptions.Builder()
                        .imagesFile(new FileInputStream(imageFile)) // fix
                        .imagesFilename(imageFile.getName())
                        .build();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try{
                result = visualRecognition.classify(classifyOptions).execute();
                return result.toString();
            } catch (ServiceResponseException e){
                return  "Error. " +
                        "IBM classifier call or result failure. Status code: " +
                        e.getStatusCode() + ": " + e.getMessage();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
    }
}
