package com.example.mobilibrary;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilibrary.DatabaseController.DatabaseHelper;
import com.example.mobilibrary.DatabaseController.RequestService;
import com.example.mobilibrary.DatabaseController.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;


/**
 * My Books fragment that is navigated to using notification bar. Contains a dropdown that organizes the User's books into status:
 * Owned, Requested, Accepted, and Borrowed. The user is able to see book title, author, isbn, and status.
 * The user is also able to add and edit their books opened from this Fragment
 */
public class MyBooksFragment extends Fragment {
    private static final String TAG = "MyBooksFragment";
    private RecyclerView.Adapter bookAdapter;
    private ArrayList<Book> bookList = new ArrayList<>();

    private Spinner statesSpin;
    private String spinnerSelected = "owned";
    private static final String[] states = new String[]{"Owned", "Requested", "Accepted", "Borrowed"};
    private FirebaseFirestore db;
    private String bookImage;


    public MyBooksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("In MyBooks Fragment");
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_my_books, container, false);
        FloatingActionButton addButton = v.findViewById(R.id.addButton);
        RecyclerView bookView = v.findViewById(R.id.book_list);
        db = FirebaseFirestore.getInstance();
        DatabaseHelper databaseHelper = new DatabaseHelper(this.getContext());
        /* we instantiate a new arraylist in case we have an empty firestore, if not we update this
        list later in updateBookList */

        bookAdapter = new customBookAdapter(getContext(), bookList);
        bookView.setAdapter(bookAdapter);
        updateBookList();

        statesSpin = (Spinner) v.findViewById(R.id.spinner);
        ArrayAdapter<String> SpinAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, states);
        SpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statesSpin.setAdapter(SpinAdapter);

        RequestService requestService = RequestService.getInstance();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addIntent = new Intent(getActivity(), AddBookFragment.class);
                startActivity(addIntent);
            }
        });


        statesSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spinnerSelected = statesSpin.getSelectedItem().toString().toLowerCase();;
                updateBookList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return v;
    }

    /**
     * Used to fill bookList with firestore items, will get the information from the current User
     * Call back and use it to instantiate a new book object from the firestore information and add
     * it to the bookList (clears it in case we have new items and want to count them) and updates
     * adapter. As well as added a spinned functions (which lets you filter your books based on
     * status)
     */
    public void updateBookList() {
        CurrentUser bookUser = CurrentUser.getInstance();
        System.out.println("IN UPDATE BOOKLIST");
        if (spinnerSelected.equals("owned")) {
            db.collection("Books").whereEqualTo("Owner", bookUser.getCurrentUser().getUsername()).orderBy("Title")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            bookList.removeAll(bookList);
                            if (value != null ) {
                                for (final QueryDocumentSnapshot doc : value) {
                                    Log.d(TAG, String.valueOf(doc.getData().get("Owner")));
                                    String bookId = doc.getId();
                                    String bookTitle = Objects.requireNonNull(doc.get("Title")).toString();
                                    String bookAuthor = Objects.requireNonNull(doc.get("Author")).toString();
                                    String bookISBN = Objects.requireNonNull(doc.get("ISBN")).toString();
                                    String bookStatus = Objects.requireNonNull(doc.get("Status")).toString();
                                    if (doc.get("imageID") != null) {
                                        bookImage = Objects.requireNonNull(doc.get("imageID")).toString();
                                    }
                                    Book currentBook = new Book(bookId, bookTitle, bookISBN, bookAuthor, bookStatus, bookImage,
                                            bookUser.getCurrentUser());

                                    bookList.add(currentBook);
                                }
                                bookAdapter.notifyDataSetChanged(); // Notifying the adapter to render any new data fetched from the cloud

                            }
                        }
                    });
        } else if (spinnerSelected.equals("requested")) {
            db.collection("Books").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    bookList.removeAll(bookList);
                    if (value != null) {
                        for (final QueryDocumentSnapshot doc : value) {
                            System.out.println(doc.getId());
                            System.out.println(doc.getString("Title"));
                            String bookId = doc.getId();
                            String bookTitle = Objects.requireNonNull(doc.get("Title")).toString();
                            String bookAuthor = Objects.requireNonNull(doc.get("Author")).toString();
                            String bookISBN = Objects.requireNonNull(doc.get("ISBN")).toString();
                            String bookStatus = Objects.requireNonNull(doc.get("Status")).toString();
                            String bookOwner = Objects.requireNonNull(doc.get("Owner")).toString();
                            if (doc.get("imageID") != null) {
                                bookImage = Objects.requireNonNull(doc.get("imageID")).toString();
                            }
                            User otherUser = new User(bookOwner, "other", "other", "other");
                            Book currentBook = new Book(bookId, bookTitle, bookISBN, bookAuthor, bookStatus, bookImage, otherUser);
                            db.collection("Requests").whereEqualTo("requester", bookUser.getCurrentUser().getUsername())
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                            if (value != null) {
                                                for (final QueryDocumentSnapshot doc : value) {
                                                    if (currentBook.getFirestoreID().equals(Objects.requireNonNull(doc.get("bookID")).toString())) {
                                                        bookList.add(currentBook);
                                                    }
                                                }
                                                bookAdapter.notifyDataSetChanged();

                                            }
                                        }
                                    });
                        }
                    }
                }
            });
        } else if (spinnerSelected.equals("accepted")) {
            db.collection("Books").whereEqualTo("AcceptedTo", bookUser.getCurrentUser().getUsername())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            bookList.removeAll(bookList);
                            if (value != null) {
                                for (QueryDocumentSnapshot doc : value) {
                                    String bookId = doc.getId();
                                    String bookTitle = Objects.requireNonNull(doc.get("Title")).toString();
                                    String bookAuthor = Objects.requireNonNull(doc.get("Author")).toString();
                                    String bookISBN = Objects.requireNonNull(doc.get("ISBN")).toString();
                                    String bookStatus = Objects.requireNonNull(doc.get("Status")).toString();
                                    String bookOwner = Objects.requireNonNull(doc.get("Owner")).toString();
                                    if (doc.get("imageID") != null) {
                                        bookImage = Objects.requireNonNull(doc.get("imageID")).toString();
                                    }
                                    User owner = new User(bookOwner, "other", "other", "other");
                                    Book currentBook = new Book(bookId, bookTitle, bookISBN, bookAuthor, bookStatus, bookImage, owner);
                                    bookList.add(currentBook);
                                }
                                bookAdapter.notifyDataSetChanged();
                            }
                        }
                    });


        } else if (spinnerSelected.equals("borrowed")) {
            db.collection("Books").whereEqualTo("BorrowedBy", bookUser.getCurrentUser().getUsername())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            bookList.removeAll(bookList);
                            if (value != null) {
                                for (QueryDocumentSnapshot doc : value) {
                                    String bookId = doc.getId();
                                    String bookTitle = Objects.requireNonNull(doc.get("Title")).toString();
                                    String bookAuthor = Objects.requireNonNull(doc.get("Author")).toString();
                                    String bookISBN = Objects.requireNonNull(doc.get("ISBN")).toString();
                                    String bookStatus = Objects.requireNonNull(doc.get("Status")).toString();
                                    String bookOwner = Objects.requireNonNull(doc.get("Owner")).toString();
                                    if (doc.get("imageID") != null) {
                                        bookImage = Objects.requireNonNull(doc.get("imageID")).toString();
                                    }
                                    User owner = new User(bookOwner, "other", "other", "other");
                                    Book currentBook = new Book(bookId, bookTitle, bookISBN, bookAuthor, bookStatus, bookImage, owner);
                                    bookList.add(currentBook);
                                }
                                bookAdapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }

}
