package com.example.android.goodreads.listeners;

import com.example.android.goodreads.data.BookListItem;

import java.util.ArrayList;

public interface UpdateListListener {
    void updateList(ArrayList<BookListItem> bookItems);
}
