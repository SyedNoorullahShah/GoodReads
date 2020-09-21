package com.example.android.goodreads.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.android.goodreads.data.Book;

import java.util.List;

@Dao
public interface DownloadDAO {

    int ACTION_INSERT = 0;
    int ACTION_DELETE = 1;
    int ACTION_GET_BOOK = 2;

    @Insert
    void insert(BookDownloadEntity download);

    @Delete
    void delete(BookDownloadEntity download);

    @Query("SELECT * FROM DOWNLOAD_TABLE")
    LiveData<List<BookDownloadEntity>> getAllDownloads();

    @Query("SELECT * FROM download_table WHERE  bookId=:bookId")
    Book getBook(String bookId);
}
