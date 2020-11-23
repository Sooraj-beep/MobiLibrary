package com.example.mobilibrary;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.Objects;

public class customBookAdapter extends RecyclerView.Adapter<customBookAdapter.MyViewHolder>{
    private ArrayList<Book> mBooks;
    private Context mContext;

    public customBookAdapter(Context context, ArrayList<Book> books){
        this.mContext = context;
        this.mBooks = books;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.books_rows, parent, false);

        MyViewHolder vh = new MyViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // get element from your dataset at this position
        // replace the contents of the view with that element
        holder.title.setText(mBooks.get(position).getTitle());
        holder.author.setText(mBooks.get(position).getAuthor());
        holder.isbn.setText(mBooks.get(position).getISBN());
        holder.status.setText(mBooks.get(position).getStatus());

        //click listener
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get image of book clicked
                byte[] bookImage = null;
                if (mBooks.get(position).getImageId() != null){
                    String bookImageString = mBooks.get(position).getImageId();
                    bookImage = Base64.decode(bookImageString, 0);
                }

                //Get the User object from currently clicked book by going into firestore
                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference doc = db.collection("Users").document(String.valueOf(mBooks.get(position).getOwner()));
                if (bookImage != null){
                    Base64.encodeToString(bookImage, Base64.DEFAULT);
                }
                doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        System.out.println("Document snapshot data: " + documentSnapshot.getData());
                        System.out.println("Document snapshot data email: " + documentSnapshot.get("email"));
                        String username = documentSnapshot.get("username").toString();
                        String email = documentSnapshot.get("email").toString();
                        String name = documentSnapshot.get("name").toString();
                        String phoneNo = documentSnapshot.get("phoneNo").toString();

                        User user = new User(username, email, name, phoneNo);

                        //method to initiate book details intent
                        initIntent(user);
                    }
                    private void initIntent(User user) {
                        //get the book details of currently clicked item
                        Book newBook = new Book(mBooks.get(position).getFirestoreID(), mBooks.get(position).getTitle(), mBooks.get(position).getISBN(), mBooks.get(position).getAuthor(), mBooks.get(position).getStatus(), mBooks.get(position).getImageId(), user);
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
        return (mBooks == null) ? 0 : mBooks.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView title;
        public TextView author;
        public TextView isbn;
        public TextView status;
        CardView parentLayout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.book_title);
            author = (TextView) itemView.findViewById(R.id.book_author);
            isbn = (TextView) itemView.findViewById(R.id.book_isbn);
            status = (TextView) itemView.findViewById(R.id.book_status);
            parentLayout = (CardView) itemView.findViewById(R.id.parent_layout);
        }


    }
}


//package com.example.mobilibrary;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import java.util.ArrayList;
//
//public class customBookAdapter extends ArrayAdapter<Book> {
//    private ArrayList<Book> books;
//    private Context context;
//
//    /**
//     * Used as a adapter for an array of objects
//     * @param context
//     * @param books
//     */
//    public customBookAdapter(@NonNull Context context, ArrayList<Book> books) {
//        super(context,0,books);
//        this.books = books;
//        this.context = context;
//    }
//
//    /**
//     * Create a book item in the listView with the book information (title, author and isbn)
//     * @param position
//     * @param convertView
//     * @param parent
//     * @return
//     */
//    @NonNull
//    @Override
//    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//
//        View view = convertView;
//
//        if(view == null){
//            view = LayoutInflater.from(context).inflate(R.layout.content, parent,false);
//        }
//
//        Book book = books.get(position);
//
//        TextView bookTitle = view.findViewById(R.id.my_book_title);
//        TextView bookAuthor = view.findViewById(R.id.my_book_author);
//        TextView bookISBN= view.findViewById(R.id.my_book_ISBN);
//
//        bookTitle.setText(book.getTitle());
//        bookAuthor.setText(book.getAuthor());
//        bookISBN.setText(book.getISBN());
//
//        return view;
//    }
//}


