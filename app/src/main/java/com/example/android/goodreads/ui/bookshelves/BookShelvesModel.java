package com.example.android.goodreads.ui.bookshelves;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.android.goodreads.data.BookShelf;
import com.example.android.goodreads.repository.Repository;

import java.util.ArrayList;

public class BookShelvesModel extends ViewModel {
    private Repository repository;
    private MutableLiveData<ArrayList<BookShelf>> bookShelfLiveData;
    private ArrayList<BookShelf> bookShelves;

    public BookShelvesModel(){
        repository = Repository.getInstance();
        bookShelves = new ArrayList<>();
    }

    public LiveData<ArrayList<BookShelf>> getBookShelfLiveData(){
        if(bookShelfLiveData == null){
            bookShelfLiveData = new MutableLiveData<>();
        }
        return bookShelfLiveData;
    }

    public void setBookShelfArrayList(ArrayList<BookShelf> newBookShelves) {
        bookShelves.clear();
        bookShelves.addAll(newBookShelves);
    }

    public ArrayList<BookShelf> getBookShelfArrayList(){
        return bookShelves;
    }

    public void getBookShelves(){
        repository.getBookShelves(bookShelfLiveData);
    }
}
