package com.example.android.goodreads.network;

import com.example.android.goodreads.data.BookListItem;
import com.example.android.goodreads.data.BookShelf;
import com.example.android.goodreads.network.BookNetworkAccess;
import com.example.android.goodreads.network.HttpMethodTypes;

import java.util.ArrayList;
import java.util.concurrent.Callable;

class BookShelfTask implements Callable<BookShelf> {
    private BookShelf bookShelf;

    public BookShelfTask(int id, String title) {
        bookShelf = new BookShelf(String.valueOf(id), title);
    }

    @Override
    public BookShelf call() {
        ArrayList<BookListItem> shelfBooks = BookNetworkAccess.fetchBookListData(bookShelf.getId(), HttpMethodTypes.GET_BOOKSHELVES_LIST);
        bookShelf.setBookItems(shelfBooks);
        return bookShelf;
    }
}
