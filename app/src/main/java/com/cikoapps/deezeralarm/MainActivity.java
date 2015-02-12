package com.cikoapps.deezeralarm;

import android.content.Context;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.DigitalClock;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by arvis.taurenis on 2/9/2015.
 */
@SuppressWarnings("ALL")
public class MainActivity extends ActionBarActivity {

    Toolbar toolbar;
    AlarmViewAdapter alarmViewAdapter;
    private RecyclerView alarmRecyclerView;
    Typeface notoRegular;
    TextView windTextView;
    TextView tempTextView;
    TextView summaryTextView;
    ImageView weatherImageView;
    ImageView tempImageView;
    ImageView windImageView;
    double latitude = 0;
    double longitude = 0;
    boolean metricSystem = true;
    private LocationManager locationManager;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private boolean canGetLocation;
    Location location;
    final int MIN_TIME_BW_UPDATES = 1000;
    final int MIN_DISTANCE_CHANGE_FOR_UPDATES = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        notoRegular = Typeface.createFromAsset(getAssets(), "NotoSerif-Regular.ttf");
        toolbar = (Toolbar) findViewById(R.id.appBar);
        setSupportActionBar(toolbar);


        alarmRecyclerView = (RecyclerView) findViewById(R.id.alarmRecyclerView);
        alarmRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(
                getApplicationContext()
        ));
        alarmRecyclerView.setHasFixedSize(true);
        alarmViewAdapter = new AlarmViewAdapter(getApplicationContext(), createTestData());
        alarmRecyclerView.setAdapter(alarmViewAdapter);

        alarmRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        alarmRecyclerView.setItemAnimator(new DefaultItemAnimator());

        RelativeLayout mainTopLayout = (RelativeLayout) findViewById(R.id.mainTopLayout);
        TextView dateTextView = (TextView) mainTopLayout.findViewById(R.id.dateTextView);
        weatherImageView = (ImageView) mainTopLayout.findViewById(R.id.weatherImageView);
        tempImageView = (ImageView) mainTopLayout.findViewById(R.id.tempImageView);
        windImageView = (ImageView) mainTopLayout.findViewById(R.id.windImageView);
        summaryTextView = (TextView) mainTopLayout.findViewById(R.id.summaryTextView);

        Calendar currentTime = Calendar.getInstance();
        int month = currentTime.get(currentTime.MONTH);
        int day = currentTime.get(currentTime.DAY_OF_MONTH);
        int year = currentTime.get(currentTime.YEAR);
        String monthString = getMonthFromInt(month);
        String date = monthString + " " + day + " , " + year;

        dateTextView.setText(date);
        dateTextView.setTypeface(notoRegular);

        /*DigitalClock digitalClock = (DigitalClock) findViewById(R.id.digitalClock);
        digitalClock.setTypeface(notoRegular);
        digitalClock.setTextColor(getResources().getColor(R.color.colorPrimaryText));

        LocationManager locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000L,500.0f, new MyLocationListener());
        Location location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);*/
        location = getLocation();


