package com.id2p.mycarclub.model;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

/**
 * Created by anfraga on 2015-06-03.
 */
@ParseClassName("Route")
public class Route extends ParseObject {

    private double distanceInKm = -1;

    public Route() {

    }

    public void setAddress(String address) {
        put("address", address);
    }

    public String getAddress() {
        return getString("address");
    }

    public void setGeoPoint(ParseGeoPoint geoPoint) {
        put("geopoint", geoPoint);
    }

    public ParseGeoPoint getGeoPoint() {
        return getParseGeoPoint("geopoint");
    }

    public void setDistanceInKm(double distance) {
        distanceInKm = distance;
    }

    public double getDistanceInKm() {
        return distanceInKm;
    }

    @Override
    public String toString() {
        return getAddress();
    }
}
