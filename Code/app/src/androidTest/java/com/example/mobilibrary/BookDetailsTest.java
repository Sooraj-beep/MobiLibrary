package com.example.mobilibrary;

import android.app.Activity;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;

import com.example.mobilibrary.Activity.LogIn;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import android.app.Fragment;


@RunWith (AndroidJUnit4.class)
public class BookDetailsTest {
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
     * Asserts that the current activity switches from MyBooks to BookDetailsFragment on
     * clicking a list item, if this fails it will show "Wrong Activity"
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

        // establish a book to view
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
    }

    /**
     * Asserts that by clicking on the back button the activity will switch back to MyBooks and
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

        // go to bookDetails
        solo.clickOnText("Song of the Lioness");
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);

        // leave book details without changing anything
        solo.clickOnView(solo.getView(R.id.back_to_books_button));

        // check that nothing was changed since conception
        // check that the book was added
        solo.waitForText("Song of the Lioness");
        solo.waitForText("Tamora Pierce");
    }

    /**
     * Asserts that the current activity switches from BookDetailsFragment to EditBookFragment on
     * clicking the edit button, if this fails it will show "Wrong Activity"
     */
    @Test
    public void editBookTest() {
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

        // go to bookDetails
        solo.clickOnText("Song of the Lioness");
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);

        // check that clicking on edit button takes you to edit fragment
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);
        solo.clickOnView(solo.getView(R.id.edit_button));
        solo.assertCurrentActivity("Wrong Activity", EditBookFragment.class);
    }

    /**
     * Asserts that by clicking on the delete button the activity will switch back to MyBooks
     * and the book that was deleted will no longer be in the list
     */
    @Test
    public void deleteBookTest() {
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

        // go to bookDetails
        solo.clickOnText("Song of the Lioness");
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);

        // delete the book
        solo.clickOnView(solo.getView(R.id.delete_button));
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);

        // check that the only book formerly in list is deleted from the data list
        Assert.assertFalse("Song of the Lioness found", solo.searchText("Song of the Lioness"));
    }

    @After
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }
}