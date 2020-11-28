package com.example.mobilibrary;

import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.mobilibrary.Activity.LogIn;
import com.example.mobilibrary.Activity.StartActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.robotium.solo.Solo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class requestTest {
    private Solo solo;
    private final String email_1 = "user1@gmail.com";
    private final String password_1 = "password1";
    private final String email_2 = "user2@gmail.com";
    private final String password_2 = "password2";
    private final String email_3 ="user3@gmail.com";
    private final String password_3 = "password3";
    private FirebaseFirestore db;

    @Rule
    public ActivityTestRule<LogIn> rule =
            new ActivityTestRule<>(LogIn.class, true, true);

    /**
     * Runs before all tests and creates solo instance.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
        db = FirebaseFirestore.getInstance();
    }
    public void deleteBook(){



    }
    @Test
    public void checkRequestList() {
        solo.assertCurrentActivity("Wrong Activity!", StartActivity.class);
        solo.clickOnButton("Log In");
        solo.assertCurrentActivity("Wrong Activity!", LogIn.class);
        solo.enterText((EditText) solo.getView(R.id.email_editText), email_2);
        solo.enterText((EditText) solo.getView(R.id.password_editText), password_2);
        solo.clickOnView(solo.getView(R.id.login_button));
        //solo.waitForActivity(MainActivity.class);
        solo.assertCurrentActivity("Wrong Activity!", MainActivity.class);
        solo.clickOnText("Great Pacific");
        solo.assertCurrentActivity("Wrong Activity!", BookDetailsFragment.class);
        solo.clickOnButton("Request");
        solo.clickOnView(solo.getView(R.id.back_to_books_button));
        solo.clickOnView(solo.getView(R.id.profile));
        solo.clickOnButton("Sign Out");
        //log in to second


    }




}
