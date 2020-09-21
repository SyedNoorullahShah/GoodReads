package com.example.android.goodreads;

import android.app.Application;

import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.squareup.leakcanary.LeakCanary;

public class BookApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setConnectTimeout(15_000)
                .build();
        PRDownloader.initialize(getApplicationContext(), config);

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
