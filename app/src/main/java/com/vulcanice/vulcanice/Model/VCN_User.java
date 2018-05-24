package com.vulcanice.vulcanice.Model;

/**
 * Created by paolo on 5/24/18.
 */

public class VCN_User {
    public String name, email;
    public Integer user_type;

    public VCN_User() {}

    public VCN_User(String name, String email, String user_type) {
        this.name = name;
        this.email = email;

        if ( user_type == "Client" )
        {
            this.user_type = 2;
        }
        else
        {
            this.user_type = 1;
        }
    }
}
