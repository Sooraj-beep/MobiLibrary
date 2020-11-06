package com.example.mobilibrary.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilibrary.DatabaseController.DatabaseHelper;
import com.example.mobilibrary.R;

/**
 * SignUp activity allows new user to create new account
 * User can go back to LogIn screen by clicking on the back button on toolbar
 */

public class SignUp extends AppCompatActivity {
    private EditText inputUsername;
    private EditText inputName;
    private EditText inputEmail;
    private EditText inputPhone;
    private EditText inputPassword;
    private EditText inputConfirmPassword;
    private Button signUp;

    /**
     * Defines UI and sets up listeners
     * @param savedInstanceState: saved instance state
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_signup);
        //Enables action bar to go back to home activity
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        //gets ids of all layouts
        inputUsername = findViewById(R.id.edit_username);
        inputName = findViewById(R.id.edit_name);
        inputEmail = findViewById(R.id.edit_email);
        inputPhone = findViewById(R.id.edit_phoneNo);
        inputPassword = findViewById(R.id.edit_password);
        inputConfirmPassword = findViewById(R.id.edit_password2);
        signUp = findViewById(R.id.sign_up);


        signUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //initiates boolean value to failed to prevent registering process
                boolean failed = false;

                //checks Username using regex, must be at least 3 characters
                if (inputUsername.getText().toString().trim().matches("^[a-zA-Z0-9._-]{3,}$")) {
                    inputUsername.setError(null);
                } else {
                    inputUsername.setError("Invalid input");
                    failed = true;
                }

                //checks fullname
                if (!inputName.getText().toString().trim().isEmpty()) {
                    if (inputName.getText().toString().trim().matches("^([A-z\\'\\.-ᶜ]*(\\s))+[A-z\\'\\.-ᶜ]*$")) {
                        inputName.setError(null);
                    }
                } else {
                    inputName.setError("Invalid input");
                    failed=true;
                }



                //checks Email
                if (inputEmail.getText().toString().trim().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                    inputEmail.setError(null);
                    //boolEmail = true;
                } else {
                    inputEmail.setError("Invalid input");
                    failed = true;
                }

                //checks Phone, must be at least 10 digits
                if (inputPhone.getText().toString().trim().matches("^[0-9-]{10}$")) {
                    inputPhone.setError(null);
                } else {
                    inputPhone.setError("Invalid input");
                    failed = true;
                }


                //check password
                if (!(inputPassword.getText().toString().trim().isEmpty())) {
                    inputPassword.setError(null);
                } else {
                    inputPassword.setError("Field cannot be empty");
                }

                //check confirm pwd
                if (inputConfirmPassword.getText().toString().trim().isEmpty()) {
                    inputConfirmPassword.setError("Field cannot be empty");
                    failed = true;
                }else if (!(inputPassword.getText().toString().trim().equals(inputConfirmPassword.getText().toString().trim()))){
                    inputConfirmPassword.setError("Passwords do not match");
                    failed=true;
                } else {
                    inputConfirmPassword.setError(null);
                    //boolPwd = true;
                }


                if(failed) return;
                //progresses to call DatabaseHelper to initiate registration if not failed
                String username = inputUsername.getText().toString().trim();
                String fullname = inputName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String phoneNo = inputPhone.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                DatabaseHelper databaseHelper = new DatabaseHelper(SignUp.this);
                Toast.makeText(SignUp.this, "Loading", Toast.LENGTH_SHORT).show();
                databaseHelper.regCheck(username, password, fullname, email, phoneNo);
                }


        });

    }
}
