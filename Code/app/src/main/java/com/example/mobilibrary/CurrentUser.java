package com.example.mobilibrary;

import com.example.mobilibrary.DatabaseController.User;

/**
 * Singleton for current logged in user.
 */

public class CurrentUser {
    private static CurrentUser currentUser = null;
    //private BookRepository BRepository;
    private User user;

    /**
     * this method gets an instance of currentUser class
     * @return the only instance of currentUser
     */

    public static CurrentUser getInstance(){
        if(currentUser == null)
            currentUser = new CurrentUser();
        return currentUser;

    }

    /**
     * This constructor instantiates the new single instance of currentUser
     */

    private CurrentUser(){
        this.user = null;
    }

    /**
     * This method stores the current user logged into the system
     * @param user logged in user
     */
    public void login(User user){
        this.logout();
        this.user = user;
    }

    /**
     * This method sets currentUser to null after an user logs out of system
     */
    public void logout(){
        this.user = null;
    }

    /**
     * This method used to get the current user logged in
     * @return current user
     */
    public User getCurrentUser(){
        return this.user;
    }

}
