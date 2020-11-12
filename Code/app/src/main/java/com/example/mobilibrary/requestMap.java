package com.example.mobilibrary;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.annotations.NotNull;

import java.util.Arrays;

public class requestMap extends FragmentActivity implements OnMapReadyCallback{
    private LatLng newLatLng;
    private String TAG = "requestMap";
    private GoogleMap map;
    private Button confirmButton;
    private AutocompleteSupportFragment searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyADFmERhLf1R3L2B1LDfe38bBcN4m1vtLo");
        }

        confirmButton = findViewById(R.id.confirm_button);

        mapFragment.getMapAsync(this);

        // Initialize the AutocompleteSupportFragment.
        searchButton = (AutocompleteSupportFragment) getSupportFragmentManager()
                .findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        searchButton.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        searchButton.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull Place place) {
                newLatLng = new LatLng(place.getLatLng().latitude,place.getLatLng().longitude);
                float zoom = 16.0f;
                map.addMarker(new MarkerOptions().position(newLatLng).title("Location") );
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng,zoom));
            }

            @Override
            public void onError(@NotNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent();
                mapIntent.putExtra("LatLang", newLatLng);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }
}
