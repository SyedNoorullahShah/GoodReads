package com.example.android.goodreads.utils;

import android.content.Intent;

import androidx.core.content.ContextCompat;

import com.example.android.goodreads.data.Book;
import com.example.android.goodreads.database.BookDownloadEntity;
import com.example.android.goodreads.repository.Repository;
import com.example.android.goodreads.ui.bookdetail.BookDetailActivity;
import com.example.android.goodreads.service.DownloadService;

import java.util.ArrayList;

public class DownloadUtils {
    private static ArrayList<Book> downloadQueue = new ArrayList<>();
    private static BookDetailActivity currentContext;


    public static void attachContext(BookDetailActivity bookDetailActivity) {
        currentContext = bookDetailActivity;

    }

    public static void detachContext() {
        currentContext = null;
    }

    public static void addDownload(Book book) {

        if (isQueueEmpty() && !DownloadService.isRunning()) {
            startDownloadService();
        }
        downloadQueue.add(0, book);
    }

    public static Book getDownload() {
        return downloadQueue.get(downloadQueue.size() - 1);
    }

    public static void onDownloadComplete(String currBookId, boolean isDownloadSuccess) {
        Book book = downloadQueue.remove(downloadQueue.size() - 1);
        if (isDownloadSuccess) Repository.getInstance().addDownload(new BookDownloadEntity(book));
        if (currentContext != null && shouldUpdate(currBookId)) {
            currentContext.updateDownloadButtonState();
        }

    }

    private static boolean shouldUpdate(String currBookId) {
        return currentContext.getBookId().equals(currBookId);
    }

    public static boolean isDownloadPending(String bookId) {
        return (!isQueueEmpty() && (queueContainsDownload(bookId)));
    }

    private static boolean queueContainsDownload(String bookId) {
        for (Book book : downloadQueue) {
            if (book.getBookId().equals(bookId)) return true;
        }
        return false;
    }

    private static void startDownloadService() {
        ContextCompat.startForegroundService(currentContext, new Intent(currentContext, DownloadService.class));
    }

    public static boolean isQueueEmpty() {
        return downloadQueue.isEmpty();
    }
}
