package com.route.sujoy.routenotifications;

/**
 * Created by Sujoy on 31-05-2015.
 */
public class User {
    private String userEmail;

    public User(String rEmail, String rPassword) {
        userEmail = rEmail;
        userPassword = rPassword;
    }


    public User() {

    }
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    private String uuid;
    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    private String userPassword;

}
