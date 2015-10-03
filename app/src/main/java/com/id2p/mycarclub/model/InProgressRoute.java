package com.id2p.mycarclub.model;

import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;


/**
 * Created by anfraga on 2015-09-30.
 */
@ParseClassName("InProgressRoute")
public class InProgressRoute extends ParseObject {

    public InProgressRoute() {

    }

    public void setEvent(Event event) {
        put("event", event);
    }

    public Event getEvent() {
        return (Event) get("event");
    }

    public void setHeadLocation(ParseGeoPoint headLocation) {
        put("headLocation", headLocation);
    }

    public ParseGeoPoint getHeadLocation() {
        return getParseGeoPoint("headLocation");
    }

    public void setTailLocation(ParseGeoPoint tailLocation) {
        put("tailGeoPoint", tailLocation);
    }

    public ParseGeoPoint getTailLocation() {
        return getParseGeoPoint("tailGeoPoint");
    }

    public static void getInProgressRouteForEventInBackground(Event event, GetCallback<InProgressRoute> callback) {
        ParseQuery<InProgressRoute> query = ParseQuery.getQuery("InProgressRoute");
        query.whereEqualTo("event", event);
        query.getFirstInBackground(callback);
    }


}
