package com.id2p.mycarclub.utils.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.id2p.mycarclub.R;
import com.id2p.mycarclub.model.Route;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anfraga on 2015-06-27.
 */
public class EventLocationAdapter extends ArrayAdapter<Route> {

    private List<Route> routeList = null;
    public EventLocationAdapter(Context context, ArrayList<Route> routes) {
        super(context, 0, routes);
        routeList = routes;
    }

    @Override
    public Route getItem(int position) {
        return routeList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Route route = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_location_item, parent, false);
        }
        // Lookup view for data population
        TextView eventAddress = (TextView) convertView.findViewById(R.id.eventRoute);

        StringBuilder addressAndDistance = new StringBuilder();
        addressAndDistance.append(route.getAddress());

        if (position > 0) {
            Route previous = getItem(position-1);
            double distanceKm = previous.getGeoPoint().distanceInKilometersTo(route.getGeoPoint());
            addressAndDistance.append(" (");
            addressAndDistance.append(Math.floor(distanceKm));
            addressAndDistance.append(" km)");
        }

        // Populate the data into the template view using the data object
        eventAddress.setText(addressAndDistance.toString());

        // Return the completed view to render on screen
        return convertView;
    }
}
