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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.mobilibrary.Activity.LogIn;
import com.robotium.solo.Solo;

import org.json.JSONArray;
import org.json.JSONException;
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
    }

    @Test
    public void start() throws Exception {
        Activity activity = rule.getActivity();
    }

    /**
     * Asserts that the current activity switches from BookDetailsFragment to EditBookFragment on
     * clicking the edit button, if this fails it will show "Wrong Activity"
     */
    @Test
    public void checkActivityActivation() {
        // go to MyBooks and switch to bookDetailsFragment
        solo.enterText((EditText) solo.getView(R.id.email_editText), "nataliahh@testemail.com");
        solo.enterText((EditText) solo.getView(R.id.password_editText), "PassWord");
        solo.clickOnView(solo.getView(R.id.login_button));
        solo.waitForActivity(MainActivity.class);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.clickOnMenuItem("My Books");
        solo.waitForText("My Books");

        // establish a book to work on
        solo.clickOnView(solo.getView(R.id.addButton));
        solo.assertCurrentActivity("Wrong Activity", AddBookFragment.class);
        solo.enterText((EditText) solo.getView(R.id.book_title), "Song of the Lioness");
        solo.enterText((EditText) solo.getView(R.id.book_author), "Tamora Pierce");
        solo.enterText((EditText) solo.getView(R.id.book_isbn), "1234567890123");
        solo.clickOnButton("Confirm");

        // check that the book was added
        solo.waitForText("Song of the Lioness");
        solo.waitForText("Tamora Pierce");

        // view book's details
        solo.clickOnText("Song of the Lioness");
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
        // go to MyBooks and switch to bookDetailsFragment
        solo.enterText((EditText) solo.getView(R.id.email_editText), "nataliahh@testemail.com");
        solo.enterText((EditText) solo.getView(R.id.password_editText), "PassWord");
        solo.clickOnView(solo.getView(R.id.login_button));
        solo.waitForActivity(MainActivity.class);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.clickOnMenuItem("My Books");
        solo.waitForText("My Books");

        // establish a book to work on
        solo.clickOnView(solo.getView(R.id.addButton));
        solo.assertCurrentActivity("Wrong Activity", AddBookFragment.class);
        solo.enterText((EditText) solo.getView(R.id.book_title), "Song of the Lioness");
        solo.enterText((EditText) solo.getView(R.id.book_author), "Tamora Pierce");
        solo.enterText((EditText) solo.getView(R.id.book_isbn), "1234567890123");
        solo.clickOnButton("Confirm");

        // open book details fragment on the established book and go to edit book fragment after
        solo.clickOnText("Song of the Lioness");
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
        solo.waitForText("Song of the Lioness", 1, 2000);
        solo.waitForText("Tamora Pierce", 1, 2000);
        solo.waitForText("1234567890123", 1, 2000);
    }

    /**
     * Asserts that by clicking on confirm having changed fields the activity will switch back to MyBooks and that
     * field in that book will change
     */
    @Test
    public void editPositiveTest() {
        // go to MyBooks and switch to bookDetailsFragment
        solo.enterText((EditText) solo.getView(R.id.email_editText), "nataliahh@testemail.com");
        solo.enterText((EditText) solo.getView(R.id.password_editText), "PassWord");
        solo.clickOnView(solo.getView(R.id.login_button));
        solo.waitForActivity(MainActivity.class);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.clickOnMenuItem("My Books");
        solo.waitForText("My Books");

        // establish a book to work on
        solo.clickOnView(solo.getView(R.id.addButton));
        solo.assertCurrentActivity("Wrong Activity", AddBookFragment.class);
        solo.enterText((EditText) solo.getView(R.id.book_title), "Song of the Lioness");
        solo.enterText((EditText) solo.getView(R.id.book_author), "Tamora Pierce");
        solo.enterText((EditText) solo.getView(R.id.book_isbn), "1234567890123");
        solo.clickOnButton("Confirm");

        // open book details fragment on the established book and go to edit book fragment after
        solo.clickOnText("Song of the Lioness");
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);
        solo.clickOnView(solo.getView(R.id.edit_button));
        solo.assertCurrentActivity("Wrong Activity", EditBookFragment.class);

        // change fields and leave
        solo.clearEditText((EditText) solo.getView(R.id.edit_title));
        solo.enterText((EditText) solo.getView(R.id.edit_title), "Circle of Magic");
        solo.clearEditText((EditText) solo.getView(R.id.edit_isbn));
        solo.enterText((EditText) solo.getView(R.id.edit_isbn), "1234567890124");
        solo.clickOnButton("Confirm");
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);

        // confirm fields changed in bookDetails
        solo.waitForText("Circle of Magic", 1, 2000);
        solo.waitForText("Tamora Pierce", 1, 2000);
        solo.waitForText("1234567890124", 1, 2000);
        solo.clickOnView(solo.getView(R.id.back_to_books_button));

        // confirm fields changed in bookList
        solo.waitForText("Circle of Magic", 1, 2000);
        solo.waitForText("Tamora Pierce", 1, 2000);
        solo.waitForText("1234567890124", 1, 2000);
    }

    /**
     * Asserts that by clicking on confirm not changing fields the activity will switch back to MyBooks and that
     * no fields in that book will change
     */
    @Test
    public void editNeutralTest() {
        // go to MyBooks and switch to bookDetailsFragment
        solo.enterText((EditText) solo.getView(R.id.email_editText), "nataliahh@testemail.com");
        solo.enterText((EditText) solo.getView(R.id.password_editText), "PassWord");
        solo.clickOnView(solo.getView(R.id.login_button));
        solo.waitForActivity(MainActivity.class);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.clickOnMenuItem("My Books");
        solo.waitForText("My Books");

        // establish a book to work on
        solo.clickOnView(solo.getView(R.id.addButton));
        solo.assertCurrentActivity("Wrong Activity", AddBookFragment.class);
        solo.enterText((EditText) solo.getView(R.id.book_title), "Song of the Lioness");
        solo.enterText((EditText) solo.getView(R.id.book_author), "Tamora Pierce");
        solo.enterText((EditText) solo.getView(R.id.book_isbn), "1234567890123");
        solo.clickOnButton("Confirm");

        // open book details fragment on the established book and go to edit book fragment after
        solo.clickOnText("Song of the Lioness");
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);
        solo.clickOnView(solo.getView(R.id.edit_button));
        solo.assertCurrentActivity("Wrong Activity", EditBookFragment.class);

        // leave without changing anything
        solo.clickOnButton("Confirm");
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);

        // confirm nothing has changed in bookDetail
        solo.waitForText("Song of the Lioness", 1, 2000);
        solo.waitForText("Tamora Pierce", 1, 2000);
        solo.waitForText("1234567890123", 1, 2000);
        solo.clickOnView(solo.getView(R.id.back_to_books_button));

        // confirm that nothing has changed in myBooks
        solo.waitForText("Song of the Lioness", 1, 2000);
        solo.waitForText("Tamora Pierce", 1, 2000);
        solo.waitForText("1234567890123", 1, 2000);
    }

    /**
     * Asserts that by clicking on confirm leaving (a) blank field(s) the activity will wait until the field(s)
     * are filled to switch back to MyBooks and that only changed fields in that book will change
     */
    @Test
    public void editNegativeTest() {
        // go to MyBooks and switch to bookDetailsFragment
        solo.enterText((EditText) solo.getView(R.id.email_editText), "nataliahh@testemail.com");
        solo.enterText((EditText) solo.getView(R.id.password_editText), "PassWord");
        solo.clickOnView(solo.getView(R.id.login_button));
        solo.waitForActivity(MainActivity.class);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.clickOnMenuItem("My Books");
        solo.waitForText("My Books");

        // establish a book to work on
        solo.clickOnView(solo.getView(R.id.addButton));
        solo.assertCurrentActivity("Wrong Activity", AddBookFragment.class);
        solo.enterText((EditText) solo.getView(R.id.book_title), "Song of the Lioness");
        solo.enterText((EditText) solo.getView(R.id.book_author), "Tamora Pierce");
        solo.enterText((EditText) solo.getView(R.id.book_isbn), "1234567890123");
        solo.clickOnButton("Confirm");

        // open book details fragment on the established book and go to edit book fragment after
        solo.clickOnText("Song of the Lioness");
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);
        solo.clickOnView(solo.getView(R.id.edit_button));
        solo.assertCurrentActivity("Wrong Activity", EditBookFragment.class);

        // leave with empty required fields
        solo.clearEditText((EditText) solo.getView(R.id.edit_title));
        solo.clearEditText((EditText) solo.getView(R.id.edit_author));
        solo.clearEditText((EditText) solo.getView(R.id.edit_isbn));
        solo.clickOnButton("Confirm");
        solo.waitForText("Required: Book Title!", 1, 2000); // wait for error message
        solo.waitForText("Required: Book Author!", 1, 2000); // wait for error message
        solo.waitForText("Required: Book ISBN!", 1, 2000); // wait for error message
        assertTrue(solo.searchText("Required: Book Title!"));
        assertTrue(solo.searchText("Required: Book Author!"));
        assertTrue(solo.searchText("Required: Book ISBN!"));
    }

    /**
     * Tests Jparse function
     */
    @Test
    public void fetchBookData() {
        //Asserts that when given an isbn, it fetches the correct corresponding title and author
        String base = "https://www.googleapis.com/books/v1/volumes?q=isbn:9780545010221";
        String isbn = "9781911223139";
        Uri uri = Uri.parse(base + isbn);
        Uri.Builder builder = uri.buildUpon();
        String key = builder.toString();

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, key.toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String title = "";
                        String author = "";
                        try {

                            JSONArray items = response.getJSONArray("items");
                            JSONObject item = items.getJSONObject(0);
                            JSONObject volumeInfo = item.getJSONObject("volumeInfo");

                            try {
                                title = volumeInfo.getString("title");
                                assertTrue(title == "Best Murder in Show");

                                JSONArray authors = volumeInfo.getJSONArray("authors");
                                assertTrue(author == "Debbie Young");

                            } catch (Exception e) {

                            }

                        } catch (JSONException e) { //error trying to get database info
                            e.printStackTrace();

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
    }

    @After
    public void tearDown(){
        solo.finishOpenedActivities();
    }
}


