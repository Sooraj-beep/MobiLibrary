package com.example.mobilibrary;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.mobilibrary.Activity.LogIn;
import com.example.mobilibrary.Activity.ProfileActivity;

import com.example.mobilibrary.Activity.StartActivity;

import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


import static org.junit.Assert.assertTrue;

public class requestTest {
    private Solo solo;
    private final String email_1 = "user1@gmail.com";
    private final String password_1 = "password1";
    private final String email_2 = "user2@gmail.com";
    private final String password_2 = "password2";
    private final String email_3 = "user3@gmail.com";
    private final String password_3 = "password3";
    private final String isbn_1 = "9781607066842";
    private RecyclerView requestLV;
    private Button accept;
    private Button decline;
    private View searchView;
    private RecyclerView bookLV;


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

    public void deleteBook() {
        //solo.assertCurrentActivity("Wrong Activity!", MainActivity.class);
        solo.clickOnView(solo.getView(R.id.delete_button));
        //solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
    }

    public void addBook(String isbn) {
        //solo.assertCurrentActivity("Wrong Activity!", MyBooksFragment.class);
        solo.clickOnView(solo.getView(R.id.addButton));
        solo.enterText((EditText) solo.getView(R.id.book_isbn), isbn);
        solo.sleep(3000);
        solo.clickOnView(solo.getView(R.id.confirm_book));
        //solo.assertCurrentActivity("Wrong Activity!", MyBooksFragment.class);
        //assertTrue(solo.waitForText("Successfully added book!", 1, 5000));
    }

    public void login(String email, String password) {
        solo.enterText((EditText) solo.getView(R.id.email_editText), email);
        solo.enterText((EditText) solo.getView(R.id.password_editText), password);
        solo.clickOnView(solo.getView(R.id.login_button));
        solo.sleep(3000);
        solo.assertCurrentActivity("Wrong Activity!", MainActivity.class);


    }

    public void logout() {
        solo.assertCurrentActivity("Wrong Activity!", MainActivity.class);
        solo.clickOnView(solo.getView(R.id.profile));
        solo.assertCurrentActivity("Wrong Activity!", ProfileActivity.class);
        solo.clickOnButton("Sign Out");
        solo.assertCurrentActivity("Wrong Activity!", StartActivity.class);

    }
    public void search(String title){
        searchView = solo.getView(R.id.search_view);
        bookLV = (RecyclerView) solo.getView(R.id.all_books_recycler_view);
        solo.clickOnView(searchView);
        solo.enterText(0, title);
        solo.sleep(2000);
        solo.clickOnText(title, 2);

    }

    /**
     * Checks that borrower can make a request for a book,
     * owner can see list of requests for his book and accept
     * one request.
     */
    @Test
    public void checkAcceptRequest() {
        //check request book, request list, accepting request
        //add book1 to user1
        login(email_1, password_1);
        solo.clickOnView(solo.getView(R.id.myBooks));
        addBook(isbn_1);
        solo.clickOnView(solo.getView(R.id.home));
        logout();
        //log in user2
        solo.clickOnButton("Log In");
        login(email_2, password_2);
        search("Great Pacific");
        solo.assertCurrentActivity("Wrong Activity!", BookDetailsFragment.class);
        solo.clickOnButton("Request");
        solo.clickOnView(solo.getView(R.id.back_to_books_button));
        logout();
        //log in user3
        solo.clickOnButton("Log In");
        login(email_3, password_3);
        search("Great Pacific");
        solo.assertCurrentActivity("Wrong Activity!", BookDetailsFragment.class);
        solo.clickOnButton("Request");
        solo.clickOnView(solo.getView(R.id.back_to_books_button));
        logout();
        //log in user1
        solo.clickOnButton("Log In");
        login(email_1, password_1);
        solo.clickOnView(solo.getView(R.id.myBooks));
        solo.clickOnText("Great Pacific");
        solo.assertCurrentActivity("Wrong Activity!", BookDetailsFragment.class);
        //get request list
        solo.clickOnView(solo.getView(R.id.reqBtn));
        requestLV = (RecyclerView) solo.getView(R.id.reqList);
        // accept user2's request
        View view = requestLV.getChildAt(0);
        accept = (Button) view.findViewById(R.id.accept_button);
        solo.clickOnView(accept);
        solo.goBack();
        assertTrue("List has items!", requestLV.getAdapter().getItemCount() == 0);
        //delete book
        solo.clickOnView(solo.getView(R.id.detailsBtn));
        deleteBook();
    }
    /**
     * Checks that borrower can make a request for a book,
     * owner can see list of requests for his book and decline
     * request.
     */
    @Test
    public void checkDeclineRequest() {
        //check request book, request list, deleting request
        //add book1 to user1
        login(email_1, password_1);
        solo.clickOnView(solo.getView(R.id.myBooks));
        addBook(isbn_1);
        solo.clickOnView(solo.getView(R.id.home));
        logout();
        //log in user2
        solo.clickOnButton("Log In");
        login(email_2, password_2);
        search("Great Pacific");
        solo.assertCurrentActivity("Wrong Activity!", BookDetailsFragment.class);
        solo.clickOnButton("Request");
        solo.clickOnView(solo.getView(R.id.back_to_books_button));
        logout();
        //log in user3
        solo.clickOnButton("Log In");
        login(email_3, password_3);
        search("Great Pacific");
        solo.assertCurrentActivity("Wrong Activity!", BookDetailsFragment.class);
        solo.clickOnButton("Request");
        solo.clickOnView(solo.getView(R.id.back_to_books_button));
        logout();
        //log in user1
        solo.clickOnButton("Log In");
        login(email_1, password_1);
        solo.clickOnView(solo.getView(R.id.myBooks));
        solo.clickOnText("Great Pacific");
        solo.assertCurrentActivity("Wrong Activity!", BookDetailsFragment.class);
        //get request list
        solo.clickOnView(solo.getView(R.id.reqBtn));
        requestLV = (RecyclerView) solo.getView(R.id.reqList);
        // decline user2's request
        View view = requestLV.getChildAt(0);
        decline = (Button) view.findViewById(R.id.decline_button);
        solo.clickOnView(decline);
        solo.sleep(2000);
        assertTrue("List has more than 1 item!", requestLV.getAdapter().getItemCount() == 1);
        //delete book
        solo.clickOnView(solo.getView(R.id.detailsBtn));
        deleteBook();
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
