package com.example.android.goodreads.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.android.goodreads.ui.bookdetail.SingleLiveEvent;
import com.example.android.goodreads.database.BookDownloadEntity;
import com.example.android.goodreads.database.DownloadDAO;
import com.example.android.goodreads.database.DownloadsDatabase;
import com.example.android.goodreads.data.Book;
import com.example.android.goodreads.data.BookListItem;
import com.example.android.goodreads.data.BookShelf;
import com.example.android.goodreads.network.BookNetworkAccess;
import com.example.android.goodreads.network.HttpMethodTypes;

import java.util.ArrayList;
import java.util.List;

/*
    Singleton class for communicating Network and the Database
*/

public class Repository {
    private MutableLiveData<ArrayList<BookListItem>> bookList;
    private MutableLiveData<Book> bookMutableLiveData;
    private MutableLiveData<ArrayList<BookShelf>> bookShelvesLiveData;
    private static Repository repository = new Repository();
    private NetworkThread networkThread;
    private MutableLiveData<ArrayList<BookListItem>> relatedBookList;
    private SingleLiveEvent<String> bookPostResultLiveData;
    private static DownloadDAO downloadDAO;

    private Repository() {
    }

    public static Repository getInstance() {
        if (repository == null) repository = new Repository();

        return repository;
    }

    public static Repository getInstance(Application application) {
        if (repository == null) repository = new Repository();

        downloadDAO = DownloadsDatabase.getInstance(application).downloadDAO();
        return repository;
    }


    /*
        NETWORK RELATED OPERATIONS
    */

    public void getBookList(MutableLiveData<ArrayList<BookListItem>> bookListLiveData, String book, int action) {
        if (action == HttpMethodTypes.GET_RELATED_BOOK_LIST) relatedBookList = bookListLiveData;
        else bookList = bookListLiveData;

        startNetworkThread(action, book);
    }

    public void getBook(MutableLiveData<Book> book, String bookId, boolean isBookDownloaded) {        //getting book from the api
        bookMutableLiveData = book;

        if (isBookDownloaded) getDownloadedBook(bookId);
        else startNetworkThread(HttpMethodTypes.GET_BOOK, bookId);
    }

    public void getBookShelves(MutableLiveData<ArrayList<BookShelf>> bookShelfLiveData) {
        bookShelvesLiveData = bookShelfLiveData;
        startNetworkThread(HttpMethodTypes.GET_BOOKSHELVES, null);
    }

    public void addBookToBookshelf(SingleLiveEvent<String> bookPostResultLiveData, int action, String bookId) {
        this.bookPostResultLiveData = bookPostResultLiveData;
        startNetworkThread(action, bookId);
    }

    private void startNetworkThread(int action, String data) {      //fetch book data from API on user request
        networkThread = new NetworkThread(action, data);
        networkThread.start();
    }

    public void interruptThread() {
        networkThread.interruptOperation();
    }


    /*
         DATABASE RELATED OPERATIONS
    */

    public void addDownload(BookDownloadEntity bookDownloadEntity) {
        startDatabaseThread(DownloadDAO.ACTION_INSERT, bookDownloadEntity);
    }

    public void deleteDownload(BookDownloadEntity bookDownloadEntity) {
        startDatabaseThread(DownloadDAO.ACTION_DELETE, bookDownloadEntity);
    }

    public LiveData<List<BookDownloadEntity>> getDownloads() {
        return downloadDAO.getAllDownloads();
    }

    public void getDownloadedBook(String bookId) {
        new DatabaseThread(DownloadDAO.ACTION_GET_BOOK, bookId).start();
    }

    private void startDatabaseThread(int action, BookDownloadEntity bookDownloadEntity) {
        DatabaseThread databaseThread = new DatabaseThread(action, bookDownloadEntity);
        databaseThread.start();
    }


    private class NetworkThread extends Thread {
        private int action;
        private String data;
        private boolean isInterrupted;

        public NetworkThread(int action, String data) {
            this.action = action;
            this.data = data;
            isInterrupted = false;
        }

        public void interruptOperation() {
            isInterrupted = true;
        }

        @Override
        public void run() {
            switch (action) {
                case HttpMethodTypes.GET_RELATED_BOOK_LIST:
                    ArrayList<BookListItem> bookItems = BookNetworkAccess.fetchBookListData(data, action);
                    relatedBookList.postValue(bookItems);
                    break;

                case HttpMethodTypes.VIEW_ALL_BOOKSHELF_LIST:
                case HttpMethodTypes.GET_BOOK_LIST:
                    bookItems = BookNetworkAccess.fetchBookListData(data, action);
                    if (!isInterrupted) bookList.postValue(bookItems);
                    break;

                case HttpMethodTypes.GET_BOOK:
                    Book book = BookNetworkAccess.fetchBook(data);
                    if (!isInterrupted) bookMutableLiveData.postValue(book);
                    break;

                case HttpMethodTypes.GET_BOOKSHELVES:
                    ArrayList<BookShelf> bookShelves = BookNetworkAccess.fetchBookShelves();
                    bookShelvesLiveData.postValue(bookShelves);
                    break;

                case HttpMethodTypes.POST_HAVE_READ:
                case HttpMethodTypes.POST_READING_NOW:
                case HttpMethodTypes.POST_TO_READ:
                case HttpMethodTypes.POST_FAVOURITE:
                    String result = BookNetworkAccess.postBookRequest(action, data);
                    if (!isInterrupted) {
                        Log.d("meri app", "showing data... ");
                        bookPostResultLiveData.postValue(result);
                    } else
                        Log.d("meri app", "operation interrupted !!!");
                    break;
            }
        }
    }

    private class DatabaseThread extends Thread {
        private int action;
        private BookDownloadEntity bookDownloadEntity;
        private String bookId;

        public DatabaseThread(int action, BookDownloadEntity bookDownloadEntity) {
            this.action = action;
            this.bookDownloadEntity = bookDownloadEntity;
        }

        public DatabaseThread(int action, String bookId) {
            this.action = action;
            this.bookId = bookId;
        }

        @Override
        public void run() {
            switch (action) {
                case DownloadDAO.ACTION_INSERT:
                    downloadDAO.insert(bookDownloadEntity);
                    break;
                case DownloadDAO.ACTION_DELETE:
                    downloadDAO.delete(bookDownloadEntity);
                    break;
                case DownloadDAO.ACTION_GET_BOOK:
                    Book book = downloadDAO.getBook(bookId);
                    bookMutableLiveData.postValue(book);
                    break;
            }
        }
    }
}
