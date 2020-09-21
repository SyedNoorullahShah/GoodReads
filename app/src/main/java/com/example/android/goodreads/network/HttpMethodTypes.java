package com.example.android.goodreads.network;

public interface HttpMethodTypes {
    // GET methods
    int GET_BOOK_LIST = 0;
    int GET_RELATED_BOOK_LIST = 1;
    int GET_BOOK = 2;
    int GET_BOOKSHELVES = 3;
    int GET_BOOKSHELVES_LIST = 4;
    int VIEW_ALL_BOOKSHELF_LIST = 5;

    // POST methods
    int POST_FAVOURITE = 6;
    int POST_TO_READ = 7;
    int POST_READING_NOW = 8;
    int POST_HAVE_READ = 9;
}
