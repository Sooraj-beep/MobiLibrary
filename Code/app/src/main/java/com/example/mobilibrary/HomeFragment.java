package com.example.mobilibrary;

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
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

/**
 * Homepage fragment that can be navigated to using navigation bar. Contains search bar, and access to the User profile.
 * If possible, will also show a list of all available books as default (low priority)
 * Can view book details by clicking on book
 */
public class HomeFragment extends Fragment implements SearchView.OnQueryTextListener {

    private DatabaseHelper databaseHelper;
    private SearchView searchBar;
    private ArrayAdapter<Book> allBooksAdapter;
    private ArrayList<Book> allBooksList;
    private ListView allBooksListView;
    private FirebaseFirestore db;
    private static final String TAG = "HomeFragment";

    private RecyclerView booksRV;
    private RecyclerView.Adapter mAdaptor;

    private List<String> titles = new ArrayList<>();
    private List<String> authors = new ArrayList<>();
    private List<String> isbns = new ArrayList<>();
    private List<String> statuses = new ArrayList<>();
    private List<String> owners = new ArrayList<>();
    private List<String> images = new ArrayList<>();
    private List<String> ids = new ArrayList<>();

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        databaseHelper = new DatabaseHelper(this.getContext());
        db = FirebaseFirestore.getInstance();

        allBooksListView = view.findViewById(R.id.all_books_list_view);
        searchBar = view.findViewById(R.id.search_view);

        // list and adapter same as MyBooksFragment ListView
        allBooksList = new ArrayList<>();
        allBooksAdapter = new customBookAdapter(this.getActivity(), allBooksList);
        allBooksListView.setAdapter(allBooksAdapter);
        allBooksListView.setTextFilterEnabled(true);
        setAllBooksList();

        searchBar.setOnQueryTextListener(this);

        allBooksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Book book = allBooksList.get(i);
                Intent viewBook = new Intent(getActivity(), BookDetailsFragment.class);
                viewBook.putExtra("view book", book);
                startActivityForResult(viewBook, 1);
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
     * Sets the allBooksListView, code minimally edited from MyBooksFragment
     */
    public void setAllBooksList() {
        db.collection("Books").orderBy("Title")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null) {
                            allBooksList.clear();
                            for (QueryDocumentSnapshot doc : value) {
                                Log.d(TAG, String.valueOf(doc.getData().get("Owner")));
                                final String bookId = doc.getId();
                                final String bookOwner = Objects.requireNonNull(doc.get("Owner")).toString();
                                final String bookTitle = Objects.requireNonNull(doc.get("Title")).toString();
                                final String bookAuthor = Objects.requireNonNull(doc.get("Author")).toString();
                                final String bookISBN = Objects.requireNonNull(doc.get("ISBN")).toString();
                                final String bookStatus = Objects.requireNonNull(doc.get("Status")).toString();
                                byte[] bookImage = null;
                                if ((Blob) doc.get("Image") != null) {
                                    Blob imageBlob = (Blob) doc.get("Image");
                                    bookImage = Objects.requireNonNull(imageBlob).toBytes();
                                }
                                final byte[] finalBookImage = bookImage;
                                db.collection("Users").document(bookOwner).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        databaseHelper.getUserProfile(bookOwner, new Callback() {
                                            @Override
                                            public void onCallback(User user) {
                                                allBooksList.add(new Book(bookId,bookTitle, bookISBN, bookAuthor, bookStatus, finalBookImage, user));
                                                allBooksAdapter.notifyDataSetChanged(); // Notifying the adapter to render any new data fetched from the cloud
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    }
                });
    }

    /**
     * Code from MyBooksFragment
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
        if (requestCode == 1) {
            if (resultCode == 1) {
                // book needs to be deleted, intent has book to delete
                Book delete_book = (Book) data.getSerializableExtra("delete book");

                // find the book to delete and delete it
                for (int i = 0; i < allBooksAdapter.getCount(); i++) {
                    Book currentBook = allBooksAdapter.getItem(i);
                    if (delete_book.getFirestoreID().equals(currentBook.getFirestoreID())) {
                        allBooksAdapter.remove(currentBook);
                    }
                }

                allBooksAdapter.notifyDataSetChanged();
            } else if (resultCode == 2) {
                // book was edited update data set
                Book edited_book = (Book) data.getSerializableExtra("edited book");

                // find the book to edit and edit it
                for (int i = 0; i < allBooksList.size(); i++) {
                    Book currentBook = allBooksList.get(i);
                    if (edited_book.getFirestoreID().equals(currentBook.getFirestoreID())) {
                        currentBook.setTitle(edited_book.getTitle());
                        currentBook.setAuthor(edited_book.getAuthor());
                        currentBook.setISBN(edited_book.getISBN());
                        currentBook.setImage(edited_book.getImage());
                    }
                }
                allBooksAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            allBooksListView.clearTextFilter();
        } else {
            allBooksListView.setFilterText(newText);
        }
        return true;
    }
}