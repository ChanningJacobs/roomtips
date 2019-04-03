package com.strawberryjulats.roomtips.ikea;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ProgressBar;

import com.strawberryjulats.roomtips.CameraActivity;
import com.strawberryjulats.roomtips.CameraConnectionFragment;
import com.strawberryjulats.roomtips.DetectorActivity;
import com.strawberryjulats.roomtips.FurnitureAdapter;
import com.strawberryjulats.roomtips.R;
import jp.wasabeef.blurry.Blurry;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class IkeaAPIAccessTask extends AsyncTask<String, Void, ArrayList<Product>> {
    private final String TAG = "IkeaAPIAccessTask";
    private View root;
    private Context context;

    public IkeaAPIAccessTask(View view){
        root = view;
        context = root.getContext();
    }

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
        // 2560 by 1440
        ObjectAnimator animation = ObjectAnimator.ofFloat(CameraConnectionFragment.recyclerView, "translationX", 1440f);
        animation.setDuration(2000);
        animation.start();
        CameraConnectionFragment.recyclerAdapter.notifyDataSetChanged();
        ObjectAnimator animation2 = ObjectAnimator.ofFloat(CameraConnectionFragment.recyclerView, "translationX", -1440f);
        animation2.setDuration(2000);
        animation2.start();
        CameraActivity.isProcessingFrame = true;
        while(!CameraConnectionFragment.cameraOpenCloseLock.tryAcquire()){}
        CameraConnectionFragment.cameraDevice.close();
        CameraConnectionFragment.cameraOpenCloseLock.release();
        root.findViewById(R.id.imageView).setVisibility(View.VISIBLE);
        TextureView image = root.findViewById(R.id.texture);
        if(image == null){
            Log.d(TAG, "NULL IMAGE");
        }
        Blurry.with(context).radius(10).from(image.getBitmap()).into(root.findViewById(R.id.imageView));
    }
}
