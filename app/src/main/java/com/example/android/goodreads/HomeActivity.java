package com.example.android.goodreads;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.example.android.goodreads.ui.bookdetail.BookDetailActivity;
import com.example.android.goodreads.ui.bookshelves.BookshelvesFragment;
import com.example.android.goodreads.ui.downloads.DownloadsFragment;
import com.example.android.goodreads.ui.search.SearchActivity;
import com.example.android.goodreads.R;
import com.example.android.goodreads.databinding.ActivityHomeBinding;
import com.example.android.goodreads.listeners.OnBookItemClickListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity implements OnBookItemClickListener {
    public static final String TAG_BOOKSHELVES = "BOOKSHELVES";
    public static final String TAG_DOWNLOADS = "DOWNLOADS";

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment selectedFragment = null;
            switch (menuItem.getItemId()) {

                case R.id.nav_bookshelves:
                    selectedFragment = getSupportFragmentManager().findFragmentByTag(TAG_BOOKSHELVES);
                    break;

                case R.id.nav_downloads:
                    selectedFragment = getSupportFragmentManager().findFragmentByTag(TAG_DOWNLOADS);
                    break;
            }

            setFragment(selectedFragment);

            return true;
        }
    };

    private void setFragment(Fragment selectedFragment) {

        Fragment detachFragment = selectedFragment.getTag().equals(TAG_BOOKSHELVES)
                ? getSupportFragmentManager().findFragmentByTag(TAG_DOWNLOADS)
                : getSupportFragmentManager().findFragmentByTag(TAG_BOOKSHELVES);

        getSupportFragmentManager()
                .beginTransaction()
                .detach(detachFragment)
                .attach(selectedFragment)
                .commit();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("noor", "ACTIVITY ---------- onCreate: ");
        ActivityHomeBinding homeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(homeBinding.getRoot());
        setSupportActionBar(homeBinding.homeToolbar.getRoot());

        if (savedInstanceState == null) {
            BookshelvesFragment bookshelvesFragment = new BookshelvesFragment();
            DownloadsFragment downloadsFragment = new DownloadsFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, bookshelvesFragment, TAG_BOOKSHELVES)
                    .add(R.id.fragment_container, downloadsFragment, TAG_DOWNLOADS)
                    .detach(downloadsFragment)
                    .commit();
        }
        homeBinding.bottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.white_background));
        homeBinding.bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_item);
        SearchView searchView = (SearchView) menuItem.getActionView();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName componentName = new ComponentName(this, SearchActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

        return true;
    }



    @Override
    public void onBookItemClicked(String bookId, String bookTitle) {
        Log.d("noor", getClass().getSimpleName());
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra("bookId", bookId);
        intent.putExtra("bookTitle", bookTitle);
        startActivity(intent);
    }
}