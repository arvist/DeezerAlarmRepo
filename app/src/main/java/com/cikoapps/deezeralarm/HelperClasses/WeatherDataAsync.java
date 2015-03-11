package com.cikoapps.deezeralarm.HelperClasses;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
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
    TextView textAgo;
    DigitalClock digitalClock;

    double latitude;
    double longitude;
    HelperClass helperClass;

    TextView timeTextView;
    private String TAG = "WeatherDataAsync.java";

    private Animation a;
    boolean windMilesBool;
    boolean tempFBool;
    boolean clock12Hours;
    RelativeLayout weatherLayout;
    Context context;
    Toolbar toolbar;
    private Typeface robotoRegular;

    public WeatherDataAsync(RelativeLayout weatherLayout, double latitude, double longitude, Toolbar toolbar, Context context) {

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
        digitalClock = (DigitalClock) weatherLayout.findViewById(R.id.digitalClock);
        textAgo = (TextView) weatherLayout.findViewById(R.id.textAgo);
        this.weatherLayout = weatherLayout;
        this.context = context;
        this.toolbar = toolbar;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        windMilesBool = preferences.getBoolean("windMilesBool", false);
        tempFBool = preferences.getBoolean("tempFBool", false);
        clock12Hours = preferences.getBoolean("clock12HoursBool", false);

        this.latitude = latitude;
        this.longitude = longitude;
        setDate();

        this.context = context;
        robotoRegular = Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf");
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
        int month = currentTime.get(Calendar.MONTH);
        int day = currentTime.get(Calendar.DAY_OF_MONTH);
        int year = currentTime.get(Calendar.YEAR);
        String monthString = HelperClass.getMonthFromInt(month);
        String date = monthString + " " + day + " , " + year;
        dateTextView.setText(date);
        dateTextView.setTypeface(robotoRegular);
    }

    protected String doInBackground(Void... arg0) {

        Log.e(TAG, "doInBackground Weather");
        final String apiKey = "0a6bad312fb111db3c658e0250965";
        latitude = HelperClass.round(latitude, 3);
        longitude = HelperClass.round(longitude, 3);
        String url = "http://api.worldweatheronline.com/premium/v1/weather.ashx?q=" +
                "" + latitude + "%2C" + longitude + "" +
                "&format=json&num_of_days=0&fx=no&cc=yes&mca=no&includelocation=yes&show_comments=no&showlocaltime=no&" +
                "key=" + apiKey + "";
        Log.e(TAG, url);
        weatherJson = getJSONFromUrl(url);
        return "You are at PostExecute";
    }

    protected void onPostExecute(String result) {
        if (weatherJson != null) {
            try {
                JSONObject dataJsonObj = weatherJson.getJSONObject("data");
                JSONObject currentConditionJsonObj = dataJsonObj.getJSONArray("current_condition").getJSONObject(0);
                JSONObject weatherDesc = currentConditionJsonObj.getJSONArray("weatherDesc").getJSONObject(0);
                String summary = weatherDesc.getString("value");
                String tempC = currentConditionJsonObj.getString("temp_C");
                String weatherCode = currentConditionJsonObj.getString("weatherCode");
                String tempF = currentConditionJsonObj.getString("temp_F");
                String windSpeedMph = currentConditionJsonObj.getString("windspeedMiles");
                String windSpeedKmph = currentConditionJsonObj.getString("windspeedKmph");
                String city = dataJsonObj.getJSONArray("nearest_area").getJSONObject(0).getJSONArray("areaName").getJSONObject(0).getString("value");
                setWeatherImage(weatherCode);
                cityTextView.setText(city);
                summaryTextView.setText(summary);
                summaryTextView.setMaxLines(2);
                timeTextView.setText("0 minutes ");
                if (windMilesBool) {
                    windTextView.setText(Float.parseFloat(windSpeedMph) + " mph");
                } else {
                    windTextView.setText(Float.parseFloat(windSpeedKmph) + " kph");
                }
                if (tempFBool) {
                    tempTextView.setText(Float.parseFloat(tempF) + " ℉");
                } else {
                    tempTextView.setText(Float.parseFloat(tempC) + " ℃");
                }
                saveWeatherToSharedPreferences(summary, Float.parseFloat(tempC), Float.parseFloat(tempF)
                        , Float.parseFloat(windSpeedKmph), Float.parseFloat(windSpeedMph), city, weatherCode);

            } catch (JSONException ignored) {
            }
        }
        a.cancel();
        a = null;
    }


    public JSONObject getJSONFromUrl(String url) {
        InputStream is = null;
        JSONObject jObj = null;
        String json;
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
                                                String city, String weatherImage) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("summary", summary);
        editor.putFloat("tempC", tempC);
        editor.putFloat("tempF", tempF);
        editor.putFloat("windKmh", windKmh);
        editor.putFloat("windMph", windMph);
        editor.putString("city", city);
        editor.putLong("time", Calendar.getInstance().getTimeInMillis());
        editor.putString("weather", weatherImage);
        editor.apply();
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
        try {
            String weatherImage = sharedPreferences.getString("weather", "");
            setWeatherImage(weatherImage);
        } catch (Exception ignored) {
        }
        long timeFromUpdateLong = timeNow - updateTime;
        int minutes = (int) (timeFromUpdateLong / 60000);
        if (minutes < 60) {
            timeTextView.setText(minutes + " minutes ");
        } else if (minutes > 60 && minutes < 1439) {
            int hours = minutes / 60;
            timeTextView.setText(hours + " hours ");
        } else if (minutes > 1439) {
            int days = minutes / 1440;
            timeTextView.setText(days + " days ");
        }
        cityTextView.setText(city);

        if (windMilesBool) {
            windTextView.setText(windMph + " mph");
        } else {
            windTextView.setText(windKmh + " kph");
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

    public void setTextColorLight() {
        dateTextView.setTextColor(context.getResources().getColor(R.color.colorWhite));
        summaryTextView.setTextColor(context.getResources().getColor(R.color.colorWhite));
        windTextView.setTextColor(context.getResources().getColor(R.color.colorWhite));
        tempTextView.setTextColor(context.getResources().getColor(R.color.colorWhite));
        timeTextView.setTextColor(context.getResources().getColor(R.color.colorWhite));
        cityTextView.setTextColor(context.getResources().getColor(R.color.colorWhite));
        digitalClock.setTextColor(context.getResources().getColor(R.color.colorWhite));
        textAgo.setTextColor(context.getResources().getColor(R.color.colorWhite));
        refreshButton.setImageResource(R.drawable.ic_action_cached_white);
    }

    public void setTextColorDark() {
        dateTextView.setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
        summaryTextView.setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
        windTextView.setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
        tempTextView.setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
        timeTextView.setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
        cityTextView.setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
        digitalClock.setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
        textAgo.setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
        refreshButton.setImageResource(R.drawable.ic_action_cached);
    }

    public void setWeatherImage(String icon) {
        Calendar calendar = Calendar.getInstance();
        int weatherCode = Integer.parseInt(icon);
        if (weatherCode == 248 || weatherCode == 260 || weatherCode == 122 || weatherCode == 143) {
            setTextColorDark();
            weatherImageView.setImageResource(R.drawable.ic_fog);
            weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundMist));
            if (toolbar != null) {
                toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundMist));
                ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
                ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings_dark);
                windImageView.setImageResource(R.drawable.ic_wind_dark);
                tempImageView.setImageResource(R.drawable.ic_temperature_dark);
            }
        } else if (weatherCode == 113) {
            if (calendar.get(Calendar.HOUR_OF_DAY) < 18 && calendar.get(Calendar.HOUR_OF_DAY) > 6) {
                setTextColorDark();
                weatherImageView.setImageResource(R.drawable.ic_sunny);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundSun));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundSun));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings_dark);
                    windImageView.setImageResource(R.drawable.ic_wind_dark);
                    tempImageView.setImageResource(R.drawable.ic_temperature_dark);
                }

            } else {
                setTextColorLight();
                weatherImageView.setImageResource(R.drawable.ic_clear_sky_night);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundClearSkyNight));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundClearSkyNight));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorWhite));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings);
                    windImageView.setImageResource(R.drawable.ic_wind_light);
                    tempImageView.setImageResource(R.drawable.ic_temperature_light);

                }
            }
        } else if (weatherCode == 116) {
            if (calendar.get(Calendar.HOUR_OF_DAY) < 18 && calendar.get(Calendar.HOUR_OF_DAY) > 6) {
                setTextColorDark();
                weatherImageView.setImageResource(R.drawable.ic_sunny_intervals);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundSun));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundSun));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings_dark);
                    windImageView.setImageResource(R.drawable.ic_wind_dark);
                    tempImageView.setImageResource(R.drawable.ic_temperature_dark);
                }

            } else {
                setTextColorLight();
                weatherImageView.setImageResource(R.drawable.ic_cloudy_night);
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundCloudyNight));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorWhite));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings);
                    windImageView.setImageResource(R.drawable.ic_wind_light);
                    tempImageView.setImageResource(R.drawable.ic_temperature_light);

                }
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundCloudyNight));
            }
        } else if (weatherCode == 119) {
            if (calendar.get(Calendar.HOUR_OF_DAY) < 18 && calendar.get(Calendar.HOUR_OF_DAY) > 6) {
                setTextColorDark();
                weatherImageView.setImageResource(R.drawable.ic_white_cloud);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundSun));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundSun));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings_dark);
                    windImageView.setImageResource(R.drawable.ic_wind_dark);
                    tempImageView.setImageResource(R.drawable.ic_temperature_dark);
                }

            } else {
                setTextColorLight();
                weatherImageView.setImageResource(R.drawable.ic_black_low_cloud);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundBlackCloudLow));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundBlackCloudLow));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorWhite));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings);
                    windImageView.setImageResource(R.drawable.ic_wind_light);
                    tempImageView.setImageResource(R.drawable.ic_temperature_light);

                }
            }
        } else if (weatherCode == 389) {
            if (calendar.get(Calendar.HOUR_OF_DAY) < 18 && calendar.get(Calendar.HOUR_OF_DAY) > 6) {
                setTextColorDark();
                weatherImageView.setImageResource(R.drawable.ic_thunderstorms);
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundBlackCloudLow));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings_dark);
                    windImageView.setImageResource(R.drawable.ic_wind_dark);
                    tempImageView.setImageResource(R.drawable.ic_temperature_dark);
                }
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundBlackCloudLow));
            } else {
                setTextColorLight();
                weatherImageView.setImageResource(R.drawable.ic_thunderstorms_night);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundThunderStormsNight));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundThunderStormsNight));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorWhite));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings);
                    windImageView.setImageResource(R.drawable.ic_wind_light);
                    tempImageView.setImageResource(R.drawable.ic_temperature_light);

                }
            }
        } else if (weatherCode == 176 || weatherCode == 263 || weatherCode == 353) {
            if (calendar.get(Calendar.HOUR_OF_DAY) < 18 && calendar.get(Calendar.HOUR_OF_DAY) > 6) {
                setTextColorDark();
                weatherImageView.setImageResource(R.drawable.ic_light_rain_showers);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundSun));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundSun));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings_dark);
                    windImageView.setImageResource(R.drawable.ic_wind_dark);
                    tempImageView.setImageResource(R.drawable.ic_temperature_dark);
                }
            } else {
                setTextColorLight();
                weatherImageView.setImageResource(R.drawable.ic_light_rain_showers_night);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundClearSkyNight));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundClearSkyNight));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorWhite));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings);
                    windImageView.setImageResource(R.drawable.ic_wind_light);
                    tempImageView.setImageResource(R.drawable.ic_temperature_light);

                }
            }
        } else if (weatherCode == 299 || weatherCode == 305 || weatherCode == 356) {
            if (calendar.get(Calendar.HOUR_OF_DAY) < 18 && calendar.get(Calendar.HOUR_OF_DAY) > 6) {
                setTextColorDark();
                weatherImageView.setImageResource(R.drawable.ic_heavy_rain_showers);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundSun));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundSun));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings_dark);
                    windImageView.setImageResource(R.drawable.ic_wind_dark);
                    tempImageView.setImageResource(R.drawable.ic_temperature_dark);
                }
            } else {
                setTextColorLight();
                weatherImageView.setImageResource(R.drawable.ic_heavy_rain_showers_night);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundClearSkyNight));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundClearSkyNight));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorWhite));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings);
                    windImageView.setImageResource(R.drawable.ic_wind_light);
                    tempImageView.setImageResource(R.drawable.ic_temperature_light);

                }
            }
        } else if (weatherCode == 323 || weatherCode == 326 || weatherCode == 368) {
            if (calendar.get(Calendar.HOUR_OF_DAY) < 18 && calendar.get(Calendar.HOUR_OF_DAY) > 6) {
                setTextColorDark();
                weatherImageView.setImageResource(R.drawable.ic_light_snow_showers);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundSun));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundSun));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings_dark);
                    windImageView.setImageResource(R.drawable.ic_wind_dark);
                    tempImageView.setImageResource(R.drawable.ic_temperature_dark);
                }
            } else {
                setTextColorLight();
                weatherImageView.setImageResource(R.drawable.ic_light_snow_showers_night);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundClearSkyNight));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundClearSkyNight));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorWhite));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings);
                    windImageView.setImageResource(R.drawable.ic_wind_light);
                    tempImageView.setImageResource(R.drawable.ic_temperature_light);

                }
            }
        } else if (weatherCode == 335 || weatherCode == 371) {
            if (calendar.get(Calendar.HOUR_OF_DAY) < 18 && calendar.get(Calendar.HOUR_OF_DAY) > 6) {
                setTextColorDark();
                weatherImageView.setImageResource(R.drawable.ic_heavy_snow_showers);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundSun));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundSun));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings_dark);
                    windImageView.setImageResource(R.drawable.ic_wind_dark);
                    tempImageView.setImageResource(R.drawable.ic_temperature_dark);

                }
            } else {
                setTextColorLight();
                weatherImageView.setImageResource(R.drawable.ic_heavy_snow_showers_night);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundClearSkyNight));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundClearSkyNight));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorWhite));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings);
                    windImageView.setImageResource(R.drawable.ic_wind_light);
                    tempImageView.setImageResource(R.drawable.ic_temperature_light);

                }
            }
        } else if (weatherCode == 179 || weatherCode == 362 || weatherCode == 365 || weatherCode == 374) {
            if (calendar.get(Calendar.HOUR_OF_DAY) < 18 && calendar.get(Calendar.HOUR_OF_DAY) > 6) {
                setTextColorDark();
                weatherImageView.setImageResource(R.drawable.ic_sleet_showers);
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundSun));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings_dark);
                    windImageView.setImageResource(R.drawable.ic_wind_dark);
                    tempImageView.setImageResource(R.drawable.ic_temperature_dark);

                }
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundSun));
            } else {
                setTextColorLight();
                weatherImageView.setImageResource(R.drawable.ic_sleet_showers_night);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundClearSkyNight));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundClearSkyNight));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorWhite));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings);
                    windImageView.setImageResource(R.drawable.ic_wind_light);
                    tempImageView.setImageResource(R.drawable.ic_temperature_light);


                }
            }
        } else if (weatherCode == 200 || weatherCode == 386 || weatherCode == 392) {
            if (calendar.get(Calendar.HOUR_OF_DAY) < 18 && calendar.get(Calendar.HOUR_OF_DAY) > 6) {
                setTextColorDark();
                weatherImageView.setImageResource(R.drawable.ic_thundery_showers);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundSun));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundSun));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings_dark);
                    windImageView.setImageResource(R.drawable.ic_wind_dark);
                    tempImageView.setImageResource(R.drawable.ic_temperature_dark);

                }
            } else {
                setTextColorLight();
                weatherImageView.setImageResource(R.drawable.ic_thundery_showers_night);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundClearSkyNight));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundClearSkyNight));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorWhite));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings);
                    windImageView.setImageResource(R.drawable.ic_wind_light);
                    tempImageView.setImageResource(R.drawable.ic_temperature_light);


                }
            }
        } else if (weatherCode == 266 || weatherCode == 293 || weatherCode == 296) {
            if (calendar.get(Calendar.HOUR_OF_DAY) < 18 && calendar.get(Calendar.HOUR_OF_DAY) > 6) {
                setTextColorDark();
                weatherImageView.setImageResource(R.drawable.ic_cloudy_with_light_rain);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundBlackCloudLow));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundBlackCloudLow));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings_dark);
                    windImageView.setImageResource(R.drawable.ic_wind_dark);
                    tempImageView.setImageResource(R.drawable.ic_temperature_dark);

                }
            } else {
                setTextColorLight();
                weatherImageView.setImageResource(R.drawable.ic_cloudy_with_light_rain_night);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundThunderStormsNight));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundThunderStormsNight));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorWhite));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings);
                    windImageView.setImageResource(R.drawable.ic_wind_light);
                    tempImageView.setImageResource(R.drawable.ic_temperature_light);


                }
            }
        } else if (weatherCode == 302 || weatherCode == 308 || weatherCode == 359) {
            if (calendar.get(Calendar.HOUR_OF_DAY) < 18 && calendar.get(Calendar.HOUR_OF_DAY) > 6) {
                setTextColorDark();
                weatherImageView.setImageResource(R.drawable.ic_cloudy_with_heavy_rain);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundBlackCloudLow));
                if (toolbar != null) {

                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundBlackCloudLow));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings_dark);
                    windImageView.setImageResource(R.drawable.ic_wind_dark);
                    tempImageView.setImageResource(R.drawable.ic_temperature_dark);

                }
            } else {
                setTextColorLight();
                weatherImageView.setImageResource(R.drawable.ic_cloudy_with_heavy_rain_night);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundThunderStormsNight));
                if (toolbar != null) {
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorWhite));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings);
                    windImageView.setImageResource(R.drawable.ic_wind_light);
                    tempImageView.setImageResource(R.drawable.ic_temperature_light);


                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundThunderStormsNight));
                }
            }
        } else if (weatherCode == 227 || weatherCode == 320 || weatherCode == 395) {
            if (calendar.get(Calendar.HOUR_OF_DAY) < 18 && calendar.get(Calendar.HOUR_OF_DAY) > 6) {
                setTextColorDark();
                weatherImageView.setImageResource(R.drawable.ic_cloudy_with_light_snow);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundBlackCloudLow));
                if (toolbar != null) {
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings_dark);
                    windImageView.setImageResource(R.drawable.ic_wind_dark);
                    tempImageView.setImageResource(R.drawable.ic_temperature_dark);

                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundBlackCloudLow));
                }
            } else {
                setTextColorLight();
                weatherImageView.setImageResource(R.drawable.ic_cloudy_with_light_snow_night);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundThunderStormsNight));
                if (toolbar != null) {
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorWhite));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings);
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundThunderStormsNight));
                    windImageView.setImageResource(R.drawable.ic_wind_light);
                    tempImageView.setImageResource(R.drawable.ic_temperature_light);
                }
            }

        } else if (weatherCode == 230 || weatherCode == 329 || weatherCode == 332 || weatherCode == 338) {
            if (calendar.get(Calendar.HOUR_OF_DAY) < 18 && calendar.get(Calendar.HOUR_OF_DAY) > 6) {
                setTextColorDark();
                weatherImageView.setImageResource(R.drawable.ic_cloudy_with_heavy_snow);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundBlackCloudLow));
                if (toolbar != null) {
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings_dark);
                    windImageView.setImageResource(R.drawable.ic_wind_dark);
                    tempImageView.setImageResource(R.drawable.ic_temperature_dark);

                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundBlackCloudLow));
                }
            } else {
                setTextColorLight();
                weatherImageView.setImageResource(R.drawable.ic_cloudy_with_heavy_snow_night);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundThunderStormsNight));
                if (toolbar != null) {
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorWhite));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings);
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundThunderStormsNight));
                    windImageView.setImageResource(R.drawable.ic_wind_light);
                    tempImageView.setImageResource(R.drawable.ic_temperature_light);
                }
            }
        } else if (weatherCode == 182 || weatherCode == 185 || weatherCode == 281 || weatherCode == 284 ||
                weatherCode == 311 || weatherCode == 314 || weatherCode == 317 || weatherCode == 350 || weatherCode == 377) {
            if (calendar.get(Calendar.HOUR_OF_DAY) < 18 && calendar.get(Calendar.HOUR_OF_DAY) > 6) {
                setTextColorDark();
                weatherImageView.setImageResource(R.drawable.ic_cloudy_with_sleet);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundBlackCloudLow));
                if (toolbar != null) {
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings_dark);
                    windImageView.setImageResource(R.drawable.ic_wind_dark);
                    tempImageView.setImageResource(R.drawable.ic_temperature_dark);
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundBlackCloudLow));
                }
            } else {
                setTextColorLight();
                weatherImageView.setImageResource(R.drawable.ic_cloudy_with_sleet_night);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundThunderStormsNight));
                if (toolbar != null) {
                    toolbar.setBackgroundColor(context.getResources().getColor(R.color.backgroundThunderStormsNight));
                    ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTextColor(context.getResources().getColor(R.color.colorWhite));
                    ((ImageButton) toolbar.findViewById(R.id.app_bar_settings)).setImageResource(R.drawable.ic_action_settings);
                    windImageView.setImageResource(R.drawable.ic_wind_light);
                    tempImageView.setImageResource(R.drawable.ic_temperature_light);
                }
            }
        }

    }

}

