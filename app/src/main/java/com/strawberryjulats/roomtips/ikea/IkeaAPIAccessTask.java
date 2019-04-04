package com.strawberryjulats.roomtips.ikea;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Camera;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
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
        int maxPrice = params.length >= 2 ? Integer.parseInt(params[1]) : 9999;
        int minPrice = params.length >= 3 ? Integer.parseInt(params[2]) : 0;
        FurnitureAdapter.products = NativeAPIKt.getSuggestionsIkea(params[0], 10, minPrice, maxPrice, false, 2);
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
        // 2560 by 1440

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        Log.i("COOL",Integer.toString(height));
        Log.i("COOL",Integer.toString(width));

        CameraConnectionFragment.recyclerView.animate().translationXBy(1440f).setDuration(1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                CameraConnectionFragment.recyclerView.setVisibility(View.VISIBLE);
                CameraConnectionFragment.recyclerView.animate().translationXBy(-1440f).setDuration(2000).setListener(null);
            }
        });
        CameraConnectionFragment.recyclerAdapter.notifyDataSetChanged();
        CameraActivity.isProcessingFrame = true;
        while(!CameraConnectionFragment.cameraOpenCloseLock.tryAcquire()){}
        CameraConnectionFragment.cameraDevice.close();
        CameraConnectionFragment.cameraOpenCloseLock.release();
        root.findViewById(R.id.imageView).setVisibility(View.VISIBLE);
        TextureView image = root.findViewById(R.id.texture);
        if(image == null){
            Log.d(TAG, "NULL IMAGE");
        }
        // Check for rotated image
        // TODO
        Blurry.with(context).radius(50).animate(2000).from(image.getBitmap()).into(root.findViewById(R.id.imageView));

    }
}
