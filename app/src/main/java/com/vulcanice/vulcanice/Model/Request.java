package com.vulcanice.vulcanice.Model;

/**
 * Created by User on 16/06/2018.
 */

public class Request {
    protected String description, clientName, clientUid, isAccepted;
    protected Boolean isValid;
    public Request () {
    }

    public String getIsAccepted() {
        return isAccepted;
    }

    public void setIsAccepted(String isAccepted) {
        this.isAccepted = isAccepted;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Boolean getValid() {
        return isValid;
    }

    public void setValid(boolean b) {
        isValid = b;
    }

    public String getClientUid() {
        return clientUid;
    }

    public void setClientUid(String clientUid) {
        this.clientUid = clientUid;
    }
}
