package com.vulcanice.vulcanice.Model;

/**
 * Created by paolo on 5/24/18.
 */

public class VCN_User {
    public String name, email, user_type;

    public VCN_User() {}

    public VCN_User(String name, String email, String user_type) {
        this.name = name;
        this.email = email;
        this.user_type = user_type;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUser_type() {
        return user_type;
    }
}
