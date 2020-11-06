package com.example.mobilibrary;

import android.app.Activity;
import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.mobilibrary.Activity.LogIn;
import com.example.mobilibrary.Activity.SignUp;
import com.example.mobilibrary.DatabaseController.DatabaseHelper;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SignUpTest {
    private Solo solo;
    private EditText nameField;
    private EditText usernameField;
    private EditText emailField;
    private EditText phoneField;
    private EditText passField;
    private EditText confirmField;

    @Rule
    public ActivityTestRule<SignUp> rule = new ActivityTestRule<>(SignUp.class, true, true);

    /**
     * Runs before all tests and creates solo instance.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception{
            solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
            nameField = (EditText)solo.getView(R.id.edit_name);
            usernameField =(EditText)solo.getView(R.id.edit_username);
            emailField = (EditText)solo.getView(R.id.edit_email) ;
            phoneField = (EditText)solo.getView(R.id.edit_phoneNo);
            passField = (EditText)solo.getView(R.id.edit_password);
            confirmField = (EditText)solo.getView(R.id.edit_password2);

    }

    @Test
    public void CorrectInput() {
        //Asserts that the current activity is SignUp Activity. Otherwise, show "Wrong Activity"
        solo.assertCurrentActivity("Wrong Activity", SignUp.class);
        //Enter inputs
        solo.enterText(nameField, "Test User");
        solo.enterText(usernameField, "test_user");
        solo.enterText(emailField, "testuser@gmail.com");
        solo.enterText(phoneField, "7801112222");
        solo.enterText(passField, "testpass");
        solo.enterText(confirmField, "testpass");
        //Select Sign Up button
        solo.clickOnButton("Sign Up");
        //Clear the EditTexts
        solo.clearEditText(nameField);
        solo.clearEditText(usernameField);
        solo.clearEditText(emailField);
        solo.clearEditText(phoneField);
        solo.clearEditText(passField);
        solo.clearEditText(confirmField);
        //Check for toast text
        assertTrue(solo.waitForText("Loading",1, 1000));
    }

    @Test
    public void checkActivitySwitch() {
        //Asserts that the current activity is SignUp Activity. Otherwise, show "Wrong Activity"
        solo.assertCurrentActivity("Wrong Activity", SignUp.class);
        //Enter inputs
        solo.enterText(nameField, "New User");
        solo.enterText(usernameField, "new_user");
        solo.enterText(emailField, "newuser@gmail.com");
        solo.enterText(phoneField, "7801118888");
        solo.enterText(passField, "testpass");
        solo.enterText(confirmField, "testpass");
        //Select Sign Up button
        solo.clickOnButton("Sign Up");
        //Clear the EditTexts
        solo.clearEditText(nameField);
        solo.clearEditText(usernameField);
        solo.clearEditText(emailField);
        solo.clearEditText(phoneField);
        solo.clearEditText(passField);
        solo.clearEditText(confirmField);
        //Check for current activity
        //assertTrue(solo.waitForText("Loading",1, 1000));
        solo.assertCurrentActivity("Wrong activity", LogIn.class);
    }

    @Test
    public void SignUpFailed() {
        //Asserts that the current activity is SignUp Activity. Otherwise, show "Wrong Activity"
        solo.assertCurrentActivity("Wrong Activity", SignUp.class);
        //Enter inputs
        solo.enterText(nameField, "Test User123");
        solo.enterText(usernameField, "test123");
        solo.enterText(emailField, "testuser123@gmail.com");
        solo.enterText(phoneField, "7801115555");
        solo.enterText(passField, "testpass2");
        solo.enterText(confirmField, "testpass2");
        //Select Sign Up button
        solo.clickOnButton("Sign Up");
        //Clear the EditTexts
        solo.clearEditText(nameField);
        solo.clearEditText(usernameField);
        solo.clearEditText(emailField);
        solo.clearEditText(phoneField);
        solo.clearEditText(passField);
        solo.clearEditText(confirmField);
        //Check for toast text
        assertTrue(solo.waitForText("Loading",1, 1000));
        assertTrue(solo.waitForText("Username already exists. Please try again!",1, 10000));
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
