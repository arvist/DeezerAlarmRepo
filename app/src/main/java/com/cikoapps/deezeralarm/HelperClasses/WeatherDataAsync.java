package com.cikoapps.deezeralarm.HelperClasses;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.DigitalClock;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cikoapps.deezeralarm.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

@SuppressWarnings("deprecation")
public class WeatherDataAsync extends AsyncTask<Void, Integer, String> {

    ImageButton refreshButton;
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

    private Animation a;
    boolean windMilesBool;
    boolean tempFBool;
    boolean clock12Hours;

    public WeatherDataAsync(RelativeLayout weatherLayout, boolean tempCBool, boolean windKBool, double latitude, double longtitude, Context context) {

        dateTextView = (TextView) weatherLayout.findViewById(R.id.dateTextView);
        weatherImageView = (ImageView) weatherLayout.findViewById(R.id.weatherImageView);
        tempImageView = (ImageView) weatherLayout.findViewById(R.id.tempImageView);
        windImageView = (ImageView) weatherLayout.findViewById(R.id.windImageView);
        summaryTextView = (TextView) weatherLayout.findViewById(R.id.summaryTextView);
        windTextView = (TextView) weatherLayout.findViewById(R.id.windTextView);
        tempTextView = (TextView) weatherLayout.findViewById(R.id.tempTextView);
        timeTextView = (TextView) weatherLayout.findViewById(R.id.timeTextView);
        cityTextView = (TextView) weatherLayout.findViewById(R.id.cityTextView);
        refreshButton = (ImageButton) weatherLayout.findViewById(R.id.refreshButton);
        DigitalClock digitalClock = (DigitalClock) weatherLayout.findViewById(R.id.digitalClock);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        windMilesBool = preferences.getBoolean("windMilesBool", false);
        tempFBool = preferences.getBoolean("tempFBool", false);
        clock12Hours = preferences.getBoolean("clock12HoursBool", false);

        this.latitude = latitude;
        this.longitude = longtitude;
        setDate();

        this.context = context;
        notoRegular = Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf");
        helperClass = new HelperClass(context);

        a = AnimationUtils.loadAnimation(context, R.anim.progress_anim);
        a.setDuration(700);
        refreshButton.startAnimation(a);
        a.setInterpolator(new Interpolator() {
            private final int frameCount = 16;

            @Override
            public float getInterpolation(float input) {
                return (float) Math.floor(input * frameCount) / frameCount;
            }
        });

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


        final String apikey = "0a6bad312fb111db3c658e0250965";
        latitude = HelperClass.round(latitude, 3);
        longitude = HelperClass.round(longitude, 3);
        String url = "http://api.worldweatheronline.com/premium/v1/weather.ashx?q=" +
                "" + latitude + "%2C" + longitude + "" +
                "&format=json&num_of_days=0&fx=no&cc=yes&mca=no&includelocation=yes&show_comments=no&showlocaltime=no&" +
                "key=" + apikey + "";
        weatherJson = getJSONFromUrl(url);
        return "You are at PostExecute";
    }

    protected void onPostExecute(String result) {
        if (weatherJson != null) {
            try {
                JSONObject dataJsonObj = weatherJson.getJSONObject("data");
                JSONObject currentConditionJsonObj = dataJsonObj.getJSONArray("current_condition").getJSONObject(0);
                JSONObject weatherDesc = currentConditionJsonObj.getJSONArray("weatherDesc").getJSONObject(0);
                String summary = weatherDesc.get("value").toString();
                String tempC = currentConditionJsonObj.getString("temp_C");
                String tempF = currentConditionJsonObj.getString("temp_F");
                String windSpeedMph = currentConditionJsonObj.getString("windspeedMiles");
                String windSpeedKmph = currentConditionJsonObj.getString("windspeedKmph");
                String city = dataJsonObj.getJSONArray("nearest_area").getJSONObject(0).getJSONArray("region").getJSONObject(0).getString("value").toString();
                cityTextView.setText(city);
                summaryTextView.setText(summary);
                summaryTextView.setMaxLines(2);
                timeTextView.setText("0 minutes");
                if (windMilesBool) {
                    windTextView.setText(windSpeedMph + " mph");
                } else {
                    windTextView.setText(windSpeedKmph + " kph");
                }
                if (tempFBool) {
                    tempTextView.setText(tempF + " ℉");
                } else {
                    tempTextView.setText(tempC + " ℃");
                }
                saveWeatherToSharedPreferences(summary, Float.parseFloat(tempC), Float.parseFloat(tempF)
                        , Float.parseFloat(windSpeedKmph), Float.parseFloat(windSpeedMph), city, -1);

            } catch (JSONException e) {
            }
        }
        a.cancel();
        a = null;
    }


    private int setWeatherImage(String icon) {

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
        return -1;
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
            jObj = new JSONObject(json);
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // return JSON String
        return jObj;
    }

    private void saveWeatherToSharedPreferences(String summary, float tempC, float tempF, float windKmh, float windMph,
                                                String city, int weatherImage) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("summary", summary);
        editor.putFloat("tempC", tempC);
        editor.putFloat("tempF", tempF);
        editor.putFloat("windKmh", windKmh);
        editor.putFloat("windMph", windMph);
        editor.putString("city", city);
        editor.putLong("time", Calendar.getInstance().getTimeInMillis());
        editor.putInt("weather", weatherImage);
        editor.commit();
        Log.e(TAG, "Saved to shared preferences");
    }

    public void setFromSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar calendar = Calendar.getInstance();
        long timeNow = calendar.getTimeInMillis();
        boolean windMilesBool = sharedPreferences.getBoolean("windMilesBool", true);
        Log.e(TAG, "WindMilesBool" + windMilesBool);
        boolean tempFBool = sharedPreferences.getBoolean("tempFBool", false);
        String summary = sharedPreferences.getString("summary", "");

        float tempC = sharedPreferences.getFloat("tempC", -1);
        float tempF = sharedPreferences.getFloat("tempF", -1);
        float windKmh = sharedPreferences.getFloat("windKmh", -1);
        float windMph = sharedPreferences.getFloat("windMph", -1);

        String city = sharedPreferences.getString("city", "");
        long updateTime = sharedPreferences.getLong("time", -1);
        int weatherImage = sharedPreferences.getInt("weather", -1);
        weatherImageView.setImageResource(weatherImage);
        long timeFromUpdateLong = timeNow - updateTime;
        int minutes = (int) (timeFromUpdateLong / 60000);
        if (minutes < 60) {
            timeTextView.setText(minutes + " minutes");
        } else if (minutes > 60 && minutes < 1439) {
            int hours = minutes / 60;
            timeTextView.setText(hours + " hours");
        } else if (minutes > 1439) {
            int days = minutes / 1440;
            timeTextView.setText(days + " days");
        }
        cityTextView.setText(city);
        windImageView.setImageResource(R.drawable.wind);
        tempImageView.setImageResource(R.drawable.temp);
        if (windMilesBool) {
            windTextView.setText(windMph + "mph");
        } else {
            windTextView.setText(windKmh + "kph");
        }
        if (tempFBool) {
            tempTextView.setText(tempF + " ℉");

        } else {
            tempTextView.setText(tempC + " ℃");
        }

        summaryTextView.setText(summary);
        Log.e(TAG, "data loaded from shared preferences");
        a.cancel();
    }
}
