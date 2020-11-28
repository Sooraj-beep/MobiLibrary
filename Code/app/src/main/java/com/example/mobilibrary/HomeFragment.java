package com.example.mobilibrary;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.mobilibrary.Activity.ProfileActivity;
import com.example.mobilibrary.DatabaseController.DatabaseHelper;
import com.example.mobilibrary.DatabaseController.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;


/**
 * @author Jill, Chloe;
 * Homepage fragment that can be navigated to using navigation bar. Contains search bar, and access to the User profile.
 * Shows list of all available or requested (but not accepted) books that are not the user's own (searchable).
 * Can view book details by clicking on book.
 */
public class HomeFragment extends Fragment implements SearchView.OnQueryTextListener {

    private DatabaseHelper databaseHelper;
    private SearchView searchBar;
    private customBookAdapter allBooksAdapter;
    private ArrayList<Book> allBooksList;
    private FirebaseFirestore db;
    private String bookImage;
    private static final String TAG = "HomeFragment";

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        databaseHelper = new DatabaseHelper(this.getContext());
        db = FirebaseFirestore.getInstance();

        RecyclerView allBooksRV = view.findViewById(R.id.all_books_recycler_view);
        searchBar = view.findViewById(R.id.search_view);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        allBooksRV.setLayoutManager(mLayoutManager);

        allBooksList = new ArrayList<>();
        allBooksAdapter = new customBookAdapter(getContext(), allBooksList);
        allBooksRV.setAdapter(allBooksAdapter);
        setAllBooksList();

        searchBar.setOnQueryTextListener(this);

