package com.example.mobilibrary;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.mobilibrary.DatabaseController.BookService;
import com.example.mobilibrary.DatabaseController.RequestService;
import com.example.mobilibrary.DatabaseController.User;
import com.example.mobilibrary.DatabaseController.aRequest;
import com.example.mobilibrary.DatabaseController.RequestService;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.Query;

import com.google.firebase.firestore.FirebaseFirestoreException;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.Objects;


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
    private FloatingActionButton editButton;
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

    private CurrentUser currentUser;



    private FirebaseFirestore db;
    private BookService bookService;
    private RequestService requestService;
    private Context context;
    private RequestQueue mRequestQueue;

    private Button requested;
    private Button requestButton;
    private Button returnButton;
    private Button receiveButton;
    private boolean checkTitle = false;
    private boolean checkAuthor = false;
    private boolean checkISBN = false;

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

        requested = findViewById(R.id.requested);
        requestButton = findViewById(R.id.request_button);
        returnButton = findViewById(R.id.return_button);
        receiveButton = findViewById(R.id.receive_button);

        //set all status changing buttons to be invisible
        requestButton.setVisibility(View.GONE);
        returnButton.setVisibility(View.GONE);
        receiveButton.setVisibility(View.GONE);
        requested.setVisibility(View.GONE);

        // set up firestore instance
        bookService = BookService.getInstance();
        requestService = RequestService.getInstance();
        context = getApplicationContext();

        // set up permissions for scanning intent
        mRequestQueue = Volley.newRequestQueue(this);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},
                PackageManager.PERMISSION_GRANTED); //Request permission to use Camera

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

        convertImage(viewBook.getFirestoreID());

        //get current user name and book owners name, check if they match
        String userName = getUsername();
        String bookOwner = viewBook.getOwner().getUsername();
        if (userName.equals(bookOwner)) { //user is looking at their own book (only happens when on myBooks page), can edit or delete, view requests, etc
            // hide request list at open of activity
            requestAssets = new TextView[]{title, author, owner, status, ownerTitle,ISBN, isbnTitle, statusTitle };
            reqDataList = new ArrayList<>();
            for (String user: req_users){
                reqDataList.add(user + "has requested your book");
            }
            reqAdapter =  new ArrayAdapter<String>(this,R.layout.req_custom_list, R.id.textView, reqDataList);
            reqList.setAdapter(reqAdapter);
            reqList.setVisibility(View.GONE);

            // get book status
            if (viewBook.getStatus().equals("borrowed") || (viewBook.getStatus().equals("returned"))) {
                // if book is borrowed, show receive button
                receiveButton.setVisibility(View.VISIBLE);

            } else {
                // show loan button for available, requested or accepted books
            }


        } else{ //user is looking at another user's book (from homepage), hide the edit, delete, two tabs buttons. Depending on the status of the book will show diff buttons (request, borrow, etc)
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            detailsBtn.setVisibility(View.GONE);
            requestsBtn.setVisibility(View.GONE);

            //get book status
            if (viewBook.getStatus().equals("available") || (viewBook.getStatus().equals("requested"))) {
                //if book is available or has requests (and also make sure user hasn't requested it before) display request button

                //check is user has requested this book before
                if (viewBook.getStatus().equals("requested")) {
                    //get requestors
                    ArrayList<String> requestors = new ArrayList<String>();
                    final boolean[] alreadyRequested = new boolean[1];
                    CollectionReference requestsRef;
                    db = FirebaseFirestore.getInstance();
                    //CollectionReference requestsRef = db.collection("Requests");
                    requestsRef = db.collection("Requests");
                    System.out.println("Got collection reference");
                    Query query = requestsRef.whereEqualTo("bookID", viewBook.getFirestoreID());
                    query.get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        requestors.clear();
                                        alreadyRequested[0] = false;
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            System.out.println("In query document snapshot: " + document.getData());
                                            requestors.add(document.getData().toString());
                                            String bookRequester = document.getString("requester");
                                            //if requester is equal to user then show requested button and exit
                                            if (bookRequester.equals(getUsername())) {
                                                alreadyRequested[0] = true;
                                                requested.setVisibility(View.VISIBLE);
                                                return;
                                            }

                                        }
                                    }

                                }
                            });

                    if (alreadyRequested[0] == false) {
                        requestButton.setVisibility(View.VISIBLE);
                    }

                }else {
                    requestButton.setVisibility(View.VISIBLE);
                }
                //requestButton.setVisibility(View.VISIBLE);

            }
            else if (viewBook.getStatus().equals("borrowed")){
                // if book is borrowed, show return button
                returnButton.setVisibility(View.VISIBLE);

            }

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
                        storageReference.child("books/" + viewBook.getFirestoreID() + ".jpg").delete()
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

                //also have to delete all requests from firebase that came with this book, and their notifications
                //delete all requests for the book on firestore
                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Requests").whereEqualTo("bookID", viewBook.getFirestoreID())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    document.getReference().delete();
                                }
                            }
                        });

                //delete notifications for everyone who has requested that book
                //*this is done in the notifictions fragment (going to check if book still exists before displaying notification)

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
         *If receive button is pressed
         */
        receiveButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                // open scanner to check for correct book
                ScanButton(view);

                // if all information matches the book, change book status to available
                if (checkISBN && checkTitle && checkAuthor) {
                    viewBook.setStatus("available");
                    bookService.changeStatus(context, viewBook, "available");
                    finish();
                }
            }
        });

        /**
         * If return button is pressed, check if the book brought to the exchange is the one that
         * is to be returned.
         */
        returnButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                // open scanner to check for correct book
                ScanButton(view);

                // if all information matches the book, change book status to returned
                if (checkISBN && checkTitle && checkAuthor) {
                    viewBook.setStatus("returned");
                    bookService.changeStatus(context, viewBook, "returned");
                    finish();
                }
            }
        });

        /**
         * If Request Button is pressed, create new Request object, save to firestore, change Book status to request, and change the button
         */
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //change book status to requested

                viewBook.setStatus("requested");
                bookService.changeStatus(context, viewBook, "requested");
                requestButton.setVisibility(View.GONE);
                requested.setVisibility(View.VISIBLE);
                requested.setPressed(true);

                //create new request and store in firestore
                aRequest request = new aRequest(getUsername(), viewBook.getFirestoreID());
                System.out.println("Created new request: " + request);
                System.out.println("Request service: " + requestService);
                requestService.createRequest(request).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(getApplicationContext(), "Successfully requested book!", Toast.LENGTH_LONG).show();

                    }else{
                        System.out.println("Could not create request");
                        Toast.makeText(getApplicationContext(), "Unable to request book!", Toast.LENGTH_LONG).show();
                    }
                });

                //create notification
                addToNotifications(viewBook.getOwner().getUsername(), getUsername(), "Has requested to borrow your book.", "1", viewBook.getFirestoreID());

                //System.out.println("VIEWED BOOK FIRESTOREID: " + viewBook.getFirestoreID());
                /*String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                db = FirebaseFirestore.getInstance();
                db.collection("Requests").whereEqualTo("requester", username)
                        .whereEqualTo("bookID", viewBook.getFirestoreID()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Boolean requestExist = false;
                            for (QueryDocumentSnapshot document : task.getResult()){
                                requestExist = true;
                        }
                            if (requestExist) {
                                Toast.makeText(getApplicationContext(), "You have already requested this book", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                viewBook.setStatus("requested");
                                requestButton.setText("Requested");
                                bookService.changeStatus(context, viewBook, "requested");

                                com.example.mobilibrary.DatabaseController.Request request = new com.example.mobilibrary.DatabaseController.Request(username, viewBook.getFirestoreID());
                                Log.d("SOORAJ", viewBook.getFirestoreID());
                                requestService = requestService.getInstance();
                                requestService.createRequest(request).addOnCompleteListener(task2 -> {
                                    if(task2.isSuccessful()){
                                        Log.d("SOORAJ", "ADDED NEW REQUEST");
                                    }
                                    else {
                                        Log.d("SOORAJ", "FAILED");
                                    }
                                });
                            }
                    }else {
                            Log.d("SOORAJ", "error");

                            }


                    }
                });*/



                //later add: make sure button text stays "requested" when user who already requested clicks on it again


            }
        });

        /**
         * Toggles view, shows request list and hides book details
         */
        requestsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//set adapter for requestlistview

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

                // get book status
                if (viewBook.getStatus() == "borrowed" || (viewBook.getStatus() == "returned")) {
                    // if book is borrowed, show receive button
                    receiveButton.setVisibility(View.VISIBLE);

                } else {
                    // show loan button for available, requested or accepted books
                }
            }
        });
    }


    /**
     * Gets username of current user
     * @return String username
     */
    private String getUsername(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userName = user.getDisplayName();
        return userName;
    }


     /**
      *  When the Scan Button is pressed the scan activity is initiated
      * @param view the Scan Button
      */
    private void ScanButton(View view) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.initiateScan();
    }


    /**
     *
     * @param imageId
     */

    private void convertImage(String imageId) {
        final long ONE_MEGABYTE = 1024 * 1024;
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        storageRef.child("books/" + imageId + ".jpg").getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(bytes -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    editBitMap = bitmap;
                    photo.setImageBitmap(bitmap);
                }).addOnFailureListener(e -> {
                    editBitMap = null;
                    photo.setImageBitmap(null);
                 });
    }

    /**
     * Logic for returning from EditBookFragment activity, if requestCode is 2 and resultCode is RESULT_OK
     * then edit the corresponding fields to match the passed book. Otherwise, logic for checking that the
     * information for the book scanned matches the information of the book being viewed.
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
                owner.setText(editedBook.getOwner().getUsername());
                ISBN.setText(String.valueOf(editedBook.getISBN()));
                if(editedBook.getImageId() != null){
                    byte [] encodeByte= Base64.decode(editedBook.getImageId(),Base64.DEFAULT);
                    Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                    editBitMap = bitmap;
                    photo.setImageBitmap(bitmap);
                } else {
                    editBitMap = null;
                    photo.setImageBitmap(null);
                }
            }
        } else {
            // check scanned book's information against the book being viewed
            IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            if (intentResult != null) { //scanner got a result
                if (intentResult.getContents() == null) { //scanner worked, but was not able to get data
                    System.out.println("scanner worked, but not able to get data");
                    Toast toast = Toast.makeText(this, "Unable to obtain data from barcode",
                            Toast.LENGTH_SHORT); // used to display error message
                    toast.show();
                } else {
                    //got ISBN
                    //Use the ISBN to search through Google Books API to find the author, and title.
                    String isbn = intentResult.getContents();

                    // determine if the ISBN is correct
                    if (ISBN.getText().toString() == isbn) {
                        checkISBN = true;
                    }

                    //Check if connected to internet
                    boolean isConnected = isNetworkAvailable();
                    if (!isConnected) {
                        System.out.println("Check Internet Connection");
                        Toast.makeText(getApplicationContext(), "Please check Internet connection", Toast.LENGTH_LONG).show(); //Popup message for user
                        return;
                    }

                    final String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:"; //base url
                    Uri uri = Uri.parse(url + isbn);
                    Uri.Builder builder = uri.buildUpon();  // build url with ISBN

                    parseJson(builder.toString()); //get results from webpage
                }
            }
        }
    }


    /**
     * Given a webpage built from the ISBN, find the book's information and set match information for the book
     * from the information obtained from the ISBN
     * @param key webpage url built from the ISBN
     */
    private void parseJson(String key) {
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, key.toString(), null,
                new Response.Listener<JSONObject>() { //volley stuff
                    @Override
                    public void onResponse(JSONObject response) {
                        String matchTitle = "";
                        String matchAuthor = "";

                        try {
                            System.out.println("RESPPONSSEEE: " + response);

                            JSONArray items = response.getJSONArray("items");
                            JSONObject item = items.getJSONObject(0);
                            JSONObject volumeInfo = item.getJSONObject("volumeInfo");

                            try {
                                matchTitle = volumeInfo.getString("title");
                                System.out.println("title: " + matchTitle);
                                if (title.getText().toString() == matchTitle) {
                                    checkTitle = true;
                                }

                                JSONArray authors = volumeInfo.getJSONArray("authors");
                                if (authors.length() == 1) {
                                    matchAuthor = authors.getString(0);
                                } else { //if there are multiple authors
                                    matchAuthor = authors.getString(0) + "," + authors.getString(1);
                                }
                                System.out.println("author: " + matchAuthor);
                                if (author.getText().toString() == matchAuthor) {
                                    checkAuthor = true;
                                }

                            } catch (Exception e) { //the book info in database does not contain a title or author
                                if (matchTitle == "") {
                                    Toast.makeText(getApplicationContext(), "Could not obtain title information", Toast.LENGTH_SHORT).show(); //Popup message for user
                                } else {
                                    Toast.makeText(getApplicationContext(), "Could not obtain author information", Toast.LENGTH_SHORT).show(); //Popup message for user
                                }
                            }

                        } catch (JSONException e) { //error trying to get database info
                            Toast.makeText(getApplicationContext(), "Could not obtain book information", Toast.LENGTH_SHORT).show(); //Popup message for user
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mRequestQueue.add(request);
    }


    /**
     * Check if connnected to the internet
     * @return boolean true if connected, false otherwise
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return info != null && info.isConnected();
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
     * Determines if the book's photograph has a null bitmap
     * @return boolean true if the book's photograph has a null bitmap, false otherwise
     */
    private boolean nullPhoto () {
        Drawable drawable = photo.getDrawable();    // get image
        BitmapDrawable bitmapDrawable;
        if (!(drawable instanceof BitmapDrawable)) {
            bitmapDrawable = null;  // image has no bitmap
        } else {
            bitmapDrawable = (BitmapDrawable) photo.getDrawable();  // get image bitmap
        }
        return drawable == null || bitmapDrawable.getBitmap() == null;  // determine if bitmap is null
    }

    private void addToNotifications(String otherUser, String user, String notification, String type, String fireStoreID){

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("otherUser", otherUser);
        hashMap.put("user", user);
        hashMap.put("notification", notification);
        hashMap.put("type", type);
        hashMap.put("bookFSID", fireStoreID);


        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(otherUser).collection("Notifications").add(hashMap);

    }
}
