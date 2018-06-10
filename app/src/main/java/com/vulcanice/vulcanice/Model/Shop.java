package com.vulcanice.vulcanice.Model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by paolo on 4/14/18.
 */

public class Shop {
    private String name, description, type, owner;
    private String latitude, longitude;

    /*
        Type:
            1 - Vulcanizing shop
            2 - Gas Station
    */

    public Shop() {}

    public void setLocation(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getOwner() { return owner; }

    public void setOwner(String owner) {
        this.owner = owner;
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

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean is_valid() {
        if (
            this.name == "" ||
            this.description == "" ||
            this.type == ""
        )
        {
            return false;
        }
        return true;
    }

    public boolean is_location_valid() {
        if ( this.latitude == "" || this.longitude == "" )
        {
            return false;
        }
        return true;
    }
}
