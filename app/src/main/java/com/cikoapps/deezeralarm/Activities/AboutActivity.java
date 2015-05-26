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


public class AboutActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout of activity
        setContentView(R.layout.about_activity_layout);
        Typeface robotoRegular = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");

        // Find activity's views
        TextView contactTextView = (TextView)findViewById(R.id.contact);
        contactTextView.setTypeface(robotoRegular);
        TextView rateTextView = (TextView) findViewById(R.id.rate);
        rateTextView.setTypeface(robotoRegular);
        ImageView deezerImage = (ImageView) findViewById(R.id.deezerLogoImage);

        /// Set on click listeners for views
        contactTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"info@cikoapps.com"});
                // Opens dialog to choose email application
                startActivity(Intent.createChooser(emailIntent, ""));
            }
        });
         rateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + getApplicationContext().getPackageName()));
                // Opens GooglePlayStore app page
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

    /*
        Opens deezer applications or GooglePlayStore Deezer app page
     */
    void openDeezerApplication(final Context context) {
        Intent intent;
        PackageManager pm = context.getPackageManager();
        // check if Deezer is installed
        if (isDeezerApplicationInstalled(pm)) {
            // Generate the default launcher intent
            intent = pm.getLaunchIntentForPackage("deezer.android.app");
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://play.google.com/store/apps/details?id=deezer.android.app"));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    /*
        Used to determine whether device has installed Deezer application or not
     */
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
