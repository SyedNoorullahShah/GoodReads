package com.example.android.goodreads.ui.bookdetail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.goodreads.ui.bookviewer.BookViewerActivity;
import com.example.android.goodreads.network.AuthUtils;
import com.example.android.goodreads.data.Book;
import com.example.android.goodreads.data.BookListItem;
import com.example.android.goodreads.utils.DownloadUtils;
import com.example.android.justjava.R;
import com.example.android.justjava.databinding.ActivityBookDetailBinding;
import com.example.android.justjava.databinding.DetailButtonsBinding;
import com.example.android.goodreads.listeners.OnBookItemClickListener;
import com.example.android.goodreads.network.BookNetworkAccess;
import com.example.android.goodreads.network.HttpMethodTypes;
import com.example.android.goodreads.network.NetworkUtils;
import com.google.android.material.appbar.AppBarLayout;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class BookDetailActivity extends AppCompatActivity implements OnBookItemClickListener, View.OnClickListener {

    private ActivityBookDetailBinding root;
    private BookDetailModel bookDetailModel;
    private HorizontalBookListAdapter horizontalBookListAdapter;
    private String webReaderLink;
    private Book currBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        root = ActivityBookDetailBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());
        setToolbarSettings();

        bookDetailModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(BookDetailModel.class);
        LiveData<Book> bookData = bookDetailModel.getBookLiveData();
        LiveData<ArrayList<BookListItem>> relatedBooksLiveData = bookDetailModel.getRelatedBooksLiveData();
        LiveData<String> postResultLiveData = bookDetailModel.getPostResultLiveData();

        //getting book data from the web service OR from the database ONLY when activity is opened for the first time
        if (savedInstanceState == null) {
            bookDetailModel.getBook(getBookId(), isBookDownloaded());
        }

        postResultLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String response) {
                if (response != null)
                    Toast.makeText(BookDetailActivity.this, response, Toast.LENGTH_SHORT).show();
            }
        });

        bookData.observe(this, new Observer<Book>() {
            @Override
            public void onChanged(Book book) {
                Log.d("asdfg", "onChanged: ");
                if (isBookDownloaded()) {
                    showBookDetails(book);
                } else {
                    showResultsFromApi(book);
                }
            }

            private void showResultsFromApi(Book book) {
                if (!BookNetworkAccess.isInFetchBookProcess()) {
                    if (book != null) {     //book loading successful... show details
                        showBookDetails(book);
                    } else {            //error loading book... show error message
                        toggleViews(View.VISIBLE, View.GONE, View.GONE);
                        root.detailsErrMsg.setText(NetworkUtils.getError_msg());
                    }
                } else {
                    toggleViews(View.GONE, View.VISIBLE, View.GONE);    //keep showing progress bar
                }
            }

            private void showBookDetails(Book book) {
                toggleViews(View.GONE, View.GONE, View.VISIBLE);
                currBook = book;
                setDetails();
            }

        });

        relatedBooksLiveData.observe(this, new Observer<ArrayList<BookListItem>>() {
            @Override
            public void onChanged(ArrayList<BookListItem> bookItems) {

                if (!BookNetworkAccess.isInFetchRelatedListProcess()) {
                    root.bookDetails.relBooksProgress.setVisibility(View.GONE);

                    if (bookItems != null) {        //list loaded successfully
                        int listMsgVisibility = bookItems.size() == 0 ? View.VISIBLE : View.GONE;       //display message if list is empty
                        root.bookDetails.listMsg.setVisibility(listMsgVisibility);

                        if (listMsgVisibility != View.VISIBLE)
                            horizontalBookListAdapter.updateList(bookItems);
                        else
                            root.bookDetails.listMsg.setText("No results found for related books.");

                    } else {       //there was a problem while loading list...display error message
                        root.bookDetails.listMsg.setVisibility(View.VISIBLE);
                        root.bookDetails.listMsg.setText("Couldn't get related books.");
                    }
                }


            }
        });


    }

    private boolean isBookDownloaded() {
        File bookFile = new File(getFilesDir().getAbsolutePath(), getBookId() + ".pdf");
        return bookFile.exists();
    }

    private void setToolbarSettings() {
        final Toolbar toolbar = root.activityDetailToolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        root.appBarContainer.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                String toolbarTitle = (Math.abs(verticalOffset) - appBarLayout.getTotalScrollRange() == 0) ? "Book Details" : " ";
                toolbar.setTitle(toolbarTitle);
            }
        });
    }

    private void setDetails() {

        //book title image
        Picasso.get()
                .load(currBook.getImage())
                .resize(128, 0)
                .placeholder(R.drawable.book_placeholder)
                .into(root.bookCollapsingDetails.coverDetail);
        //setting buttons
        webReaderLink = currBook.getWebReaderLink();
        setButtons();
        //ratings
        root.bookCollapsingDetails.btnRate.noRatings.setText(String.valueOf(currBook.getNo_ratings()));
        root.bookCollapsingDetails.btnRate.rating.setText(String.valueOf(currBook.getRating()));
        //title
        root.bookCollapsingDetails.titleDetail.setText(currBook.getTitle());
        //author
        root.bookCollapsingDetails.authorDetail.setText(currBook.getAuthor());
        //lang
        root.bookCollapsingDetails.bookLang.setText(currBook.getLang());
        //category
        root.bookDetails.bookCategory.setText(currBook.getCategory());
        //published date
        root.bookDetails.bookDate.setText(currBook.getPublishedDate());
        //pages
        root.bookDetails.bookPages.setText(String.valueOf(currBook.getPageCount()));
        //description
        root.bookDetails.bookDesc.setText(currBook.getDescription().replaceAll("\\<.*?\\>", "").replace("&nbsp;", " "));
        //publisher
        root.bookDetails.bookPublisher.setText(currBook.getPublisher());

        //related books list
        setRecyclerView();

        if (!bookDetailModel.relatedListHasBeenLoaded()) {
            bookDetailModel.getRelatedBooks(currBook.getTitle());
        }
    }

    private void setButtons() {
        DetailButtonsBinding detailActivityButtons = root.bookDetails.btnsDetailActivity;
        detailActivityButtons.btnViewOnline.setOnClickListener(this);
        detailActivityButtons.btnReadLater.setOnClickListener(this);
        detailActivityButtons.btnFavourite.setOnClickListener(this);
        detailActivityButtons.btnDownload.setOnClickListener(this);
        setDownloadBtnVisibility();

    }

    private void setDownloadBtnVisibility() {

        if (!currBook.getDownloadLink().equals("not available")) {
            updateDownloadButtonState();

        } else {
            root.bookDetails.btnsDetailActivity.btnDownload.setVisibility(View.GONE);
            root.bookDetails.pdfMsg.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        DownloadUtils.attachContext(this);
        if (root.bookDetailsContainer.getVisibility() == View.VISIBLE) {
            setDownloadBtnVisibility();
        }
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_view_online:
                viewBookOnline();
                break;

            case R.id.btn_read_later:
                bookDetailModel.addBookToBookShelf(HttpMethodTypes.POST_TO_READ, getBookId());
                break;

            case R.id.btn_favourite:
                bookDetailModel.addBookToBookShelf(HttpMethodTypes.POST_FAVOURITE, getBookId());
                break;

            case R.id.btn_download:
                setDownloadBtnEvent((Button) v);
                break;
        }

    }

    private void setDownloadBtnEvent(Button v) {
        String btnText = (String) v.getText();

        if (btnText.equals("Download")) {
            verifyDownload();

        } else if (btnText.equals("Read")) {
            bookDetailModel.addBookToBookShelf(HttpMethodTypes.POST_READING_NOW, getBookId());
            readBook();
        }
    }

    private void verifyDownload() {
        Intent intent = new Intent(this, BookViewerActivity.class);
        intent.setAction(BookViewerActivity.ACTION_VERIFY_DOWNLOAD);
        intent.putExtra("downloadLink", currBook.getDownloadLink());
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                currBook.setDownloadLink(data.getStringExtra("downloadLink"));
                DownloadUtils.addDownload(currBook);
                updateDownloadButtonState();
            }
        }
    }

    //open PDF book
    private void readBook() {
        Intent intent = new Intent(this, BookViewerActivity.class);
        intent.putExtra("bookId", getBookId());
        intent.setAction(BookViewerActivity.ACTION_PDF_VIEW_BOOK);
        startActivity(intent);
    }

    private void viewBookOnline() {
        if (AuthUtils.getToken() != null) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(this, Uri.parse(webReaderLink));
        } else {
            Intent intent = new Intent(this, BookViewerActivity.class);
            intent.putExtra("readerLink", webReaderLink);
            intent.setAction(BookViewerActivity.ACTION_ONLINE_VIEW_BOOK);
            startActivity(intent);
        }
    }

    private void setRecyclerView() {
        RecyclerView relatedBookList = root.bookDetails.relatedBookList;
        horizontalBookListAdapter = new HorizontalBookListAdapter(this);
        relatedBookList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        relatedBookList.setAdapter(horizontalBookListAdapter);
    }

    private void toggleViews(int errorMsgVisibility, int barVisibility, int detailsVisibility) {
        root.detailsErrMsg.setVisibility(errorMsgVisibility);
        root.progressDetails.setVisibility(barVisibility);
        root.bookDetailsContainer.setVisibility(detailsVisibility);

        int bookCollapsingDetailsVisibility = detailsVisibility == View.VISIBLE ? View.VISIBLE : View.GONE;
        root.bookCollapsingDetails.getRoot().setVisibility(bookCollapsingDetailsVisibility);
    }


    @Override
    public void onBookItemClicked(String bookId, String bookTitle) {
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra("bookId", bookId);
        intent.putExtra("bookTitle", bookTitle);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            Log.d("aesi", "onDestroy: ");
            DownloadUtils.detachContext();
            if ((BookNetworkAccess.isInFetchBookProcess() || BookNetworkAccess.isInPostRequestProcess()) && !isChangingConfigurations()) {
                bookDetailModel.interruptBookSearch();
            }
        }
    }

    public String getBookId() {
        return getIntent().getExtras().getString("bookId");
    }

    public void updateDownloadButtonState() {
        Button downloadButton = root.bookDetails.btnsDetailActivity.btnDownload;

        if (isBookDownloaded()) {
            downloadButton.setText("Read");
            downloadButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.read, 0, 0);
        } else if (DownloadUtils.isDownloadPending(getBookId())) {
            downloadButton.setText("Downloading");
            downloadButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.loading, 0, 0);
        } else {
            downloadButton.setText("Download");
            downloadButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.download, 0, 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.read_check_menu_item) {
            bookDetailModel.addBookToBookShelf(HttpMethodTypes.POST_HAVE_READ, getBookId());
        } else finish();

        return true;
    }
}
