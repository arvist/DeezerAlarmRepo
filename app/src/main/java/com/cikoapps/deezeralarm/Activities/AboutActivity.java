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

import com.cikoapps.deezeralarm.R;

import org.w3c.dom.Text;


public class AboutActivity extends Activity {

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
