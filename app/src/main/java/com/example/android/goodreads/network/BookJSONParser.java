package com.example.android.goodreads.network;

import android.text.TextUtils;
import android.util.Log;

import com.example.android.goodreads.data.Book;
import com.example.android.goodreads.data.BookListItem;
import com.example.android.goodreads.data.BookShelf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*
    This class is responsible for parsing the JSON responses.
 */

final class BookJSONParser {

    private BookJSONParser() {
    }

    public static ArrayList<BookListItem> extractBookItems(String httpResponse, String bookName, int action) {

        if (TextUtils.isEmpty(httpResponse)) {
            return null;
        }

        ArrayList<BookListItem> bookItems = null;

        try {
            JSONObject root = new JSONObject(httpResponse);
            JSONArray itemsArray = root.optJSONArray("items");

            if (itemsArray != null) {
                bookItems = new ArrayList<>();
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject bookItem = itemsArray.optJSONObject(i);
                    if (isBookWanted(bookItem)) {
                        BookListItem book = getBookItem(bookItem);
                        bookItems.add(book);
                    }

                }

            } else {
                if (action == HttpMethodTypes.GET_BOOK_LIST)
                    NetworkUtils.setError_msg("No results found for " + "\" " + bookName + "\"");
                else
                    return new ArrayList<>();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bookItems;
    }

    public static Book extractBook(String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        Book book = null;
        try {
            JSONObject root = new JSONObject(jsonResponse);
            if (!root.has("error")) {
                book = getBookDetails(root);
            } else {
                NetworkUtils.setError_msg("Data for this book was not found.");
            }

        } catch (JSONException e) {
            Log.d("noor", "extractBook: " + e.getMessage());
        }
        return book;
    }


    public static ArrayList<BookShelf> extractBookShelf(String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        ArrayList<BookShelf> bookShelves = null;

        try {
            JSONObject root = new JSONObject(jsonResponse);
            JSONArray bookShelfArray = root.optJSONArray("items");


            if (bookShelfArray != null) {
                bookShelves = new ArrayList<>();
                ExecutorService executorService = Executors.newCachedThreadPool();
                ArrayList<Future<BookShelf>> futures = new ArrayList<>();
                BookShelf.resetNullCount();

                /*
                    fetching all bookshelves asynchronously by executing threads in a cached thread pool
                */
                for (int i = 0; i < bookShelfArray.length(); i++) {
                    JSONObject bookshelf = bookShelfArray.getJSONObject(i);

                    if (isBookShelfWanted(bookshelf.getString("title"))) {
                        //extracting bookshelf
                        int id = bookshelf.getInt("id");
                        String title = bookshelf.getString("title");

                        Future<BookShelf> bookShelfFuture = executorService.submit(new BookShelfTask(id, title));
                        futures.add(bookShelfFuture);
                    }
                }

                /*
                    getting the result of each fetched bookshelf from the Future object contained in the ArrayList
                    NOTE: calling Future.get(int) will block this "parent" thread until the result is returned
                */
                for (int i = 0; i < futures.size(); i++) {
                    try {
                        BookShelf bookShelf = futures.get(i).get();
                        bookShelves.add(bookShelf);
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return bookShelves;
    }


    private static boolean isBookShelfWanted(String title) {
        return !(title.equals("My Google eBooks") ||
                title.equals("Purchased") ||
                title.equals("Reviewed") ||
                title.equals("Browsing history"));
    }

    private static boolean isBookWanted(JSONObject bookItem) {
        JSONObject accessInfo = bookItem.optJSONObject("accessInfo");
        String viewability = accessInfo.optString("viewability", "");

        return viewability.equals("ALL_PAGES");
    }


    private static BookListItem getBookItem(JSONObject bookItem) {
        String bookId = bookItem.optString("id");
        JSONObject bookInfo = bookItem.optJSONObject("volumeInfo");

        String bookTitle = bookInfo.optString("title");
        String bookAuthor = getBookAuthor(bookInfo);
        String bookImage = getBookImage(bookInfo);

        return new BookListItem(bookId, bookTitle, bookAuthor, bookImage);
    }

    private static Book getBookDetails(JSONObject root) throws JSONException {
        BookListItem bookItem = getBookItem(root);
        Book book = new Book(bookItem);
        JSONObject bookInfo = root.optJSONObject("volumeInfo");
        JSONObject accessInfo = root.optJSONObject("accessInfo");

        String publisher = bookInfo.optString("publisher", "Not specified");
        book.setPublisher(publisher);

        String publishedDate = bookInfo.optString("publishedDate", "Unknown");
        book.setPublishedDate(publishedDate);

        String description = bookInfo.optString("description", "Description for this book is not provided.");
        book.setDescription(description);

        int pageCount = bookInfo.optInt("pageCount");
        book.setPageCount(pageCount);

        String category = getBookCategory(bookInfo);
        book.setCategory(category);

        double rating = bookInfo.optDouble("averageRating", 0.0);
        book.setRating(rating);

        int no_ratings = bookInfo.optInt("ratingsCount", 0);
        book.setNo_ratings(no_ratings);

        String lang = bookInfo.optString("language", "");
        book.setLang(lang);

        String downloadLink = getDownloadLink(accessInfo);
        book.setDownloadLink(downloadLink);

        String webReaderLink = accessInfo.optString("webReaderLink", "");
        book.setWebReaderLink(webReaderLink);

        return book;
    }


    private static String getDownloadLink(JSONObject accessInfo) {
        String epubLink, pdfLink, downloadLink;
        epubLink = accessInfo.optJSONObject("epub").optString("downloadLink", "not available");
        pdfLink = accessInfo.optJSONObject("pdf").optString("downloadLink", "not available");

        downloadLink = !epubLink.equals("not available") ? epubLink : pdfLink;  //selecting any one which is not null

        return downloadLink;

    }

    private static String getBookCategory(JSONObject bookInfo) throws JSONException {
        JSONArray categories = bookInfo.optJSONArray("categories");
        return categories == null ? "Not specified" : categories.getString(0);
    }

    private static String getBookImage(JSONObject bookInfo) {
        JSONObject imgObject = bookInfo.optJSONObject("imageLinks");
        return (imgObject != null && imgObject.has("thumbnail"))
                ? imgObject.optString("thumbnail") : "https://gangarams.com/image/cache/placeholder-250x250.png";
    }

    private static String getBookAuthor(JSONObject bookInfo) {
        JSONArray bookAuthors = bookInfo.optJSONArray("authors");
        return bookAuthors == null ? "" : bookAuthors.optString(0);
    }

}
