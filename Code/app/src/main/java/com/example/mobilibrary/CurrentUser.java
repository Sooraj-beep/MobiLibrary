package com.example.mobilibrary;

import com.example.mobilibrary.DatabaseController.User;

public class CurrentUser {
    private static CurrentUser currentUser = null;
    //private BookRepository BRepository;
    private User user;

    public static CurrentUser getInstance(){
        if(currentUser == null)
            currentUser = new CurrentUser();
        return currentUser;

    }

    private CurrentUser(){
        this.user = null;

    }

    public void login(User user){
        this.logout();
        this.user = user;
    }

    public void logout(){
        this.user = null;
    }

    public User getCurrentUser(){
        return this.user;
    }

}
