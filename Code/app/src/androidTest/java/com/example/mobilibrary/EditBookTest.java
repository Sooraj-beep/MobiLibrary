package com.example.mobilibrary;

import android.app.Activity;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import android.app.Fragment;
import android.net.Uri;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.mobilibrary.Activity.LogIn;
import com.robotium.solo.Solo;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import android.app.Fragment;

@RunWith (AndroidJUnit4.class)
public class EditBookTest {
    private Solo solo;

    @Rule
    public ActivityTestRule<LogIn> rule =
            new ActivityTestRule<>(LogIn.class, true, true);

    /**
     * Sets up list with at least one book to test one
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // establish an instrument
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());

        // go to MyBooks and switch to addBookFragment
        solo.enterText((EditText) solo.getView(R.id.email_editText), "nrhassan@ualberta.ca");
        solo.enterText((EditText) solo.getView(R.id.password_editText), "PassWord15");
        solo.clickOnView(solo.getView(R.id.login_button));
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.clickOnMenuItem("My Books");

        // establish a book to work on
        solo.clickOnView(solo.getView(R.id.addButton));
        solo.enterText((EditText) solo.getView(R.id.book_title), "Song of the Lioness");
        solo.enterText((EditText) solo.getView(R.id.book_author), "Tamora Pierce");
        solo.enterText((EditText) solo.getView(R.id.book_isbn), "1234567890123");
        solo.clickOnButton("confirm");

        // view book's details
        ArrayList<TextView> book = solo.clickInList(1);
        solo.clickOnText("Song of the Lioness");
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);

    }

    /**
     * Asserts that the current activity switches from BookDetailsFragment to EditBookFragment on
     * clicking the edit button, if this fails it will show "Wrong Activity"
     */
    @Test
    public void checkActivityActivation() {
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);
        solo.clickOnView(solo.getView(R.id.edit_button));
        solo.assertCurrentActivity("Wrong Activity", EditBookFragment.class);
    }

    /**
     * Asserts that by clicking on the back button the activity will switch back to BookDetailsFragment and
     * nothing will change in the book list
     */
    @Test
    public void backButtonTest() {
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);
        solo.clickOnView(solo.getView(R.id.edit_button));
        solo.assertCurrentActivity("Wrong Activity", EditBookFragment.class);

        // leave without changing anything
        solo.assertCurrentActivity("Wrong Activity", EditBookFragment.class);
        solo.clickOnView(solo.getView(R.id.back_to_view_button));
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);
        solo.waitForText("Song of the Lioness", 1, 2000);
        solo.waitForText("Tamora Pierce", 1, 2000);
        solo.waitForText("1234567890123", 1, 2000);
        solo.clickOnView(solo.getView(R.id.back_to_books_button));

        // confirm that nothing has changed
        Fragment books = solo.getCurrentActivity().getFragmentManager().findFragmentById(R.id.myBooks);
        final ListView bookView = (ListView) solo.getView(R.id.book_list);
        assertEquals(1, bookView.getCount());
        Book book = (Book) bookView.getItemAtPosition(0);
        assertEquals("Song of the Lioness", book.getTitle());
        assertEquals("Tamora Pierce", book.getAuthor());
        assertEquals("1234567890123", book.getISBN());
    }

    /**
     * Asserts that by clicking on confirm having changed fields the activity will switch back to MyBooks and that
     * field in that book will change
     */
    @Test
    public void editPositiveTest() {
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);
        solo.clickOnView(solo.getView(R.id.edit_button));
        solo.assertCurrentActivity("Wrong Activity", EditBookFragment.class);

        // changing a field and leave
        solo.assertCurrentActivity("Wrong Activity", EditBookFragment.class);
        solo.clearEditText((EditText) solo.getView(R.id.edit_title));
        solo.enterText((EditText) solo.getView(R.id.edit_title), "Circle of Magic");
        solo.enterText((EditText) solo.getView(R.id.edit_isbn), "1234567890124");
        solo.clickOnButton("CONFIRM");
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);

        // confirm fields changed in bookDetails
        solo.waitForText("Circle of Magic", 1, 2000);
        solo.waitForText("Tamora Pierce", 1, 2000);
        solo.waitForText("1234567890124", 1, 2000);
        solo.clickOnView(solo.getView(R.id.back_to_books_button));

        // confirm fields changed in bookList
        Fragment books = solo.getCurrentActivity().getFragmentManager().findFragmentById(R.id.myBooks);
        final ListView bookView = (ListView) solo.getView(R.id.book_list);
        Book book = (Book) bookView.getItemAtPosition(0);
        assertNotEquals("Song of the Lioness", book.getTitle());
        assertEquals("Circle of Magic", book.getTitle());
        assertEquals("Tamora Pierce", book.getAuthor());
        assertEquals("1234567890123", book.getISBN());
    }

    /**
     * Asserts that by clicking on confirm not changing fields the activity will switch back to MyBooks and that
     * no fields in that book will change
     */
    @Test
    public void editNeutralTest() {
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);
        solo.clickOnView(solo.getView(R.id.edit_button));
        solo.assertCurrentActivity("Wrong Activity", EditBookFragment.class);

        // leave without changing anything
        solo.assertCurrentActivity("Wrong Activity", EditBookFragment.class);
        solo.clickOnButton("CONFIRM");
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);

        // confirm nothing has changed in bookDetail
        solo.waitForText("Song of the Lioness", 1, 2000);
        solo.waitForText("Tamora Pierce", 1, 2000);
        solo.waitForText("1234567890124", 1, 2000);
        solo.clickOnView(solo.getView(R.id.back_to_books_button));

        // confirm that nothing has changed in myBooks
        Fragment books2 = solo.getCurrentActivity().getFragmentManager().findFragmentById(R.id.myBooks);
        final ListView bookView = (ListView) solo.getView(R.id.book_list);
        Book book = (Book) bookView.getItemAtPosition(0);
        assertEquals("Song of the Lioness", book.getTitle());
        assertEquals("Tamora Pierce", book.getAuthor());
        assertEquals("1234567890123", book.getISBN());
    }

    /**
     * Asserts that by clicking on confirm leaving (a) blank field(s) the activity will wait until the field(s)
     * are filled to switch back to MyBooks and that only changed fields in that book will change
     */
    @Test
    public void editNegativeTest() {
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);
        solo.clickOnView(solo.getView(R.id.edit_button));
        solo.assertCurrentActivity("Wrong Activity", EditBookFragment.class);

        // leave with empty required fields
        solo.assertCurrentActivity("Wrong Activity", EditBookFragment.class);
        solo.clearEditText((EditText) solo.getView(R.id.edit_title));
        solo.clearEditText((EditText) solo.getView(R.id.edit_author));
        solo.clearEditText((EditText) solo.getView(R.id.edit_isbn));
        solo.clickOnButton("CONFIRM");
        solo.waitForText("Required: Book Title!", 1, 2000); // wait for error message
        solo.waitForText("Required: Book Author!", 1, 2000); // wait for error message
        solo.waitForText("Required: Book ISBN!", 1, 2000); // wait for error message
        assertTrue(solo.searchText("Required: Book Title!"));
        assertTrue(solo.searchText("Required: Book Author!"));
        assertTrue(solo.searchText("Required: Book ISBN!"));
    }

    @After
    public void tearDown(){
        solo.finishOpenedActivities();
    }
}


