package com.example.mobilibrary.DatabaseController;

public class Request {
    private final String ID;//to be set only after pulling request from firestore
    private final String requester;
    private final String bookID; //book's firestoreID

    public Request(String ID, String requester, String bookID){
        this.ID = ID;
        this.requester = requester;
        this.bookID = bookID;
    }
    public Request(String requester, String bookID){
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

