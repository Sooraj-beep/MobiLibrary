package com.example.mobilibrary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.util.NumberUtils;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Locale;

public class customBookAdapter extends ArrayAdapter<Book> {
    private ArrayList<Book> books;
    private ArrayList<Book> preSearch;
    private Context context;

    /**
     * Used as a adapter for an array of objects
     *
     * @param context
     * @param books
     */
    public customBookAdapter(@NonNull Context context, ArrayList<Book> books) {
        super(context, 0, books);
        this.books = books;
        this.preSearch = books;
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

        Book book = books.get(position);

        TextView bookTitle = view.findViewById(R.id.my_book_title);
        TextView bookAuthor = view.findViewById(R.id.my_book_author);
        TextView bookISBN = view.findViewById(R.id.my_book_ISBN);

        bookTitle.setText(book.getTitle());
        bookAuthor.setText(book.getAuthor());
        bookISBN.setText(book.getISBN());

        return view;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults result = new FilterResults();
                ArrayList<Book> found = new ArrayList<Book>();
                if (preSearch == null) {
                    preSearch = books;
                }
                if (isNumeric(constraint.toString())) {
                    if (constraint != null && constraint.toString().length() > 0) {
                        for (Book b : preSearch) {
                            if (b.getISBN().contains(constraint.toString())) {
                                found.add(b);
                            }
                        }
                        result.values = found;
                    } else {
                        result.values = preSearch;
                    }
                } else {
                    constraint = constraint.toString().toLowerCase();
                    if (constraint != null && constraint.toString().length() > 0) {
                        for (Book b : preSearch) {
                            if (b.getTitle().toLowerCase().contains(constraint) ||
                                    b.getAuthor().toLowerCase().contains(constraint)) {
                                found.add(b);
                            }
                        }
                        result.values = found;
                    } else {
                        result.values = preSearch;
                    }
                }
                return result;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                ArrayList<Book> filtered = (ArrayList<Book>) results.values;
                notifyDataSetChanged();
                clear();
                for (int i = 0; i < filtered.size(); i++)
                    add((Book) filtered.get(i));
                notifyDataSetInvalidated();
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
