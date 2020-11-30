package com.example.mobilibrary;

import com.example.mobilibrary.DatabaseController.User;

import java.io.Serializable;

/**
 * Class for Book objects.
 */
public class Book implements Serializable {
    private String firestoreID;
    private static int nextID = 0;
    private String title;
    private String ISBN;
    private String author;
    private String status;
    private User owner;
    private String imageId;

   public Book(String title, String ISBN, String author, String status, String image, User user){
        this.firestoreID = null;
        this.title = title;
        this.ISBN = ISBN;
        this.author = author;
        this.status = status;
        this.imageId = image;
        this.owner = user;
        nextID++;
    }

    public Book(String firestoreID, String title, String ISBN, String author, String status, String imageId, User user){
        this.firestoreID = firestoreID;
        this.title = title;
        this.ISBN = ISBN;
        this.author = author;
        this.status = status;
        this.imageId = imageId;
        this.owner = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getFirestoreID() {
        return firestoreID;
    }

    public void setFirestoreID(String firestoreID) {
        this.firestoreID = firestoreID;
    }
}
