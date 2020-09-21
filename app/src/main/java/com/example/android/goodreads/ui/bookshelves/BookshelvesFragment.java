package com.example.android.goodreads.ui.bookshelves;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.android.goodreads.HomeActivity;
import com.example.android.goodreads.data.BookShelf;
import com.example.android.goodreads.network.AuthUtils;
import com.example.android.goodreads.ui.search.SearchActivity;
import com.example.android.goodreads.R;
import com.example.android.goodreads.databinding.FragmentBookshelvesBinding;
import com.example.android.goodreads.listeners.OnTokenAcquiredListener;
import com.example.android.goodreads.listeners.OnViewAllChildListListener;
import com.example.android.goodreads.network.BookNetworkAccess;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import static com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount;
import static com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent;


public class BookshelvesFragment extends Fragment implements OnTokenAcquiredListener, OnViewAllChildListListener {

    public static final String TAG = "NOOR";

    private static final int RC_SIGN_IN = 0;
    private BookShelvesModel bookShelvesModel;
    private GoogleSignInOptions gso;
    GoogleSignInClient googleSignInClient;
    FragmentBookshelvesBinding bookshelvesBinding;
    private BookshelfAdapter bookshelfAdapter;
    private static boolean initialLogin = true;
    private HomeActivity currentActivity;

    private Observer<ArrayList<BookShelf>> bookShelvesLiveObserver = new Observer<ArrayList<BookShelf>>() {
        @Override
        public void onChanged(ArrayList<BookShelf> bookShelves) {

            if (!BookNetworkAccess.isInFetchBookShelfProcess()) {

                if (bookShelves != null && BookShelf.getNullCount() < (bookShelves.size() / 2)) {        //bookshelves (more than half the size) fetched successfully
                    Log.d("azhar", "Show list ");
                    toggleViews(View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE);           //show list

                    if (bookshelvesBinding.swipeRefresh.isRefreshing()) {
                        bookshelvesBinding.swipeRefresh.setRefreshing(false);
                    }

                    bookShelvesModel.setBookShelfArrayList(bookShelves);
                    bookshelfAdapter.updateList(bookShelves);
                } else if (bookshelvesBinding.swipeRefresh.isRefreshing()) {      //problem getting bookshelves on refresh... show toast message
                    bookshelvesBinding.swipeRefresh.setRefreshing(false);
                    Toast.makeText(currentActivity, "Couldn't refresh bookshelves", Toast.LENGTH_SHORT).show();

                } else {
                    int shelfErrMsgVisibility = bookShelvesModel.getBookShelfArrayList().size() == 0 ? View.VISIBLE : View.GONE;         //show bookshelf error message on first time
                    int shelfVisibility = shelfErrMsgVisibility == View.VISIBLE ? View.GONE : View.VISIBLE;             //else show list
                    toggleViews(View.GONE, shelfVisibility, View.GONE, View.GONE, shelfErrMsgVisibility);
                    if (shelfVisibility == View.VISIBLE) {
                        bookshelfAdapter.updateList(bookShelvesModel.getBookShelfArrayList());
                    }
                }

            } else if (!bookshelvesBinding.swipeRefresh.isRefreshing()) {
                Log.d("azhar", "Show progress bar ");
                toggleViews(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE);     //keep showing progress bar
            } else {
                toggleViews(View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE);           //show list
                bookshelfAdapter.updateList(bookShelvesModel.getBookShelfArrayList());
            }
        }
    };


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.google_sign_in:
                    Intent signInIntent = googleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                    break;

                case R.id.btn_retry_access:
                    toggleViews(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE); // show progress bar
                    AuthUtils.getAuthToken(BookshelvesFragment.this, currentActivity, getLastSignedInAccount(currentActivity).getAccount());
                    break;

