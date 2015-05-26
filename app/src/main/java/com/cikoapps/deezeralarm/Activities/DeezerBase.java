package com.cikoapps.deezeralarm.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;

public class DeezerBase extends ActionBarActivity {

    // Deezer API Application ID
    public static final String APP_ID = "153961";
    public DeezerConnect deezerConnect;
    private SessionStore sessionStore;
    private Context context;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getApplicationContext();
        deezerConnect = new DeezerConnect(getApplication(), APP_ID);
        sessionStore = new SessionStore();
        // Restore session if possible otherwise ask user to login
        if (sessionStore.restore(deezerConnect, getApplication())) {
            sessionStore.save(deezerConnect, getApplicationContext());
        } else {
            loginDeezer();
        }
    }



    void loginDeezer() {
         String[] permissions = new String[]{
                Permissions.BASIC_ACCESS,
                Permissions.MANAGE_LIBRARY,
                Permissions.LISTENING_HISTORY};
        DialogListener listener = new DialogListener() {
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
         }

    }

    @SuppressWarnings("UnusedReturnValue")
    boolean logoutDeezer() {
        sessionStore.clear(this);
        deezerConnect.logout(this);
        return true;
    }

}