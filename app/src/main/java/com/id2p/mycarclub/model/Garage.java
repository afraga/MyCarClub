package com.id2p.mycarclub.model;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by anfraga on 2015-06-09.
 */
@ParseClassName("Garage")
public class Garage extends ParseObject {

    public Garage() {

    }

    public void setOwner(User owner) {
        put("owner", owner);
    }

    public User getOwner() {
        return (User) get("owner");
    }

    public void setCarMake(String make) {
        put("make", make);
    }

    public String getCarMake() {
        return getString("make");
    }

    public void setCarModel(String model) {
        put("model", model);
    }

    public String getCarModel() {
        return getString("model");
    }

    public void setCarYear(int year) {
        put("year", year);
    }

    public Integer getCarYear() {
        return getInt("year");
    }

    public void setCarDetails(String details) {
        put("details", details);
    }

    public String getCarDetails() {
        return getString("details");
    }

    public void setCarThumbImages(List<ParseFile> thumbs) {
        put("thumbnails", thumbs);
    }

    public List<ParseFile> getCarThumbImages() {
        return (List<ParseFile>) get("thumbnails");
    }

    public void setCarImages(List<ParseFile> images) {
        put("images", images);
    }

    public List<ParseFile> getCarImages() {
        return (List<ParseFile>) get("images");
    }

    public static List<Garage> getUserGarage(User user) throws ParseException {
        ParseQuery<Garage> query = new ParseQuery<Garage>("Garage");
        query.whereEqualTo("owner", user);
        List<Garage> garageList = query.find();
        return garageList;
    }

}
