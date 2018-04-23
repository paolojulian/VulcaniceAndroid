package com.vulcanice.vulcanice.Model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by paolo on 4/14/18.
 */

public class Shop {
    DatabaseReference onlineShops;
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

    public void insert( String type ) {

        onlineShops = FirebaseDatabase.getInstance().getReference("Shops");
        onlineShops.child(type).setValue(this);
    }
}
