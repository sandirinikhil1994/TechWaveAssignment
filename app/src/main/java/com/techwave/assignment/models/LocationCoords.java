package com.techwave.assignment.models;

public class LocationCoords {

    private double lat;
    private double lon;
    private String ts = "";

    public LocationCoords(double lat1, double lon1) {
        this.lat = lat1;
        this.lon = lon1;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }
}
