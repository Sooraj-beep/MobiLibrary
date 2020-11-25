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

/**
 * @author Kimberly;
 * When book is borrowed, both the other and the borrower will be able to see location
 * Confirm button will return to previous screen
 */
public class bookMap extends FragmentActivity implements OnMapReadyCallback {
    private LatLng bookLatLng;
    private String TAG = "bookMap";
    private static FirebaseFirestore db;

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
        Bundle bundle = intent.getExtras();
        Book book = (Book) bundle.get("Book");
        String bookId = book.getFirestoreID();

        Button confirmButton = findViewById(R.id.confirm_location);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        Bundle locationBundle = getIntent().getExtras();
        assert locationBundle != null;
        DocumentReference docRef = db.collection("Books").document(bookId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        bookLatLng = (LatLng) document.getData().get("LatLang");
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

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