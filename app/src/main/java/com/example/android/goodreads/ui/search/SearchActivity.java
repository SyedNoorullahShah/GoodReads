package com.example.android.goodreads.ui.search;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.goodreads.data.BookListItem;
import com.example.android.justjava.R;
import com.example.android.goodreads.ui.bookdetail.BookDetailActivity;
import com.example.android.justjava.databinding.ActivitySearchBinding;
import com.example.android.goodreads.listeners.OnBookItemClickListener;
import com.example.android.goodreads.listeners.UpdateListListener;
import com.example.android.goodreads.network.BookNetworkAccess;
import com.example.android.goodreads.network.HttpMethodTypes;
import com.example.android.goodreads.network.NetworkUtils;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity implements OnBookItemClickListener, UpdateListListener {

    private SearchView bookSearch;
    private VerticalBookListAdapter verticalBookListAdapter;
    private SearchBookViewModel searchBookViewModel;
    private RecyclerView bookList;
    private TextView errorMsg;
    private static String currBookData;
    private static boolean updateSameList;
    private static boolean keepUpdatingList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySearchBinding root = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());
        setSupportActionBar(root.mainToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchBookViewModel = new ViewModelProvider(this).get(SearchBookViewModel.class);
        LiveData<ArrayList<BookListItem>> books = searchBookViewModel.getBookListLiveData();
        bookList = findViewById(R.id.book_list);
        errorMsg = findViewById(R.id.error_message);
        setRecyclerView();
        String barTitle = getActionBarTitle();
        setTitle(barTitle);
        if (savedInstanceState == null) {
            updateSameList = false;
            keepUpdatingList = false;
            handleSearch(getIntent());
        }

        books.observe(this, new Observer<ArrayList<BookListItem>>() {
            @Override
            public void onChanged(ArrayList<BookListItem> bookItems) {
                if (!BookNetworkAccess.isInFetchListProcess()) {

                    if (bookItems != null) {        //book list data is fetched successfully.
                        toggleViews(View.VISIBLE, View.GONE, View.GONE);
                        verticalBookListAdapter.updateBooks(bookItems, updateSameList);
                        updateSameList = false;
                        keepUpdatingList = bookItems.size() == 25;

                    } else if (!updateSameList) {       //some problem occurs while loading the main book list (show error message).
                        errorMsg.setText(NetworkUtils.getError_msg());
                        toggleViews(View.GONE, View.GONE, View.VISIBLE);

                    } else {        //some problem occurs while updating the current book list (show retry and reset the startIndex).
                        BookNetworkAccess.updateStartIndex(false);
                        toggleViews(View.VISIBLE, View.GONE, View.GONE);
                        verticalBookListAdapter.notifyItemChanged(verticalBookListAdapter.getBookItemData().size() - 1);
                    }

                } else if (!updateSameList) {
                    toggleViews(View.GONE, View.VISIBLE, View.GONE);        //keep showing progress bar

                } else {        //keep showing update list progress
                    toggleViews(View.VISIBLE, View.GONE, View.GONE);
                    verticalBookListAdapter.notifyItemChanged(verticalBookListAdapter.getBookItemData().size() - 1);
                }

            }
        });
    }

    private String getActionBarTitle() {
        return Intent.ACTION_SEARCH.equals(getIntent().getAction()) ? "Search" : getIntent().getStringExtra("shelfName");
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getStringExtra(SearchManager.QUERY) != null) {
            Intent sendIntent = getIntent().getAction().equals(Intent.ACTION_SEARCH) ? intent : getIntent();
            handleSearch(sendIntent);
        }
    }

    private void handleSearch(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {          //search action
            String bookName = intent.getStringExtra(SearchManager.QUERY);

            if (bookName == null || bookName.trim().isEmpty()) {
                bookSearch.clearFocus();
                return;
            }

            VerticalBookListAdapter.isDataHandled = false;
            toggleViews(View.GONE, View.VISIBLE, View.GONE);        //view progress bar
            currBookData = bookName;
            if (BookNetworkAccess.isInFetchListProcess())
                searchBookViewModel.interruptSearch();       //cancel/interrupt ongoing search before starting a new one
            searchBookViewModel.getBookList(bookName, HttpMethodTypes.GET_BOOK_LIST, false);

        }

        else {                       //view all action
            String shelfId = intent.getStringExtra("shelfId");

            VerticalBookListAdapter.isDataHandled = false;
            toggleViews(View.GONE, View.VISIBLE, View.GONE);        //view progress bar
            currBookData = shelfId;
            searchBookViewModel.getBookList(shelfId, HttpMethodTypes.VIEW_ALL_BOOKSHELF_LIST, false);
        }
    }

    private void setRecyclerView() {
        bookList.setLayoutManager(new LinearLayoutManager(this));
        verticalBookListAdapter = new VerticalBookListAdapter(this, searchBookViewModel.getBookItemData());
        bookList.setAdapter(verticalBookListAdapter);
        bookList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                ArrayList<BookListItem> bookItems = verticalBookListAdapter.getBookItemData();

                if (!keepUpdatingList || bookItems.size() >= 100 || updateSameList) {
                    return;                 //exit if the same list is already being updated (i.e updateSameList is true)
                }

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (linearLayoutManager != null && linearLayoutManager.findLastVisibleItemPosition() == bookItems.size() - 1    //end of book list recycler view
                        && !BookNetworkAccess.isInFetchListProcess()) {
                    updateList(bookItems);
                }
            }
        });
    }

    @Override
    public void updateList(ArrayList<BookListItem> bookItems) {
        updateSameList = true;
        VerticalBookListAdapter.isDataHandled = false;
        //adding null item i.e progress bar item
        bookItems.add(null);
        verticalBookListAdapter.notifyItemInserted(bookItems.size() - 1);
        int action = Intent.ACTION_SEARCH.equals(getIntent().getAction()) ? HttpMethodTypes.GET_BOOK_LIST : HttpMethodTypes.VIEW_ALL_BOOKSHELF_LIST;
        searchBookViewModel.getBookList(currBookData, action, true);
    }

    private void toggleViews(int listVisibility, int barVisibility, int msgVisibility) {
        ProgressBar progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(barVisibility);
        bookList.setVisibility(listVisibility);
        errorMsg.setVisibility(msgVisibility);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            getMenuInflater().inflate(R.menu.search_menu, menu);
            MenuItem menuItem = menu.findItem(R.id.search_item);

            bookSearch = (SearchView) menuItem.getActionView();
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            bookSearch.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing() && BookNetworkAccess.isInFetchListProcess() && !isChangingConfigurations()) {
            searchBookViewModel.interruptSearch();
        }

    }

    @Override
    public void onBookItemClicked(String bookId, String bookTitle) {
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra("bookId", bookId);
        intent.putExtra("bookTitle", bookTitle);
        startActivity(intent);
    }


}
