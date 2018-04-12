package com.vulcanice.vulcanice.Model;

/**
 * Created by User on 03/04/2018.
 */

public class User {
    private String email;
    private int status; //1 = online, 0 = offline

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getEmail() { return email; }
    public int getStatus() { return status; }
}
