package com.cikoapps.deezeralarm.HelperClasses;

import android.content.Context;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cikoapps.deezeralarm.R;
import com.google.android.gms.common.api.GoogleApiClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

public class WeatherDataAsync extends AsyncTask<Void, Integer, String> {

    TextView cityTextView;
    JSONObject weatherJson;
    TextView dateTextView;
    ImageView weatherImageView;
    ImageView tempImageView;
    ImageView windImageView;
    TextView summaryTextView;
    TextView windTextView;
    TextView tempTextView;
    boolean metricSystem;
    double latitude;
    double longitude;
    Typeface notoRegular;
    Context context;
    HelperClass helperClass;

    TextView timeTextView;
    private String TAG = "WeatherDataAsync.java";
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    public WeatherDataAsync(RelativeLayout weatherLayout, boolean metricSystem, double latitude, double longtitude, Context context) {

        dateTextView = (TextView) weatherLayout.findViewById(R.id.dateTextView);
        weatherImageView = (ImageView) weatherLayout.findViewById(R.id.weatherImageView);
        tempImageView = (ImageView) weatherLayout.findViewById(R.id.tempImageView);
        windImageView = (ImageView) weatherLayout.findViewById(R.id.windImageView);
        summaryTextView = (TextView) weatherLayout.findViewById(R.id.summaryTextView);
        windTextView = (TextView) weatherLayout.findViewById(R.id.windTextView);
        tempTextView = (TextView) weatherLayout.findViewById(R.id.tempTextView);
        timeTextView = (TextView) weatherLayout.findViewById(R.id.timeTextView);
        cityTextView = (TextView) weatherLayout.findViewById(R.id.cityTextView);


        this.metricSystem = metricSystem;
        this.latitude = latitude;
        this.longitude = longtitude;
        setDate();

        this.context = context;
        notoRegular = Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf");
        helperClass = new HelperClass(context);

    }

    public void setDate() {
        Calendar currentTime = Calendar.getInstance();
        int month = currentTime.get(currentTime.MONTH);
        int day = currentTime.get(currentTime.DAY_OF_MONTH);
        int year = currentTime.get(currentTime.YEAR);
        String monthString = HelperClass.getMonthFromInt(month);
        String date = monthString + " " + day + " , " + year;
        dateTextView.setText(date);
        dateTextView.setTypeface(notoRegular);
    }

    protected String doInBackground(Void... arg0) {

        Log.e(TAG, "weather data async in doInBackground");
        final String apikey = "0a6bad312fb111db3c658e0250965";
        String url = "http://api.worldweatheronline.com/premium/v1/weather.ashx?q=" +
                "" + latitude + "%2C" + longitude + "" +
                "&format=json&num_of_days=0&fx=no&cc=yes&mca=no&includelocation=yes&show_comments=no&showlocaltime=no&" +
                "key=" + apikey + "";
        latitude = HelperClass.round(latitude, 3);
        longitude = HelperClass.round(longitude, 3);

        Log.e(TAG, url);

        Log.e(TAG, url);
        //String url = "http://api.forecast.io/forecast/9f1f81ecb721deab510e0c1249aca1b4/" + latitude + "," + longitude + "";
        weatherJson = getJSONFromUrl(url);
        return "You are at PostExecute";
    }

