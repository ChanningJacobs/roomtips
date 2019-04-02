package com.strawberryjulats.roomtips.ikea;

import android.graphics.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.strawberryjulats.roomtips.CameraActivity;
import com.strawberryjulats.roomtips.CameraConnectionFragment;
import com.strawberryjulats.roomtips.DetectorActivity;
import com.strawberryjulats.roomtips.FurnitureAdapter;
import com.strawberryjulats.roomtips.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class IkeaAPIAccessTask extends AsyncTask<String, Void, ArrayList<Product>> {
    private final String TAG = "IkeaAPIAccessTask";

    @Override
    protected ArrayList<Product> doInBackground(String... params) {
        publishProgress();
        FurnitureAdapter.products = NativeAPIKt.getSuggestionsIkea(params[0], 10, 0, 9999, false, 2);
        return FurnitureAdapter.products;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        CameraActivity.spinner.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostExecute(ArrayList<Product> products) {
        for (Product product : products) {
            Log.d(TAG, "Found: " + product.getName());
        }
        Log.d("OUR PRODUCTS", "" + FurnitureAdapter.products);
        // Remove spinning loading symbol
        CameraActivity.spinner.setVisibility(View.INVISIBLE);
        CameraConnectionFragment.recyclerView.setVisibility(View.VISIBLE);
        CameraConnectionFragment.recyclerAdapter.notifyDataSetChanged();
        CameraActivity.isProcessingFrame = true;
        while(!CameraConnectionFragment.cameraOpenCloseLock.tryAcquire()){}
        CameraConnectionFragment.cameraDevice.close();
        CameraConnectionFragment.cameraOpenCloseLock.release();
    }
}
