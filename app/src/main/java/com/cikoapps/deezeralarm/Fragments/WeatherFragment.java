package com.cikoapps.deezeralarm.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.DigitalClock;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.activities.SettingsActivity;
import com.cikoapps.deezeralarm.helpers.HelperClass;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

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
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings({"deprecation", "CanBeFinal", "SameParameterValue"})
@SuppressLint("ValidFragment")
public class WeatherFragment extends Fragment {
    private static final String TEMP_FAHRENHEIT = "tempF";
    private static final String WIND_KM = "windKmh";
    private static final String WIND_MI = "windMph";
    private static final String CITY = "city";
    private static final String TIME_UPDATED = "time";
    private static final String WEATHER_CODE = "weather";
    private static final String SUMMARY = "summary";
    private static final String TEMP_CELSIUS = "tempC";
    private ImageButton refreshButton;
    private TextView cityTextView;
    private TextView dateTextView;
    private ImageView weatherImageView;
    private ImageView tempImageView;
    private ImageView windImageView;
    private TextView summaryTextView;
    private TextView windTextView;
    private TextView tempTextView;
    private TextView textAgo;
    private DigitalClock digitalClock;
    private TextView timeTextView;
    private boolean windMilesBool;
    private boolean tempFBool;
    private RelativeLayout weatherLayout;
    private Toolbar toolbar;
    private Typeface robotoRegular;
    private JSONObject weatherJson;
    private double latitude;
    private double longitude;
    private Animation refreshAnimation;
    private Context context;
    private Location location;

    public WeatherFragment(double latitude, double longitude, Toolbar toolbar, Context context) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.toolbar = toolbar;
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        container.removeAllViews();
        weatherLayout = (RelativeLayout) inflater.inflate(R.layout.weather_layout, container, false);
        initializeView();
        refreshWeatherButton();
        updateWeatherDataOnRefreshTime();
        updateDisplay();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        long updateTime = sharedPreferences.getLong(TIME_UPDATED, -1);
        int selectedInterval = sharedPreferences.getInt(SettingsActivity.SELECTED_INTERVAL, -1);
        Calendar currentTime = Calendar.getInstance();
        long currentMillis = currentTime.getTimeInMillis();
        long diffMillis = currentMillis - updateTime;
        long intervalMillis = (selectedInterval*5 +5)*1000;

