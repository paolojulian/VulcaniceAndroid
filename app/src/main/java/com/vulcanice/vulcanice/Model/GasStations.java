package com.vulcanice.vulcanice.Model;

/**
 * Created by paolo on 4/14/18.
 */

public class GasStations {
    private String latitude;
    private String longitude;
    private String ownerId;
    private String name;

    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
