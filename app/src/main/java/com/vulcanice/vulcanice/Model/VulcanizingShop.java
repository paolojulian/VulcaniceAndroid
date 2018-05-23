package com.vulcanice.vulcanice.Model;

/**
 * Created by User on 12/04/2018.
 */

public class VulcanizingShop {
    private String latitude;
    private String longitude;
    private String name;

    public VulcanizingShop(String latitude, String longitude, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public String getLatitude() {
        return latitude;
    }
    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
