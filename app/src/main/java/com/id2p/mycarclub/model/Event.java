package com.id2p.mycarclub.model;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import java.util.Date;
import java.util.List;

/**
 * Created by anfraga on 2015-06-03.
 */
@ParseClassName("Event")
public class Event extends ParseObject {

    public Event() {

    }

    public void setName(String name) {
        put("name", name);
    }

    public String getName() {
        return getString("name");
    }

    public void setDescription(String description) {
        put("description", description);
    }

    public String getDescription() {
        return getString("description");
    }

    public void setOrganizer(User organizer) {
        put("organizer", organizer);
    }

    public User getOrganizer() {
        return (User) get("organizer");
    }

    public void setRoute(List<Route> route) {
        put("route", route);
    }

    public List<Route> getRoute() {
        return getList("route");
    }

    public void setChapter(String chapter) {
        put("chapter", chapter);
    }

    public String getChapter() {
        return getString("chapter");
    }

    public void setDate(Date date) {
        put("date", date);
    }

    public Date getDate() {
        return getDate("date");
    }

    public static List<Event> getUserEvents(User user) throws ParseException {
        ParseQuery<Event> query = new ParseQuery<Event>("Event");
        query.whereEqualTo("organizer", user);
        List<Event> eventList = query.find();
        return eventList;
    }

    public static List<Event> getChapterEvents(User user) throws ParseException {
        ParseQuery<Event> query = new ParseQuery<Event>("Event");
        query.whereEqualTo("chapter", user.getChapter());
        List<Event> eventList = query.find();
        return eventList;
    }

}
