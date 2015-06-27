package com.id2p.mycarclub.model;

import com.parse.CountCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by anfraga on 2015-06-03.
 */
@ParseClassName("EventRegistration")
public class EventRegistration extends ParseObject {

    public EventRegistration() {

    }

    public void setEvent(Event event) {
        put("event", event);
    }

    public Event getEvent() {
        return (Event) get("event");
    }

    public void setRegisteredUser(User user) {
        put("user", user);
    }

    public User getUser() {
        return (User) get("user");
    }

    public static void getNumberOfRegistrationsInBackGround(Event event, CountCallback callback) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("EventRegistration");
        query.whereEqualTo("event", event);
        query.countInBackground(callback);
    }

    public static boolean isUserRegistered(Event event, User user) throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("EventRegistration");
        query.whereEqualTo("event", event);
        query.whereEqualTo("user", user);
        return (query.count() > 0);
    }

    public static EventRegistration getRegistration(Event event, User user) throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("EventRegistration");
        query.whereEqualTo("event", event);
        query.whereEqualTo("user", user);
        return (EventRegistration) query.getFirst();
    }

}
