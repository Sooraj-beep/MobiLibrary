package com.example.mobilibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilibrary.Activity.ProfileActivity;
import com.example.mobilibrary.DatabaseController.DatabaseHelper;
import com.example.mobilibrary.DatabaseController.User;
import com.example.mobilibrary.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Homepage fragment that can be navigated to using navigation bar. Contains search bar, and access to the User profile.
 * If possible, will also show a list of all available books as default (low priority)
 * Can view book details by clicking on book
 */
public class HomeFragment extends Fragment {

    private DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

    private RecyclerView booksRV;
    private RecyclerView.Adapter mAdaptor;

    private List<String> titles = new ArrayList<>();
    private List<String> authors = new ArrayList<>();
    private List<String> isbns = new ArrayList<>();
    private List<String> statuses = new ArrayList<>();
    private List<String> owners = new ArrayList<>();
    private List<String> imageIDs = new ArrayList<>();
    private List<String> firestoreIDs = new ArrayList<>();

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //each time homepage is opened will show all available/requested books from other users from collection
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Books").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                titles.clear();
                authors.clear();
                isbns.clear();
                statuses.clear();
                owners.clear();
                imageIDs.clear();
                firestoreIDs.clear();

                for (DocumentSnapshot snapshot : value) { //only add available/requested books and books that do not belong to the user
                    if ((snapshot.getString("Status").equals("available")) || (snapshot.getString("Status").equals("requested"))) {
                        if (! snapshot.get("Owner").equals(databaseHelper.getUser().getDisplayName())) {
                            titles.add(snapshot.getString("Title"));
                            authors.add(snapshot.getString("Author"));
                            isbns.add(snapshot.getString("ISBN"));
                            statuses.add(snapshot.getString("Status"));
                            owners.add(String.valueOf(snapshot.get("Owner")));
                            imageIDs.add(snapshot.getString("imageID"));
                            firestoreIDs.add(snapshot.getId());
                        }
                    }
                }
                mAdaptor = new BookListAdaptor(getContext(), titles, authors, isbns, statuses, owners, imageIDs, firestoreIDs);
                booksRV.setAdapter(mAdaptor);
            }
        });
        booksRV = (RecyclerView) view.findViewById(R.id.booksRV);


        //When profile is clicked
        FloatingActionButton profileButton = (FloatingActionButton) view.findViewById(R.id.profile);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("profile",
                        databaseHelper.getUser().getDisplayName());
                startActivity(intent);
            }
        });

        // Test for clicking another profile
        TextView otherProfile = (TextView) view.findViewById(R.id.other_profile);
        otherProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("profile",
                        "test123");
                startActivity(intent);
            }
        });


        // Inflate the layout for this fragment
        return view;
    }

}