package com.example.mobilibrary.DatabaseController;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mobilibrary.Book;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class interacts with the database to get books on the cloud
 * and do all the database related tasks for the books

 */
public class BookService {
    private static final String TAG = "AddBookFragment";
    //Singleton class implementation
    private static BookService bookDb = null;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    /**
     * This methods gets the instance of BookService class. Creates one if it does not exist
     * @return the instance of BookService class
     */
    public static BookService getInstance(){
        if (BookService.bookDb == null)
            BookService.bookDb = new BookService();

        return BookService.bookDb;
    }

    /**
     * Singleton class implementation. This constructor instantiating the only instance of BookService.
     */
    private BookService(){
        db = FirebaseFirestore.getInstance();
    }

    /**
     * This method attempts to add a new book to the database. Two different toast messages one for success and
     * one for failure
     * @param context the current construct
     * @param newBook new Book object
     */

    public void addBook(final Context context, Book newBook){
        // Checks if the book is already added to database
        if (newBook.getFirestoreID()!= null)
            throw new IllegalArgumentException("This book is already added to the database");
         Map<String, Object> data = new HashMap<>();
         data.put("Title", newBook.getTitle());
         data.put("ISBN", newBook.getISBN());
         data.put("Author", newBook.getAuthor());
         data.put("Status", newBook.getStatus());
         data.put("Owner", newBook.getOwner().getUsername());
         data.put("imageID", newBook.getImageId());
         db.collection("Books").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
             @Override
             public void onSuccess(DocumentReference documentReference) {
                 newBook.setFirestoreID(documentReference.getId());
                 Toast.makeText(context, "Successfully added book!", Toast.LENGTH_SHORT).show();
             }
         }).addOnFailureListener(new OnFailureListener() {
             @Override
             public void onFailure(@NonNull Exception e) {
                 Log.e(TAG,"Failed with: " +e.toString());
                 Toast.makeText(context, "Book not added!" , Toast.LENGTH_SHORT).show();
             }
         });
    }

    /**
     *
     * @param id
     * @param imageBitmap
     * @param successListener
     * @param failureListener
     */

    public void uploadImage(String id, Bitmap imageBitmap, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference ref = storageReference.child("books/" + id + ".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if(imageBitmap != null) {
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        }
        byte[] data = baos.toByteArray();

        final UploadTask uploadTask = ref.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return ref.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downUri = task.getResult();
                            Log.d("Final URL", "onComplete: Url: " + downUri.toString());
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Final URL", "could no upload image");
            }
        });
    }

    /**
     * Attempts to delete book from database. Two different Toast messages depending on success or
     * failure of operation.
     * @param context current construct
     * @param deleteBook book object to delete
     */
    public void deleteBook(final Context context, Book deleteBook) {
        if (deleteBook.getFirestoreID() == null)
            throw new IllegalArgumentException("This book is not in database");
        // delete document
        db.collection("Books").document(deleteBook.getFirestoreID()).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Successfully deleted book!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"Failed with: " +e.toString());
                        Toast.makeText(context, "Book not deleted!" , Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Attempts to edit a book in the database. Two different Toast messages depending on success or
     * failure of operation.
     * @param context current construct
     * @param editBook book object to edit
     */
    public void editBook(final Context context, Book editBook) {
        System.out.println("IN EDITBOOK IN BOOKSERVICE,  get FirestoreId: " + editBook.getFirestoreID());
        if (editBook.getFirestoreID() == null)
            throw new IllegalArgumentException("This book is not in database");

        // create hash map of fields that could be changed in editBook
        ;
        Map<String, Object> data = new HashMap<>();
        data.put("ISBN", editBook.getISBN());
        data.put("Author", editBook.getAuthor());
        data.put("Image", editBook.getImageId());
        data.put("Title", editBook.getTitle());
        data.put("imageID", editBook.getImageId());
        // edit document
        db.collection("Books").document(editBook.getFirestoreID()).update(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Successfully edited book!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"Failed with: " +e.toString());
                        Toast.makeText(context, "Book not edited!" , Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void changeStatus(final Context context, final Book book, final String newStatus){
        System.out.println("Book title clicked is: " + book.getTitle());
        //Map<String, Object> data = new HashMap<>();
        //data.put("Status", newStatus);

        db.collection("Books").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                for (DocumentSnapshot snapshot : value) {
                    if (book.getTitle().equals(snapshot.getString("Title"))) {
                        System.out.println("found book");
                        System.out.println("Document id: " + snapshot.getId());
                        Map<String, Object> data = new HashMap<>();
                        data.put("Status", newStatus);
                        db.collection("Books").document(snapshot.getId())
                                .set(data, SetOptions.merge());
                    }
                }

            }
        });

    }
}
