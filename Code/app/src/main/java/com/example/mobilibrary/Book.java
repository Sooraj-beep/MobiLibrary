package com.example.mobilibrary;

import android.graphics.Bitmap;
import android.os.Parcelable;
import android.widget.ImageView;

import com.example.mobilibrary.DatabaseController.User;
import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;

public class Book implements Serializable, Comparable<Book> {
    private String firestoreID;
    private static int nextID = 0;
    private String title;
    private String ISBN;
    private String author;
    private String status;
    private User owner;
    // location variable?
    private byte[] image;
    private int id;

    /**Constructor for a new book. Since the new book is not saved to firestore yet,
     *  the firestore id would be null
     *
     * @param title Title of the book
     * @param ISBN  The book's isbn
     * @param author The book's author
     * @param status The book's status
     * @param image  Byte array of image that user can choose to attach to the book
     * @param user  The book's owner
     *
     */
    public Book(String title, String ISBN, String author, String status, byte [] image, User user){
        this.firestoreID = null;
        this.id = nextID;
        this.title = title;
        this.ISBN = ISBN;
        this.author = author;
        this.status = status;
        this.image = image;
        this.owner = user;
        nextID++;
    }

    public Book(String firestoreID, String title, String ISBN, String author, String status, byte [] image, User user){
        this.firestoreID = firestoreID;
        this.title = title;
        this.ISBN = ISBN;
        this.author = author;
        this.status = status;
        this.image = image;
        this.owner = user;
    }

    public int getId() {
        return id;
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

    public byte [] getImage() {
        return image;
    }

    public void setImage(byte [] image) {
        this.image = image;
    }

    public String getFirestoreID() {
        return firestoreID;
    }

    public void setFirestoreID(String firestoreID) {
        this.firestoreID = firestoreID;
    }

    /**
     * Compares a book the book passed in the parameter by comparing their IDs,
     * if they are the same return 0, otherwise return 1
     * @param book
     * @return int value, 0 if the books are the same, 1 otherwise
     */
    @Override
    public int compareTo(Book book){
        if (this.id == book.getId()){
            return 0;
        }
        else {
            return 1;
        }
    }
}
