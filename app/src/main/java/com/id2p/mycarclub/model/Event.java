package com.id2p.mycarclub.model;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import java.util.ArrayList;
import java.util.Calendar;
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

    // TODO: experiment with doing in background later
    public static List<Event> getEventsComingUp() throws ParseException {
        // we will define coming up as in the next 30 days
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        calendar.add(Calendar.DATE, 30);
        Date endDate = calendar.getTime();

        ParseQuery<Event> query = new ParseQuery<Event>("Event");
        query.whereGreaterThan("date", today);
        query.whereLessThan("date", endDate);
        List<Event> eventList = query.find();
        return eventList;
    }

    public static void getEventsComingUpInBackground(FindCallback<Event> callback) {
        // we will define coming up as in the next 30 days
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        calendar.add(Calendar.DATE, 30);
        Date endDate = calendar.getTime();

        ParseQuery<Event> query = new ParseQuery("Event");
        query.whereGreaterThan("date", today);
        query.whereLessThan("date", endDate);
        query.findInBackground(callback);
    }

    // TODO: experiment with doing in background later
    public static List<Event> getEventsUserRegistered(User user) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        calendar.add(Calendar.DATE, 30);
        Date endDate = calendar.getTime();

        List<Event> eventsRegisteredFor = new ArrayList<Event>();

        List<EventRegistration> userRegistrations = EventRegistration.getUserEventRegistrations(user);
        for (EventRegistration registration : userRegistrations) {
            eventsRegisteredFor.add(registration.getEvent());
        }

        // TODO: need to debug this to make sure it is working as expected when UI is ready for it
        ParseQuery<Event> query = new ParseQuery<Event>("Event");
        query.whereGreaterThan("date", today);
        query.whereContainedIn("objectId", eventsRegisteredFor);
        List<Event> eventList = query.find();
        return eventList;
    }

    public static Event getEventById(String eventId) throws ParseException {
        ParseQuery<Event> query = new ParseQuery<Event>("Event");
        query.whereEqualTo("objectId", eventId);
        return query.getFirst();
    }

    public static void getEventByIdInBackGround(String eventId, GetCallback<Event> callback) {
        ParseQuery<Event> query = ParseQuery.getQuery("Event");
        query.include("route");
        query.getInBackground(eventId, callback);
    }

    public static void deleteEventInBackGround(Event event, DeleteCallback callback) {
        List<Route> routeList = event.getRoute();
        for (Route route : routeList) {
            route.deleteEventually();
        }

        event.deleteInBackground(callback);
    }

}
