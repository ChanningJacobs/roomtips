package com.strawberryjulats.roomtips;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;

public class FurnitureAdapter extends RecyclerView.Adapter<FurnitureAdapter.FurnitureViewHolder> {
    private String[] mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class FurnitureViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;
        public TextView textView;
        public FurnitureViewHolder(View v) {
            super(v);
            mView = v;
            textView = mView.findViewById(R.id.textView);

            Log.i("Adapter", "view holder reached");
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FurnitureAdapter(String[] myDataset) {
        mDataset = myDataset;
        Log.i("Adapter", "created reached");
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FurnitureAdapter.FurnitureViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.furniture_item, parent, false);
        return new FurnitureViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(FurnitureViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.setText(mDataset[position]);
        Log.i("Adapter", "bind reached");

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        Log.i("Adapter", Arrays.toString(mDataset));
        return mDataset.length;
    }

}
