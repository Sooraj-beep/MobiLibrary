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
        Book mockBook = new Book(null,"Song of the Lioness", "123456789","Tamora Pierce", "available", null, mockOwner);
        return mockBook;
    }

    /**
     * Tests if a the getUser() function in books is correct. Test passes if all fields of the User in the constructor
     * is reported the same as all fields of the User reported by getUser() function.
     */
    @Test
    void getOwnerTest(){
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
    void setOwnerTest(){
        Book mockBook = mockBook();
        User mockOwner = new User("username1", "email_1@example.com", "First Middle Last", "123-123-1235");
        mockBook.setOwner(mockOwner);
        assertEquals(mockOwner.getUsername(), mockBook.getOwner().getUsername());
        assertEquals(mockOwner.getEmail(), mockBook.getOwner().getEmail());
        assertEquals(mockOwner.getName(), mockBook.getOwner().getName());
        assertEquals(mockOwner.getPhoneNo(), mockBook.getOwner().getPhoneNo());
    }

    @Test
    void getStatusTest() {
        Book book = mockBook();
        assertEquals("available",book.getStatus());
    }

    @Test
    void setStatusTest() {
        Book book = mockBook();
        book.setStatus("borrowed");
        assertEquals("borrowed",book.getStatus());
        book.setStatus("available");
        assertEquals("available",book.getStatus());
    }
}
