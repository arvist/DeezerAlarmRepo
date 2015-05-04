package com.cikoapps.deezeralarm.helpers;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


public class Location implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final Context context;
    private GoogleApiClient googleApiClient;
    private final Toolbar toolbar;
    private final RelativeLayout mainTopLayout;

    public Location(Context context, RelativeLayout mainTopLayout, Toolbar toolbar) {
        this.context = context;
        this.toolbar = toolbar;
        this.mainTopLayout = mainTopLayout;
    }

    // Lai varētu iegūt lietotāja atrašanās vietas koordinātas
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
            String lat = String.valueOf(myLastLocation.getLatitude());
            String lng = String.valueOf(myLastLocation.getLongitude());
            new WeatherDataAsync(mainTopLayout, Double.parseDouble(lat), Double.parseDouble(lng), toolbar, context).execute();
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
