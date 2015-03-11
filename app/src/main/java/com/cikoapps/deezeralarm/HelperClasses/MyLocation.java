package com.cikoapps.deezeralarm.HelperClasses;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


public class MyLocation implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MyLocation";
    private final Context context;
    private GoogleApiClient mGoogleApiClient;
    private Toolbar toolbar;
    private RelativeLayout mainTopLayout;

    public MyLocation(Context context, RelativeLayout mainTopLayout, Toolbar toolbar) {
        this.context = context;
        this.toolbar = toolbar;
        this.mainTopLayout = mainTopLayout;
    }

    public synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public synchronized void reconnectGoogleApiClient() {
        mGoogleApiClient.disconnect();
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            String lat = String.valueOf(mLastLocation.getLatitude());
            String lng = String.valueOf(mLastLocation.getLongitude());
            double latitude = Double.parseDouble(lat);
            double longitude = Double.parseDouble(lng);
            Log.e(TAG, lat + " - LAT");
            Log.e(TAG, lng + " - LNG");
            new WeatherDataAsync(mainTopLayout, latitude, longitude, toolbar, context).execute();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");
    }
}
