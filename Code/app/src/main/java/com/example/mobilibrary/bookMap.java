package com.example.mobilibrary;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * @author Kimberly;
 *
 */
public class bookMap extends FragmentActivity implements OnMapReadyCallback {
    private LatLng bookLatLng;

    /**
     * Used to create the map and set the marker
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_map);

        Button confirmButton = findViewById(R.id.confirm_location);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        Bundle locationBundle = getIntent().getExtras();
        assert locationBundle != null;
        bookLatLng = (LatLng) locationBundle.get("LatLang");

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
        float zoom = 16.0f;
        googleMap.addMarker(new MarkerOptions().position(bookLatLng)
                .title("Marker"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bookLatLng,zoom));
    }
}