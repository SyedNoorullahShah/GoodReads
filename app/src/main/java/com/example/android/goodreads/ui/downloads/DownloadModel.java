package com.example.android.goodreads.ui.downloads;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.android.goodreads.database.BookDownloadEntity;
import com.example.android.goodreads.repository.Repository;

import java.util.List;

public class DownloadModel extends AndroidViewModel {
    private Repository repository;
    private LiveData<List<BookDownloadEntity>> downloads;

    public DownloadModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance(application);
        downloads = repository.getDownloads();
    }



    public void deleteDownload(BookDownloadEntity bookDownloadEntity){
        repository.deleteDownload(bookDownloadEntity);
    }

    public LiveData<List<BookDownloadEntity>> getDownloads() {
        return downloads;
    }
}
