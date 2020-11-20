package com.example.mobilibrary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.mobilibrary.DatabaseController.User;
import com.example.mobilibrary.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Initializes the navigation bar when the app is run, and sets the Homepage Fragment as the main page
 */

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    //test currentUser
    private CurrentUser currentUser;
    private Context context;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNav);
        //test currentUser
        currentUser = CurrentUser.getInstance();
        user = currentUser.getCurrentUser();
        context = getApplicationContext();
        Toast.makeText( context,"Account: "+user.getUsername(), Toast.LENGTH_SHORT).show();

        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavMethod);
        bottomNavigationView.setSelectedItemId(R.id.home);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeFragment()).commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener bottomNavMethod = new
            BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {


                    Fragment fragment = null;
                    switch(menuItem.getItemId())
                    {
                        case R.id.home:
                            fragment= new HomeFragment();
                            break;

                        case R.id.myBooks:
                            fragment = new MyBooksFragment();
                            break;

                        case R.id.notifications:
                            fragment= new NotificationsFragment();
                            break;

                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();

                    return true;
                }
            };
}