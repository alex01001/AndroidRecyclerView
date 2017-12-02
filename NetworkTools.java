package com.example.android.priceviewer;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkTools {
    final static String BASE_URL =
            "https://finance.google.com/finance/getprices";
//            "https://api.github.com/search/repositories";

    final static String PARAM_QUERY = "q";

    /*
     * The sort field. One of stars, forks, or updated.
     * Default: results are sorted by best match if no field is specified.
     */
    final static String PARAM_1 = "i";
    final static String PARAM_2 = "p";
    final static String PARAM_3 = "f";
    final static String param1 = "86400";
    final static String param3 = "d,o,h,l,c,v";

    /**
     * Builds the URL
     */
    public static URL buildUrl(String symbol, int numberOfDays) {

        String param2 = Integer.toString(numberOfDays) + "d";
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(PARAM_QUERY, symbol)
                .appendQueryParameter(PARAM_1, param1)
                .appendQueryParameter(PARAM_2, param2)
                .appendQueryParameter(PARAM_3, param3)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }



}

