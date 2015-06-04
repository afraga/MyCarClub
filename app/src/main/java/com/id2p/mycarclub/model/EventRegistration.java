package com.id2p.mycarclub.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

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

    public void setRegisteredUsers(List<User> registeredUsers) {
        put("registeredUsers", registeredUsers);
    }

    public List<User> getRegisteredUsers() {
        return getList("registeredUsers");
    }
}
