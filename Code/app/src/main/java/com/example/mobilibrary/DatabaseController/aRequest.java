package com.example.mobilibrary.DatabaseController;

import java.io.Serializable;

public class aRequest {
    private final String ID;//to be set only after pulling request from firestore
    private final String requester;
    private final String bookID; //book's firestoreID

    public aRequest(String ID, String requester, String bookID){
        this.ID = ID;
        this.requester = requester;
        this.bookID = bookID;
    }
    public aRequest(String requester, String bookID){
        this.ID = null;
        this.requester = requester;
        this.bookID = bookID;

    }

    public String getID() {
        return ID;
    }

    public String getRequester() {
        return requester;
    }

    public String getBookID() {
        return bookID;
    }


}

