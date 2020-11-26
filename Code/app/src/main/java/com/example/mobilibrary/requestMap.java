package com.example.mobilibrary;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.mobilibrary.DatabaseController.aRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
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
        Bundle bundle = intent.getExtras();
        aRequest request  = (aRequest) bundle.get("book");
        System.out.println("BOOK ID: " + request.getBookID());

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


        mapFragment.getMapAsync(this);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newLatLng != null) {
                    System.out.println("LATLING: " + newLatLng);
                    db = FirebaseFirestore.getInstance();
                    WriteBatch batch = db.batch();
                    System.out.println("Wrote batch");
                    System.out.println(request);
                    assert request != null;
                    System.out.println("BOOK ID: " + request.getBookID());
                    DocumentReference bookDoc = db.collection("Books")
                            .document(request.getBookID());
                    System.out.println("Got doc reference");

                    Map<String, Object> newData = new HashMap<>();
                    //Add the user whose request has been accepted to the book
                    newData.put("LatLang", newLatLng);

                    batch.update(bookDoc, newData);
                    batch.update(bookDoc, "Status", "accepted");
                    Intent mapIntent = new Intent();
                    mapIntent.putExtra("LatLang", newLatLng);
                    batch.commit();
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
