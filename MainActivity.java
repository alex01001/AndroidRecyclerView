package com.example.android.priceviewer;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.priceviewer.NetworkTools;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements PriceAdapter.PriceItemClickListener{

    private EditText mSearchBoxEditText;
    private TextView mErrorDisplayTextView;
    private TextView mSearchResultsTextView;
    private RecyclerView mRecyclerView;

    private PriceAdapter adapter;

    private static int NUMBER_OF_SECONDS_IN_INTERVAL = 86400;
    private static int MAX_OUTPUT_ARRAY_LIST = 25;

    private byte outputArraysLength;

    private List<PriceListItem> priceList;

    private Toast mToast;

    ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchBoxEditText = (EditText) findViewById(R.id.et_search_box);
        mErrorDisplayTextView = (TextView) findViewById(R.id.tv_error_display);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_priceLines);

        outputArraysLength = 0;

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading);

        adapter = new PriceAdapter(getBaseContext(),this);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
    }

    // method to display the result query URL
    private void makeSearchQuery() {
        String priceQuery = mSearchBoxEditText.getText().toString();
        URL priceUrl = NetworkTools.buildUrl(priceQuery,MAX_OUTPUT_ARRAY_LIST);
        String searchResults = null;
        Log.i("sss", priceUrl.toString());
        new PriceQueryTask().execute(priceUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        makeSearchQuery();
        return super.onOptionsItemSelected(item);
    }


    public void mDisplayError(){
        mErrorDisplayTextView.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);


    }
    public String convertUnixDateToSting(long unixSeconds){
        Date date = new Date(unixSeconds*1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-4")); // give a timezone reference for formating (see comment at the bottom
        String formattedDate = sdf.format(date);
        int pos = formattedDate.indexOf(' ');
        if(pos == -1){
            return "";
        }
        else {
            formattedDate = formattedDate.substring(0,pos);
        }
        return formattedDate;
    }

    @Override
    public void onPriceItemClick(int ClickedItemIndex) {
        String msg = priceList.get(ClickedItemIndex).date +" "+ priceList.get(ClickedItemIndex).price;
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }

    public class PriceQueryTask extends AsyncTask<URL, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL searchUrl = urls[0];
            String searchResults = null;
            try{
                searchResults = NetworkTools.getResponseFromHttpUrl(searchUrl);
            }
            catch (IOException e){
                e.printStackTrace();
            }
            Log.i("sss", "started");
            return searchResults;
        }

        @Override
        protected void onPostExecute(String s) {
            // parsing the response. see the response format below
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            float previousPrice = 0;
            float currentCloePriceF = 0;
            if(s!=null && !s.equals("")) {

                mErrorDisplayTextView.setVisibility(View.INVISIBLE);
                mRecyclerView.setVisibility(View.VISIBLE);

                priceList = new ArrayList<>();
                outputArraysLength = 0;
                int pos = s.indexOf("TIMEZONE_OFFSET");
                if(pos==-1){
                    mDisplayError();
                }else{
                    s = s.substring(pos+1,s.length());
                    pos = s.indexOf("\n");
                    s = s.substring(pos+1,s.length());
                    pos = s.indexOf("\n");
                    int commaPos = -1, aPos = -1;
                    String currentDate, currentClosePrice;
                    String currentDateStr;
                    int currentIncrement = 0;
                    long currentUnixDate =0;
                    String currentString = "";
                    String currentChange = "0.00%";
                    // looping through lines of data, extracting comma separated values
                    while (pos>-1){

                        currentString = s.substring(0,pos);
                        Log.i("sss", currentString);
                        s = s.substring(pos+1,s.length());
                        pos = currentString.indexOf("TIMEZONE_OFFSET");
                        if(pos==-1){
                            commaPos = currentString.indexOf(',');
                            currentDate = currentString.substring(0,commaPos);
                            currentString = currentString.substring(commaPos+1,currentString.length());
                            commaPos = currentString.indexOf(',');
                            currentClosePrice = currentString.substring(0,commaPos);
                            // In the first position of the data line there is either a base-date in Unix format (which starts with symbol 'a'
                            // or the increment of the date relative to the base-date (measured in number of seconds in the interval
                            // see the format sample below

                            if(currentDate.indexOf('a')>-1){
                                try {
                                    currentUnixDate = Long.parseLong(currentDate.substring(1, currentDate.length()));
                                }
                                catch ( NumberFormatException e) {
                                    Log.e("error", e.toString());
                                    mDisplayError();
                                    continue;
                                }
                                currentDateStr = convertUnixDateToSting(currentUnixDate);
                            }else {
                                try {
                                    currentIncrement = Integer.parseInt(currentDate);
                                }
                                    catch ( NumberFormatException e) {
                                    Log.e("error", e.toString());
                                    mDisplayError();
                                    continue;
                                }
                                currentDateStr = convertUnixDateToSting(currentUnixDate +currentIncrement*NUMBER_OF_SECONDS_IN_INTERVAL);
                            }
                            currentCloePriceF = Float.valueOf(currentClosePrice);

                            if(previousPrice == 0){
                                currentChange = "0.00%";
                            }
                            else{
                                currentChange = String.format("%.2f",100*(currentCloePriceF-previousPrice)/previousPrice) +'%';
                            }
                            PriceListItem current = new PriceListItem();

                            if(previousPrice == 0) {
                                current.img = R.drawable.neutral;
                            }
                            else if(previousPrice>currentCloePriceF) {
                                current.img = R.drawable.down;
                            }else{
                                current.img = R.drawable.up;
                            }
                            current.date =currentDateStr;
                            current.price = "$" + currentClosePrice;
                            current.change = currentChange;
                            // filling the data source

                            priceList.add(current);
                            previousPrice = currentCloePriceF;


                            outputArraysLength+=1;

                        }
                        pos = s.indexOf("\n");
                    }
                }
                adapter.setPriceData(priceList);
            }else{
                mDisplayError();
            }

        }
    }

}


