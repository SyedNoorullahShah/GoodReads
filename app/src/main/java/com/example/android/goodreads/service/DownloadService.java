package com.example.android.goodreads.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.example.android.goodreads.data.Book;
import com.example.android.goodreads.utils.DownloadUtils;
import com.example.android.goodreads.utils.NotificationUtils;

public class DownloadService extends Service {

    public static final String DOWNLOAD_TAG = "download_tag";
    private static final int FOREGROUND_ID = 1;
    private static boolean isRunning = false;
    private Book currBook;
    private NotificationCompat.Builder builder;

    public static boolean isRunning() {
        return isRunning;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        builder = NotificationUtils.getNotificationBuilder(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(FOREGROUND_ID, builder.build());
        startDownload();
        return START_NOT_STICKY;
    }

    private void startDownload() {
        Log.d("asdfg", "startDownload: ");
        currBook = DownloadUtils.getDownload();
        String fileName = currBook.getBookId() + ".pdf";

        PRDownloader.download(currBook.getDownloadLink(), getFilesDir().getAbsolutePath(), fileName)
                .setTag(DOWNLOAD_TAG)
                .build()

                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {
                        NotificationUtils.createInitialNotification(DownloadService.this, builder, currBook.getTitle(), currBook.getImage());
                    }
                })

                .setOnProgressListener(new OnProgressListener() {
                    private int percentDone = 0;

                    @Override
                    public void onProgress(Progress progress) {
                        int latestPercentDone = (int) (progress.currentBytes * 100.0 / progress.totalBytes + 0.5);
                        if (percentDone != latestPercentDone) {
                            percentDone = latestPercentDone;
                            NotificationUtils.setProgressNotification(DownloadService.this, builder, progress.currentBytes, progress.totalBytes, percentDone);
                        }
                    }
                })

                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        NotificationUtils.cancelDownload(DownloadService.this, builder);
                        onDownloadFinished(false);
                    }
                })

                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        Log.d("asdfg", "onDownloadComplete: ");
                        NotificationUtils.setFinalNotification(DownloadService.this, builder, currBook.getBookId(), currBook.getTitle(), true);
                        onDownloadFinished(true);
                    }

                    @Override
                    public void onError(Error error) {
                        Log.d("asdfg", "onError: ");
                        NotificationUtils.setFinalNotification(DownloadService.this, builder, currBook.getBookId(), currBook.getTitle(), false);
                        onDownloadFinished(false);

                    }
                });
    }

    private void onDownloadFinished(boolean isDownloadSuccess) {
        Log.d("asdfg", "onDownloadFinished: ");
        DownloadUtils.onDownloadComplete(currBook.getBookId(), isDownloadSuccess);

        if (DownloadUtils.isQueueEmpty()) stopSelf();
        else startDownload();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("asdfg", "onDestroy: SERVICE");
        isRunning = false;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("asdfg", "onTaskRemoved: ");
        DownloadUtils.detachContext();
        super.onTaskRemoved(rootIntent);
    }
}
