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

import com.robotium.solo.Solo;

import org.junit.After;
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
    public ActivityTestRule<MainActivity> rule =
            new ActivityTestRule<>(MainActivity.class, true, true);

    /**
     * Sets up list with at least one book to test one
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // establish an instrument
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());

        // go to MyBooks and switch to addBookFragment
        solo.enterText((EditText) solo.getView(R.id.email_editText), "nrhassan@testemail.ca");
        solo.enterText((EditText) solo.getView(R.id.password_editText), "PassWord15");
        solo.clickOnView(solo.getView(R.id.login_button));
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.clickOnMenuItem("My Books");

        // establish a book to work on
        solo.clickOnView(solo.getView(R.id.addButton));
        solo.assertCurrentActivity("Wrong Activity", AddBookFragment.class);
        solo.enterText((EditText) solo.getView(R.id.book_title), "Song of the Lioness");
        solo.enterText((EditText) solo.getView(R.id.book_author), "Tamora Pierce");
        solo.enterText((EditText) solo.getView(R.id.book_isbn), "1234567890123");
        solo.clickOnButton("confirm");
    }

    /**
     * Asserts that the current activity switches from MyBooks to BookDetailsFragment on
     * clicking a list item, if this fails it will show "Wrong Activity"
     */
    @Test
    public void checkActivityActivation() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        // make sure book is in myBooks to click on
        Fragment books = solo.getCurrentActivity().getFragmentManager().findFragmentById(R.id.myBooks);
        solo.waitForText("Song of the Lioness", 1, 2000);
        solo.waitForText("Tamora Pierce", 1, 2000);
        solo.waitForText("123456780123");
        ArrayList<TextView> book = solo.clickInList(1);
        solo.clickOnText("Song of the Lioness");
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);

        // check that correct information is displayed
        BookDetailsFragment bookDetails = (BookDetailsFragment) solo.getCurrentActivity();

        // determine if photo is null
        ImageView photo = (ImageView) solo.getView(R.id.imageView);
        Drawable drawable = photo.getDrawable();
        if (!(drawable instanceof BitmapDrawable)) {
            drawable = null;
        } else {
            if (((BitmapDrawable) drawable).getBitmap() == null) {
                drawable = null;
            }
        }
        
        // get displayed information
        String title = ((TextView) solo.getView(R.id.view_title)).getText().toString();
        String author = ((TextView) solo.getView(R.id.view_author)).getText().toString();
        String owner = ((TextView) solo.getView(R.id.view_owner)).getText().toString();
        String isbn = ((TextView) solo.getView(R.id.view_isbn)).getText().toString();
        String status = ((TextView) solo.getView(R.id.view_status)).getText().toString();

        // validate displayed information
        assertEquals("Song of the Lioness", title);
        assertEquals("Tamora Pierce", author);
        assertEquals("username", owner);
        assertEquals("1234567890123", isbn);
        assertNull(drawable);
    }

    /**
     * Asserts that by clicking on the back button the activity will switch back to MyBooks and
     * nothing will change in the book list
     */
    @Test
    public void backButtonTest() {
        // go from MyBooks to viewing a book
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        Fragment books1 = solo.getCurrentActivity().getFragmentManager().findFragmentById(R.id.myBooks);
        solo.waitForText("Song of the Lioness", 1, 2000);
        solo.waitForText("Tamora Pierce", 1, 2000);
        solo.waitForText("123456780123");
        ArrayList<TextView> book1 = solo.clickInList(1);
        solo.clickOnText("Song of the Lioness");
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);

        // leave book details without changing anything
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);
        solo.clickOnView(solo.getView(R.id.back_to_books_button));

        // check that nothing was changed since conception
        Fragment books2 = solo.getCurrentActivity().getFragmentManager().findFragmentById(R.id.myBooks);
        final ListView bookView = (ListView) solo.getView(R.id.book_list);
        assertEquals(1, bookView.getCount());
        Book book2 = (Book) bookView.getItemAtPosition(0);
        assertEquals("Song of the Lioness", book2.getTitle());
        assertEquals("Tamora Pierce", book2.getAuthor());
        assertEquals("1234567890123", book2.getISBN());

    }

    /**
     * Asserts that the current activity switches from BookDetailsFragment to EditBookFragment on
     * clicking the edit button, if this fails it will show "Wrong Activity"
     */
    @Test
    public void editBookTest() {
        // go from MyBooks to viewing a book
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        Fragment books1 = solo.getCurrentActivity().getFragmentManager().findFragmentById(R.id.myBooks);
        solo.waitForText("Song of the Lioness", 1, 2000);
        solo.waitForText("Tamora Pierce", 1, 2000);
        solo.waitForText("123456780123");
        ArrayList<TextView> book1 = solo.clickInList(1);
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
        // go from MyBooks to viewing a book
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        Fragment books1 = solo.getCurrentActivity().getFragmentManager().findFragmentById(R.id.myBooks);
        solo.waitForText("Song of the Lioness", 1, 2000);
        solo.waitForText("Tamora Pierce", 1, 2000);
        solo.waitForText("123456780123");
        ArrayList<TextView> book1 = solo.clickInList(1);
        solo.clickOnText("Song of the Lioness");
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);

        // delete the book
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);
        solo.clickOnView(solo.getView(R.id.delete_button));
        solo.assertCurrentActivity("Wrong Activity", BookDetailsFragment.class);

        // check that the only book formerly in list is deleted from the data list
        Fragment books2 = solo.getCurrentActivity().getFragmentManager().findFragmentById(R.id.myBooks);
        final ListView bookView = (ListView) solo.getView(R.id.book_list);
        assertEquals(0, bookView.getCount());
    }

    @After
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }
}

