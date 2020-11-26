package com.example.mobilibrary.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilibrary.Callback;
import com.example.mobilibrary.DatabaseController.DatabaseHelper;
import com.example.mobilibrary.DatabaseController.User;
import com.example.mobilibrary.R;
import com.example.mobilibrary.reAuthFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements reAuthFragment.OnFragmentInteractionListener {

    private ImageButton editButton;
    private TextView usernameText;
    private TextView emailText;
    private TextView phoneText;
    private EditText editEmail;
    private EditText editPhone;
    private Button confirmButton;
    private Button cancelButton;
    private Button signOutButton;
    private User profileUser;
    private User currentUser;
    private Context context;
    private DatabaseHelper databaseHelper;
    final List<View> toggleViews = new ArrayList<View>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Set variables
        editButton = findViewById(R.id.edit_button);
        usernameText = findViewById(R.id.username_text_view);
        emailText = findViewById(R.id.email_text_view);
        phoneText = findViewById(R.id.phone_text_view);
        editEmail = findViewById(R.id.edit_new_email);
        editPhone = findViewById(R.id.edit_phone);
        confirmButton = findViewById(R.id.confirm_book);
        cancelButton = findViewById(R.id.cancel_button);
        signOutButton = findViewById(R.id.sign_out_button);
        context = getApplicationContext();

        // Set visibility
        emailText.setVisibility(View.INVISIBLE);
        phoneText.setVisibility(View.INVISIBLE);
        toggleViews.add(editEmail);
        toggleViews.add(editPhone);
        toggleViews.add(cancelButton);
        toggleViews.add(confirmButton);
        toggleViews.add(emailText);
        toggleViews.add(phoneText);
        toggleVisibility(toggleViews);

        getProfileInfo();

        // Returning to the previous activity where the user came from
        final FloatingActionButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    /**
     * Get user information from database: get profileUser's username from previous activity and then by searching database using databaseHelper
     */
    private void getProfileInfo() {
        Intent intent = getIntent();
        String profileUsername = (String) intent.getSerializableExtra("profile");
        databaseHelper = new DatabaseHelper(this);
        databaseHelper.getUserProfile(profileUsername, new Callback() {
            @Override
            public void onCallback(User user) {
                profileUser = user;
                toggleViews.add(editButton);
                toggleViews.add(signOutButton);
                checkIfOwnProfile();
            }
        });
    }

    /**
     * Checks to see if the profile opened is the currently logged in user's own profile
     */
    private void checkIfOwnProfile() {
        String myUsername = databaseHelper.getUser().getDisplayName();
        if (myUsername != null ) {
            if (myUsername.equals(profileUser.getUsername())) {
                currentUser = profileUser;
                setProfilePage();
            } else {
                databaseHelper.getUserProfile(myUsername, new Callback() {
                    @Override
                    public void onCallback(User user) {
                        currentUser = user;
                        setProfilePage();
                    }
                });
            }
        } else {
            Log.d("USERNAME", "Username not found!");
        }
    }

    /**
     * Sets the profile of the user that is being viewed currently.
     */
    private void setProfilePage() {
        // Set TextViews
        System.out.println("In Set Profile Page");
        usernameText.setText(profileUser.getUsername());
        emailText.setText(profileUser.getEmail());
        phoneText.setText(profileUser.getPhoneNo());

        // Only show edit button if the user is viewing their own profile
        if (profileUser.getUsername().equals(currentUser.getUsername())) {
            editProfile();
        } else {
            editButton.setVisibility(View.INVISIBLE);
            signOutButton.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Only visible if the user is viewing their own profile, allows editing of email and/or phone number.
     */
    private void editProfile() {
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // User must correctly re-authenticate before editing their account
                new reAuthFragment().show(getSupportFragmentManager(), "RE-AUTHENTICATION");

                // User cancels edit
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleVisibility(toggleViews);
                    }
                });

                // User confirms edit
                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!validateEmail(editEmail.getText().toString())) {
                            Toast.makeText(context, "Entered an invalid email!", Toast.LENGTH_SHORT).show();
                        } else if (editEmail.getText().toString().equals(profileUser.getEmail()) && editPhone.getText().toString().equals(profileUser.getPhoneNo())) {
                            Toast.makeText(context, "No edits were made!", Toast.LENGTH_SHORT).show();
                            toggleVisibility(toggleViews);
                        } else if (!TextUtils.isEmpty(editPhone.getText().toString()) && editPhone.getText().toString().length() > 8) { // Phone number input & length already restricted by layout
                            // Update user with new email and/or phone in database
                            databaseHelper.updateUser(currentUser.getUsername(), editEmail.getText().toString(), editPhone.getText().toString(), profileUser.getName(), new Callback() {
                                @Override
                                public void onCallback(User user) {
                                    Toast.makeText(context, "Profile saved!", Toast.LENGTH_SHORT).show();
                                    toggleVisibility(toggleViews);
                                    getProfileInfo();
                                }
                            });
                        } else {
                            Toast.makeText(context, "Entered an invalid phone number!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseHelper.signOut();
                Intent intent = new Intent(ProfileActivity.this, LogIn.class);
                startActivity(intent);
                finish();
            }
        });

    }

    /**
     * Switches visibility of a view from invisible to visible or vice versa.
     *
     * @param views - the list of views that should change visibility
     */
    private void toggleVisibility(List<View> views) {
        for (int i = 0; i < views.size(); i++) {
            if (views.get(i).getVisibility() == View.INVISIBLE) {
                views.get(i).setVisibility(View.VISIBLE);
            } else {
                views.get(i).setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * Checks if the provided editText email is valid.
     * https://stackoverflow.com/questions/12947620/email-address-validation-in-android-on-edittext
     *
     * @param target - the email entered to be validated
     * @return true for valid email pattern, false otherwise
     */
    private boolean validateEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    @Override
    public void onOkPressed() {
        toggleVisibility(toggleViews);
        editEmail.setText(profileUser.getEmail());
        editPhone.setText(profileUser.getPhoneNo());
    }
}