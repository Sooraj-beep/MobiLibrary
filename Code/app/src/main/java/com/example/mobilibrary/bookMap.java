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

public class bookMap extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap map;
    private Button confirmButton;
    private SupportMapFragment mapFragment;
    private LatLng bookLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_map);

        confirmButton = findViewById(R.id.confirm_location);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        Bundle locationBundle = getIntent().getExtras();
        bookLatLng = (LatLng) locationBundle.get("LatLang");

        confirmButton.setOnClickListener(v -> finish());

        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        float zoom = 16.0f;
        map = googleMap;
        map.addMarker(new MarkerOptions().position(bookLatLng)
                .title("Marker"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(bookLatLng,zoom));
    }
}