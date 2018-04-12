package com.vulcanice.vulcanice.Model;

/**
 * Created by User on 07/04/2018.
 */

public class Tracking {
    private String email;
    private String uid;
    private String latitude;
    private String longitude;
    private String shopId;
    private String gasStationId;

    private int status; //1 = online, 0 = offline

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public void setGasStationId(String gasStationId) {
        this.gasStationId = gasStationId;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public String getUid() {
        return uid;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getShopId() {
        return shopId;
    }

    public String getGasStationId() {
        return gasStationId;
    }

    public int getStatus() {
        return status;
    }
}
