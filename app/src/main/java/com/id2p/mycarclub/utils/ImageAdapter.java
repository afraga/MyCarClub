package com.id2p.mycarclub.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.id2p.mycarclub.R;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anfraga on 2015-06-06.
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private List<ParseImageView> imageList = null;

    public ImageAdapter(Context c) {
        mContext = c;
        imageList = new ArrayList<ParseImageView>();

        for (int i = 0; i < 6; i++) {
            ParseImageView view = new ParseImageView(mContext);
            view.setLayoutParams(new GridView.LayoutParams(150, 150));
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setPadding(2, 2, 2, 2);
            view.setImageResource(R.drawable.com_facebook_profile_picture_blank_portrait);
            imageList.add(i, view);
        }
    }

    public int getCount() {
        return imageList.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public void setImageAtPosition(int position, ParseFile imageFile) {
        final ParseImageView viewPointer = imageList.get(position);
        viewPointer.setParseFile(imageFile);
        viewPointer.loadInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    viewPointer.setVisibility(View.VISIBLE);
                }
            });
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ParseImageView imageView = imageList.get(position);

        return imageView;
    }

}