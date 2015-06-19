package com.id2p.mycarclub.utils.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.id2p.mycarclub.R;
import com.id2p.mycarclub.model.Event;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anfraga on 2015-06-16.
 */
public class EventsAdapter extends ArrayAdapter<Event> {

    private List<Event> eventList = null;
    public EventsAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        eventList = events;
    }

    @Override
    public Event getItem(int position) {
        return eventList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Event event = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_item, parent, false);
        }
        // Lookup view for data population
        TextView eventName = (TextView) convertView.findViewById(R.id.event_name);
        TextView eventDate = (TextView) convertView.findViewById(R.id.event_date);
        //TextView eventNumAttendees = (TextView) convertView.findViewById(R.id.eventNumAttendees);

        // Populate the data into the template view using the data object
        eventName.setText(event.getName());
        eventDate.setText(event.getDate().toString());
        //eventNumAttendees.setText(event.numAttendees);

        // Return the completed view to render on screen
        return convertView;
    }
}
