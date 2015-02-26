package com.cikoapps.deezeralarm.HelperClasses;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by arvis.taurenis on 2/26/2015.
 */
public class MyLocation implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MyLocation";
    private final Context context;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private boolean metricSystem;
    private RelativeLayout mainTopLayout;

    public MyLocation(Context context, boolean metricSystem, RelativeLayout mainTopLayout) {
        this.context = context;
        this.metricSystem = metricSystem;
        this.mainTopLayout = mainTopLayout;
    }

    public synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
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
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            String lat = String.valueOf(mLastLocation.getLatitude());
            String lng = String.valueOf(mLastLocation.getLongitude());
            double latitude = Double.parseDouble(lat);
            double longitude = Double.parseDouble(lng);
            Log.e(TAG, lat + " - LAT");
            Log.e(TAG, lng + " - LNG");
            new WeatherDataAsync(mainTopLayout, true ,true, latitude, longitude, context).execute();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
