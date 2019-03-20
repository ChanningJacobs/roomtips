package ai.fritz.camera;

import android.os.AsyncTask;
import android.util.Log;

import ai.fritz.camera.NativeAPIKt;
import ai.fritz.camera.Product;

import java.util.ArrayList;

public class IkeaAPIAccessTask extends AsyncTask<String, Void, ArrayList<Product>> {
    private final String TAG = "IkeaAPIAccessTask";

    @Override
    protected ArrayList<Product> doInBackground(String... params) {
        FurnitureFragment.products = NativeAPIKt.getSuggestionsIkea(params[0], 10, 0, 9999, false, 4);
        return FurnitureFragment.products;
    }

    @Override
    protected void onPostExecute(ArrayList<Product> products) {
        Log.d("OOF", Integer.toString(products.size()));
        for (Product product : products) {
            Log.d(TAG, "Found: " + product.getName());
            Log.d("DEMOSPRINT3", "Found: " + product.getName());
        }
        Log.d("OUR PRODUCTS", "HELLO TEST FIRST");
        FurnitureFragment.products = products;
        Log.d("OOF", Integer.toString(FurnitureFragment.products.size()));
        Log.d("OUR PRODUCTS", "HELLO TEST");
        Log.d("OUR PRODUCTS", "" + FurnitureFragment.products);
        Log.d("OUR PRODUCTS", "HELLO TEST THIRD");
        FurnitureFragment.adapter.mValues = FurnitureFragment.products;
        Log.d("OOF", Integer.toString(FurnitureFragment.adapter.mValues.size()));
        FurnitureFragment.adapter.notifyDataSetChanged();
        Log.d("OUR PRODUCTS", "HELLO TEST LAST");
    }
}
