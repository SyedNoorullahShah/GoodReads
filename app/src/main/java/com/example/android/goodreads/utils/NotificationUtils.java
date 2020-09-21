package com.example.android.goodreads.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.android.goodreads.receiver.ActionReceiver;
import com.example.android.goodreads.ui.bookdetail.BookDetailActivity;
import com.example.android.justjava.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.DecimalFormat;

public class NotificationUtils {

    private static final String CHANNEL_ID = "1";
    private static boolean isChannelCreated = false;
    private static int notification_id = 1;

    public static NotificationCompat.Builder getNotificationBuilder(Context ctx) {
        setNotificationChannel(ctx);
        return new NotificationCompat.Builder(ctx, CHANNEL_ID);
    }

    private static void setNotificationChannel(Context ctx) {
        if (!isChannelCreated && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel downloadChannel = new NotificationChannel(CHANNEL_ID, "Downloads", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(downloadChannel);
            isChannelCreated = true;
        }
    }


    public static void createInitialNotification(Context ctx, NotificationCompat.Builder builder, String title, String image) {
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(ctx);
        notification_id++;
        Log.d("asdfg", "createInitialNotification: ID ------> " + notification_id);
        builder.setContentTitle(title)
                .setProgress(0, 0, false)
                .setContentText("Starting download, please wait...")
                .setOngoing(true)
                .addAction(R.drawable.close, "Cancel Download", getActionIntent(ctx))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOnlyAlertOnce(true);

        setLargeIcon(builder, image);

        managerCompat.notify(notification_id, builder.build());
    }


    public static void setProgressNotification(Context ctx, NotificationCompat.Builder builder, long currentBytes, long totalBytes, int progress) {
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(ctx);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        double currMbs = ((double) currentBytes / 1000000);
        double totMbs = ((double) totalBytes / 1000000);

        String contentText = (decimalFormat.format(currMbs) + " / " + decimalFormat.format(totMbs) + " MB");

        builder.setContentText(contentText);
        builder.setProgress(100, progress, false);

        managerCompat.notify(notification_id, builder.build());
    }


    public static void setFinalNotification(Context ctx, NotificationCompat.Builder builder, String bookId, String bookName, boolean isDownloadSuccess) {
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(ctx);
        String contentText = isDownloadSuccess ? "Download completed." : "Couldn't download book.";

        cancelDownload(ctx, builder);
        builder.setProgress(0, 0, false)
                .setContentTitle(bookName)
                .setContentIntent(getContentIntent(ctx, bookName, bookId))
                .setAutoCancel(true)
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(false);
        //if (!isDownloadSuccess) builder.setLargeIcon(null);
        Log.d("asdfg", "setFinalNotification: ----------> " + notification_id);
        managerCompat.notify(notification_id, builder.build());
    }


    public static void cancelDownload(Context ctx, NotificationCompat.Builder builder) {
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(ctx);
        builder.mActions.clear();
        managerCompat.cancel(notification_id);
    }

    private static void setLargeIcon(final NotificationCompat.Builder builder, String image) {

        Picasso.get().load(image)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        builder.setLargeIcon(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }

    private static PendingIntent getActionIntent(Context ctx) {
        Intent actionIntent = new Intent(ctx, ActionReceiver.class);
        return PendingIntent.getBroadcast(ctx, 0, actionIntent, 0);

    }

    private static PendingIntent getContentIntent(Context ctx, String bookName, String bookId) {
        Intent intent = new Intent(ctx, BookDetailActivity.class);
        intent.setAction(String.valueOf(notification_id));
        intent.putExtra("bookId", bookId);
        intent.putExtra("bookTitle", bookName);
        return PendingIntent.getActivity(ctx, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


}