        // Cancelling the search to return to all books again
        searchBar.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });

        // Go to the profile activity for the current logged in user
        FloatingActionButton profileButton = (FloatingActionButton) view.findViewById(R.id.profile);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("profile",
                        databaseHelper.getUser().getDisplayName());
                startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    /**
     * Sets the allBooksRV, code edited from MyBooksFragment
     */
    public void setAllBooksList() {
        db.collection("Books").orderBy("Title")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null) {
                            allBooksList.clear();
                            for (QueryDocumentSnapshot doc : value) {
                                final String bookStatus = Objects.requireNonNull(doc.get("Status")).toString();
                                final String bookOwner = Objects.requireNonNull(doc.get("Owner")).toString();
                                // Ensures that only books that are not the user's and also available or requested are shown on the page
                                if (!bookOwner.equals(databaseHelper.getUser().getDisplayName()) &&
                                        (bookStatus.equals("available") || bookStatus.equals("requested"))) {
                                    final String bookId = doc.getId();
                                    final String bookTitle = Objects.requireNonNull(doc.get("Title")).toString();
                                    final String bookAuthor = Objects.requireNonNull(doc.get("Author")).toString();
                                    final String bookISBN = Objects.requireNonNull(doc.get("ISBN")).toString();
                                    if (doc.get("imageID") != null) {
                                        bookImage = Objects.requireNonNull(doc.get("imageID")).toString();
                                    }
                                    db.collection("Users").document(bookOwner).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            databaseHelper.getUserProfile(bookOwner, new Callback() {
                                                @Override
                                                public void onCallback(User user) {
                                                    allBooksList.add(new Book(bookId, bookTitle, bookISBN, bookAuthor, bookStatus, bookImage, user));
                                                    Log.d(TAG, "Added all books not belonging to the user, either available or requested.");
                                                    allBooksAdapter.notifyDataSetChanged(); // Notifying the adapter to render any new data fetched from the cloud
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
    }

    /**
     * What is called when the search text is submitted.
     * @param query user input search query
     * @return bool (false to finish search)
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d("OnQueryTextSubmit", query);
        allBooksAdapter.getFilter().filter(query);
        searchBar.clearFocus(); // Exit keyboard
        return false;
    }

    /**
     * What is called when a user inputs text into the search bar, called with each new character change.
     * @param newText current user input (search query)
     * @return bool (true to search)
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d("OnQueryTextChange", newText);
        if (TextUtils.isEmpty(newText)) {
            allBooksAdapter.getFilter().filter("");
        } else {
            allBooksAdapter.getFilter().filter(newText);
        }
        return true;
    }

}
/*
package com.example.mobilibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;

import android.widget.TextView;

import com.example.mobilibrary.Activity.ProfileActivity;
import com.example.mobilibrary.DatabaseController.DatabaseHelper;
import com.example.mobilibrary.DatabaseController.User;
import com.example.mobilibrary.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

*/
/**
 * @author Jill, Chloe;
 * Homepage fragment that can be navigated to using navigation bar. Contains search bar, and access to the User profile.
 * Shows list of all available or requested (but not accepted) books that are not the user's own (searchable).
 * Can view book details by clicking on book.
 *//*

public class HomeFragment extends Fragment implements SearchView.OnQueryTextListener {

    private DatabaseHelper databaseHelper;
    private SearchView searchBar;
    private customBookAdapter allBooksAdapter;
    private ArrayList<Book> allBooksList;
    private RecyclerView allBooksListView;
    private FirebaseFirestore db;
    private String bookImage;
    private static final String TAG = "HomeFragment";

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        databaseHelper = new DatabaseHelper(this.getContext());
        db = FirebaseFirestore.getInstance();

        allBooksListView = view.findViewById(R.id.booksRV);
        searchBar = view.findViewById(R.id.searchbar);

        allBooksList = new ArrayList<>();
        allBooksAdapter = new customBookAdapter(this.getActivity(), allBooksList);
        allBooksListView.setAdapter(allBooksAdapter);
        setAllBooksList();

        searchBar.setOnQueryTextListener(this);

        // Cancelling the search to return to all books again
        searchBar.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });

        // Go to the book that is clicked on from the current list
//        allBooksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                allBooksAdapter.notifyDataSetChanged();
//                Book book = allBooksAdapter.getItem(i);
//                Intent viewBook = new Intent(getActivity(), BookDetailsFragment.class);
//                viewBook.putExtra("view book", book);
//                startActivity(viewBook);
//            }
//        });

        // Go to the profile activity for the current logged in user
        FloatingActionButton profileButton = (FloatingActionButton) view.findViewById(R.id.profile);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("profile",
                        databaseHelper.getUser().getDisplayName());
                startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    */
/**
     * Sets the allBooksListView, code edited from MyBooksFragment
     *//*

    public void setAllBooksList() {
        db.collection("Books").orderBy("Title")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null) {
                            allBooksList.clear();
                            for (QueryDocumentSnapshot doc : value) {
                                final String bookStatus = Objects.requireNonNull(doc.get("Status")).toString();
                                final String bookOwner = Objects.requireNonNull(doc.get("Owner")).toString();
                                // Ensures that only books that are not the user's and also available or requested are shown on the page
                                if (!bookOwner.equals(databaseHelper.getUser().getDisplayName()) &&
                                        (bookStatus.equals("available") || bookStatus.equals("requested"))) {
                                    final String bookId = doc.getId();
                                    final String bookTitle = Objects.requireNonNull(doc.get("Title")).toString();
                                    final String bookAuthor = Objects.requireNonNull(doc.get("Author")).toString();
                                    final String bookISBN = Objects.requireNonNull(doc.get("ISBN")).toString();
                                    if (doc.get("imageID") != null) {
                                        bookImage = Objects.requireNonNull(doc.get("imageID")).toString();
                                    }
                                    db.collection("Users").document(bookOwner).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            databaseHelper.getUserProfile(bookOwner, new Callback() {
                                                @Override
                                                public void onCallback(User user) {
                                                    allBooksList.add(new Book(bookId, bookTitle, bookISBN, bookAuthor, bookStatus, bookImage, user));
                                                    Log.d(TAG, "Added all books not belonging to the user, either available or requested.");
                                                    allBooksAdapter.notifyDataSetChanged(); // Notifying the adapter to render any new data fetched from the cloud
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
    }

    */
/**
     * What is called when the search text is submitted.
     * @param query user input search query
     * @return bool (false to finish search)
     *//*

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d("OnQueryTextSubmit", query);
        searchBar.clearFocus(); // Exit keyboard
        return false;
    }

    */
/**
     * What is called when a user inputs text into the search bar, called with each new character change.
     * @param newText current user input (search query)
     * @return bool (true to search)
     *//*

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d("OnQueryTextChange", newText);
        if (TextUtils.isEmpty(newText)) {
            allBooksAdapter.getFilter().filter("");
        } else {
            allBooksAdapter.getFilter().filter(newText);
        }
        return true;
    }

}*/
