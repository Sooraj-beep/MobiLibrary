package com.example.mobilibrary.DatabaseController;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mobilibrary.Book;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestService {
    //Singleton class implementation
    private static RequestService requestDb = null;
    private FirebaseFirestore db;

    public static RequestService getInstance(){
        if (RequestService.requestDb == null)
            RequestService.requestDb = new RequestService();
        return RequestService.requestDb;
    }

    private RequestService(){
        db = FirebaseFirestore.getInstance();
    }

// call: RequestService.createRequest.addOnCompleteListener(task->{if task.issuccesfull(): print message else: print failed message)
    public Task<DocumentReference> createRequest(Request request) {
        Map<String, Object> data = new HashMap<>();
        data.put("requester", request.getRequester());
        data.put("bookID", request.getBookID());
        Log.d("SOORAJ", "createRequest: ");
        return db.collection("Requests").add(data);
    }

    public ListenerRegistration getRequests(Book book, final DataListener<List<Request>> dataListener ){
        return db.collection("Requests").whereEqualTo("bookID", book.getFirestoreID())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                        List<Request> requests = new ArrayList<>();
                        for (QueryDocumentSnapshot doc: queryDocumentSnapshots)
                            requests.add(getRequestfromFirestore(doc));

                        dataListener.onDataChange(requests);
                    }
                });
    }

    //used to pull a request from firestore
    public Request getRequestfromFirestore(DocumentSnapshot documentSnapshot){
        String requester = documentSnapshot.getString("requester");
        String bookID = documentSnapshot.getString("bookID");
        return new Request(documentSnapshot.getId(), requester, bookID);
    }

    public Task<Void> acceptRequest(Request request){
        WriteBatch batch = db.batch();

        DocumentReference requestDoc = db.collection("Requests")
                .document(request.getID());

        DocumentReference bookDoc = db.collection("Books")
                .document(request.getBookID());

        DocumentReference userDoc = db.collection("Users")
                .document(request.getRequester());

        Map<String, Object> newData = new HashMap<>();
        newData.put("Borrowing", FieldValue.arrayUnion(request.getBookID()));
        batch.update(userDoc, newData);
        batch.delete(requestDoc);
        batch.update(bookDoc, "status", "Borrowed");
        return batch.commit();
    }

    public Task<Void> declineOthers(List<String> IDs){
        WriteBatch batch = db.batch();
        for (String requestID: IDs) {
            batch.delete(db.collection("Requests").document(requestID));
        }
        return batch.commit();
    }

    public Task<Void> decline(String requestID){
        return db.collection("Requests").document(requestID).delete();
    }
}






