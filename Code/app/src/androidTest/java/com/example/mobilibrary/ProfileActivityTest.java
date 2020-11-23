package com.example.mobilibrary;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.mobilibrary.Activity.LogIn;
import com.example.mobilibrary.Activity.ProfileActivity;
import com.example.mobilibrary.Activity.SignUp;
import com.example.mobilibrary.Activity.StartActivity;
import com.example.mobilibrary.DatabaseController.DatabaseHelper;
import com.example.mobilibrary.DatabaseController.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DecimalFormat;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class for ProfileActivity. All the UI tests are written here.
 * Robotium test framework is used.
 */

@RunWith(AndroidJUnit4.class)
public class ProfileActivityTest {
    private Solo solo;
    private final String username = "profiletest";
    private final String password = "Pas5W0rd!";
    private String email;
    private String phone;
    private FirebaseAuth mAuth;

    @Rule
    public ActivityTestRule<MainActivity> rule =
            new ActivityTestRule<>(MainActivity.class, true, true);

    /**
     * Runs before all tests and creates solo instance.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
        DatabaseHelper databaseHelper = new DatabaseHelper(InstrumentationRegistry.getInstrumentation().getContext());
        mAuth = databaseHelper.getAuth();
        databaseHelper.getUserProfile(username, new Callback() {
            @Override
            public void onCallback(User user) {
                email = user.getEmail();
                phone = user.getPhoneNo();
                mAuth.signInWithEmailAndPassword(email, password);
            }
        });
    }

    /**
     * Checks that the user's own profile shows appropriate visibility at different times,
     * and that each available text view displays the correct view (own profile).
     * e.g. the edit button on start, the edit views & cancel/confirm buttons on click,
     * and back to how it looked before on confirm or cancel (if input validated).
     */
    public void checkSameUserVisibility() {
        solo.assertCurrentActivity("Wrong activity!", MainActivity.class);
        solo.clickOnView(solo.getView(R.id.profile));
        solo.assertCurrentActivity("Wrong activity!", ProfileActivity.class);
        solo.sleep(2000);
        assertEquals(solo.getView(R.id.edit_button).getVisibility(), View.VISIBLE);
        assertEquals(solo.getView(R.id.sign_out_button).getVisibility(), View.VISIBLE);
        TextView usernameTV = (TextView) solo.getView(R.id.username_text_view);
        TextView emailTV = (TextView) solo.getView(R.id.email_text_view);
        TextView phoneTV = (TextView) solo.getView(R.id.phone_text_view);
        assertEquals(usernameTV.getText(), username);
        assertEquals(emailTV.getText(), email);
        assertEquals(phoneTV.getText(), phone);
        solo.clickOnView(solo.getView(R.id.back_button));
    }

    /**
     * Tests the editing of user profile phone number
     */
    public void checkEditUserProfile() {
        solo.clickOnView(solo.getView(R.id.profile));
        solo.assertCurrentActivity("Wrong activity!", ProfileActivity.class);
        solo.clickOnView(solo.getView(R.id.edit_button));

        // Re-authentication dialog should pop up
        assertTrue("Couldn't find dialog fragment!", solo.searchText("Re-authentication"));
        solo.enterText((EditText) solo.getView(R.id.old_email_text_view), email);
        solo.enterText((EditText) solo.getView(R.id.password_text_view), password);
        solo.clickOnButton("Sign In");

        // Test cancel button & then re-authenticate to test editing
        solo.clickOnButton("Cancel");
        solo.sleep(2000);
        assertEquals(solo.getView(R.id.edit_button).getVisibility(), View.VISIBLE);
        assertEquals(solo.getView(R.id.sign_out_button).getVisibility(), View.VISIBLE);
        solo.clickOnView(solo.getView(R.id.edit_button));
        assertTrue("Couldn't find dialog fragment!", solo.searchText("Re-authentication"));
        solo.enterText((EditText) solo.getView(R.id.old_email_text_view), email);
        solo.enterText((EditText) solo.getView(R.id.password_text_view), password);
        solo.clickOnButton("Sign In");
        solo.sleep(2000);

        // Edit button and sign-out should go invisible, and email/phone TextView should change to an EditText
        assertEquals(solo.getView(R.id.edit_button).getVisibility(), View.INVISIBLE);
        assertEquals(solo.getView(R.id.sign_out_button).getVisibility(), View.INVISIBLE);
        assertEquals(solo.getView(R.id.email_text_view).getVisibility(), View.INVISIBLE);
        assertEquals(solo.getView(R.id.phone_text_view).getVisibility(), View.INVISIBLE);
        assertEquals(solo.getView(R.id.edit_new_email).getVisibility(), View.VISIBLE);
        assertEquals(solo.getView(R.id.edit_phone).getVisibility(), View.VISIBLE);

        // Test changing element in user's profile
        Random rand = new Random();
        int num1 = rand.nextInt(1000);
        int num2 = rand.nextInt(1000);
        int num3 = rand.nextInt(10000);
        DecimalFormat df3 = new DecimalFormat("000"); // 3 zeros
        DecimalFormat df4 = new DecimalFormat("0000"); // 4 zeros
        String newPhone = df3.format(num1) + df3.format(num2) + df4.format(num3);
        solo.clearEditText((EditText) solo.getView(R.id.edit_phone));
        solo.enterText((EditText) solo.getView(R.id.edit_phone), newPhone);
        solo.clickOnButton("Confirm");
        solo.sleep(4000);
        assertEquals(solo.getView(R.id.edit_button).getVisibility(), View.VISIBLE);
        assertEquals(solo.getView(R.id.sign_out_button).getVisibility(), View.VISIBLE);
        TextView phoneTV = (TextView) solo.getView(R.id.phone_text_view);
        assertEquals(phoneTV.getText(), newPhone);
        solo.clickOnView(solo.getView(R.id.back_button));
    }

    /**
     * Tests the signing out of a user -- brings them back to log-in page
     */
    public void checkSignOutButton() {
        solo.clickOnView(solo.getView(R.id.profile));
        solo.assertCurrentActivity("Wrong activity!", ProfileActivity.class);
        solo.clickOnButton("Sign Out");
        solo.sleep(2000);
        solo.assertCurrentActivity("Wrong activity!", StartActivity.class);
    }

    /**
     * Tests the user profile in its entirety
     */
    @Test
    public void checkAccount() {
        solo.sleep(5000);
        checkSameUserVisibility();
        checkEditUserProfile();
        checkSignOutButton();
    }

    /**
     * Close activity after each test
     *
     * @throws Exception if activity can't be closed
     */
    @After
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

}
