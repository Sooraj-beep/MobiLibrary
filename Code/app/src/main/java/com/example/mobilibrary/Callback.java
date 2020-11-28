package com.example.mobilibrary;

import com.example.mobilibrary.DatabaseController.User;

/**
 * @author Jill;
 * Used for returning a user during asynchronous tasks for profileActivity
 */
public interface Callback {
    void onCallback(User user);
}

