package com.example.mobilibrary;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kimberly;
 * Can search map to find a location (to meet for request) when owner accepts book
 * Confirm will save location in Firestore
 */
public class requestMap extends FragmentActivity implements OnMapReadyCallback{
    private LatLng newLatLng;
    private String TAG = "requestMap";
    private GoogleMap map;
    private SearchView searchButton;
    private static FirebaseFirestore db;

    /**
     * Used to create the map and setting up the search bar
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        String bookID = intent.getExtras().getString("bookID");
        String otherUser = intent.getExtras().getString("otherUser");
        System.out.println("BOOK ID: " + bookID);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        searchButton = findViewById(R.id.search_button);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyADFmERhLf1R3L2B1LDfe38bBcN4m1vtLo");
        }

        Button confirmButton = findViewById(R.id.confirm_request);

        // Set up a PlaceSelectionListener to handle the response.
        searchButton.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchButton.getQuery().toString();
                List<Address> addresses = new ArrayList<>();
                if (location != null || location != "") {
                    Geocoder geocoder = new Geocoder(requestMap.this);
                    try {
                        addresses = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    assert addresses != null;
                    Address address = addresses.get(0);
                    float zoom = 16.0f;
                    newLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                    map.addMarker(new MarkerOptions().position(newLatLng).title("Meeting spot"));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, zoom));
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newLatLng != null) {
                    db = FirebaseFirestore.getInstance();
                    WriteBatch batch = db.batch();

                    //assert request != null;
                    assert bookID != null;
                    DocumentReference bookDoc = db.collection("Books")
                            .document(bookID);
                    Map<String, Object> newData = new HashMap<>();
                    //Add the user whose request has been accepted to the book
                    newData.put("LatLang", newLatLng);

                    batch.update(bookDoc, newData);
                    Intent mapIntent = new Intent();
                    mapIntent.putExtra("LatLang", newLatLng);
                    batch.commit();

                    //create notifications

                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference docRef = db.collection("Books").document(bookID);

                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot document = task.getResult();
                            String title = document.getString("Title");
                            String currUser = document.getString("Owner");

                            String bookStatus = document.getString("Status");
                            assert bookStatus != null;
                            if (bookStatus.equals("borrowed")) { //borrower has clicked return button and is choosing location to return the book

                                //String acceptedTo = document.getString("AcceptedTo"); //book will have an accepted to field
                                HashMap<Object, String> hashMap = new HashMap<>();
                                hashMap.put("otherUser", otherUser);
                                hashMap.put("user", currUser);
                                hashMap.put("notification", "Is ready to return back your book: " + title);
                                hashMap.put("type", "5");
                                hashMap.put("bookFSID", bookID);

                                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("Users").document(otherUser).collection("Notifications").add(hashMap);


                            } else { //owner is choosing location to lend book

                                HashMap<Object, String> hashMap = new HashMap<>();
                                hashMap.put("otherUser", otherUser);
                                hashMap.put("user", currUser);
                                hashMap.put("notification", "Has accepted your request for: " + title);
                                hashMap.put("type", "3");
                                hashMap.put("bookFSID", bookID);

                                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("Users").document(otherUser).collection("Notifications").add(hashMap);

                            }
                            //send notification to current user (#4), saying the location you have chosen has been sent
                            HashMap<Object, String> userMap = new HashMap<>();
                            userMap.put("otherUser", currUser);
                            userMap.put("user", otherUser);
                            userMap.put("notification", "Has received the location you have chosen to meet for: " + title);
                            userMap.put("type", "4");
                            userMap.put("bookFSID", bookID);

                            db.collection("Users").document(currUser).collection("Notifications").add(userMap);

                            //delete all the notifications that involve others who had requested that book
                            db.collection("Users").document(currUser).collection("Notifications")
                                    .whereEqualTo("notification", "Has requested to borrow your book: " + title)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                document.getReference().delete();
                                            }
                                        }
                                    });
                        }

                    });




                    finish();
                } else {
                    Toast.makeText(requestMap.this, "Please search for a location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * When the map is ready on start or is updated (from the search bar). This will
     * change the map on change when it's ready
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }
}
