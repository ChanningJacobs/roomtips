package ai.fritz.camera;

import android.os.AsyncTask;
import android.util.Log;

import com.ibm.watson.developer_cloud.service.exception.ServiceResponseException;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

// Given a list of image files, classify separately and return each classification string in a list
public class FurnitureImageClassifier extends AsyncTask<File, Void, ArrayList<String>> {

    /*
    WeakReference<Fragment> fragment;

    FurnitureImageClassifier(Fragment currentFragment) {
        fragment = new WeakReference<>(currentFragment);
    }
    */

    @Override
    protected ArrayList<String> doInBackground(File... testImage) {

        ArrayList<String> results = new ArrayList<String>();

        IamOptions options = new IamOptions.Builder()
                .apiKey("7Kls80rztqLhMwTVNxBe-hXVc6khzpICr71TKWuUUjy-")
                .build();

        VisualRecognition visualRecognition = new VisualRecognition("2018-03-19", options);

        for (File f : testImage){
            ClassifyOptions classifyOptions = null;
            ClassifiedImages result = null;
            try {
                classifyOptions = new ClassifyOptions.Builder()
                        .imagesFile(new FileInputStream(f))
                        .imagesFilename(f.getName())
                        .build();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return new ArrayList<String>(Collections.singletonList("Error. Couldn't find one of the image files."));
            }
            try{
                result = visualRecognition.classify(classifyOptions).execute();
            } catch (ServiceResponseException e){
                return new ArrayList<String>(Collections.singletonList("Error. " +
                        "IBM classifier call or result failure. Status code: " +
                        e.getStatusCode() + ": " + e.getMessage()));
            }
            results.add(result.toString());
        }
        // results handed to onPostExecute when object instance is executed
        return results;
    }

    @Override
    protected void onPostExecute(ArrayList<String> results) {
        for (String classification : results) {
            Log.d("classification", classification);
            Log.d("DEMOSPRINT3", classification);
            String query_product = "";
            String tag = "ClassifierFound";
            Set<String> furn = new HashSet<String>(Arrays.asList("chair", "desk", "table"));

            for(String label : furn) {
                if(classification.contains(label)) query_product = label;
            }


            Log.d("QUERYING", query_product);
            if(query_product != null && query_product.equals("table") || query_product.equals("chair") || query_product.equals("desk")) {
                new IkeaAPIAccessTask().execute(query_product);
            }
        }





        //returnClassifications(results);
    }

    private ArrayList<String> returnClassifications(ArrayList<String> results){
        return results;
    }
}