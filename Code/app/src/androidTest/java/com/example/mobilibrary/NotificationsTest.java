package com.example.mobilibrary;

import android.app.Activity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.mobilibrary.Activity.LogIn;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertFalse;

/**
 * Test class for NotificationsFragment. All the UI tests are written here.
 * Robotium test framework is used.
 */

@RunWith(AndroidJUnit4.class)
public class NotificationsTest {

    private Solo solo;
    private RecyclerView requests;
    private final String ownerEmail = "cching@ualberta.ca";
    private final String ownerPassword = "chloeching";
    private final String borrower1Email = "chloe@gmail.com";
    private final String borrower1Password = "chloeching";

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

    /**
     * Checks that the owner receives a notification after a borrower requests their book
     */
    @Test
    public void requestNotification() {
        //owner creates a book
        logIn(ownerEmail, ownerPassword);
        solo.clickOnMenuItem("My Books");
        addBook("BookToBeRequested", "author", "1231231231231");
        //check notification make sure there is none yet
        assertFalse(solo.searchText("Has requested to borrow your book: "));
        solo.clickOnMenuItem("Home");
        logOut();
        //borrower requests book
        logIn(borrower1Email, borrower1Password);
        searchBook("BookToBeRequested");
        requestBook();
        logOut();
        //owner logs back in and checks notifications
        logIn(ownerEmail, ownerPassword);
        solo.clickOnMenuItem("Notifications");
        assertTrue(solo.searchText("Has requested to borrow your book: "));
        solo.clickOnMenuItem("Home");
        //delete book to return to original state
        deleteBook();

    }

    /**
     * Checks that borrowers will receive a notification if the owner interacts with their request
     */
    @Test
    public void borrowerNotifications() {
        //owner creates a book
        logIn(ownerEmail, ownerPassword);
        solo.clickOnMenuItem("My Books");
        addBook("BookToBeDeclined", "author", "3453453453453");
        logOut();
        //borrower requests book
        logIn(borrower1Email, borrower1Password);
        searchBook("BookToBeDeclined");
        requestBook();
        logOut();
        //owner rejects borrower
        logIn(ownerEmail, ownerPassword);
        solo.clickOnMenuItem("My Books");
        solo.clickInRecyclerView(0);
        solo.clickOnView(solo.getView(R.id.reqBtn));
        RecyclerView requests = (RecyclerView) solo.getView(R.id.reqList);
        View view = requests.getChildAt(0);
        Button button = (Button) view.findViewById(R.id.decline_button);
        solo.clickOnView(button);
        solo.clickOnView(solo.getView(R.id.back_to_books_button));
        solo.clickOnMenuItem("Home");
        logOut();
        //login borrower and check notification
        logIn(borrower1Email, borrower1Password);
        solo.clickOnMenuItem("Notifications");
        assertTrue(solo.searchText("Has declined your request for: "));
        solo.clickOnMenuItem("Home");
        logOut();
        //owner delete book to return to original state
        logIn(ownerEmail, ownerPassword);
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



    public void logIn(String email, String password) {
        solo.enterText((EditText) solo.getView(R.id.email_editText), email);
        solo.enterText((EditText) solo.getView(R.id.password_editText), password);
        solo.clickOnView(solo.getView(R.id.login_button));
        solo.waitForActivity(MainActivity.class);
    }

    public void logOut(){
        solo.clickOnView(solo.getView(R.id.profile));
        solo.clickOnView(solo.getView(R.id.sign_out_button));
        solo.clickOnView(solo.getView(R.id.home_log_in_button));
    }

    public void addBook(String title, String author, String isbn){
        solo.clickOnView(solo.getView(R.id.addButton));
        solo.enterText((EditText) solo.getView(R.id.book_isbn), isbn);
        solo.sleep(3000);
        solo.clearEditText((EditText) solo.getView(R.id.book_title));
        solo.clearEditText((EditText) solo.getView(R.id.book_author));
        solo.enterText((EditText) solo.getView(R.id.book_title), title);
        solo.enterText((EditText) solo.getView(R.id.book_author), author);
        solo.clickOnView(solo.getView(R.id.confirm_book));
        solo.getCurrentActivity().getFragmentManager().findFragmentById(R.id.myBooks);
        solo.waitForText(title, 1, 2000);
        solo.waitForText(author, 1, 2000);
        solo.waitForText(isbn, 1, 2000);
        solo.clickOnMenuItem("Home");

    }

    public void deleteBook(){
        solo.clickOnMenuItem("My Books");
        solo.clickInRecyclerView(0);
        solo.clickOnView(solo.getView(R.id.delete_button));
    }

    public void searchBook(String title){
        solo.clickOnView(solo.getView(R.id.search_view));
        solo.enterText(0, title);
        solo.sleep(2000);
        solo.clickInRecyclerView(0);

    }

    public void requestBook(){
        solo.clickOnView(solo.getView(R.id.request_button));
        solo.clickOnView(solo.getView(R.id.back_to_books_button));
        solo.clickOnMenuItem("Home");
    }
}
