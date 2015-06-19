package com.id2p.mycarclub.utils.adapter;

/**
 * Created by anfraga on 2015-06-12.
 */

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.id2p.mycarclub.R;
import com.id2p.mycarclub.model.Garage;
import com.id2p.mycarclub.model.User;
import com.id2p.mycarclub.view.GarageCreationActivity;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;

import java.util.List;


/**
 * Created by Edwin on 18/01/2015.
 */

public class GarageCardAdapter extends RecyclerView.Adapter<GarageCardAdapter.ViewHolder> {

    List<Garage> mItems = null;
    User currentUser = null;

    public GarageCardAdapter(User user) throws ParseException {
        super();

        currentUser = user;
        mItems = Garage.getUserGarage(user);;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.garage_view_card_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        Garage item = mItems.get(i);
        viewHolder.mGarage = item;
        viewHolder.carName.setText(item.getCarMake() + " " + item.getCarModel());
        viewHolder.carThumbnail.setParseFile(item.getCarImages().get(0));
        viewHolder.carThumbnail.loadInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                viewHolder.carThumbnail.setVisibility(View.VISIBLE);
            }
        });
    }

    public Garage getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ParseImageView carThumbnail;
        public TextView carName;
        public Garage mGarage;

        public ViewHolder(View itemView) {
            super(itemView);
            carThumbnail = (ParseImageView)itemView.findViewById(R.id.car_thumbnail);
            carName = (TextView)itemView.findViewById(R.id.car_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(view.getContext(), "position = " + mItems.get(getPosition()).getCarModel(), Toast.LENGTH_SHORT).show();
            Garage item = mItems.get(getPosition());
            if (currentUser == item.getOwner()) {
                // owner, edit item
                Intent editIntent = new Intent(view.getContext(), GarageCreationActivity.class);
                editIntent.putExtra("garageId", item.getObjectId());
                view.getContext().startActivity(editIntent);
            }

        }
    }
}
