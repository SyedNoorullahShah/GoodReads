package com.example.android.goodreads.ui.bookviewer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import com.example.android.justjava.R;
import com.example.android.justjava.databinding.ActivityBookViewerBinding;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.File;
/*
    Activity responsible for:-
        1. opening the downloaded PDF book
        2. viewing the book online (using a WebView or a Chrome custom tab)
        3. viewing the book download verification page (using WebView)
*/

public class BookViewerActivity extends AppCompatActivity {

    public static final String ACTION_ONLINE_VIEW_BOOK = "ACTION_ONLINE_VIEW_BOOK";
    public static final String ACTION_PDF_VIEW_BOOK = "ACTION_PDF_VIEW_BOOK";
    public static final String ACTION_VERIFY_DOWNLOAD = "ACTION_VERIFY_DOWNLOAD";
    private ActivityBookViewerBinding root;
    private WebView bookViewer;
    private String action;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        root = ActivityBookViewerBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());
        action = getIntent().getAction();

        if (ACTION_ONLINE_VIEW_BOOK.equals(action) || ACTION_VERIFY_DOWNLOAD.equals(action)) {
            setWebViewEnvironment(savedInstanceState);
        } else if (ACTION_PDF_VIEW_BOOK.equals(action)) {
            setPDFViewer();
        }

    }

    private void toggleLayouts(int onlineViewerVis, int pdfViewerVis) {
        root.onlineViewerLayout.getRoot().setVisibility(onlineViewerVis);
        root.pdfViewerLayout.getRoot().setVisibility(pdfViewerVis);
    }

    private void setPDFViewer() {
        toggleLayouts(View.GONE, View.VISIBLE);
        PDFView pdfViewer = root.pdfViewerLayout.pdfViewer;

        pdfViewer.fromFile(getPdfBook())
                .defaultPage(0)
                .pageSnap(true)
                .autoSpacing(true)
                .pageFling(true)

                .scrollHandle(new DefaultScrollHandle(this))
                .enableSwipe(true)
                .swipeHorizontal(true)
                .load();
    }

    private File getPdfBook() {
        String bookId = getIntent().getStringExtra("bookId");
        return new File(getFilesDir().getAbsolutePath(), bookId + ".pdf");
    }


    private void setWebViewEnvironment(Bundle savedInstanceState) {
        toggleLayouts(View.VISIBLE, View.GONE);

        bookViewer = root.onlineViewerLayout.onlineBookViewer;
        WebSettings settings = bookViewer.getSettings();
        settings.setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(bookViewer, true);
        } else {
            CookieManager.getInstance().setAcceptCookie(true);
        }
        String url = getUrl();

        bookViewer.setWebViewClient(getWebViewClient());

        if (savedInstanceState == null) bookViewer.loadUrl(url);
    }

    private String getUrl() {
        if (ACTION_ONLINE_VIEW_BOOK.equals(action)) {
            return getIntent().getExtras().getString("readerLink");
        } else {
            return getIntent().getStringExtra("downloadLink");
        }
    }

    private WebViewClient getWebViewClient() {
        return new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                root.onlineViewerLayout.bookLoader.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                root.onlineViewerLayout.bookLoader.setVisibility(View.GONE);
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                if (url.contains("/books/download/") || url.contains("https://play.google.com/books/reader?id=")) {
                    return false;

                } else if (url.contains("https://books.googleusercontent.com/books/content?")) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("downloadLink", url);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                    return false;
                }

                //open chrome tab for other requests
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setToolbarColor(ContextCompat.getColor(BookViewerActivity.this, R.color.colorPrimary));
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(BookViewerActivity.this, Uri.parse(request.getUrl().toString()));
                return true;
            }

        };
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        if (bookViewer != null) bookViewer.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (bookViewer != null) bookViewer.restoreState(savedInstanceState);
    }
}