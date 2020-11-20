package com.example.mobilibrary;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.util.NumberUtils;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Locale;

public class customBookAdapter extends ArrayAdapter<Book> implements Filterable {
    private ArrayList<Book> books;
    private ArrayList<Book> filtered;
    private Context context;
    final private static String TAG = "customBookAdapter";

    /**
     * Used as a adapter for an array of objects
     *
     * @param context
     * @param books
     */
    public customBookAdapter(@NonNull Context context, ArrayList<Book> books) {
        super(context, 0, books);
        this.books = books;
        this.filtered = books;
        this.context = context;
    }

    /**
     * Create a book item in the listView with the book information (title, author and isbn)
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.content, parent, false);
        }

        TextView bookTitle = view.findViewById(R.id.my_book_title);
        TextView bookAuthor = view.findViewById(R.id.my_book_author);
        TextView bookISBN = view.findViewById(R.id.my_book_ISBN);

        Log.d(TAG, (position + " " + filtered.get(position).getTitle()));
        Book book = filtered.get(position);

        bookTitle.setText(book.getTitle());
        bookAuthor.setText(book.getAuthor());
        bookISBN.setText(book.getISBN());

        return view;
    }

    @Override
    public int getCount() {
        return filtered.size();
    }

    @Nullable
    @Override
    public Book getItem(int position) {
        return filtered.get(position);
    }

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
                        for (Book b : books) {
                            if (b.getISBN().contains(constraint.toString())) {
                                found.add(b);
                            }
                        }
                    } else {
                        constraint = constraint.toString().toLowerCase();
                        for (Book b : books) {
                            Log.d(TAG, ("GET BOOK: " + b.getTitle() + " " + b.getAuthor() + " " + b.getOwner()));
                            if (b.getTitle().toLowerCase().contains(constraint) ||
                                    b.getAuthor().toLowerCase().contains(constraint)) {
                                found.add(b);
                            }
                        }
                    }
                    result.values = found;
                    result.count = found.size();
                } else {
                    result.values = books;
                    result.count = books.size();
                }
                return result;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                Log.d(TAG, ("count: " + String.valueOf(results.count)));
                if (results.count == 0) {
                    notifyDataSetInvalidated();
                } else {
                    filtered = (ArrayList<Book>) results.values;
                    notifyDataSetChanged();
                }
            }

        };
    }

    public static boolean isNumeric(String str) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(str, pos);
        return str.length() == pos.getIndex();
    }

}
