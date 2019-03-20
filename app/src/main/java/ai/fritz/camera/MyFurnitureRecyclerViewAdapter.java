package ai.fritz.camera;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

//import com.example.roomtips.FurnitureFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Product} and makes a call to the
 * specified {@link //OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyFurnitureRecyclerViewAdapter extends RecyclerView.Adapter<MyFurnitureRecyclerViewAdapter.ViewHolder> {

    Context parentContext = null;

    public static ArrayList<Product> mValues;
    //private final OnListFragmentInteractionListener mListener;

    public MyFurnitureRecyclerViewAdapter(ArrayList<Product> products){//,OnListFragmentInteractionListener listener) {
        mValues = products;
        //mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        parentContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_furniture, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);
        Glide.with(parentContext).load(mValues.get(position).getImgUrl()).into(holder.mImageView);
        holder.mNameView.setText(mValues.get(position).getName());
        //holder.mDescriptionView.setText(mValues.get(position).getDescription());
        holder.mPriceView.setText(mValues.get(position).getPrice() + "");

        //holder.mIdView.setText(mValues.get(position).id);
        //holder.mContentView.setText(mValues.get(position).content);
/*
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
        */
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;

        public final ImageView mImageView;
        public final TextView mNameView;
        //public final TextView mDescriptionView;
        public final TextView mPriceView;

        //public final TextView mIdView;
        //public final TextView mContentView;

        public Product mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;

            mImageView = (ImageView) view.findViewById(R.id.image);
            mNameView = (TextView) view.findViewById(R.id.name);
           // mDescriptionView = (TextView) view.findViewById(R.id.description);
            mPriceView = (TextView) view.findViewById(R.id.price);


            //mIdView = (TextView) view.findViewById(R.id.item_number);
            //mContentView = (TextView) view.findViewById(R.id.content);
        }

        //TODO:
        @Override
        public String toString() {
            return "Implement me.";
            //return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
