package com.example.android.goodreads.ui.bookdetail;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.android.goodreads.data.Book;
import com.example.android.goodreads.data.BookListItem;
import com.example.android.goodreads.repository.Repository;
import com.example.android.goodreads.network.HttpMethodTypes;

import java.util.ArrayList;

public class BookDetailModel extends AndroidViewModel {
    private Repository repository;
    private MutableLiveData<Book> bookLiveData;
    private MutableLiveData<ArrayList<BookListItem>> relatedBooksLiveData;
    private boolean relatedListHasBeenLoaded = false;
    private SingleLiveEvent<String> postResultLiveData;


    public BookDetailModel(Application application) {
        super(application);
        repository = Repository.getInstance(application);
    }

    public LiveData<Book> getBookLiveData() {
        if (bookLiveData == null) {
            bookLiveData = new MutableLiveData<>();
        }
        return bookLiveData;
    }

    public void getRelatedBooks(String book) {
        relatedListHasBeenLoaded = true;
        repository.getBookList(relatedBooksLiveData, book, HttpMethodTypes.GET_RELATED_BOOK_LIST);
    }

    public void getBook(String bookId, boolean isBookDownloaded) {
        repository.getBook(bookLiveData, bookId, isBookDownloaded);
    }

    public LiveData<ArrayList<BookListItem>> getRelatedBooksLiveData() {
        if (relatedBooksLiveData == null) {
            relatedBooksLiveData = new MutableLiveData<>();
        }
        return relatedBooksLiveData;
    }

    public LiveData<String> getPostResultLiveData() {
        if (postResultLiveData == null) {
            postResultLiveData = new SingleLiveEvent<>();
        }
        return postResultLiveData;
    }

    public boolean relatedListHasBeenLoaded() {
        return relatedListHasBeenLoaded;
    }

    public void interruptBookSearch() {
        repository.interruptThread();
    }

    public void addBookToBookShelf(int action, String bookId) {
        repository.addBookToBookshelf(postResultLiveData, action, bookId);
    }

}
