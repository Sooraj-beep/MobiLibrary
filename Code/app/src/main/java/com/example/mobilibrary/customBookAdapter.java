package com.example.mobilibrary;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.example.mobilibrary.DatabaseController.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Objects;

public class customBookAdapter extends RecyclerView.Adapter<customBookAdapter.MyViewHolder> implements Filterable {
    private ArrayList<Book> mBooks;
    private ArrayList<Book> mBooksfiltered;
    private Context mContext;

    public customBookAdapter(Context context, ArrayList<Book> books){
        this.mContext = context;
        this.mBooks = books;
        this.mBooksfiltered = books;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content, parent, false);

        MyViewHolder vh = new MyViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // get element from your dataset at this position
        // replace the contents of the view with that element
        holder.title.setText(mBooksfiltered.get(position).getTitle());
        System.out.println("Title " + mBooksfiltered.get(position).getTitle());
        holder.author.setText(mBooksfiltered.get(position).getAuthor());
        System.out.println("Author " + mBooksfiltered.get(position).getAuthor());
        holder.isbn.setText(mBooksfiltered.get(position).getISBN());
        System.out.println("ISBN " + mBooksfiltered.get(position).getISBN());
        String currStatus = mBooksfiltered.get(position).getStatus();
        holder.status.setText(currStatus);
        System.out.println("Status " + currStatus);
        switch (currStatus) {
            case "available":
                holder.status.setBackgroundResource(R.drawable.rounded_bg_avail);
                break;
            case "requested":
                holder.status.setBackgroundResource(R.drawable.rounded_bg_req);
                break;
            case "accepted":
                holder.status.setBackgroundResource(R.drawable.rounded_bg_acc);
                break;
            case "borrowed":
                holder.status.setBackgroundResource(R.drawable.rounded_bg_borr);
                break;
        }

        //click listener
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get image of book clicked
                byte[] bookImage = null;
                if (mBooksfiltered.get(position).getImageId() != null){
                    String bookImageString = mBooksfiltered.get(position).getImageId();
                    bookImage = Base64.decode(bookImageString, 0);
                }

                //Get the User object from currently clicked book by going into firestore
                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                String owner_username = mBooksfiltered.get(position).getOwner().getUsername();
                DocumentReference doc = db.collection("Users").document(owner_username);
                if (bookImage != null){
                    Base64.encodeToString(bookImage, Base64.DEFAULT);
                }
                doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        System.out.println("Document snapshot data: " + documentSnapshot.getData());
                        System.out.println("Document snapshot data email: " + documentSnapshot.get("email"));
                        String username = Objects.requireNonNull(documentSnapshot.get("username").toString());
                        String email = Objects.requireNonNull(documentSnapshot.get("email").toString());
                        String name = Objects.requireNonNull(documentSnapshot.get("name").toString());
                        String phoneNo = Objects.requireNonNull(documentSnapshot.get("phoneNo").toString());

                        User user = new User(username, email, name, phoneNo);

                        //method to initiate book details intent
                        initIntent(user);
                    }
                    private void initIntent(User user) {
                        //get the book details of currently clicked item
                        Book newBook = new Book(mBooksfiltered.get(position).getFirestoreID(), mBooksfiltered.get(position).getTitle(), mBooksfiltered.get(position).getISBN(), mBooksfiltered.get(position).getAuthor(), mBooksfiltered.get(position).getStatus(), mBooksfiltered.get(position).getImageId(), user);
                        Intent viewBook = new Intent(mContext, BookDetailsFragment.class);
                        viewBook.putExtra("view book", newBook);
                        mContext.startActivity(viewBook);
                    }
                });
            }


        });

    }
    // Return the size of your dataset (invoked by the layout manager)

    @Override
    public int getItemCount() {
        return (mBooksfiltered == null) ? 0 : mBooksfiltered.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView title;
        public TextView author;
        public TextView isbn;
        public TextView status;
        CardView parentLayout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.my_book_title);
            author = itemView.findViewById(R.id.my_book_author);
            isbn = itemView.findViewById(R.id.my_book_ISBN);
            status = itemView.findViewById(R.id.my_book_status);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }


    }

    /**
     * Filters adapter by a constraint entered by the user, used to search for specific books.
     * @return search result
     */
    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults result = new FilterResults();
                ArrayList<Book> found = new ArrayList<Book>();
                if (constraint.toString().length() > 0) {
                    if (isNumeric(constraint.toString())) {
                        for (Book b : mBooks) {
                            if (b.getISBN().contains(constraint.toString())) {
                                found.add(b);
                            }
                        }
                    } else {
                        constraint = constraint.toString().toLowerCase();
                        for (Book b : mBooks) {
                            Log.d("", ("GET BOOK: " + b.getTitle() + " " + b.getAuthor() + " " + b.getOwner()));
                            if (b.getTitle().toLowerCase().contains(constraint) ||
                                    b.getAuthor().toLowerCase().contains(constraint)) {
                                found.add(b);
                            }
                        }
                    }
                    result.values = found;
                    result.count = found.size();
                } else {
                    result.values = mBooks;
                    result.count = mBooks.size();
                }
                return result;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mBooksfiltered = (ArrayList<Book>) results.values;
                notifyDataSetChanged();
            }

        };
    }

    /**
     * Checks if a string is a number value. Used for ISBN search.
     * @param str string to be checked
     * @return bool
     */
    public static boolean isNumeric(String str) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(str, pos);
        return str.length() == pos.getIndex();
    }

}



