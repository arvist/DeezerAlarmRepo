package com.cikoapps.deezeralarm.HelperClasses;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.cikoapps.deezeralarm.R;

import java.util.Calendar;

public class WeatherIconData {

    private static final String TAG = "WeatherIconData" ;

    public static void setWeatherImage(String icon,ImageView im) {
        Calendar calendar = Calendar.getInstance();
        Context context;
        Log.e(TAG,icon);
        int weatherCode = Integer.parseInt(icon);
        Log.e(TAG, weatherCode + " " + calendar.getTime());
        if( weatherCode == 122) {
            im.setImageResource(R.drawable.ic_mist);
        } else if (weatherCode == 248 || weatherCode == 260 ) {
            im.setImageResource(R.drawable.ic_fog);
        } else if (weatherCode == 113) {
            if(calendar.get(Calendar.HOUR_OF_DAY) < 18){
                im.setImageResource(R.drawable.ic_sunny);
            } else {
                im.setImageResource(R.drawable.ic_clear_sky_night);
            }
        } else if (weatherCode == 116){
            if(calendar.get(Calendar.HOUR_OF_DAY) < 18){
                im.setImageResource(R.drawable.ic_sunny_intervals);
            } else {
                im.setImageResource(R.drawable.ic_cloudy_night);
            }
        } else if (weatherCode == 119) {
            if(calendar.get(Calendar.HOUR_OF_DAY) < 18){
                im.setImageResource(R.drawable.ic_white_cloud);
            } else {
                im.setImageResource(R.drawable.ic_black_low_cloud);
            }
        } else if (weatherCode == 389 ) {
            if(calendar.get(Calendar.HOUR_OF_DAY) < 18){
                im.setImageResource(R.drawable.ic_thunderstorms);
            } else {
                im.setImageResource(R.drawable.ic_thunderstorms_night);
            }
        } else if (weatherCode == 176 || weatherCode == 263 || weatherCode == 353 ) {
            if(calendar.get(Calendar.HOUR_OF_DAY) < 18){
                im.setImageResource(R.drawable.ic_light_rain_showers);
            } else {
                im.setImageResource(R.drawable.ic_light_rain_showers_night);
            }
        } else if (weatherCode == 299 || weatherCode == 305 || weatherCode == 356 ) {
            if(calendar.get(Calendar.HOUR_OF_DAY) < 18){
                im.setImageResource(R.drawable.ic_heavy_rain_showers);
            } else {
                im.setImageResource(R.drawable.ic_heavy_rain_showers_night);
            }
        } else if (weatherCode == 323 || weatherCode == 326 || weatherCode == 368 ) {
            if(calendar.get(Calendar.HOUR_OF_DAY) < 18){
                im.setImageResource(R.drawable.ic_light_snow_showers);
            } else {
                im.setImageResource(R.drawable.ic_light_snow_showers_night);
            }
        } else if (weatherCode == 299 || weatherCode == 305 || weatherCode == 356 ) {
            if(calendar.get(Calendar.HOUR_OF_DAY) < 18){
                im.setImageResource(R.drawable.ic_heavy_rain_showers);
            } else {
                im.setImageResource(R.drawable.ic_heavy_rain_showers_night);
            }
        } else if (weatherCode == 335 || weatherCode == 371 ) {
            if(calendar.get(Calendar.HOUR_OF_DAY) < 18){
                im.setImageResource(R.drawable.ic_heavy_snow_showers);
            } else {
                im.setImageResource(R.drawable.ic_heavy_snow_showers_night);
            }
        } else if (weatherCode == 179 || weatherCode == 362 || weatherCode == 365 || weatherCode == 374 ) {
            if(calendar.get(Calendar.HOUR_OF_DAY) < 18){
                im.setImageResource(R.drawable.ic_sleet_showers);
            } else {
                im.setImageResource(R.drawable.ic_sleet_showers_night);
            }
        } else if (weatherCode == 200 || weatherCode == 386 || weatherCode == 392 ) {
            if(calendar.get(Calendar.HOUR_OF_DAY) < 18){
                im.setImageResource(R.drawable.ic_thundery_showers);
            } else {
                im.setImageResource(R.drawable.ic_thundery_showers_night);
            }
        } else if (weatherCode == 266 || weatherCode == 293 || weatherCode == 296 ) {
            if(calendar.get(Calendar.HOUR_OF_DAY) < 18){
                im.setImageResource(R.drawable.ic_cloudy_with_light_rain);
            } else {
                im.setImageResource(R.drawable.ic_cloudy_with_heavy_rain_night);
            }
        } else if (weatherCode == 302 || weatherCode == 308 || weatherCode == 359 ) {
            if(calendar.get(Calendar.HOUR_OF_DAY) < 18){
                im.setImageResource(R.drawable.ic_cloudy_with_heavy_rain);
            } else {
                im.setImageResource(R.drawable.ic_cloudy_with_heavy_rain_night);
            }
        } else if (weatherCode == 227 || weatherCode == 320 || weatherCode == 395 ) {
            if(calendar.get(Calendar.HOUR_OF_DAY) < 18){
                im.setImageResource(R.drawable.ic_cloudy_with_light_snow);
            } else {
                im.setImageResource(R.drawable.ic_cloudy_with_light_snow_night);
            }
        } else if(weatherCode == 389){
            if(calendar.get(Calendar.HOUR_OF_DAY) < 18){
                im.setImageResource(R.drawable.ic_thunderstorms);
            } else {
                im.setImageResource(R.drawable.ic_thunderstorms_night);
            }
        } else if (weatherCode == 230 || weatherCode == 329 || weatherCode == 332 || weatherCode == 338 ) {
            if(calendar.get(Calendar.HOUR_OF_DAY) < 18){
                im.setImageResource(R.drawable.ic_cloudy_with_heavy_rain);
            } else {
                im.setImageResource(R.drawable.ic_cloudy_with_heavy_rain_night);
            }
        } else if (weatherCode == 182 || weatherCode == 185 || weatherCode == 281 || weatherCode == 284 || weatherCode == 311
                || weatherCode == 311 || weatherCode == 314 || weatherCode == 317 || weatherCode == 350 || weatherCode == 377) {
            if(calendar.get(Calendar.HOUR_OF_DAY) < 18){
                im.setImageResource(R.drawable.ic_cloudy_with_sleet);
            } else {
                im.setImageResource(R.drawable.ic_cloudy_with_sleet_night);
            }
        }

    }
}
