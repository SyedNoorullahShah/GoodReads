package com.example.android.goodreads.network;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.example.android.goodreads.ui.bookshelves.BookshelvesFragment;
import com.example.android.goodreads.HomeActivity;
import com.example.android.goodreads.listeners.OnTokenAcquiredListener;

import java.io.IOException;

 public final class AuthUtils {
    private static boolean isFirstCall = true;
    private static String newToken;
    public static final String SCOPE_BOOKS = "https://www.googleapis.com/auth/books";
    private static String currUser = "public"; //default value

    public static void getAuthToken(final BookshelvesFragment ctx, final HomeActivity currentActivity, Account account) {
        if(currentActivity == null) {
            return;
        }

        AccountManager accountManager = AccountManager.get(currentActivity);
        String oldToken = getCachedToken(currentActivity);
        currUser = account.name;

        if (oldToken != null && isFirstCall) {
            Log.d("noor", "invalidating token: ");
            accountManager.invalidateAuthToken(account.type, oldToken);
            isFirstCall = false;
        }

        accountManager.getAuthToken(account, "oauth2:" + SCOPE_BOOKS, new Bundle(), currentActivity,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
                        try {
                            Bundle bundle = accountManagerFuture.getResult();
                            newToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                            storeToken(currentActivity, newToken);
                            ((OnTokenAcquiredListener) ctx).onTokenAcquired(true);
                        } catch (AuthenticatorException | IOException | OperationCanceledException e) {
                            Log.d("noor", "ERROR IN getAuthToken:-   " + e.getClass().getName() + "   " + e.getMessage());
                            newToken = null;
                            ((OnTokenAcquiredListener) ctx).onTokenAcquired(false);
                        }
                    }
                },
                null);
    }


    private static String getCachedToken(Context ctx) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("BookKeys", Context.MODE_PRIVATE);
        return sharedPreferences.getString("authCode", null);
    }

    private static void storeToken(FragmentActivity ctx, String token) {
        if (token != null && token.equals(getCachedToken(ctx))) {
            Log.d("noor", "storeToken: DON'T OVERWRITE TOKEN!");
            return;
        }
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("BookKeys", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("authCode", token);
        editor.commit();
    }

    public static String getToken() {
        return newToken;
    }

    public static void dropToken() {
        newToken = null;
    }

    public static String getCurrentUser() {
        return currUser;
    }
}
