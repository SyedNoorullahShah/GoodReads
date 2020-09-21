package com.example.android.goodreads.data;

import android.util.Log;

import com.example.android.goodreads.data.BookListItem;

import java.util.ArrayList;

public class BookShelf {
    private String id;
    private String title;
    private ArrayList<BookListItem> bookItems;
    private static int nullCount;      //no. of child book items that are null;

    public BookShelf(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public static void resetNullCount() {
        nullCount = 0;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }


    public ArrayList<BookListItem> getBookItems() {
        return bookItems;
    }

    public void setBookItems(ArrayList<BookListItem> bookItems) {
        if (bookItems == null) nullCount++;
        Log.d("noor", "NULL BOOKSHELVES -----------> "+nullCount);
        this.bookItems = bookItems;
    }

    public static int getNullCount() {
        return nullCount;
    }
}
