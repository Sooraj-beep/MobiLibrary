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
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class takes in a book and edits it Title, Author, ISBN and photograph. The first three
 * can be done manually or via scanning the book's ISBN
 */
public class EditBookFragment extends AppCompatActivity {
    private EditText title;
    private EditText author;
    private EditText ISBN;
    private ImageView photo;
    private Bitmap imageBitMap;
    private Bitmap oldBitmap;
    private FloatingActionButton editImageButton;
    private FloatingActionButton deleteImageButton;

    private FloatingActionButton backButton;
    private FloatingActionButton scanButton;
    private Button confirmButton;

    private RequestQueue mRequestQueue;
    private FirebaseFirestore db;
    private BookService bookService;
    private Context context;

    /**
     * Creates the activity for editing books and the necessary logic to do so
     * @param SavedInstances The book to be edited
     */
    @Override
    protected void onCreate (@Nullable Bundle SavedInstances){
        super.onCreate(SavedInstances);
        setContentView(R.layout.layout_edit_book_fragment);

        // set each variable to correct view
        title = findViewById(R.id.edit_title);
        author = findViewById(R.id.edit_author);
        ISBN = findViewById(R.id.edit_isbn);
        confirmButton = findViewById(R.id.confirm_button);
        backButton = findViewById(R.id.back_to_view_button);
        scanButton = findViewById(R.id.edit_scan_button);
        confirmButton = findViewById(R.id.confirm_button);
        photo = findViewById(R.id.image);
        editImageButton = findViewById(R.id.edit_image_button);
        deleteImageButton = findViewById(R.id.delete_image_button);

        // set up permissions for scanning intent
        mRequestQueue = Volley.newRequestQueue(this);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},
                PackageManager.PERMISSION_GRANTED); //Request permission to use Camera
        
        // set up firestore instance
        bookService = BookService.getInstance();
        context = getApplicationContext();

        // check that a book was passed to this activity, otherwise end the activity
        if (getIntent() == null) {
            finish();
        }
        final Book book = (Book) getIntent().getSerializableExtra("edit");

        // fill fields with correct information from the passed book
        title.setText(book.getTitle());
        author.setText(book.getAuthor());
        ISBN.setText(String.valueOf(book.getISBN()));
        
        //Bitmap bitmap = null;
        System.out.println("BOOK.GETIMAGE: " + book.getImageId());
        if(book.getImageId() != null){
            convertImage(book.getImageId());
        } else {
            imageBitMap = null;
            oldBitmap = null;
        }

        /**
         * If Back Button is pressed, return to BookDetailsFragment without changing anything about the book
         */
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        /**
         * When Confirm Button is pressed the activity returns to BookDetailsFragment and passes along the
         * book with the correct changes made to its details
         */
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String bookTitle = title.getText().toString();
                final String bookAuthor = author.getText().toString();
                final String stringISBN = ISBN.getText().toString().replaceAll(" ", "");

                // if input is valid, edit book and return it to parent activity
                if (validateInputs(bookTitle, bookAuthor, stringISBN)) {
                    currentUser(new Callback() {

                        // change book in firestore as well as in app
                        @Override
                        public void onCallback(User user) {
                            // set all required fields to what is in their EditText views
                            book.setTitle(bookTitle);
                            book.setAuthor(bookAuthor);
                            book.setISBN(stringISBN);
                            byte[] emptyArray = new byte[0];

                            // if a book has a photo pass along the photo's bitmap
                            if (imageBitMap != null) {
                                book.setImageId(imageBitMap.toString());
                            } else {
                                book.setImageId(null);    // book has no photo so image bitmap is set to null
                            }

                            // edit book in firestore
                            bookService.editBook(context, book);

                            // upload any changed images in firestore or if deleted, delete it from firestore
                            if (imageBitMap != oldBitmap && imageBitMap != null) {
                                bookService.uploadImage(imageBitMap.toString(), imageBitMap, new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(EditBookFragment.this, " edited image", Toast.LENGTH_SHORT).show();
                                    }
                                }, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EditBookFragment.this, "Failed to edit image", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                if(oldBitmap != null) {
                                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                    storageReference.child("books/" + oldBitmap.toString() + ".jpg").delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    String TAG = "editBookFragment";
                                                    Log.d(TAG, "onSuccess: deleted file");
                                                }
                                            });
                                }
                            } else if (imageBitMap == null){
                                if(oldBitmap != null) {
                                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                    storageReference.child("books/" + oldBitmap.toString() + ".jpg").delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    String TAG = "editBookFragment";
                                                    Log.d(TAG, "onSuccess: deleted file");
                                                }
                                            });
                                }
                            }

                            // pass edited book back to bookDetailsFragment
                            Intent editIntent = new Intent();
                            editIntent.putExtra("edited", book);    // mark book as edited in app
                            setResult(RESULT_OK, editIntent);
                            finish();
                        }
                    });
                }
            }
        });

        /**
         * When the Scan Button is pressed, a new activity intent opens to take a picture of the
         * book's barcode
         */
        scanButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                ScanButton(view);
            }
        });

        /**
         * When the Delete Image Button is pressed, the book's photograph's bitmap is set to null
         */
        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageBitMap = null;
                book.setImageId(null);
                photo.setImageBitmap(null);
            }
        });

        /**
         * When the Edit Image Button is pressed a new activity intent opens to take a picture to
         * attach to the book
         */
        editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                int pic_id = 2;
                startActivityForResult(camera_intent, pic_id);
            }
        });
    }

    private void convertImage(String imageId) {
        final long ONE_MEGABYTE = 1024 * 1024;
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        storageRef.child("books/" + imageId + ".jpg").getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageBitMap = bitmap;
                        oldBitmap = imageBitMap;
                        photo.setImageBitmap(bitmap);
                    }
                });
    }

    /**
     * When the Scan Button is pressed the scan activity is initiated
     * @param view the Scan Button
     */
    private void ScanButton(View view) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.initiateScan();
    }

    /**
     * Validates that the required fields are not left empty or given an invalid value at the time of
     * confirming changes made to the book
     * @param validateTitle string that is in the title field
     * @param validateAuthor sting that is in the author field
     * @param validateISBN string that is in the ISBN field
     * @return boolean true if all fields are non empty and given valid values, false otherwise
     */
    private boolean validateInputs(String validateTitle, String validateAuthor, String validateISBN){
        boolean validation = true;
        if (validateTitle.isEmpty() == true) {
            title.setError("Required: Book Title!");
            validation = false;
        }
        if (validateAuthor.isEmpty() == true) {
            author.setError("Required: Book Author!");
            validation = false;
        }
        if (validateISBN.isEmpty() == true || validateISBN.length() != 13) {
            ISBN.setError("Required: Book ISBN!");
            validation = false;
        }
        return validation;
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

    /**
     * Logic for returning from the camera for scanning or taking a picture for the book photograph.
     * If requestCode is 2, the image's bitmap is set as the book photograph's bitmap, otherwise the ISBN is
     * set as the book's ISBN and the author and title fields are filled based on information from ISBN
     * @param requestCode 2 if picture was taken for the book photograph, otherwise return from scan activity
     * @param resultCode
     * @param data image bitmap if requestCode is 2, isbn information otherwise
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            Bitmap book_photo = (Bitmap) data.getExtras().get("data");
            imageBitMap = book_photo;
            photo.setImageBitmap(imageBitMap);
        } else {
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
                    ISBN.setText(isbn);

                    //Check if connected to internet
                    boolean isConnected = isNetworkAvailable();
                    if (!isConnected) {
                        System.out.println("Check Internet Connection");
                        Toast.makeText(getApplicationContext(), "Please check Internet conncetion", Toast.LENGTH_LONG).show(); //Popup message for user
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
     * Given a webpage built from the ISBN, find the book's information and fill the title and author fields
     * @param key webpage url built from the ISBN
     */
    private void parseJson(String key) {
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, key.toString(), null,
                new Response.Listener<JSONObject>() { //volley stuff
                    @Override
                    public void onResponse(JSONObject response) {
                        String editTitle = "";
                        String editAuthor = "";

                        try {
                            System.out.println("RESPPONSSEEE: " + response);

                            JSONArray items = response.getJSONArray("items");
                            JSONObject item = items.getJSONObject(0);
                            JSONObject volumeInfo = item.getJSONObject("volumeInfo");

                            try {
                                editTitle = volumeInfo.getString("title");
                                System.out.println("title: " + editTitle);
                                title.setText(editTitle);

                                JSONArray authors = volumeInfo.getJSONArray("authors");
                                if (authors.length() == 1) {
                                    editAuthor = authors.getString(0);
                                } else { //if there are multiple authors
                                    editAuthor = authors.getString(0) + "," + authors.getString(1);
                                }
                                System.out.println("author: " + author);
                                author.setText(editAuthor);

                            } catch (Exception e) { //the book info in database does not contain a title or author
                                if (editTitle == "") {
                                    title.setText("");
                                    title.setError("Title not found");
                                } else {
                                    author.setText("");
                                    author.setError("Author not found");
                                }
                            }

                        } catch (JSONException e) { //error trying to get database info
                            e.printStackTrace();
                            title.setText("");
                            title.setError("Title not found");
                            author.setText("");
                            author.setError("Author not found");
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
}

