package com.example.mobilibrary;


import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.mobilibrary.Activity.LogIn;
import com.example.mobilibrary.DatabaseController.DatabaseHelper;
import com.example.mobilibrary.DatabaseController.User;
import com.google.firebase.auth.FirebaseAuth;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Test class for ProfileActivity. All the UI tests are written here.
 * Robotium test framework is used.
 */

@RunWith(AndroidJUnit4.class)
public class SearchTest {
    private Solo solo;
    private final String email = "test@mail.com";
    private final String password = "Pas5W0rd!";
    private View searchView;
    private ListView booksListView;
    private final DatabaseHelper databaseHelper = new DatabaseHelper(InstrumentationRegistry.getInstrumentation().getContext());

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
     * @throws Exception if activity can't be started
     */
    @Test
    public void start() throws Exception {
        Activity activity = rule.getActivity();
        solo.assertCurrentActivity("Wrong activity!", LogIn.class);
    }

    // go to fragment

    @Test
    public void checkTitleSearchFound() {
        solo.enterText((EditText) solo.getView(R.id.email_editText), email);
        solo.enterText((EditText) solo.getView(R.id.password_editText), password);
        solo.clickOnView(solo.getView(R.id.login_button));
        solo.waitForActivity(MainActivity.class);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        searchView = solo.getView(R.id.search_view);
        booksListView = (ListView) solo.getView(R.id.all_books_list_view);
        // Enters query and submits
        solo.clickOnView(searchView);
        solo.enterText(0, "Found Book Title");
        solo.sleep(2000);
        assertTrue("Title search not found!", solo.waitForText("Found Book Title", 1, 2000));
    }

    @Test
    public void checkTitleSearchFoundDetails() {
        solo.enterText((EditText) solo.getView(R.id.email_editText), email);
        solo.enterText((EditText) solo.getView(R.id.password_editText), password);
        solo.clickOnView(solo.getView(R.id.login_button));
        solo.waitForActivity(MainActivity.class);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        searchView = solo.getView(R.id.search_view);
        booksListView = (ListView) solo.getView(R.id.all_books_list_view);
        // Enters query and submits
        solo.clickOnView(searchView);
        solo.enterText(0, "Found Book Title");
        solo.sleep(2000);
        solo.clickInList(1);
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);
        String title = ((TextView) solo.getView(R.id.view_title)).getText().toString();
        assertEquals("Found Book Title", title);
    }

    @Test
    public void checkAuthorSearchFound() {
        solo.enterText((EditText) solo.getView(R.id.email_editText), email);
        solo.enterText((EditText) solo.getView(R.id.password_editText), password);
        solo.clickOnView(solo.getView(R.id.login_button));
        solo.waitForActivity(MainActivity.class);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        searchView = solo.getView(R.id.search_view);
        booksListView = (ListView) solo.getView(R.id.all_books_list_view);
        // Enters query and submits
        solo.clickOnView(searchView);
        solo.enterText(0, "Search Test Author");
        solo.sleep(2000);
        assertTrue("Author search not found!", solo.waitForText("Search Test Author", 1, 2000));
    }

    @Test
    public void checkISBNSearchFound() {
        solo.enterText((EditText) solo.getView(R.id.email_editText), email);
        solo.enterText((EditText) solo.getView(R.id.password_editText), password);
        solo.clickOnView(solo.getView(R.id.login_button));
        solo.waitForActivity(MainActivity.class);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        searchView = solo.getView(R.id.search_view);
        booksListView = (ListView) solo.getView(R.id.all_books_list_view);
        // Enters query and submits
        // Enters query and submits
        solo.clickOnView(searchView);
        solo.enterText(0, "9999999999999");
        solo.sleep(2000);
        assertTrue("ISBN search not found!", solo.waitForText("Search Test ISBN", 1, 2000));
    }

    @Test
    public void checkNotFound() {
        solo.enterText((EditText) solo.getView(R.id.email_editText), email);
        solo.enterText((EditText) solo.getView(R.id.password_editText), password);
        solo.clickOnView(solo.getView(R.id.login_button));
        solo.waitForActivity(MainActivity.class);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        searchView = solo.getView(R.id.search_view);
        booksListView = (ListView) solo.getView(R.id.all_books_list_view);
        // Enters query and submits
        solo.clickOnView(searchView);
        solo.enterText(0, "SearchQueryNotAbleToBeFoundAsRealBook");
        solo.sleep(2000);
        assertTrue("List has items!",booksListView.getAdapter().getCount() == 0);
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