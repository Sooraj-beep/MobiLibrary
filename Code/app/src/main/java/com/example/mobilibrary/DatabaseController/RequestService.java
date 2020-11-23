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
import com.google.android.gms.tasks.TaskCompletionSource;
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

import java.time.chrono.IsoChronology;
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
        getBookfromFirestore(request.getBookID())
                .addOnCompleteListener(new OnCompleteListener<Book>() {
            @Override
            public void onComplete(@NonNull Task<Book> task) {
                Book currentBook = task.getResult();
                newData.put("Accepted", FieldValue.arrayUnion(currentBook));
                batch.update(userDoc, newData);
                batch.delete(requestDoc);
                batch.update(bookDoc, "status", "Accepted");
                batch.commit();
            }
        });
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

    public static Task<Book> getBookfromFirestore(final String bookID){
        return db.collection("Books")
                .document(bookID)
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful()){
                        DocumentSnapshot doc = task.getResult();
                        String title = doc.getString("Title");
                        String ISBN = doc.getString("ISBN");
                        String author = doc.getString("Author");
                        String status = doc.getString("Status");
                        String imageId = doc.getString("imageID");
                        String username = doc.getString("Owner");
                        return getUserbyUsername(username).continueWith(task1 -> {
                            User owner = task1.getResult();
                            Book book = new Book(doc.getId(), title, ISBN, author, status, imageId, owner);
                            return book;
                        });
                    } else {
                        TaskCompletionSource<Book> source = new TaskCompletionSource<Book>();
                        source.setException(task.getException());
                        return source.getTask();
                    }
                });

        }
    public static Task<User> getUserbyUsername(final String username){
        return db.collection("Users")
                .document(username)
                .get()
                .continueWith(new Continuation<DocumentSnapshot, User>() {
                    @Override
                    public User then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                        DocumentSnapshot doc = task.getResult();
                        User user = new User(username, doc.getString("email"), doc.getString("name"), doc.getString("phone"));
                        return user;
                    }
                });
    }
}







