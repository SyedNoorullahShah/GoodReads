package com.example.android.goodreads.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.downloader.PRDownloader;
import com.example.android.goodreads.service.DownloadService;

/*
    Broadcast Receiver for cancelling/stopping the background downloading which is executed in DownloadService
*/

public class ActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PRDownloader.cancel(DownloadService.DOWNLOAD_TAG);
    }
}
