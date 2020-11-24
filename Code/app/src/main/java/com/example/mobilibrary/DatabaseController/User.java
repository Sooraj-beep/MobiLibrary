package com.example.mobilibrary.DatabaseController;

import java.io.Serializable;

/**
 * @author Nguyen;
 * This is a class for User objects.
 */

public class User implements Serializable {
    private final String username;
    private String email;
    private final String name;
    private String phoneNo;

    /**
     * Full constructor
     *
     * @param username: unique username
     * @param email:    user's email address
     * @param name:     user's name
     * @param phoneNo:  user's phone number
     */
    public User(String username, String email, String name, String phoneNo) {
        if (username == null || email == null || name == null || phoneNo == null) {
            throw new IllegalArgumentException("These fields must not be null.");
        }
        this.username = username;
        this.email = email;
        this.name = name;
        this.phoneNo = phoneNo;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNo() {
        return phoneNo;
    }
}

