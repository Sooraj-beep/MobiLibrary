package com.example.mobilibrary;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mobilibrary.R;

/**
 * Notifications fragment that is navigated to using the navigation bar. Shows the user's notifications they will receive when interacting
 * with other users. Clicking on a notification will lead to another activity depending on the kind of notification:
 * 1) Another user has requested your book -> lead to book details
 * 2) User has declined your request
 * 3) User has accepted your request -> lead to map with already-picked location to meet
 * 4) Location the user/borrower has selected to meet with the user/borrower has been sent
 * 5) The borrower is ready to return back User's book -> lead to map with already-picked location
 */
public class NotificationsFragment extends Fragment {

    RecyclerView notificationsRv;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_notifications, container, false);

        //init recycleview
        notificationsRv = v.findViewById(R.id.notificationsRV);

        return v;
    }
}