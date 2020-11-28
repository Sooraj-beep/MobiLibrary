package com.example.mobilibrary;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kimberly;
 * When book is borrowed, both the other and the borrower will be able to see location
 * Confirm button will return to previous screen
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