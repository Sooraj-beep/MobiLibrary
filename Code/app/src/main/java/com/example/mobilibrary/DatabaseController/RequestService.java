package com.example.mobilibrary.DatabaseController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to work with all requests in Firestore.
 */

public class RequestService {
    //Singleton class implementation
    private static RequestService requestDb = null;
    private static FirebaseFirestore db;

    public static RequestService getInstance(){
        if (RequestService.requestDb == null)
            RequestService.requestDb = new RequestService();
        return RequestService.requestDb;
    }

    private RequestService(){
        db = FirebaseFirestore.getInstance();
    }


    // call: RequestService.createRequest.addOnCompleteListener(task->{if task.issuccesfull(): print message else: print failed message)
    public Task<DocumentReference> createRequest(aRequest request) {
        //public void createRequest(aRequest request) {
        System.out.println("In create Request");
        Map<String, Object> data = new HashMap<>();
        data.put("requester", request.getRequester());
        data.put("bookID", request.getBookID());
        return db.collection("Requests").add(data);

    }

    public void acceptRequest(aRequest request, OnSuccessListener<Void> successListener, OnFailureListener failureListener){
        final DocumentReference bookDoc = db.collection("Books")
                .document(request.getBookID());

        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                transaction.update(bookDoc, "Status", "accepted");
                Map<String, Object> newData = new HashMap<>();
                newData.put("AcceptedTo", request.getRequester());
                transaction.update(bookDoc, newData);

                return null;
            }
        }).addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);

    }

    //delete all requests in firestore for a book after accepting
    public static Task<Void> deleteAll(List<String> IDs){
        WriteBatch batch = db.batch();
        for (String requestID: IDs) {
            batch.delete(db.collection("Requests").document(requestID));
        }
        return batch.commit();
    }

    public static Task<Void> decline(aRequest request, boolean declineAll) {
        WriteBatch batch = db.batch();

        DocumentReference bookDoc = db.collection("Books")
                .document(request.getBookID());

        DocumentReference requestDoc = db.collection("Requests")
                .document(request.getID());
        if (declineAll)
            batch.update(bookDoc, "Status", "available");

        batch.delete(requestDoc);
        return batch.commit();

    }
}





