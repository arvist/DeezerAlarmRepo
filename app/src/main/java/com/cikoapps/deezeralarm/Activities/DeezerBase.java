package com.cikoapps.deezeralarm.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;

public class DeezerBase extends ActionBarActivity {

    public static final String APP_ID = "153961";
    private static final String TAG = "DeezerBase.java";
    public DeezerConnect deezerConnect;
    private SessionStore sessionStore;
    private Context context;
    private DialogListener listener;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.e(TAG, "onCreate DeezerBase.java");
        this.context = getApplicationContext();
        deezerConnect = new DeezerConnect(getApplication(), APP_ID);
        sessionStore = new SessionStore();
        if (sessionStore.restore(deezerConnect, getApplication())) {
            //Log.e(TAG, "Session is valid " + deezerConnect.isSessionValid() + " onCreate");
            sessionStore.save(deezerConnect, getApplicationContext());
        } else {
            loginDeezer();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Log.e(TAG, "DeezerBase onCancel");
    }

    void loginDeezer() {
         String[] permissions = new String[]{
                Permissions.BASIC_ACCESS,
                Permissions.MANAGE_LIBRARY,
                Permissions.LISTENING_HISTORY};
        listener = new DialogListener() {
            public void onComplete(Bundle values) {
                sessionStore.save(deezerConnect, context);
                deezerConnect.getRadioToken();
            }

            public void onCancel() {
            }

            public void onException(Exception e) {
            }
        };

        if (sessionStore.restore(deezerConnect, getApplicationContext())) {
            if (deezerConnect.isSessionValid()) {
                deezerConnect.getRadioToken();
                sessionStore.save(deezerConnect, getApplicationContext());
            }
        } else {
            deezerConnect.authorize(DeezerBase.this, permissions, listener);
            //Log.e(TAG, "Authorizing deezer account");
        }
        //Log.e(TAG, deezerConnect.getRadioToken());
    }

    boolean logoutDeezer() {
        sessionStore.clear(this);
        deezerConnect.logout(this);
        return true;
    }

}