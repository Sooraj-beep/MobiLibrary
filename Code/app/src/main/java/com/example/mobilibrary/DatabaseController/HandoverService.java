package com.example.mobilibrary.DatabaseController;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Natalia
 * Class handles updating handover in Firestore
 */
public class HandoverService {
    //Singleton class implementation
    private static HandoverService handoverDb = null;
    private static FirebaseFirestore db;

    public static HandoverService getInstance(){
        if (HandoverService.handoverDb == null)
            HandoverService.handoverDb = new HandoverService();
        return HandoverService.handoverDb;
    }

    private HandoverService(){
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Book's owner lends the book to the borrower
     * @param lendRequest a aRequest object that has the book's id, and name of the requester
     * @return
     */
    public static Task<Void> lendBook(aRequest lendRequest){
        WriteBatch batch = db.batch();

        DocumentReference bookDoc = db.collection("Books")
                .document(lendRequest.getBookID());

        Map<String, Object> updates = new HashMap<>();
        //Add the user whose request has been allowed to borrow the book
        updates.put("BorrowedBy", lendRequest.getRequester());
        batch.update(bookDoc, updates);
        batch.update(bookDoc, "Status", "borrowed");
        return batch.commit();
    }

    public static Task<Void> borrowBook(aRequest borrowRequest){
        WriteBatch batch = db.batch();

        DocumentReference bookDoc = db.collection("Books")
                .document(borrowRequest.getBookID());

        Map<String, Object> updates = new HashMap<>();
        //remove AcceptedTo field as book has been borrowed
        updates.put("AcceptedTo", FieldValue.delete());
        updates.put("LatLang", FieldValue.delete());
        batch.update(bookDoc, updates);
        batch.update(bookDoc, "Status", "borrowed");
        return batch.commit();
    }

    public static Task<Void> receiveBook(aRequest receiveRequest){
        WriteBatch batch = db.batch();

        DocumentReference bookDoc = db.collection("Books")
                .document(receiveRequest.getBookID());

        Map<String, Object> updates = new HashMap<>();
        // Borrower field is deleted as book is back with owner
        updates.put("BorrowedBy", FieldValue.delete());
        updates.put("LatLang", FieldValue.delete());
        batch.update(bookDoc, updates);
        batch.update(bookDoc, "Status", "available");
        return batch.commit();
    }

    public static Task<Void> returnBook(aRequest returnRequest){
        WriteBatch batch = db.batch();

        DocumentReference bookDoc = db.collection("Books")
                .document(returnRequest.getBookID());
        
        // change status to indicate handover
        batch.update(bookDoc, "Status", "available");
        return batch.commit();
    }
}
