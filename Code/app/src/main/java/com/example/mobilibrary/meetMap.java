package com.example.mobilibrary;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.mobilibrary.DatabaseController.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;


/**
 * @author Kimberly;
 * When book is borrowed (or being returned), both the other and the borrower will be able to
 * see location of meeting spot. Confirm button will return to previous screen
 */

public class meetMap extends FragmentActivity implements OnMapReadyCallback {
    private LatLng bookLatLng;

    private String TAG = "bookMap";
    private FirebaseFirestore db;
    private String bookDetails = null;
    private String bookOwner = null;
    private int type;

    /**
     * Used to create the map and set the marker
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_map);

        Bundle b = getIntent().getExtras();
        double latitude = b.getDouble("latitude");
        double longitude = b.getDouble("longitude");
        type = b.getInt("type");
        if(type == 3 || type == 5) {
            bookDetails = b.getString("bookFSID");
            bookOwner = b.getString("bookOwner");
        }
        bookLatLng = new LatLng(latitude, longitude);

        Button confirmButton = findViewById(R.id.confirm_location);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        Bundle locationBundle = getIntent().getExtras();

        assert locationBundle != null;

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((type == 3) || (type == 5)){

                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference docRef = db.collection("Users").document(bookOwner);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot document = task.getResult();
                            System.out.println("GOT USER DOCUMENT");
                            String username = Objects.requireNonNull(document.get("username")).toString();
                            String email = Objects.requireNonNull(document.get("email")).toString();
                            String name = Objects.requireNonNull(document.get("name")).toString();
                            String phoneNo = Objects.requireNonNull(document.get("phoneNo")).toString();

                            User user = new User(username, email, name, phoneNo);
                            System.out.println("CREATED NEW USER");
                            initIntent(user);
                        }
                        public void initIntent(User user){
                            //get the book details of currently clicked item
                            final FirebaseFirestore db = FirebaseFirestore.getInstance();
                            DocumentReference docRef = db.collection("Books").document(bookDetails);
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    DocumentSnapshot document = task.getResult();
                                    String title = Objects.requireNonNull(document.get("Title")).toString();
                                    String isbn = Objects.requireNonNull(document.get("ISBN")).toString();
                                    String author = Objects.requireNonNull(document.get("Author")).toString();
                                    String status = Objects.requireNonNull(document.get("Status")).toString();
                                    //String image = Objects.requireNonNull(document.get("imageID")).toString();
                                    String image;
                                    try {
                                        image = Objects.requireNonNull(document.get("imageID")).toString();
                                    }
                                    catch(Exception e) {
                                        image = "";
                                    }

                                    System.out.println("CREATED NEW BOOK");
                                    Book clickedBook = new Book(bookDetails, title, isbn, author, status, image, user);
                                    Intent viewBook = new Intent(meetMap.this, BookDetailsFragment.class);
                                    viewBook.putExtra("view book", clickedBook);
                                    meetMap.this.startActivity(viewBook);

                                }
                            });

                        }

                    });

                } else {
                    finish();
                }
            }
        });

        assert mapFragment != null;
        mapFragment.getMapAsync(this);

    }

    /**
     * Used when the map is starting up or if the map is updated
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        System.out.println("IN ONMAP LATLANG: " + bookLatLng);
        if (bookLatLng == null) {
            return;
        } else {
            float zoom = 16.0f;
            googleMap.addMarker(new MarkerOptions().position(bookLatLng)
                    .title("Marker"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bookLatLng,zoom));
        }

    }

}