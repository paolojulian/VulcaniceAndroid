package com.vulcanice.vulcanice.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


/**
 * Created by User on 14/12/2018.
 */

public class Session {
    private final String TAG = "TAG_SESSION";

    private SharedPreferences prefs;

    public Session(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setUser(String uid, VCN_User user) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("UID", uid);
        editor.putString("EMAIL", user.getEmail());
        editor.putString("NAME", user.getName());
        editor.putString("MOBILE", user.getMobile());
        editor.putString("USERTYPE", user.getUser_type());
        editor.commit();
    }

    public void setImage(String image) {
        prefs.edit().putString("IMAGE", image).commit();
    }

    public String getUid() {
        return prefs.getString("UID", "");
    }

    public String getEmail() {
        return prefs.getString("EMAIL", "");
    }

    public String getName() {
        return prefs.getString("NAME", "");
    }

    public String getMobile() {
        return prefs.getString("MOBILE", "");
    }

    public String getUser_type() {
        return prefs.getString("USERTYPE", "");
    }

    /**
     * Checks if session exists
     * @return
     */
    public boolean exists() {
        String uid = prefs.getString("UID", null);
        return uid != null;
    }
}