// data return format sample

//EXCHANGE%3DNYSE
//        MARKET_OPEN_MINUTE=570
//        MARKET_CLOSE_MINUTE=960
//        INTERVAL=86400
//        COLUMNS=DATE,CLOSE,HIGH,LOW,OPEN,VOLUME
//        DATA=
//        TIMEZONE_OFFSET=-240
//        a1505851200,46.2,46.44,44.86,45.22,4854943
//        1,47.5,47.95,46.53,46.98,7305740
//        2,46.25,47.25,45.97,47.11,3695561
//        3,45.38,45.78,44.67,45.31,3224564
//        6,45.29,45.7,44.62,45.33,3071319
//        7,44.74,45.52,44.72,45.11,4539129
//        8,45.07,45.56,44.48,45.28,2391140
//        9,46.08,46.46,44.81,45.07,5282162
//        10,46.62,47,45.9,46.45,3825604
//        13,47.65,48.03,46.41,46.43,4043139
//        14,48.31,48.33,47.44,47.97,3313591
//        15,47.91,49.3,47.75,48.27,4124246
//        16,48.25,48.7293,47.65,48.08,3192080
//        17,47.49,47.89,47.3,47.89,3963549
//        20,47.07,47.72,46.58,47.63,2235684
//        21,46.89,47.45,46.58,47.28,3578577
//        22,46.41,46.72,46.21,46.72,255409
//        23,47.53,47.77,46.8,46.95,3027556
//        24,47.71,48.6299,47.6,48.32,3288641
//        27,48.24,48.93,47.48,48,4682021
//        28,47.77,48.72,47.42,48.65,3960640
//        29,47.75,47.84,46.51,47.38,4697989
//        30,46.54,47.74,46.01,46.5,6033142
//        31,47.93,48.13,47.06,47.13,4209075
//        34,47.93,48.56,47.83,48.04,4020138
//        35,50.08,50.31,48.06,48.13,6702523
//        36,49.52,49.8799,48.51,49.73,3750687
//        37,49.15,49.73,48.52,49.52,3468520
//        38,47.91,48.25,47.01,47.835,3870215
//        41,47.42,48.38,47.37,47.75,2072449
//        42,47.78,48.07,46.38,47.15,2721478
//        43,47.7,49.27,47.45,48.91,3175057
//        44,47.46,47.7671,47.07,47.68,2059018
//        45,47.12,47.43,46.71,47.25,1839001
