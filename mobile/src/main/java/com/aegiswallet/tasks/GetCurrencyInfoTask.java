/*
 * Aegis Bitcoin Wallet - The secure Bitcoin wallet for Android
 * Copyright 2014 Bojan Simic and specularX.co, designed by Reuven Yamrom
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aegiswallet.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.aegiswallet.utils.Constants;
import com.google.bitcoin.core.CoinDefinition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Created by bsimic on 3/17/14.
 */
public class GetCurrencyInfoTask extends AsyncTask<String, Void, Void> {

    private Context context;
    private String urlString = Constants.BLOCKCHAIN_CURRENCY_CALL;
    private JSONObject jsonObject;
    private SharedPreferences sharedPreferences;

    private String TAG = this.getClass().getName();

    public GetCurrencyInfoTask(Context context) {
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected Void doInBackground(String... strings) {

        Log.d(TAG, "inside currency info task...");

        if(fileExistance(Constants.BLOCKCHAIN_CURRENCY_FILE_NAME) && !shouldRefreshFile(Constants.BLOCKCHAIN_CURRENCY_FILE_NAME)){
            return null;
        }

        HttpURLConnection urlConnection = null;
        URL url = null;
        jsonObject = null;
        Object grs_btc_price = null;
        InputStream inStream = null;
        try {
            url = new URL(urlString.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.connect();
            inStream = urlConnection.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
            String temp, response = "";
            while ((temp = bReader.readLine()) != null) {
                response += temp;
            }
            jsonObject = (JSONObject) new JSONTokener(response).nextValue();

            grs_btc_price = getCoinValueBTC_bittrex();

            JSONArray array = jsonObject.names();
            for(int i = 0; i < array.length(); ++i) {
                String currency = array.getString(i);
                JSONObject thisOne = jsonObject.getJSONObject(currency);
                double last = thisOne.getDouble("last");
                last *= (Double)grs_btc_price;
                thisOne.put("last", last);
            }
        } catch (Exception e) {
        } finally {
            if (inStream != null) {
                try {
                    // this will close the bReader as well
                    inStream.close();
                } catch (IOException ignored) {
                    Log.e("Currency Task", "File Close IO Exception: " + ignored.getMessage());
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        if (jsonObject != null) {

            try {
                FileOutputStream fos = context.getApplicationContext().openFileOutput(Constants.BLOCKCHAIN_CURRENCY_FILE_NAME, Context.MODE_PRIVATE);
                /*double btcPrice = Double.parseDouble(jsonObject.toString());
                double grsPrice = grs_btc_price != null ? (Double)grs_btc_price : 0;

                JSONArray array = new JSONArray(jsonObject.toString());

                for (int i = 0; i < array.length(); ++i)
                {
                    JSONObject currency = array.getJSONObject(i);
                    double last = currency.getDouble("last") * grsPrice;
                    currency.s
                }

*/
                fos.write(jsonObject.toString().getBytes());
                fos.close();
            } catch (IOException e) {
                Log.e("Currency Task", "Cannot save or create file " + e.getMessage());
            } /*catch (JSONException e)
            {
                Log.e("Currency Task", "JSON exception " + e.getMessage());
            }*/
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        //UPDATE views
    }

    public boolean fileExistance(String fname){
        File file = context.getApplicationContext().getFileStreamPath(fname);
        return file.exists();
    }

    public boolean shouldRefreshFile(String fname){
        File file = context.getApplicationContext().getFileStreamPath(fname);

        long msBetweenDates = new Date().getTime() - file.lastModified();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(msBetweenDates);
        if(minutes > 15)
            return true;
        else
            return false;
    }
    private static Object getCoinValueBTC_poloniex()
    {




        //final Map<String, ExchangeRate> rates = new TreeMap<String, ExchangeRate>();
        // Keep the LTC rate around for a bit
        Double btcRate = 0.0;
        String currencyCryptsy = CoinDefinition.cryptsyMarketCurrency;
        String urlCryptsy =  "https://poloniex.com/public?command=returnTradeHistory&currencyPair="+CoinDefinition.cryptsyMarketCurrency +"_" + CoinDefinition.coinTicker;




        try {
            // final String currencyCode = currencies[i];
            final URL URLCryptsy = new URL(urlCryptsy);
            final HttpURLConnection connectionCryptsy = (HttpURLConnection)URLCryptsy.openConnection();
            connectionCryptsy.setConnectTimeout(15*1000 * 2);
            connectionCryptsy.setReadTimeout(15*1000* 2);
            connectionCryptsy.connect();

            final StringBuilder contentCryptsy = new StringBuilder();

            Reader reader = null;
            try
            {
                reader = new InputStreamReader(new BufferedInputStream(connectionCryptsy.getInputStream(), 1024));
                copy(reader, contentCryptsy);
                //final JSONObject head = new JSONObject(contentCryptsy.toString());
                //JSONObject returnObject = head.getJSONObject("return");
                //JSONObject markets = returnObject.getJSONObject("markets");
                //JSONObject coinInfo = head.getJSONObject(CoinDefinition.cryptsyMarketCurrency +"_" + CoinDefinition.coinTicker);





                JSONArray recenttrades = new JSONArray(contentCryptsy.toString());//coinInfo.getJSONArray("recenttrades");

                double btcTraded = 0.0;
                double coinTraded = 0.0;

                for(int i = 0; i < recenttrades.length(); ++i)
                {
                    JSONObject trade = (JSONObject)recenttrades.get(i);

                    btcTraded += trade.getDouble("total");
                    coinTraded += trade.getDouble("amount");

                }

                Double averageTrade = btcTraded / coinTraded;



                /*if(currencyCryptsy.equalsIgnoreCase("BTC"))*/ btcRate = averageTrade;

            }
            finally
            {
                if (reader != null)
                    reader.close();
            }
            return btcRate;
        }
        catch (final IOException x)
        {
            x.printStackTrace();
        }
        catch (final JSONException x)
        {
            x.printStackTrace();
        }

        return null;
    }

    private final static String BITTREX_URL = "https://bittrex.com/api/v1.1/public/getticker?market=btc-grs";
    private static Object getCoinValueBTC_bittrex()
    {
        Double btcRate = 0.0;

        try {
            // final String currencyCode = currencies[i];
            final URL URLCryptsy = new URL(BITTREX_URL);
            final HttpURLConnection connection = (HttpURLConnection)URLCryptsy.openConnection();
            connection.setConnectTimeout(15*1000 * 2);
            connection.setReadTimeout(15*1000* 2);
            connection.connect();

            final StringBuilder content = new StringBuilder();

            Reader reader = null;
            try
            {
                reader = new InputStreamReader(new BufferedInputStream(connection.getInputStream(), 1024));
                copy(reader, content);

                final JSONObject head = new JSONObject(content.toString());

                String result = head.getString("success");

                Double averageTrade = null;
                if(result.equals("true"))
                {
                    JSONObject dataObject = head.getJSONObject("result");

                    averageTrade = dataObject.getDouble("Last");
                }



                btcRate = averageTrade;

            }
            finally
            {
                if (reader != null)
                    reader.close();
            }
            return btcRate;
        }
        catch (final IOException x)
        {
            x.printStackTrace();
        }
        catch (final JSONException x)
        {
            x.printStackTrace();
        }

        return null;
    }

    public static final long copy(@Nonnull final Reader reader, @Nonnull final StringBuilder builder) throws IOException
    {
        return copy(reader, builder, 0);
    }

    public static final long copy(@Nonnull final Reader reader, @Nonnull final StringBuilder builder, final long maxChars) throws IOException
    {
        final char[] buffer = new char[256];
        long count = 0;
        int n = 0;
        while (-1 != (n = reader.read(buffer)))
        {
            builder.append(buffer, 0, n);
            count += n;

            if (maxChars != 0 && count > maxChars)
                throw new IOException("Read more than the limit of " + maxChars + " characters");
        }
        return count;
    }
}
