package com.example.mobilibrary;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


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
                    db.collection("Books").document(bookDetails).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String author = documentSnapshot.getString("Author");
                            String ISBN = documentSnapshot.getString("ISBN");
                            User bookOwner = (User) documentSnapshot.get("User");
                            String status = documentSnapshot.getString("Status");
                            String title = documentSnapshot.getString("Title");
                            String imageId = documentSnapshot.getString("imageId");
                            Book newBook = new Book(title,ISBN,author,status,imageId,bookOwner);
                            Intent viewBook = new Intent(meetMap.this, BookDetailsFragment.class);
                            viewBook.putExtra("view book", newBook);
                            (meetMap.this).startActivity(viewBook);
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