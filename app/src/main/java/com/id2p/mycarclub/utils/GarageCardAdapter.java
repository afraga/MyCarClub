package com.id2p.mycarclub.utils;

/**
 * Created by anfraga on 2015-06-12.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.id2p.mycarclub.R;
import com.id2p.mycarclub.model.Garage;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;

import java.util.List;


/**
 * Created by Edwin on 18/01/2015.
 */

public class GarageCardAdapter extends RecyclerView.Adapter<GarageCardAdapter.ViewHolder> {

    List<Garage> mItems;

    public GarageCardAdapter(List<Garage> itemList) {
        super();

        mItems = itemList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.garage_view_card_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        Garage item = mItems.get(i);
        viewHolder.carName.setText(item.getCarMake() + " " + item.getCarModel());
        viewHolder.carThumbnail.setParseFile(item.getCarImages().get(0));
        viewHolder.carThumbnail.loadInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                viewHolder.carThumbnail.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ParseImageView carThumbnail;
        public TextView carName;

        public ViewHolder(View itemView) {
            super(itemView);
            carThumbnail = (ParseImageView)itemView.findViewById(R.id.car_thumbnail);
            carName = (TextView)itemView.findViewById(R.id.car_name);
        }
    }
}