/*
        latitude = location.getLatitude();
        longitude = location.getLongitude();*/


        windTextView = (TextView) mainTopLayout.findViewById(R.id.windTextView);
        tempTextView = (TextView) mainTopLayout.findViewById(R.id.tempTextView);

        new TestAsync().execute();

    }

    public Location getLocation() {

        try {
            locationManager = (LocationManager) this
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.d("Network", "Network NOT  Enabled");
            } else {
                canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, new MyLocationListener());
                    Log.d("Network", "Network Enabled");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, new MyLocationListener());
                        Log.e("GPS", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Log.e("Latitude", latitude + "");
                                Log.e("Longtitude", longitude + "");
                            }
                            else {
                                Log.e("LOCATION", "Can not determine last know location");
                            }
                        }
                        else {
                            Log.e("LOCATION MANAGER", "Returns NULL");
                        }
                    }
                    else{
                        Log.e("LOCATION", "Returns NULL");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public static String getMonthFromInt(int month) {
        String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        return monthNames[month];
    }

    @Override
    public View onCreateView(String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public List<Alarm> createTestData() {
        List<Alarm> testAlarms = new ArrayList<>();
        Alarm alarm1 = new Alarm();
        alarm1.title = "Morning Alarm";
        alarm1.time = "06:00 AM";
        alarm1.isEnabled = true;
        alarm1.repeatingDays = new boolean[]{true, true, true, true, true, false, false};
        Alarm alarm2 = new Alarm();
        alarm2.title = "You are late alarm";
        alarm2.time = "07:00 AM";
        alarm2.isEnabled = true;
        alarm2.repeatingDays = new boolean[]{true, true, true, true, true, false, false};
        Alarm alarm3 = new Alarm();
        alarm3.title = "Weekend alarm";
        alarm3.time = "08:00 AM";
        alarm3.isEnabled = true;
        alarm3.repeatingDays = new boolean[]{false, false, false, false, false, true, true};
        Alarm alarm4 = new Alarm();
        alarm4.title = "You are late alarm";
        alarm4.time = "07:00 AM";
        alarm4.isEnabled = true;
        alarm4.repeatingDays = new boolean[]{false, false, false, false, false, false, false};
        Alarm alarm5 = new Alarm();
        alarm5.title = "Empty";
        alarm5.time = "Empty";
        alarm5.isEnabled = true;
        alarm5.repeatingDays = new boolean[]{false, false, false, false, false, false, false};
        testAlarms.add(alarm1);
        testAlarms.add(alarm2);
        testAlarms.add(alarm3);
        testAlarms.add(alarm4);
        testAlarms.add(alarm1);
        testAlarms.add(alarm2);
        testAlarms.add(alarm3);
        testAlarms.add(alarm4);
        testAlarms.add(alarm1);
        testAlarms.add(alarm2);
        testAlarms.add(alarm3);
        testAlarms.add(alarm4);
        testAlarms.add(alarm1);
        testAlarms.add(alarm2);
        testAlarms.add(alarm3);
        testAlarms.add(alarm4);
        testAlarms.add(alarm1);
        testAlarms.add(alarm2);
        testAlarms.add(alarm3);
        testAlarms.add(alarm4);
        testAlarms.add(alarm5);
        return testAlarms;
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    class TestAsync extends AsyncTask<Void, Integer, String> {
        JSONObject weatherJson;

        protected String doInBackground(Void... arg0) {

            String url = "http://api.forecast.io/forecast/9f1f81ecb721deab510e0c1249aca1b4/" + latitude + "," + longitude + "";
            weatherJson = getJSONFromUrl(url);
            return "You are at PostExecute";
        }

        protected void onPostExecute(String result) {
            if (weatherJson != null) {
                try {
                    JSONObject currentlyJSONObj = weatherJson.getJSONObject("currently");
                    String icon = currentlyJSONObj.getString("icon");
                    String summary = currentlyJSONObj.getString("summary");
                    double tempF = currentlyJSONObj.getDouble("temperature");
                    double windSpeedMph = currentlyJSONObj.getDouble("windSpeed");
                    double tempC = round(getCelsiusFromFarenheit(tempF), 1);
                    double windSpeedMs = round(getMsFromMph(windSpeedMph), 1);
                    setWeatherImage(icon);

                    tempImageView.setImageResource(R.drawable.temp);
                    windImageView.setImageResource(R.drawable.wind);
                    if (metricSystem) {
                        windTextView.setText(windSpeedMs + "m/s");
                        tempTextView.setText(tempC + " ℃");
                        summaryTextView.setText(summary);
                    } else {
                        windTextView.setText(windSpeedMph + "mph");
                        tempTextView.setText(tempF + " °F ");
                        summaryTextView.setText(summary);

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

        double getCelsiusFromFarenheit(double temp) {
            return (temp - 32) * 0.555555556;
        }

        double getMsFromMph(double speed) {
            return speed * 0.44704;
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

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