                case R.id.btn_retry_bookshelves:
                    toggleViews(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE);     //show progress bar
                    bookShelvesModel.getBookShelves();
                    break;
            }
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof HomeActivity) {
            currentActivity = (HomeActivity) context;
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        currentActivity = null;
    }

    @Override
    public void onDestroy() {
        if (bookshelfAdapter != null) bookshelfAdapter.detachContext();
        if(bookshelvesBinding != null) bookshelvesBinding.bookshelvesList.setAdapter(null);
        bookshelvesBinding = null;
        initialLogin = currentActivity.isChangingConfigurations() ? initialLogin : true;
        super.onDestroy();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "BOOKSHELVES FRAGMENT --------> onCreate: ");
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(AuthUtils.SCOPE_BOOKS))
                .requestEmail()
                .build();

        bookShelvesModel = new ViewModelProvider(this).get(BookShelvesModel.class);
        ConnectivityManager connectivityManager = (ConnectivityManager) currentActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().build(),
                new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(@NonNull Network network) {
                        super.onAvailable(network);
                        Log.d("noor2", "onAvailable: ");
                        if (currentActivity == null) return;
                        currentActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (bookshelvesBinding != null && bookshelvesBinding.offlineErrMsg.getRoot().getVisibility() == View.VISIBLE) {     //fetch bookshelves automatically when wifi is available only if error message is visible
                                    toggleViews(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE);     //show progress bar
                                    if (AuthUtils.getToken() == null) {
                                        AuthUtils.getAuthToken(BookshelvesFragment.this, currentActivity, getLastSignedInAccount(currentActivity).getAccount());
                                    } else {
                                        bookShelvesModel.getBookShelves();
                                    }
                                }
                            }
                        });

                    }

                });

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "BOOKSHELVES FRAGMENT --------> onCreateView: ");
        bookshelvesBinding = FragmentBookshelvesBinding.inflate(inflater, container, false);
        return bookshelvesBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "BOOKSHELVES FRAGMENT --------> onActivityCreated: " + initialLogin);

        bookShelvesModel.getBookShelfLiveData().observe(getViewLifecycleOwner(), bookShelvesLiveObserver);

        googleSignInClient = GoogleSignIn.getClient(currentActivity, gso);
        GoogleSignInAccount lastSignedInAccount = getLastSignedInAccount(currentActivity);
        updateUI(lastSignedInAccount, initialLogin);
        bookshelvesBinding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                bookShelvesModel.getBookShelves();
            }
        });
        bookshelvesBinding.offlineErrMsg.btnRetryAccess.setOnClickListener(onClickListener);
        bookshelvesBinding.bookshelfErrorMsg.btnRetryBookshelves.setOnClickListener(onClickListener);
        bookshelvesBinding.homeScreen.googleSignIn.setOnClickListener(onClickListener);
    }

    private void setRecyclerView() {
        RecyclerView bookshelfList = bookshelvesBinding.bookshelvesList;
        bookshelfAdapter = new BookshelfAdapter(this);
        bookshelfList.setLayoutManager(new LinearLayoutManager(getContext()));
        bookshelfList.setAdapter(bookshelfAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> accountTask = getSignedInAccountFromIntent(data);
            handleSignIn(accountTask);
        }
    }

    private void handleSignIn(Task<GoogleSignInAccount> accountTask) {
        try {
            GoogleSignInAccount account = accountTask.getResult(ApiException.class);
            toggleViews(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE);     //show progress bar
            updateUI(account, true);
        } catch (ApiException e) {
            Log.d("gooError", "handleSignIn: " + e.getMessage());
            updateUI(null, initialLogin);
        }
    }

    @Override
    public void onTokenAcquired(boolean isTokenAcquired) {

        if (isTokenAcquired) {
            initialLogin = false;
            bookShelvesModel.getBookShelves();
        } else {
            toggleViews(View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE);      //show offline error message
        }
    }

    public void updateUI(GoogleSignInAccount account, boolean isInitialLogin) {
        if (account != null) {
            if (isInitialLogin) {
                AuthUtils.getAuthToken(this, currentActivity, account.getAccount());
            }
            setRecyclerView();

        } else {
            toggleViews(View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE);        //show home screen
        }
    }

    private void toggleViews(int homeScreenVisibility, int shelfListVisibility, int offlineErrMsgVisibility, int progressHomeVisibility, int shelfErrMsgVisibility) {
        if(bookshelvesBinding == null) return;

        bookshelvesBinding.homeScreen.getRoot().setVisibility(homeScreenVisibility);
        bookshelvesBinding.swipeRefresh.setVisibility(shelfListVisibility);
        bookshelvesBinding.offlineErrMsg.getRoot().setVisibility(offlineErrMsgVisibility);
        bookshelvesBinding.progressHome.setVisibility(progressHomeVisibility);
        bookshelvesBinding.bookshelfErrorMsg.getRoot().setVisibility(shelfErrMsgVisibility);
    }

    @Override
    public void onViewAllClicked(String id, String shelfName) {
        Intent intent = new Intent(currentActivity, SearchActivity.class);
        intent.putExtra("shelfId", id);
        intent.putExtra("shelfName", shelfName);
        intent.setAction("ACTION_VIEW_ALL");
        currentActivity.startActivity(intent);
    }

    //-------------


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "BOOKSHELVES FRAGMENT --------> onStart: ");

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "BOOKSHELVES FRAGMENT --------> onViewCreated: ");

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "BOOKSHELVES FRAGMENT --------> onResume: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "BOOKSHELVES FRAGMENT --------> onPause: ");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "BOOKSHELVES FRAGMENT --------> onStop: ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "BOOKSHELVES FRAGMENT --------> onDestroyView: ");
    }


}
