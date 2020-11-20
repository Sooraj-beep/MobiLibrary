package com.example.mobilibrary;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilibrary.Activity.ProfileActivity;
import com.example.mobilibrary.DatabaseController.BookService;
import com.example.mobilibrary.DatabaseController.User;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * This class takes in a book and displays its details (Title, Author, Owner, ISBN and Status),
 * requests currently on the book, and, if available, the book's photograph.
 * Additionally, this class can toggle between displaying the book details and the list of requests on the book
 */
public class BookDetailsFragment extends AppCompatActivity {
    private TextView title;
    private TextView author;
    private TextView owner;
    private TextView ISBN;
    private TextView status;
    private TextView ownerTitle;
    private TextView isbnTitle;
    private TextView statusTitle;
    private FloatingActionButton backButton;
    private ImageButton editButton;
    private FloatingActionButton deleteButton;
    private String req_users [] = {"Natalia", "Chloe", "Kimberly", "Jill", "Nguyen", "Sooraj"}; // sort of a placeholder this one, need to replace with actual requesting users once we implement it
    private Button detailsBtn;
    private Button requestsBtn;
    private TextView[] requestAssets;
    private ImageView photo;
    private ListView reqList;
    private Bitmap editBitMap = null;
    private ArrayAdapter<String> reqAdapter;
    private ArrayList<String> reqDataList;

    private FirebaseFirestore db;
    private BookService bookService;
    private Context context;
    private Button requestButton;

