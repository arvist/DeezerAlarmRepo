package com.cikoapps.deezeralarm.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cikoapps.deezeralarm.R;
import com.deezer.sdk.network.connect.DeezerConnect;


public class AboutActivity extends Activity {

    private DeezerConnect deezerConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity_layout);
        Typeface robotoRegular = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");

        TextView contactTextView = (TextView)findViewById(R.id.contact);
        contactTextView.setTypeface(robotoRegular);
        TextView rateTextView = (TextView) findViewById(R.id.rate);
        rateTextView.setTypeface(robotoRegular);
        ImageView deezerImage = (ImageView) findViewById(R.id.deezerLogoImage);
        contactTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"info@cikoapps.com"});
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
    }

    void openDeezerApplication(final Context context) {
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
    private static boolean isDeezerApplicationInstalled(final PackageManager pm) {
        try {
            // get the corresponding package information
            PackageInfo info = pm.getPackageInfo("deezer.android.app", 0);
            return (info != null);
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}