    protected void onPostExecute(String result) {
        if (weatherJson != null) {
            try {
                JSONObject dataJsonObj = weatherJson.getJSONObject("data");
                JSONArray currentConditionJsonArr = dataJsonObj.getJSONArray("current_condition");
                JSONArray nearestAreaJsonArr = dataJsonObj.getJSONArray("nearest_area");
                String city = dataJsonObj.getJSONArray("nearest_area").getJSONObject(0).getJSONArray("region").getJSONObject(0).get("value").toString();
                Log.e(TAG, city);
                JSONObject currentConditionJsonObj = currentConditionJsonArr.getJSONObject(0);
                JSONObject weatherDesc = currentConditionJsonObj.getJSONArray("weatherDesc").getJSONObject(0);
                String summary = weatherDesc.get("value").toString();
                String tempC = currentConditionJsonObj.getString("temp_C");
                String tempF = currentConditionJsonObj.getString("temp_F");
                String windSpeedMph = currentConditionJsonObj.getString("windspeedMiles");
                String windSpeedKmph = currentConditionJsonObj.getString("windspeedKmph");
                // setWeatherImage(icon);

                tempImageView.setImageResource(R.drawable.temp);
                windImageView.setImageResource(R.drawable.wind);
                cityTextView.setText(city);
                summaryTextView.setText(summary);
                summaryTextView.setMaxLines(2);
                if (metricSystem) {
                    windTextView.setText(windSpeedKmph + " kmph");
                    tempTextView.setText(tempC + " ℃");

                } else {
                    windTextView.setText(windSpeedMph + "mph");
                    tempTextView.setText(tempF + " °F ");
                }
            } catch (JSONException e) {
                if (metricSystem) {
                    windTextView.setText(0 + "m/s");
                    tempTextView.setText(0 + " ℃");
                } else {
                    windTextView.setText(0 + "mph");
                    tempTextView.setText(0 + " °F ");
                }
            }
        } else {
            weatherImageView.setWillNotDraw(true);
            tempImageView.setWillNotDraw(true);
            windImageView.setWillNotDraw(true);
            windTextView.setWillNotDraw(true);
            tempTextView.setWillNotDraw(true);
        }

    }


    private void setWeatherImage(String icon) {
            /*
            clear-day, clear-night, rain, snow, sleet, wind, fog, cloudy, partly-cloudy-day, or partly-cloudy-night
             */
        if (icon.equalsIgnoreCase("clear-day")) {
            weatherImageView.setImageResource(R.drawable.ic_launcher);
        } else if (icon.equalsIgnoreCase("clear-night")) {
            weatherImageView.setImageResource(R.drawable.ic_launcher);
        } else if (icon.equalsIgnoreCase("rain")) {
            weatherImageView.setImageResource(R.drawable.ic_launcher);
        } else if (icon.equalsIgnoreCase("snow")) {
            weatherImageView.setImageResource(R.drawable.ic_launcher);
        } else if (icon.equalsIgnoreCase("sleet")) {
            weatherImageView.setImageResource(R.drawable.ic_launcher);
        } else if (icon.equalsIgnoreCase("wind")) {
            weatherImageView.setImageResource(R.drawable.ic_launcher);
        } else if (icon.equalsIgnoreCase("fog")) {
            weatherImageView.setImageResource(R.drawable.ic_launcher);
        } else if (icon.equalsIgnoreCase("cloudy")) {
            weatherImageView.setImageResource(R.drawable.ic_launcher);
        } else if (icon.equalsIgnoreCase("partly-cloudy-day")) {
            weatherImageView.setImageResource(R.drawable.weather_sunny);
        } else if (icon.equalsIgnoreCase("partly-cloudy-night")) {
            weatherImageView.setImageResource(R.drawable.ic_launcher);
        } else if (icon.equalsIgnoreCase("hail")) {
            weatherImageView.setImageResource(R.drawable.ic_launcher);
        } else if (icon.equalsIgnoreCase("thunderstorm")) {
            weatherImageView.setImageResource(R.drawable.ic_launcher);
        } else if (icon.equalsIgnoreCase("tornado")) {
            weatherImageView.setImageResource(R.drawable.ic_launcher);
        } else {
            weatherImageView.setImageResource(R.drawable.ic_launcher);
        }
    }

    public JSONObject getJSONFromUrl(String url) {
        InputStream is = null;
        JSONObject jObj = null;
        String json = "";
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            assert is != null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("/n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        // return JSON String
        return jObj;
    }


}

