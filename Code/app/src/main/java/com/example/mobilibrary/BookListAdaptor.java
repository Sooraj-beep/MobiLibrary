package com.example.mobilibrary;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilibrary.DatabaseController.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.List;

public class BookListAdaptor extends RecyclerView.Adapter<BookListAdaptor.MyViewHolder> {
    private List<String> mTitles;
    private List<String> mAuthors;
    private List<String> mISBNS;
    private List<String> mStatuses;
    private List<String> mOwners;
    private List<String> mImages;
    private List<String> mId;
    private Context mContext;

    public BookListAdaptor(Context context, List<String> titles, List<String> authors, List<String> isbns, List<String> statuses, List<String> owners, List<String> images, List<String> ids) {
        mTitles = titles;
        mAuthors = authors;
        mISBNS = isbns;
        mStatuses = statuses;
        mOwners = owners;
        mImages = images;
        mId = ids;
        mContext = context;


    }

    // Create new views (invoked by the layout manager)
    @Override
    public BookListAdaptor.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.books_rows, parent, false);

        MyViewHolder vh = new MyViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // get element from your dataset at this position
        // replace the contents of the view with that element
        holder.title.setText(mTitles.get(position));
        holder.author.setText(mAuthors.get(position));
        holder.isbn.setText(mISBNS.get(position));
        holder.status.setText(mStatuses.get(position));

        //click listener
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //get image of book clicked
                /*byte[] bookImage = null;
                if (mImages.get(position) != null) {
                    bookImage = mImages.get(position).toBytes();
                }*/

                //Get the User object from currently clicked book by going into firestore
                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection("Users").document(mOwners.get(position));
                //String finalBookImage = bookImage.toString();
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        System.out.println("Document snapshot data: " + document.getData());
                        System.out.println("Document snapshot data email: " + document.get("email"));

                        String username = document.get("username").toString();
                        String email = document.get("email").toString();
                        String name = document.get("name").toString();
                        String phoneNo = document.get("phoneNo").toString();

                        User user = new User(username, email, name, phoneNo);

                        //method to initiate book details intent
                        initIntent(user);

                    }
                    public void initIntent(User user){
                        //get the book details of currently clicked item
                        //Book newBook = new Book(mTitles.get(position), mISBNS.get(position), mAuthors.get(position), mStatuses.get(position), mImages.get(position), mId.get(position), user);
                        Book newBook = new Book(mId.get(position), mTitles.get(position), mISBNS.get(position), mAuthors.get(position), mStatuses.get(position), mImages.get(position), user);
                        Intent viewBook = new Intent(mContext, BookDetailsFragment.class);
                        viewBook.putExtra("view book", newBook);
                        mContext .startActivity(viewBook);
                    }
                });
            }
        });
    }



    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mTitles.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView author;
        public TextView isbn;
        public TextView status;
        CardView parentLayout;

        public MyViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.book_title);
            author = (TextView) v.findViewById(R.id.book_author);
            isbn = (TextView) v.findViewById(R.id.book_isbn);
            status = (TextView) v.findViewById(R.id.book_status);
            parentLayout = (CardView) v.findViewById(R.id.parent_layout);
        }

    }
}