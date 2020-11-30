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

    /**
     * This method get an instance of RequestService class
     * @return the instance of the class
     */
    public static RequestService getInstance(){
        if (RequestService.requestDb == null)
            RequestService.requestDb = new RequestService();
        return RequestService.requestDb;
    }

    /**
     * This constructor instantiates the single instance of RequestService
     */
    private RequestService(){
        db = FirebaseFirestore.getInstance();
    }

    /**
     * This method adds the requests made to firestore database
     * @param request the request made by borrower
     * @returnn task of type document reference to the request in firestore
     */
    public Task<DocumentReference> createRequest(aRequest request) {
        //public void createRequest(aRequest request) {
        System.out.println("In create Request");
        Map<String, Object> data = new HashMap<>();
        data.put("requester", request.getRequester());
        data.put("bookID", request.getBookID());
        return db.collection("Requests").add(data);

    }

    /**
     * This method handles accepting a request by add a temporary "AcceptedTo"
     * to the desired book and delete this request.
     * @param request the request made by borrower
     * @return task of type Void. This will succeed if all the batch operations succeed, otherwise, fail.
     */
    public static Task<Void> acceptRequest(aRequest request){
        WriteBatch batch = db.batch();

        DocumentReference bookDoc = db.collection("Books")
                .document(request.getBookID());

        batch.update(bookDoc, "Status", "accepted");

        Map<String, Object> newData = new HashMap<>();
        //Add the user whose request has been accepted to the book
        newData.put("AcceptedTo", request.getRequester());

        batch.update(bookDoc, newData);
        return batch.commit();
    }

    /**
     * This method deletes all other request after owner accepts one request.
     * @param IDs firestoreId of all other requests.
     * @return task of type Void. Task succeeds if all delete operations succeed, otherwise, fails.
     */
    public static Task<Void> deleteAll(List<String> IDs){
        WriteBatch batch = db.batch();
        for (String requestID: IDs) {
            batch.delete(db.collection("Requests").document(requestID));
        }
        return batch.commit();
    }

    /**
     * This method handles declining a single request made to a book.
     * @param request the request made by borrower
     * @param declineAll a boolean to check if this request is already the last one.
     *                  If yes, change the status of book back to available.
     * @returnt task of type Void. Task succeeds if all WriteBatch operations succeeds, otherwise, fails.
     */
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





