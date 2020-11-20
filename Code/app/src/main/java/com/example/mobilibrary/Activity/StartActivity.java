package com.example.mobilibrary.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.mobilibrary.CurrentUser;
import com.example.mobilibrary.DatabaseController.DatabaseHelper;
import com.example.mobilibrary.MainActivity;
import com.example.mobilibrary.R;

public class StartActivity extends AppCompatActivity {

    private Button logInButton;
    private Button signUpButton;
    private Button testUserButton;
    private DatabaseHelper databaseHelper;
    private Context context;
    private CurrentUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        logInButton = findViewById(R.id.home_log_in_button);
        signUpButton = findViewById(R.id.home_sign_up_button);
        testUserButton = findViewById(R.id.testing_button);
        context = this;
        databaseHelper = new DatabaseHelper(context);
        currentUser = CurrentUser.getInstance();

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartActivity.this.startActivity(new Intent(StartActivity.this, LogIn.class));
                finish();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartActivity.this.startActivity(new Intent(StartActivity.this, SignUp.class));
                finish();
            }
        });

        testUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseHelper.validateUser("test@mail.com", "Pas5W0rd!")
                        .addOnCompleteListener(task -> {
                            currentUser.login(task.getResult());
                            startActivity(new Intent(context, MainActivity.class));
                        });
            }
        });

    }
}