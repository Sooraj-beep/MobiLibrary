package com.example.mobilibrary;

import android.util.Log;
import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.mobilibrary.Activity.LogIn;
import com.example.mobilibrary.Activity.SignUp;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LogInTest {
    private Solo solo;
    private EditText emailField;
    private EditText passField;

    @Rule
    public ActivityTestRule<LogIn> rule = new ActivityTestRule<>(LogIn.class, true,true);

    @Before
    public void setUp() throws Exception{
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
        emailField = (EditText)solo.getView(R.id.email_editText);
        passField =(EditText)solo.getView(R.id.password_editText);
    }

    @Test
    public void failed_authenticationTest(){
        solo.assertCurrentActivity("Wrong activity", LogIn.class);
        solo.enterText(emailField, "hello123@gmail.com");
        solo.enterText(passField,"wrongpassword");
        solo.clickOnButton("Log In");
        solo.clearEditText(emailField);
        solo.clearEditText(passField);
        assertTrue(solo.waitForText("Authentication Failed.",1,5000));
        solo.assertCurrentActivity("Wrong Activity", LogIn.class);
    }

    @Test
    public void switchActivityTest(){
        solo.assertCurrentActivity("Wrong activity", LogIn.class);
        solo.enterText(emailField, "hello123@gmail.com");
        solo.enterText(passField,"hello123");
        solo.clickOnButton("Log In");
        solo.clearEditText(emailField);
        solo.clearEditText(passField);
        assertTrue(solo.waitForText("Authentication Succeeded.",1,5000));
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
    }

    /**
     * Closes the activity after test
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }
}
