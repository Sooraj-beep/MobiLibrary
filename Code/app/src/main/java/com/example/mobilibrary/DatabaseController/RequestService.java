package com.example.mobilibrary.DatabaseController;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nguyen ;
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

    public static Task<Void> acceptRequest(aRequest request){
        WriteBatch batch = db.batch();

        DocumentReference bookDoc = db.collection("Books")
                .document(request.getBookID());

        Map<String, Object> newData = new HashMap<>();
        //Add the user whose request has been accepted to the book
        newData.put("AcceptedTo", request.getRequester());

        batch.update(bookDoc, newData);
        batch.update(bookDoc, "Status", "accepted");
        return batch.commit();
    }

    //delete all requests in firestore for a book after accepting
    public static Task<Void> deleteAll(List<String> IDs){
        WriteBatch batch = db.batch();
        for (String requestID: IDs) {
            batch.delete(db.collection("Requests").document(requestID));
        }
        return batch.commit();
    }

    public static Task<Void> decline(String requestID){
        return db.collection("Requests").document(requestID).delete();
    }


}







