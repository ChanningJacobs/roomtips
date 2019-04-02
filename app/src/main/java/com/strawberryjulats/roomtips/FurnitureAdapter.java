package com.strawberryjulats.roomtips;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.strawberryjulats.roomtips.ikea.Product;

import java.util.ArrayList;
import java.util.Arrays;

public class FurnitureAdapter extends RecyclerView.Adapter<FurnitureAdapter.FurnitureViewHolder> {
    public static ArrayList<Product> products = new ArrayList<>();
    private Context parentContext = null;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class FurnitureViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View furn_item;
        public TextView furn_name;
        public TextView furn_price;
        public TextView furn_desc;
        public ImageView furn_img;
        public FurnitureViewHolder(View v) {
            super(v);
            furn_item = v;
            furn_name = furn_item.findViewById(R.id.furn_name);
            furn_price = furn_item.findViewById(R.id.furn_price);
            furn_desc = furn_item.findViewById(R.id.furn_desc);
            furn_img = furn_item.findViewById(R.id.furn_img);

            Log.i("Adapter", "view holder reached");
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FurnitureAdapter(String[] myDataset) {
        Log.i("Adapter", "created reached");
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FurnitureAdapter.FurnitureViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        parentContext = parent.getContext();
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
        holder.furn_name.setText(products.get(position).getName());
        holder.furn_price.setText(Double.toString(products.get(position).getPrice()));
        holder.furn_desc.setText("Description.");
        Glide.with(parentContext).load(products.get(position).getImgUrl()).into(holder.furn_img);

        Log.i("Adapter", "bind reached");

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return products.size();
    }


}
