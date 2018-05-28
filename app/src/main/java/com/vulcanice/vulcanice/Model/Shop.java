package com.vulcanice.vulcanice.Model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by paolo on 4/14/18.
 */

public class Shop {
    private String latitude, longitude, name, description, type;

    /*
        Type:
            1 - Vulcanizing shop
            2 - Gas Station
    */

    public Shop(String name, String description, String type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }
    public void setLocation(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }
}
