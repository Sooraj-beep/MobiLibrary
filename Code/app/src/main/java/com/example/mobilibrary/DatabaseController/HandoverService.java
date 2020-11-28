package com.example.mobilibrary.DatabaseController;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mobilibrary.Book;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.time.chrono.IsoChronology;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

        Map<String, Object> newData = new HashMap<>();
        //Add the user whose request has been allowed to borrow the book
        newData.put("BorrowedBy", lendRequest.getRequester());
        batch.update(bookDoc, newData);
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
        batch.update(bookDoc, updates);

        // update book status to borrowed
        batch.update(bookDoc, "Status", "borrowed");
        return batch.commit();
    }

    public static Task<Void> receiveBook(aRequest receiveRequest){
        WriteBatch batch = db.batch();

        DocumentReference bookDoc = db.collection("Books")
                .document(receiveRequest.getBookID());

        Map<String, Object> newData = new HashMap<>();
        // Borrower field is deleted as book is back with owner
        newData.put("BorrowedBy", FieldValue.delete());
        batch.update(bookDoc, newData);
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
