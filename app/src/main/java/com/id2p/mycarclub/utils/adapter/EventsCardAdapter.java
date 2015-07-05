package com.id2p.mycarclub.utils.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.id2p.mycarclub.R;
import com.id2p.mycarclub.model.Event;
import com.id2p.mycarclub.model.EventRegistration;
import com.id2p.mycarclub.model.Route;
import com.id2p.mycarclub.model.User;
import com.id2p.mycarclub.view.EventDetailActivity;
import com.parse.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by anfraga on 2015-07-04.
 */
public class EventsCardAdapter extends RecyclerView.Adapter<EventsCardAdapter.ViewHolder> {
    private static final int EVENTS_COMING_UP = 0;
    private static final int EVENTS_USER_REGISTERED = 1;
    private static final int EVENTS_CREATED_BY_USER = 2;
    private static final int EVENTS_SAME_USER_CHAPTER = 3;
    private static final int EVENTS_USER_PARTICIPATED = 4;
    List<Event> mItems = null;
    User currentUser = null;

    public EventsCardAdapter(int EventListType, User user) throws ParseException {
        super();

        currentUser = user;
        switch(EventListType) {
            case EVENTS_COMING_UP:
                mItems = Event.getEventsComingUp();
                break;
            case EVENTS_CREATED_BY_USER:
                mItems = Event.getUserEvents(currentUser);
                break;
            case EVENTS_USER_REGISTERED:
                mItems = Event.getEventsUserRegistered(currentUser);
                break;
            case EVENTS_SAME_USER_CHAPTER:
                mItems = Event.getChapterEvents(currentUser);
                break;
            case EVENTS_USER_PARTICIPATED:
                // TODO: this will be a list of events user Checked In! Not yet implemented.
                mItems = Event.getEventsUserRegistered(currentUser);
                break;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.event_card_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        Event item = mItems.get(i);
        viewHolder.mEvent = item;
        viewHolder.mEventName.setText(item.getName());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(item.getDate());

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        viewHolder.mEventDate.setText(sdf.format(calendar.getTime()));

        sdf = new SimpleDateFormat("kk:mm", Locale.US);
        viewHolder.mEventTime.setText(sdf.format(calendar.getTime()));

        try {
            Route route = item.getRoute().get(0).fetchIfNeeded();
            viewHolder.mEventLocation.setText(route.getAddress());

            int num = EventRegistration.getNumberOfRegistrations(item);
            viewHolder.mUsersRegistered.setText(num + " Users Currently Registered");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Event getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public Event mEvent;
        public TextView mEventName;
        public TextView mEventDate;
        public TextView mEventTime;
        public TextView mUsersRegistered;
        public TextView mEventLocation;

        public ViewHolder(View itemView) {
            super(itemView);
            mEventName = (TextView) itemView.findViewById(R.id.eventName);
            mEventDate = (TextView) itemView.findViewById(R.id.eventDate);
            mEventTime = (TextView) itemView.findViewById(R.id.eventTime);
            mUsersRegistered = (TextView) itemView.findViewById(R.id.eventNumRegistered);
            mEventLocation = (TextView) itemView.findViewById(R.id.eventLocation);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Event item = mItems.get(getPosition());
            Intent viewEvent = new Intent(view.getContext(), EventDetailActivity.class);
            viewEvent.putExtra("eventId", item.getObjectId());
            view.getContext().startActivity(viewEvent);
        }
    }

}
