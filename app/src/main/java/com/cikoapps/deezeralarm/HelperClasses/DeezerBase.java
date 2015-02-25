package com.cikoapps.deezeralarm.HelperClasses;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;

public class DeezerBase extends ActionBarActivity {

    private static final String TAG = "DeezerBase.java";
    public DeezerConnect deezerConnect;
    SessionStore sessionStore;
    Context context;

    public static final String SAMPLE_APP_ID = "151831";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        deezerConnect = new DeezerConnect(getApplication(), SAMPLE_APP_ID);
        sessionStore = new SessionStore();

        long accessExpires = deezerConnect.getAccessExpires();
        Log.e(TAG, "Access Expires in " + accessExpires + " milliseconds");
        Log.e(TAG, "Session is valid " + deezerConnect.isSessionValid());
        if (!deezerConnect.isSessionValid()) {
            loginDeezer();
        }


    }

    public void loginDeezer() {
        String[] permissions = new String[]{
                Permissions.BASIC_ACCESS,
                Permissions.MANAGE_LIBRARY,
                Permissions.LISTENING_HISTORY};

        DialogListener listener = new DialogListener() {
            public void onComplete(Bundle values) {
                sessionStore.save(deezerConnect, getApplication());
                deezerConnect.getRadioToken();
                deezerConnect.getAccessToken();
            }

            public void onCancel() {
            }

            public void onException(Exception e) {
            }
        };
        if (sessionStore.restore(deezerConnect, this)) {
            if (deezerConnect.isSessionValid()) {
                Log.e(TAG, "Access Expires in " + deezerConnect.getAccessExpires() + " milliseconds");
                Log.e(TAG, "Session is valid " + deezerConnect.isSessionValid());
                deezerConnect.getRadioToken();
            }
        } else {
            deezerConnect.authorize(this, permissions, listener);
            Log.e(TAG, "Access Expires in " + deezerConnect.getAccessExpires() + " milliseconds");
            Log.e(TAG, "Session is valid " + deezerConnect.isSessionValid());
        }
        Log.e(TAG, deezerConnect.getRadioToken());

    }

    public boolean logoutDeezer() {
        sessionStore.clear(this);
        deezerConnect.logout(this);
        return true;
    }

}