package com.example.android.goodreads.network;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/*
    This class is only responsible for handling Google Books API network related operations i.e:
    1. creating URL
    2. establishing https connection with the web service (Google Books) using the url
    3. getting the http response back in the form of JSON format string (through input stream).
 */

public final class NetworkUtils {

    private static String error_msg = "default";

    private NetworkUtils() {
    }


    public static void setError_msg(@NonNull String error_msg) {
        NetworkUtils.error_msg = error_msg;
    }

    public static String getError_msg() {
        return error_msg;
    }


    public static URL createURL(String url) {
        URL bookURL = null;

        try {
            bookURL = new URL(url);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return bookURL;
    }

    static String getHttpResponse(URL bookUrl) throws IOException {

        String httpResponse = "";

        if (bookUrl == null) {
            Log.d("noor", "getHttpResponse: invalid url");
            return httpResponse;
        }

        HttpsURLConnection bookConnection = null;
        InputStream bookStream = null;
        try {
            bookConnection = (HttpsURLConnection) bookUrl.openConnection();
            bookConnection.setReadTimeout(10000 /* milliseconds */);
            bookConnection.setConnectTimeout(15000 /* milliseconds */);
            bookConnection.setRequestMethod("GET");
            boolean privateRequest = AuthUtils.getToken() != null;
            if (privateRequest) {
                bookConnection.setRequestProperty("Authorization", "OAuth " + AuthUtils.getToken());
            }
            bookConnection.connect();

            //If the request was successful(response code 200)
            //then read the input stream and parse the response
            if (bookConnection.getResponseCode() == 200) {
                bookStream = bookConnection.getInputStream();
                httpResponse = readFromStream(bookStream);
            } else {
                Log.e("noor", "Error response code: " + bookConnection.getResponseMessage());
                error_msg = bookConnection.getResponseMessage();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bookConnection != null) {
                bookConnection.disconnect();
            }
            if (bookStream != null) {
                bookStream.close();
            }
        }
        return httpResponse;
    }


    static String readFromStream(InputStream bookStream) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        if (bookStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(bookStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line = bufferedReader.readLine();

            while (line != null) {
                responseBuilder.append(line);
                line = bufferedReader.readLine();
            }
        }

        return responseBuilder.toString();
    }


    public static String postRequest(URL postUrl, int shelfId) {
        if (AuthUtils.getToken() == null) {
            return "Please sign in first.";
        }

        HttpsURLConnection connection = null;
        String response = null;

        try {
            connection = (HttpsURLConnection) postUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "OAuth " + AuthUtils.getToken());

            connection.connect();
            response = connection.getResponseMessage();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response == null ? "No internet connection" : getPostResponseMessage(shelfId);
    }

    private static String getPostResponseMessage(int shelfId) {
        String message = null;

        switch (shelfId){
            case 0:     //favourites
                message = "Book added to Favourites !";
                break;

            case 2:     //to read
                message = "Book added to To Read !";
                break;

            case 4:     //have read
                message = "Book marked as read !";
                break;

        }
        return message;
    }

    public static void updateHistory(URL url) {
        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", "OAuth " + AuthUtils.getToken());
            urlConnection.connect();

            Log.d("noor", "updateHistory: " + urlConnection.getResponseMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}