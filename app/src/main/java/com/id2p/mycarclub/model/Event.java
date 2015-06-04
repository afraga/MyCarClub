package com.id2p.mycarclub.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

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

    public void setOrganizer(ParseUser organizer) {
        put("organizer", organizer);
    }

    public ParseUser getOrganizer() {
        return getParseUser("organizer");
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

}
