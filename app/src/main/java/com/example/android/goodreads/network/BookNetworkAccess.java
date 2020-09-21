package com.example.android.goodreads.network;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.android.goodreads.data.Book;
import com.example.android.goodreads.data.BookListItem;
import com.example.android.goodreads.data.BookShelf;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


public final class BookNetworkAccess {

    private static final String URL_GET_BOOK_LIST = "https://www.googleapis.com/books/v1/volumes?q=";
    private static final String URL_GET_BOOKSHELVES_LIST = "https://www.googleapis.com/books/v1/mylibrary/bookshelves/shelfId/volumes?maxResults=25";
    private static final String ADDITIONAL_PARAMETERS = "&printType=books&filter=full&maxResults=25";
    private static final String URL_GET_BOOK = "https://www.googleapis.com/books/v1/volumes/";
    private static final String URL_GET_BOOK_SHELVES = "https://www.googleapis.com/books/v1/mylibrary/bookshelves";
    private static final String URL_POST_BOOK = "https://www.googleapis.com/books/v1/mylibrary/bookshelves/shelfId/addVolume?volumeId=";

    private static int newStartIndex, prevStartIndex;
    private static boolean inFetchListProcess = false;
    private static boolean inFetchRelatedListProcess = false;
    private static boolean inFetchBookProcess = false;
    private static boolean inFetchBookShelfProcess = false;
    private static boolean inPostRequestProcess = false;

    private BookNetworkAccess() {
    }


    public static ArrayList<BookListItem> fetchBookListData(String bookData, int action) {
        NetworkUtils.setError_msg("No internet connection");   //default error message
        if (action == HttpMethodTypes.GET_BOOK_LIST || action == HttpMethodTypes.VIEW_ALL_BOOKSHELF_LIST)
            inFetchListProcess = true;
        else if (action == HttpMethodTypes.GET_RELATED_BOOK_LIST) inFetchRelatedListProcess = true;

        String url = getUrlString(bookData, action);

        //create URL object
        URL bookUrl = NetworkUtils.createURL(url);

        //perform HTTP request to the URL and get JSON response back
        String jsonResponse = null;
        try {

            jsonResponse = NetworkUtils.getHttpResponse(bookUrl);

        } catch (IOException e) {
            e.printStackTrace();
        }
        //return list of books
        ArrayList<BookListItem> bookItems = BookJSONParser.extractBookItems(jsonResponse, bookData, action);

        if (action == HttpMethodTypes.GET_BOOK_LIST || action == HttpMethodTypes.VIEW_ALL_BOOKSHELF_LIST)
            inFetchListProcess = false;
        else if (action == HttpMethodTypes.GET_RELATED_BOOK_LIST) inFetchRelatedListProcess = false;

        return bookItems;
    }

    private static String getUrlString(String bookData, int action) {
        String url;

        switch (action) {
            case HttpMethodTypes.GET_BOOK_LIST:
                url = URL_GET_BOOK_LIST + bookData + ADDITIONAL_PARAMETERS + "&startIndex=" + newStartIndex;
                url = url.replace("#", "%23");
                break;

            case HttpMethodTypes.VIEW_ALL_BOOKSHELF_LIST:
                url = URL_GET_BOOKSHELVES_LIST.replace("shelfId", bookData);
                url = url + "&startIndex=" + newStartIndex;
                break;

            case HttpMethodTypes.GET_BOOKSHELVES_LIST:
                url = URL_GET_BOOKSHELVES_LIST.replace("shelfId", bookData);
                break;

            default:
                url = URL_GET_BOOK_LIST + bookData + ADDITIONAL_PARAMETERS;
                url = url.replace("#", "%23");
                break;
        }

        return url;
    }

    public static Book fetchBook(@NonNull String bookId) {
        NetworkUtils.setError_msg("No internet connection");   //default error message
        inFetchBookProcess = true;

        //create URL object
        URL bookUrl = NetworkUtils.createURL(URL_GET_BOOK + bookId);

        URL url = NetworkUtils.createURL("https://books.google.com.pk/books?id=" + bookId);
        //perform HTTP request for the URL and get JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = NetworkUtils.getHttpResponse(bookUrl);
            if (!TextUtils.isEmpty(jsonResponse) && AuthUtils.getToken() != null)
                NetworkUtils.updateHistory(url);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //return book
        Book book = BookJSONParser.extractBook(jsonResponse);

        inFetchBookProcess = false;
        return book;
    }

    public static ArrayList<BookShelf> fetchBookShelves() {
        NetworkUtils.setError_msg("No internet connection");   //default error message
        inFetchBookShelfProcess = true;

        //creating url
        URL url = NetworkUtils.createURL(URL_GET_BOOK_SHELVES);

        //perform HTTP request for the URL and get JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = NetworkUtils.getHttpResponse(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TESTING RESPONSE
        Log.d("noor", "fetchBookShelves: " + jsonResponse + "\n\n");
        //return list of bookshelves
        ArrayList<BookShelf> bookShelves = BookJSONParser.extractBookShelf(jsonResponse);

        inFetchBookShelfProcess = false;
        return bookShelves;

    }


    public static String postBookRequest(int action, String data) {

        NetworkUtils.setError_msg("No internet connection.");
        inPostRequestProcess = true;

        //creating URL
        String shelfId = getShelfId(action);
        String url = URL_POST_BOOK.replace("shelfId", shelfId) + data;
        URL postURL = NetworkUtils.createURL(url);

        //getting http post request response
        String response = NetworkUtils.postRequest(postURL, Integer.parseInt(shelfId));
        inPostRequestProcess = false;
        return response;
    }


    private static String getShelfId(int action) {
        String shelfId = null;
        switch (action) {
            case HttpMethodTypes.POST_FAVOURITE:
                shelfId = "0";
                break;

            case HttpMethodTypes.POST_TO_READ:
                shelfId = "2";
                break;

            case HttpMethodTypes.POST_READING_NOW:
                shelfId = "3";
                break;

            case HttpMethodTypes.POST_HAVE_READ:
                shelfId = "4";
                break;
        }
        return shelfId;
    }

    public static boolean isInPostRequestProcess() {
        return inPostRequestProcess;
    }

    public static boolean isInFetchRelatedListProcess() {
        return inFetchRelatedListProcess;
    }

    public static boolean isInFetchListProcess() {
        return inFetchListProcess;
    }

    public static boolean isInFetchBookProcess() {
        return inFetchBookProcess;
    }

    public static boolean isInFetchBookShelfProcess() {
        return inFetchBookShelfProcess;
    }

    public static void resetStartIndex() {
        prevStartIndex = newStartIndex = 0;
    }

    public static void updateStartIndex(boolean isIncreased) {
        if (isIncreased) prevStartIndex = newStartIndex;
        newStartIndex = isIncreased ? prevStartIndex + 24 : prevStartIndex;
    }

}
