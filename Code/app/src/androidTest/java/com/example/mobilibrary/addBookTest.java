package com.example.mobilibrary;

import android.app.Activity;
import android.net.Uri;
import android.widget.EditText;
import android.widget.ListView;

import android.app.Fragment;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.mobilibrary.Activity.LogIn;
import com.robotium.solo.Solo;
import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Test class for MainActivity. All the UI tests are written here. Robotium test framework is
 used
 */

@RunWith(AndroidJUnit4.class)
public class addBookTest{
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
     *Opens MyBooksFragment and then tries to open addBookFragment
     */
    @Test
    public void checkActivities() {
// Asserts that the current activity is MyBooks and if it will switch to addBookFragment. Otherwise, show “Wrong Activity”
        solo.enterText((EditText) solo.getView(R.id.email_editText), "ktran5@ualberta.ca");
        solo.enterText((EditText) solo.getView(R.id.password_editText), "happyfun33");
        solo.clickOnView(solo.getView(R.id.login_button));
        solo.waitForActivity(MainActivity.class);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.clickOnMenuItem("My Books");
        solo.waitForText("My Books");
    }

    /**
     *Opens addBookFragment and tries to add a new book (title, author, ISBN)
     */
    @Test
    public void addActivity() {
// Asserts that the current activity is MyBooks and if it will switch to addBookFragment. Otherwise, show “Wrong Activity”
        solo.enterText((EditText) solo.getView(R.id.email_editText), "ktran5@ualberta.ca");
        solo.enterText((EditText) solo.getView(R.id.password_editText), "happyfun33");
        solo.clickOnView(solo.getView(R.id.login_button));
        solo.waitForActivity(MainActivity.class);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.clickOnMenuItem("My Books");
        solo.clickOnView(solo.getView(R.id.addButton));
        solo.assertCurrentActivity("Wrong Activity", AddBookFragment.class);
        solo.enterText((EditText) solo.getView(R.id.book_title), "Harry Potter");
        solo.enterText((EditText) solo.getView(R.id.book_author), "J.K Rowling");
        solo.enterText((EditText) solo.getView(R.id.book_isbn), "1234567890123");
        solo.clickOnButton("confirm");
        Fragment books = solo.getCurrentActivity().getFragmentManager().findFragmentById(R.id.myBooks);
        solo.waitForText("Harry Potter", 1, 2000); // wait for title
        solo.waitForText("J.K Rowling", 1, 2000); // wait for author
        solo.waitForText("12345678", 1, 2000); // wait for ISBN
        ListView bookView = (ListView) solo.getView(R.id.book_list); // Get the listview
        Book book = (Book) bookView.getItemAtPosition(0); // Get item from first position
        assertEquals("Harry Potter", book.getTitle());
        assertEquals("J.K Rowling", book.getAuthor());
        assertEquals("1234567890123", book.getISBN());
    }

    /**
     *Opens addBookFragment and tries to add a new book, but with empty fields and checks for set errors.
     */
    @Test
    public void addActivityEmpty() {
// Asserts that the current activity is MyBooks and if it will switch to addBookFragment. Otherwise, show “Wrong Activity
        solo.enterText((EditText) solo.getView(R.id.email_editText), "ktran5@ualberta.ca");
        solo.enterText((EditText) solo.getView(R.id.password_editText), "happyfun33");
        solo.clickOnView(solo.getView(R.id.login_button));
        solo.waitForActivity(MainActivity.class);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.clickOnMenuItem("My Books");
        solo.clickOnView(solo.getView(R.id.addButton));
        solo.assertCurrentActivity("Wrong Activity", AddBookFragment.class);
        solo.clickOnButton("confirm");
        solo.waitForText("Please insert book title!", 1, 2000); // wait for error text
        solo.waitForText("Please insert book author!", 1, 2000); // wait for error text.
        solo.waitForText("Please insert book ISBN!", 1, 2000); // wait for error text.
        assertTrue(solo.searchText("Please insert book title!"));
        assertTrue(solo.searchText("Please insert book author!"));
        assertTrue(solo.searchText("Please insert book ISBN!"));
    }

    /**
     * Tests Jparse function
     */
    @Test
    public void fetchBookData(){
        //Asserts that when given an isbn, it fetches the correct corresponding title and author
        //RequestQueue mRequestQueue = Volley.newRequestQueue(AddBookFragment);
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
        //mRequestQueue.add(request);

    }

    /**
     * Test whether title and author autofill, when isbn is filled"
     */
    @Test
    public void addBookISBNAutofill() {
        solo.enterText((EditText) solo.getView(R.id.email_editText), "ktran5@ualberta.ca");
        solo.enterText((EditText) solo.getView(R.id.password_editText), "happyfun33");
        solo.clickOnView(solo.getView(R.id.login_button));
        solo.waitForActivity(MainActivity.class);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.clickOnMenuItem("My Books");
        solo.clickOnView(solo.getView(R.id.addButton));
        solo.assertCurrentActivity("Wrong Activity", AddBookFragment.class);
        solo.enterText((EditText) solo.getView(R.id.book_isbn), "9781683838265"); //input isbn for auto fill
        solo.waitForText("Harry Potter: Knitting Magic", 1, 2000); // wait for title
        solo.waitForText("Tanis Gray", 1, 2000); // wait for author
        solo.clickOnButton("confirm");
    }

    @After
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }
}
