package com.example.mobilibrary;

import com.example.mobilibrary.DatabaseController.User;

// Used for returning a user during asynchronous tasks
public interface Callback {
    void onCallback(User user);
}