    /**
     * Creates the activity for viewing books and the requests on them, and the necessary logic to do so
     * @param SavedInstances The book to be viewed
     */
    @Override
    protected void onCreate (@Nullable Bundle SavedInstances) {
        super.onCreate(SavedInstances);
        setContentView(R.layout.layout_book_details_fragment);

        // set each variable to correct view
        title =  findViewById(R.id.view_title);
        author = findViewById(R.id.view_author);
        owner = findViewById(R.id.view_owner);
        status = findViewById(R.id.view_status);
        ISBN = findViewById(R.id.view_isbn);
        backButton = findViewById(R.id.back_to_books_button);
        editButton = findViewById(R.id.edit_button);
        deleteButton = findViewById(R.id.delete_button);
        photo = findViewById(R.id.imageView);
        detailsBtn = findViewById(R.id.detailsBtn);
        requestsBtn = findViewById(R.id.reqBtn);
        reqList = findViewById(R.id.reqListView);
        ownerTitle = findViewById(R.id.view_owner_title);
        isbnTitle = findViewById(R.id.view_isbn_title);
        statusTitle = findViewById(R.id.view_status_title);

        requestButton = findViewById(R.id.request_button);

        //set all status changing buttons to be invisible
        requestButton.setVisibility(View.GONE);

        // set up firestore instance
        bookService = BookService.getInstance();
        context = getApplicationContext();

        // check that a book was passed to this activity, otherwise end the activity
        if (getIntent() == null) {
            finish();
        }
        final Book viewBook = (Book) getIntent().getSerializableExtra("view book");

        // fill fields with correct information from the passed book
        title.setText(viewBook.getTitle());
        author.setText(viewBook.getAuthor());
        owner.setText(viewBook.getOwner().getUsername());
        ISBN.setText(viewBook.getISBN());
        status.setText(viewBook.getStatus());
        Bitmap bitmap = null;
        System.out.println("CLICKED BOOK GET TITLE: " + viewBook.getTitle());
        System.out.println("CLICKED BOOK GET IMAGE: " + viewBook.getImageId());
        if(viewBook.getImageId() != null){
            convertImage(viewBook.getImageId());
        } else {
            photo.setImageBitmap(null);
        }

        //get current user name and book owners name, check if they match
        String userName = getUsername();
        String bookOwner = viewBook.getOwner().getUsername();
        if (userName.equals(bookOwner)) { //user is looking at their own book (only happens when on myBooks page), can edit or delete, view requests, etc
            // hide request list at open of activity
            requestAssets = new TextView[]{title, author, owner, status, ownerTitle,ISBN, isbnTitle, statusTitle };
            reqDataList = new ArrayList<>();
            for (String user: req_users){
                reqDataList.add(user + " has requested your book");
            }
            reqAdapter =  new ArrayAdapter<String>(this,R.layout.req_custom_list, R.id.textView, reqDataList);
            reqList.setAdapter(reqAdapter);
            reqList.setVisibility(View.GONE);


        } else{ //user is looking at another user's book (from homepage), hide the edit, delete, two tabs buttons. Depending on the status of the book will show diff buttons (request, borrow, etc)
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            detailsBtn.setVisibility(View.GONE);
            requestsBtn.setVisibility(View.GONE);
            reqList.setVisibility(View.GONE);

            //get book status
            //if book is available/requested, show request button
            requestButton.setVisibility(View.VISIBLE);


        }

        /**
         * If Back Button is pressed, return to list of owned books, any changes in the book will be saved
         * and the book's information updated accordingly
         */
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // only return things from this intention if something was edited
                if ((title.getText().toString() != viewBook.getTitle()) ||
                        (author.getText().toString() != viewBook.getAuthor()) ||
                        (ISBN.equals(viewBook.getISBN()))){
                    viewBook.setTitle(title.getText().toString());
                    viewBook.setAuthor(author.getText().toString());
                    viewBook.setISBN(ISBN.getText().toString().replaceAll(" ", ""));

                    // if a book has a photo pass along the photo's bitmap
                    if (editBitMap != null) {
                        viewBook.setImageId(editBitMap.toString());
                    } else {
                        viewBook.setImageId(null);    // book has no photo so image bitmap is set to null
                    }
                    //If photo changed, pass along to firebase
                    if (photo != null) {
                        //System.out.println("Uploading book, id: " + editBitMap.toString());
                        bookService.uploadImage(viewBook.getImageId(), editBitMap, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(BookDetailsFragment.this, "Failed to add image.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    // return the book with its changed fields
                    Intent editedIntent = new Intent();
                    editedIntent.putExtra("edited book", viewBook);
                    setResult(2, editedIntent);
                }
                finish();
            }
        });

        /**
         * If Delete Button is pressed, return to list of owned books and pass this book along as marked
         * as to be deleted
         */
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // delete book from current user and firestore instance using the callback function
                currentUser(new Callback() {
                    @Override
                    public void onCallback(User user) {
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                        storageReference.child("books/" + viewBook.getImageId() + ".jpg").delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        String TAG = "editBookFragment";
                                        Log.d(TAG, "onSuccess: deleted file");
                                    }
                                });

                        bookService.deleteBook(context, viewBook);  // delete book from firestore
                        Intent deleteIntent = new Intent();
                        deleteIntent.putExtra("delete book", viewBook); // mark book to be deleted in app
                        setResult(1, deleteIntent);
                        finish();
                    }
                });
            }
        });

        /**
         * If Edit Button is pressed, open EditBookFragment activity and pass it the book to edit its fields
         */
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("VIEWED BOOK FIRESTOREID: " + viewBook.getFirestoreID());
                Intent editIntent = new Intent(BookDetailsFragment.this, EditBookFragment.class);
                editIntent.putExtra("edit", viewBook);
                startActivityForResult(editIntent, 2);
            }
        });

        /**
         * If Request Button is pressed, change Book status to request, and change the button
         */
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //change book status to requested
                //System.out.println("VIEWED BOOK FIRESTOREID: " + viewBook.getFirestoreID());
                viewBook.setStatus("requested");
                requestButton.setText("Requested");
                bookService.changeStatus(context, viewBook, "requested");
                //later add: make sure button text stays "requested" when user who already requested clicks on it again

            }
        });

        /**
         * Toggles view, shows request list and hides book details
         */
        requestsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                for (TextView asset : requestAssets) {
                    asset.setVisibility(View.GONE);
                }
                reqList.setVisibility(View.VISIBLE);


            }
        });

        /**
         * Toggles view, hides request list and shows book details
         */
        detailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (TextView asset : requestAssets) {
                    asset.setVisibility(View.VISIBLE);
                }
                reqList.setVisibility(View.GONE);
            }
        });

        //Opens the profile of the user who owns the book.
        owner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("profile", owner.getText());
                startActivity(intent);
            }
        });
    }

    //get current username
    private String getUsername(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userName = user.getDisplayName();
        return userName;
    }

    /**
     * Logic for returning from EditBookFragment activity, if requestCode is 2 and resultCode is RESULT_OK
     * then edit the corresponding fields to match the passed book
     * @param requestCode 2 if book is returned from the edit activity
     * @param resultCode RESULT_OK if book is returned from the edit activity
     * @param data Book object passed from the edit activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                // pass edited book back to parent activity
                Book editedBook = (Book) data.getSerializableExtra("edited");
                title.setText(editedBook.getTitle());
                author.setText(editedBook.getAuthor());
                // owner.setText(editedBook.getOwner().getUsername());
                ISBN.setText(String.valueOf(editedBook.getISBN()));
                if (editedBook.getImageId() != null) {
                    convertImage(editedBook.getImageId());
                }
            }
        }
    }

    /**
     * currentUser uses the current instance of the firebase auth to get the information of the
     * current user and create a User based on it. Because onComplete is asynchronous (so the info
     * won't arrive until after the code completes) we need to use onCallBack interface. It will
     * take the info and allow the information to be used (without null).
     *
     * @param cbh
     */
    public void currentUser(final Callback cbh) {
        final FirebaseUser userInfo = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        db.collection("Users").whereEqualTo("email", userInfo.getEmail()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String username = document.get("username").toString();
                                String email = userInfo.getEmail();
                                String name = document.get("name").toString();
                                String Phone = document.get("phoneNo").toString();
                                User currentUser = new User(username, email, name, Phone);
                                cbh.onCallback(currentUser);
                            }
                        }
                    }
                });
    }

    /**
     *
     * @param imageId
     */

    private void convertImage(String imageId) {
        final long ONE_MEGABYTE = 1024 * 1024;
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        storageRef.child("books/" + imageId + ".jpg").getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        if(bitmap != null) {
                            editBitMap = bitmap;
                            photo.setImageBitmap(bitmap);
                        } else {
                            editBitMap = null;
                            photo.setImageBitmap(null);
                        }
                    }
                });
    }
}
