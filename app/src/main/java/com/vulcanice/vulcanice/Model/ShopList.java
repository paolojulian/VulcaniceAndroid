package com.vulcanice.vulcanice.Model;

import com.firebase.geofire.GeoLocation;

public class ShopList {

    private String shop_key;
    private GeoLocation shop_location;


    public ShopList() {}

    public ShopList(String shopKey, GeoLocation shopLocation) {
        this.shop_key = shopKey;
        this.shop_location = shopLocation;
    }

    public String getShop_key() {
        return shop_key;
    }

    public GeoLocation getShop_location() {
        return shop_location;
    }
}
