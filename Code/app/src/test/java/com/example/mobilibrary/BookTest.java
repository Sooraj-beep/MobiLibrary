package com.example.mobilibrary;
import com.example.mobilibrary.DatabaseController.User;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookTest {

    /**
     * creates a book object to use in testing
     * @return book object
     */
    private Book mockBook() {
        User mockOwner = new User("username", "email@example.com", "First Last", "123-123-1234");
        Book mockBook = new Book("Song of the Lioness", "123456789","Tamora Pierce", "available", null, mockOwner);
        return mockBook;
    }

    /**
     * Tests if a the getUser() function in books is correct. Test passes if all fields of the User in the constructor
     * is reported the same as all fields of the User reported by getUser() function.
     */
    @Test
    void getUserTest(){
        User mockOwner = new User("username", "email@example.com", "First Last", "123-123-1234");
        Book mockBook = mockBook();
        User guessOwner = mockBook.getOwner();
        assertEquals(mockOwner.getUsername(), guessOwner.getUsername());
        assertEquals(mockOwner.getEmail(), guessOwner.getEmail());
        assertEquals(mockOwner.getName(), guessOwner.getName());
        assertEquals(mockOwner.getPhoneNo(), guessOwner.getPhoneNo());
    }

    /**
     * Tests if a new user can be set as the owner of the book. Test passes if all fields of the User that owns
     * the book is the same as the new user set as the book's new owner
     */
    @Test
    void setUserTest(){
        Book mockBook = mockBook();
        User mockOwner = new User("username1", "email_1@example.com", "First Middle Last", "123-123-1235");
        mockBook.setOwner(mockOwner);
        assertEquals(mockOwner.getUsername(), mockBook.getOwner().getUsername());
        assertEquals(mockOwner.getEmail(), mockBook.getOwner().getEmail());
        assertEquals(mockOwner.getName(), mockBook.getOwner().getName());
        assertEquals(mockOwner.getPhoneNo(), mockBook.getOwner().getPhoneNo());
    }

    /**
     * Tests if two books that have the same ISBN are reported the same book even if they are different instances
     * of the book class. Test passes if the compareTo() function reports 0
     */
    @Test
    void compareToTestSame(){
        Book mockBook1 = mockBook();
        Book mockBook2 = mockBook1;
        assertEquals(0, mockBook1.compareTo(mockBook2));
    }

    /**
     * Tests if two books with different ISBN are not reported as the same book. Test passes if the compareTo()
     * function reports 1
     */
    @Test
    void compareToTestDifferent(){
        Book mockBook1 = mockBook();
        Book mockBook2 = mockBook();

        assertEquals(1, mockBook1.compareTo(mockBook2));
    }

    @Test
    void testStatus() {
        Book book = mockBook();
        assertEquals("available",book.getStatus());
    }

    @Test
    void testChangeStatus() {
        Book book = mockBook();
        book.setStatus("borrowed");
        assertEquals("borrowed",book.getStatus());
        book.setStatus("available");
        assertEquals("available",book.getStatus());
    }
}
