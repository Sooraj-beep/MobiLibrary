package com.example.mobilibrary;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilibrary.Activity.ProfileActivity;
import com.example.mobilibrary.DatabaseController.DatabaseHelper;
import com.example.mobilibrary.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Homepage fragment that can be navigated to using navigation bar. Contains search bar, and access to the User profile.
 * If possible, will also show a list of all available books as default (low priority)
 * Can view book details by clicking on book
 */
public class HomeFragment extends Fragment {

    private DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Go to the profile activity for the current logged in user
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

        // TODO: Delete after implementing list for all books in database (this is a test for demo)
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