package com.example.mobilibrary;

import android.content.Intent;
import android.os.Bundle;



import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobilibrary.DatabaseController.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

/**
 * My Books fragment that is navigated to using notification bar. Contains a dropdown that organizes the User's books into status:
 * Owned, Requested, Accepted, and Borrowed. The user is able to see book title, author, isbn, and status.
 * The user is also able to add and edit their books in this Fragment
 *
 */
public class MyBooksFragment extends Fragment {
    private static final String TAG = "MyBooksFragment";
    private ListView bookView;
    private ArrayAdapter<Book> bookAdapter;
    private ArrayList<Book> bookList;
    private FloatingActionButton addButton;

    private ArrayList<Book> tempBookList;
    private Spinner statesSpin;
    private static final String[] states = new String[]{"Owned", "Requested", "Accepted", "Borrowed"};
    private FirebaseFirestore db;
    private FirebaseUser userInfo;
    public MyBooksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("In MyBooks Fragment");
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_my_books, container, false);
        addButton = (FloatingActionButton) v.findViewById(R.id.addButton);
        bookView = (ListView) v.findViewById(R.id.book_list);
        db = FirebaseFirestore.getInstance();
        userInfo = FirebaseAuth.getInstance().getCurrentUser();

        /* we instantiate a new arraylist in case we have an empty firestore, if not we update this
        list later in updateBookList */

        bookList = new ArrayList<>();
        bookAdapter = new customBookAdapter(this.getActivity(), bookList);
        bookView.setAdapter(bookAdapter);
        currentUser(new Callback() {
            @Override
            public void onCallback(User user) {
                updateBookList(user);
            }
        });

        tempBookList = new ArrayList<>();

        statesSpin = (Spinner) v.findViewById(R.id.spinner);
        ArrayAdapter<String> SpinAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, states);
        SpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statesSpin.setAdapter(SpinAdapter);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addIntent = new Intent(getActivity(), AddBookFragment.class);
                startActivityForResult(addIntent, 0);
            }
        });

        bookView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Book book = bookList.get(i);
                Intent viewBook = new Intent(getActivity(), BookDetailsFragment.class);
                viewBook.putExtra("view book", book);
                // viewBook.putExtra("book owner", user.getusername());   // need to get user somehow, add User variable to this class
                startActivityForResult(viewBook, 1);
            }
        });

        statesSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String state = (String) adapterView.getItemAtPosition(i);
                currentUser(new Callback() {
                    @Override
                    public void onCallback(User user) {
                        updateBookList(user);
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return v;
    }

    /**
     * If requestCode is 0, if its 1, we are either deleting a book (result code =1) or editing
     * an existing book (result code = 2) with data.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Book new_book = (Book) Objects.requireNonNull(data.getExtras()).getSerializable("new book");
                bookAdapter.add(new_book);
                tempBookList.add(new_book);
                bookAdapter.notifyDataSetChanged();
            }
        }

        if (requestCode == 1) {
            if (resultCode == 1) {
                // book needs to be deleted, intent has book to delete
                Book delete_book = (Book) data.getSerializableExtra("delete book");

                // find the book to delete and delete it
                for (int i = 0; i < bookAdapter.getCount(); i++) {
                    Book currentBook = bookAdapter.getItem(i);
                    if (delete_book.compareTo(currentBook) == 0) {
                        tempBookList.remove(currentBook);
                        bookAdapter.remove(currentBook);
                    }
                }

                bookAdapter.notifyDataSetChanged();
            } else if (resultCode == 2) {
                // book was edited update data set
                Book edited_book = (Book) data.getSerializableExtra("edited book");

                // find the book to edit and edit it
                for (int i = 0; i < bookList.size(); i++) {
                    Book currentBook = bookList.get(i);
                    if (edited_book.compareTo(currentBook) == 0) {
                        currentBook.setTitle(edited_book.getTitle());
                        currentBook.setAuthor(edited_book.getAuthor());
                        currentBook.setISBN(edited_book.getISBN());
                        currentBook.setImage(edited_book.getImage());
                    }
                }
                bookAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Used to get the current user from the user collection of firestore and returns it on
     * a Callback because OnCompleteListener is asynchronous
     *
     * @param cbh
     */

    public void currentUser(final Callback cbh) {
        db.collection("Users").whereEqualTo("email", userInfo.getEmail()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String username = document.get("username").toString();
                                String email = userInfo.getEmail();
                                String name = document.get("name").toString();
                                String Phone = document.get("phoneNo").toString();
                                User currentUser = new User(username, email, name, Phone);
                                cbh.onCallback(currentUser);
                            }
                        }
                    }
                });
    }

    /**
     * Used to fill bookList with firestore items, will get the information from the current User
     *Call back and use it to instantiate a new book object from the firesotre information and add
     * it to the bookList (clears it in case we have new items and want to count them) and updates
     * adapter
     *
     * @param bookUser
     */
    public void updateBookList(final User bookUser){
        db.collection("Books").whereEqualTo("Owner", userInfo.getDisplayName()).orderBy("Title")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if(value != null) {
                                bookList.clear();
                                for (final QueryDocumentSnapshot doc : value) {
                                    if (!(doc.getData().isEmpty())) {
                                        Log.d(TAG, String.valueOf(doc.getData().get("Owner")));
                                        String bookTitle = Objects.requireNonNull(doc.get("Title")).toString();
                                        String bookAuthor = Objects.requireNonNull(doc.get("Author")).toString();
                                        String bookISBN = Objects.requireNonNull(doc.get("ISBN")).toString();
                                        String bookStatus = Objects.requireNonNull(doc.get("Status")).toString();
                                        byte[] bookImage = null;
                                        if ((Blob) doc.get("Image") != null) {
                                            Blob imageBlob = (Blob) doc.get("Image");
                                            bookImage = imageBlob.toBytes();
                                        }
                                        bookList.add(new Book(bookTitle, bookISBN, bookAuthor, bookStatus, bookImage, bookUser));
                                    }
                                }
                                bookAdapter.notifyDataSetChanged(); // Notifying the adapter to render any new data fetched from the cloud
                            }

                            String currState = statesSpin.getSelectedItem().toString().toLowerCase();
                            Log.d("sooraj5",currState);

                            if (currState.equals("owned") == false) {
                                Log.d("sooraj5","a test");
                                if (currState.equals(bookStatus) == true){
                                    bookList.add(new Book(bookTitle, bookISBN, bookAuthor, bookStatus, bookImage, bookUser));
                                }
                            }
                            else {
                                bookList.add(new Book(bookTitle, bookISBN, bookAuthor, bookStatus, bookImage, bookUser));
                            }

                            tempBookList.add(new Book(bookTitle,bookISBN,bookAuthor,bookStatus,bookImage,bookUser));
                        }
                        bookAdapter.notifyDataSetChanged(); // Notifying the adapter to render any new data fetched from the cloud
                }
                });
    }

    /**
     * Used to function spinner, if the book is in a certain status it will group them and will
     * allow the user to view them as such
     *
     * @param state
     */


}