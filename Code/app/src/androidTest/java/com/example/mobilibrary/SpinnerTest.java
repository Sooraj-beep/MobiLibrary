package com.example.mobilibrary;

import android.app.Activity;

import android.widget.EditText;
import android.widget.ListView;


import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;


import com.example.mobilibrary.Activity.LogIn;
import com.robotium.solo.Solo;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;


/** Test class for Spinner functionality. All the UI tests are written here. Robotium test framework is
 used
 */

@RunWith(AndroidJUnit4.class)
public class SpinnerTest{
    private Solo solo;
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
    }

    /**
     * Gets the Activity
     *
     * @throws Exception
     */
    @Test
    public void start() throws Exception {
        Activity activity = rule.getActivity();
    }


    /**
     *Checks if Spinner works properly
     */
    @Test
    public void SpinnerTest() {
// Asserts that the current activity is MyBooks and if it will switch to addBookFragment. Otherwise, show “Wrong Activity
        solo.enterText((EditText) solo.getView(R.id.email_editText), "sooraj@mail.com");
        solo.enterText((EditText) solo.getView(R.id.password_editText), "pw1234");
        solo.clickOnView(solo.getView(R.id.login_button));
        solo.waitForActivity(MainActivity.class);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.clickOnMenuItem("My Books");
        solo.sleep(5000);

        solo.pressSpinnerItem(0,1);
        solo.sleep(5000);
        ListView bookView = (ListView) solo.getView(R.id.book_list); // Get the listview
        assertEquals(0,bookView.getCount());

    }



    @After
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }
}