        if (diffMillis > intervalMillis) {
            location = new Location(context);
            location.buildGoogleApiClient();
        } else {
            setFromSharedPreferences();
        }
        return weatherLayout;
    }

    private void updateDisplay() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setFromSharedPreferences();
                            updateWeatherDataOnRefreshTime();
                        }
                    });
                }

            }
        }, 0, 1 * 60 * 1000);
    }

    private void initializeView() {
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


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        windMilesBool = preferences.getBoolean(SettingsActivity.WIND_MILES_BOOLEAN, false);
        tempFBool = preferences.getBoolean(SettingsActivity.TEMP_FAHRENHEIT_BOOLEAN, false);
        setDate();
        robotoRegular = Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf");
        refreshAnimation = AnimationUtils.loadAnimation(context, R.anim.progress_anim);
        refreshAnimation.setDuration(700);
        refreshButton.startAnimation(refreshAnimation);
        refreshAnimation.setInterpolator(new Interpolator() {
            private final int frameCount = 16;

            @Override
            public float getInterpolation(float input) {
                return (float) Math.floor(input * frameCount) / frameCount;
            }
        });
    }

    void setDate() {
        Calendar currentTime = Calendar.getInstance();
        int month = currentTime.get(Calendar.MONTH);
        int day = currentTime.get(Calendar.DAY_OF_MONTH);
        int year = currentTime.get(Calendar.YEAR);
        String monthString = HelperClass.getMonthFromInt(month);
        String date = monthString + " " + day + " , " + year;
        dateTextView.setText(date);
        dateTextView.setTypeface(robotoRegular);
    }

    private void saveWeatherToSharedPreferences(String summary, float tempC, float tempF, float windKmh, float windMph,
                                                String city, String weatherImage) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SUMMARY, summary);
        editor.putFloat(TEMP_CELSIUS, tempC);
        editor.putFloat(TEMP_FAHRENHEIT, tempF);
        editor.putFloat(WIND_KM, windKmh);
        editor.putFloat(WIND_MI, windMph);
        editor.putString(CITY, city);
        editor.putLong(TIME_UPDATED, Calendar.getInstance().getTimeInMillis());
        editor.putString(WEATHER_CODE, weatherImage);
        editor.apply();
    }

    private void refreshWeatherButton() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new HelperClass(context).haveNetworkConnection()) {
                    if (new HelperClass(context).isLocationEnabled()) {
                        refreshButton.startAnimation(refreshAnimation);

                        if (location != null) {
                            location.reconnectGoogleApiClient();
                        } else {
                            location = new WeatherFragment.Location(context);
                            location.buildGoogleApiClient();
                        }
                    } else {
                        Toast.makeText(context, "Couldn't determine current location", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "No network connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void setFromSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar calendar = Calendar.getInstance();
        long timeNow = calendar.getTimeInMillis();
        boolean windMilesBool = sharedPreferences.getBoolean(SettingsActivity.WIND_MILES_BOOLEAN, true);
        boolean tempFBool = sharedPreferences.getBoolean(SettingsActivity.TEMP_FAHRENHEIT_BOOLEAN, false);
        String summary = sharedPreferences.getString(SUMMARY, "");
        float tempC = sharedPreferences.getFloat(TEMP_CELSIUS, -100);
        float tempF = sharedPreferences.getFloat(TEMP_FAHRENHEIT, -100);
        float windKmh = sharedPreferences.getFloat(WIND_KM, -1);
        float windMph = sharedPreferences.getFloat(WIND_MI, -1);
        String city = sharedPreferences.getString(CITY, "");
        long updateTime = sharedPreferences.getLong(TIME_UPDATED, -1);

        long timeFromUpdateLong = timeNow - updateTime;
        int minutes = (int) (timeFromUpdateLong / 60000);
        if (minutes < 60) {
            if (minutes == 1) {
                timeTextView.setText(minutes + " minute ");

            } else {
                timeTextView.setText(minutes + " minutes ");
            }
            textAgo.setText("ago");
        } else if (minutes > 60 && minutes < 1439) {
            int hours = minutes / 60;
            if (hours == 1) {
                timeTextView.setText(hours + " hour ");
            } else {
                timeTextView.setText(hours + " hours ");
            }
            textAgo.setText("ago");
        } else if (minutes > 1439) {
            int days = minutes / 1440;
            if (days == 1) {
                timeTextView.setText(days + " day ");
            } else {
                if (days > 366) {
                    timeTextView.setText(" Never ");
                    textAgo.setText("");
                } else {
                    timeTextView.setText(days + " days ");
                    textAgo.setText("ago");
                }
            }
        }
        cityTextView.setText(city);

        if (windMilesBool) {
            if (windMph == -1) {
                windTextView.setText("?" + " mph");
            } else {
                windTextView.setText(windMph + " mph");
            }
        } else {
            if (windKmh == -1) {
                windTextView.setText("?" + " kph");
            } else {
                windTextView.setText(windKmh + " kph");
            }
        }
        if (tempFBool) {
            if (tempF == -100) {
                tempTextView.setText("?" + " ℉");
            } else {
                tempTextView.setText(tempF + " ℉");
            }

        } else {
            if (tempC == -100) {
                tempTextView.setText("?" + " ℃");
            } else {
                tempTextView.setText(tempC + " ℃");
            }
        }

        summaryTextView.setText(summary);
        try {
            String weatherImage = sharedPreferences.getString(WEATHER_CODE, "");
            setWeatherImage(weatherImage);
        } catch (Exception e) {
            //Log.e(TAG, e.getMessage());
        }
        refreshAnimation.cancel();
    }

    void setWeatherUserInterfaceLight() {
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

    void setWeatherUserInterfaceDark() {
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

    void setWeatherImage(String icon) {
        Calendar calendar = Calendar.getInstance();
        int weatherCode = Integer.parseInt(icon);

        if (weatherCode == 248 || weatherCode == 260 || weatherCode == 122 || weatherCode == 143) {
            setWeatherUserInterfaceDark();
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
                setWeatherUserInterfaceDark();
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
                setWeatherUserInterfaceLight();
                weatherImageView.setImageResource(R.drawable.ic_clear_sky_night);
                weatherLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroundClearSkyNight));
                summaryTextView.setText("Clear");
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
                setWeatherUserInterfaceDark();
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
                setWeatherUserInterfaceLight();
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
                setWeatherUserInterfaceDark();
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
                setWeatherUserInterfaceLight();
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
                setWeatherUserInterfaceDark();
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
                setWeatherUserInterfaceLight();
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
                setWeatherUserInterfaceDark();
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
                setWeatherUserInterfaceLight();
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
                setWeatherUserInterfaceDark();
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
                setWeatherUserInterfaceLight();
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
                setWeatherUserInterfaceDark();
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
                setWeatherUserInterfaceLight();
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
                setWeatherUserInterfaceDark();
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
                setWeatherUserInterfaceLight();
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
                setWeatherUserInterfaceDark();
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
                setWeatherUserInterfaceLight();
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
                setWeatherUserInterfaceDark();
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
                setWeatherUserInterfaceLight();
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
                setWeatherUserInterfaceDark();
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
                setWeatherUserInterfaceLight();
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
                setWeatherUserInterfaceDark();
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
                setWeatherUserInterfaceLight();
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
                setWeatherUserInterfaceDark();
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
                setWeatherUserInterfaceLight();
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
                setWeatherUserInterfaceDark();
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
                setWeatherUserInterfaceLight();
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
                setWeatherUserInterfaceDark();
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
                setWeatherUserInterfaceLight();
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

    private void updateWeatherDataOnRefreshTime() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int refreshTime = preferences.getInt(SettingsActivity.SELECTED_INTERVAL, 1);
        if (refreshTime != 12) {
            long lastUpdateTimeMillis = preferences.getLong(TIME_UPDATED, 0);
            boolean onlyWiFiUpdate = preferences.getBoolean(SettingsActivity.ONLY_WIFI_SELECTED, false);
            if ((onlyWiFiUpdate && new HelperClass(context).isWifiConnected()) || !onlyWiFiUpdate) {
                Calendar deleteCalendar = Calendar.getInstance();
                deleteCalendar.setTimeInMillis(lastUpdateTimeMillis);
                int milliSecondsRefreshTime = (refreshTime + 1) * 5 * 60000;
                Calendar calendar = Calendar.getInstance();
                long currentMillis = calendar.getTimeInMillis();
                if ((currentMillis - lastUpdateTimeMillis) > milliSecondsRefreshTime) {
                    location = new Location(context);
                    location.buildGoogleApiClient();
                }
            }
        }
    }

    private class Location implements
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        private final Context context;
        private GoogleApiClient googleApiClient;


        public Location(Context context) {
            this.context = context;
        }

        public synchronized void buildGoogleApiClient() {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        }

        public synchronized void reconnectGoogleApiClient() {
            googleApiClient.disconnect();
            googleApiClient.connect();
        }

        @Override
        public void onConnected(Bundle bundle) {
            android.location.Location myLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    googleApiClient);
            if (myLastLocation != null) {
                latitude = (myLastLocation.getLatitude());
                longitude = (myLastLocation.getLongitude());
                new WeatherDataAsync().execute();
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            reconnectGoogleApiClient();
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            reconnectGoogleApiClient();
        }
    }

    private class WeatherDataAsync extends AsyncTask<Void, Integer, String> {

        protected String doInBackground(Void... arg0) {
            final String API_KEY = "9427f818e265ae4296ab81b2d885d";
            latitude = HelperClass.round(latitude, 3);
            longitude = HelperClass.round(longitude, 3);
            String url = "http://api2.worldweatheronline.com/premium/v1/weather.ashx?q=" +
                    "" + latitude + "%2C" + longitude + "" +
                    "&format=json&num_of_days=0&fx=no&cc=yes&mca=no&includelocation=yes&showlocaltime=no&key=" + API_KEY + "";
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

                    cityTextView.setText(city);
                    summaryTextView.setText(summary);
                    summaryTextView.setMaxLines(2);
                    setWeatherImage(weatherCode);
                    timeTextView.setText("0 minutes ");
                    textAgo.setText("ago");


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
            refreshAnimation.cancel();
        }

        JSONObject getJSONFromUrl(String url) {
            InputStream is = null;
            JSONObject jsonObject = null;
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
                jsonObject = new JSONObject(json);
            } catch (final Exception e) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, "Error connecting to weather data provider", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return jsonObject;
        }
    }
}
