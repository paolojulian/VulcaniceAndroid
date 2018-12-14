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

    public String getUser(String index) {
        String hold = prefs.getString(index, "");
        if (hold == null) {
            Log.w(TAG, "No com.vulcanice.vulcanice.Model.Session with index of " + index);
            return "";
        }
        return hold;
    }
}
