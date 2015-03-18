package com.cikoapps.deezeralarm.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cikoapps.deezeralarm.DeezerBase;
import com.cikoapps.deezeralarm.R;
import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.player.AlbumPlayer;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;


public class AboutActivity extends Activity {

    private DeezerConnect deezerConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity_layout);
        TextView contactTextView = (TextView)findViewById(R.id.contact);
        TextView rateTextView = (TextView) findViewById(R.id.rate);
        ImageView deezerImage = (ImageView) findViewById(R.id.deezerLogoImage);
        contactTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {"info@cikoapps.com"});
                startActivity(Intent.createChooser(emailIntent, ""));
            }
        });
        rateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + getApplicationContext().getPackageName()));
                startActivity(intent);
            }
        });
        deezerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDeezerApplication(getApplicationContext());
            }
        });



        // Restore or Login Deezer Account
        SessionStore sessionStore = new SessionStore();
        deezerConnect = new DeezerConnect(getApplicationContext(), DeezerBase.APP_ID);
        if (sessionStore.restore(deezerConnect,getApplicationContext())) {
            try {

                AlbumPlayer albumPlayer = new AlbumPlayer(getApplication(),deezerConnect,new WifiAndMobileNetworkStateChecker());
                albumPlayer.playAlbum(5943680);
            } catch (TooManyPlayersExceptions tooManyPlayersExceptions) {
                tooManyPlayersExceptions.printStackTrace();
            } catch (DeezerError deezerError) {
                deezerError.printStackTrace();
            }

        } else {
            String[] permissions = new String[]{
                    Permissions.BASIC_ACCESS,
                    Permissions.MANAGE_LIBRARY,
                    Permissions.LISTENING_HISTORY};
            // The listener for authentication events
            DialogListener listener = new DialogListener() {
                public void onComplete(Bundle values) {
                    SessionStore sessionStore = new SessionStore();
                    sessionStore.save(deezerConnect, getApplication());
                }

                public void onCancel() {

                }

                public void onException(Exception e) {

                }
            };
            // Launches the authentication process
            deezerConnect.authorize(AboutActivity.this, permissions, listener);
        }
    }

    public void openDeezerApplication(final Context context) {
        Intent intent = null;
        PackageManager pm = context.getPackageManager();
        // check if Deezer is installed
        if (isDeezerApplicationInstalled(pm)) {
            // Generate the default launcher intent
            intent = pm.getLaunchIntentForPackage("deezer.android.app");
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://play.google.com/store/apps/details?id=deezer.android.app"));
        }
        // recommended to keep your app's task clear
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    public static boolean isDeezerApplicationInstalled(final PackageManager pm) {
        try {
            // get the corresponding package information
            PackageInfo info = pm.getPackageInfo("deezer.android.app", 0);
            return (info != null);
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
}
