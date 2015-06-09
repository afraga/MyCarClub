package com.id2p.mycarclub.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by anfraga on 2015-06-05.
 */
@ParseClassName("Ad")
public class Ad extends ParseObject {

    public Ad() { }

    public void setOwner(User user) {
        put("owner", user);
    }

    public User getOwner() {
        return (User) get("owner");
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

    public void setType(String adType) {
        put("type", adType);
    }

    public String getAdType() {
        return getString("type");
    }

    public void setCategory(String category) {
        put("category", category);
    }

    public String getCategory() {
        return getString("category");
    }

    public void setPrice(double price) {
        put("price", price);
    }

    public double getPrice() {
        return getDouble("price");
    }

    public void setLocation(Route location) {
        put("location", location);
    }

    public Route getLocation() {
        return (Route) get("location");
    }

    public void setThumbImages(List<ParseFile> thumbs) {
        put("thumbnails", thumbs);
    }

    public List<ParseFile> getThumbnails() {
        return (List<ParseFile>) get("thumbnails");
    }

    public void setImages(List<ParseFile> images) {
        put("images", images);
    }

    public List<ParseFile> getImages() {
        return (List<ParseFile>) get("images");
    }
}
