package com.example.android.goodreads.ui.search;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.android.goodreads.data.BookListItem;
import com.example.android.goodreads.repository.Repository;
import com.example.android.goodreads.network.BookNetworkAccess;

import java.util.ArrayList;

public class SearchBookViewModel extends ViewModel {

    private Repository bookRepository;
    private MutableLiveData<ArrayList<BookListItem>> bookListLiveData;
    private ArrayList<BookListItem> bookItemData;

    public SearchBookViewModel() {
        bookRepository = Repository.getInstance();
    }

    public LiveData<ArrayList<BookListItem>> getBookListLiveData() {
        if (bookListLiveData == null) {
            bookListLiveData = new MutableLiveData<>();
        }
        return bookListLiveData;
    }

    public ArrayList<BookListItem> getBookItemData() {
        if (bookItemData == null) {
            bookItemData = new ArrayList<>();
        }
        Log.d("azhar", "getBookItemData: " + bookItemData.size());
        return bookItemData;
    }

    public void getBookList(String bookData, int action, boolean updateSameList) {
        if (!updateSameList) BookNetworkAccess.resetStartIndex();
        else BookNetworkAccess.updateStartIndex(true);

        bookRepository.getBookList(bookListLiveData, bookData, action);
    }


    public void interruptSearch() {
        Log.d("noor", "interruptSearch: ");
        bookRepository.interruptThread();
    }
}
