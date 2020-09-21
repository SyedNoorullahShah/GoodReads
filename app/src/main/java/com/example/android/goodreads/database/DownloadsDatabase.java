package com.example.android.goodreads.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = BookDownloadEntity.class, version = 1)
public abstract class DownloadsDatabase extends RoomDatabase {
    private static DownloadsDatabase instance;
    private static final String DB_FILE_NAME = "book_downloads";

    public abstract DownloadDAO downloadDAO();

    public synchronized static DownloadsDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    DownloadsDatabase.class,
                    DB_FILE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
