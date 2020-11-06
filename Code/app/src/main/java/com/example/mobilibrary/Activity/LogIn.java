package com.example.mobilibrary.Activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilibrary.DatabaseController.DatabaseHelper;
import com.example.mobilibrary.R;
/**
 * LogIn activity lets user log into the app with their unique email registered to Firebase Authentication
 * User can also choose to go to SignUp activity to create a new account.
 */

public class LogIn extends AppCompatActivity {
    private EditText inputEmail;
    private EditText inputPassword;

    private Button login;
    private TextView signup;

    final DatabaseHelper databaseHelper = new DatabaseHelper(this);

    /**
     * Defines UI and sets listener
     * @param savedInstanceState: saved instance state
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);
        //gets id of layouts
        inputEmail = findViewById(R.id.email_editText);
        inputPassword = findViewById(R.id.password_editText);
        login = findViewById(R.id.login_button);
        signup = findViewById(R.id.signup_bar2);
        //navigate to SignUp activity
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogIn.this.startActivity(new Intent(LogIn.this, SignUp.class ));
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //gets username and password( trim leading and trailing spaces)
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                //initiates boolean values to keep track of input validity
                boolean boolEmail = false;
                boolean boolPwd = false;

                /**Checks if user provides valid inputs
                 * If the inputs are valid the booleans are set to true otherwise,
                 * the initial values of false will prevent the validation process
                 */
                if(email.isEmpty()){
                    inputEmail.setError("Field cannot be empty");
                } else {
                    inputEmail.setError(null);
                    boolEmail = true;
                }

                if(password.isEmpty()){
                    inputPassword.setError("Field cannot be empty");
                } else {
                    inputPassword.setError(null);
                    boolPwd = true;
                }
                if ((boolEmail)&&(boolPwd)){
                    ProgressBar progressBar = findViewById(R.id.progress_bar);
                    progressBar.setVisibility(View.VISIBLE);
                    databaseHelper.validateUser(email, password);
                    //clear password field
                    inputPassword.setText("");
                }
            }
        });

    }
}
