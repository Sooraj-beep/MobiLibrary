package com.example.mobilibrary;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;


/**
 * @author Kimberly;
 * When book is borrowed (or being returned), both the other and the borrower will be able to
 * see location of meeting spot. Confirm button will return to previous screen
 */

public class meetMap extends FragmentActivity implements OnMapReadyCallback {
    private LatLng bookLatLng;

    private String TAG = "bookMap";
    private FirebaseFirestore db;

    /**
     * Used to create the map and set the marker
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_map);

        Intent intent = getIntent();
        Bundle b = getIntent().getExtras();
        double latitude = b.getDouble("latitude");
        double longitude = b.getDouble("longitude");

        bookLatLng = new LatLng(latitude, longitude);

        Button confirmButton = findViewById(R.id.confirm_location);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        Bundle locationBundle = getIntent().getExtras();

        assert locationBundle != null;


        confirmButton.setOnClickListener(v -> finish());

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