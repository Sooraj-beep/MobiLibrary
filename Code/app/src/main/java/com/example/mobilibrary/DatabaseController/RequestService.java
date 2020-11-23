package com.example.mobilibrary.DatabaseController;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mobilibrary.Book;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
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

        DocumentReference requestDoc = db.collection("Requests")
                .document(request.getID());

        DocumentReference bookDoc = db.collection("Books")
                .document(request.getBookID());

        DocumentReference userDoc = db.collection("Users")
                .document(request.getRequester());

        Map<String, Object> newData = new HashMap<>();
        DocumentSnapshot book_doc = db.collection("Books")
                .document(request.getBookID())
                .get()
                .getResult();

        DocumentSnapshot owner_doc = db.collection("Users")
                .document(book_doc.getString("Owner"))
                .get()
                .getResult();

        User owner = new User(owner_doc.getId(), owner_doc.getString("email"), owner_doc.getString("name"), owner_doc.getString("phoneNo"));
        Book currentBook = new Book(book_doc.getId(), book_doc.getString("Title"), book_doc.getString("ISBN"), book_doc.getString("Author"),
                book_doc.getString("status"), book_doc.getString("imageID"), owner);

        newData.put("Borrowing", FieldValue.arrayUnion(currentBook));
        batch.update(userDoc, newData);
        batch.delete(requestDoc);
        batch.update(bookDoc, "status", "Borrowed");
        return batch.commit();
    }

    public static Task<Void> declineOthers(List<String> IDs){
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







