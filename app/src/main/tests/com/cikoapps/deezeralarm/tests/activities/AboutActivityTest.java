package com.cikoapps.deezeralarm.tests.activities;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.IntentFilter;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.ImageView;
import android.widget.TextView;

import com.cikoapps.deezeralarm.Activities.AboutActivity;
import com.cikoapps.deezeralarm.R;


/**
 * Created by Arvis on 5/3/2015.
 */
public class AboutActivityTest extends ActivityInstrumentationTestCase2<AboutActivity> {

    AboutActivity activity;
    TextView contactTextView;
    TextView rateTextView;
    ImageView deezerImage;

    public AboutActivityTest() {
        super(AboutActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
        contactTextView = (TextView) activity.findViewById(R.id.contact);
        rateTextView = (TextView) activity.findViewById(R.id.rate);
        deezerImage = (ImageView) activity.findViewById(R.id.deezerLogoImage);
    }

    @SmallTest
    public void testViewsNotNull() {
        assertNotNull(contactTextView);
        assertNotNull(rateTextView);
        assertNotNull(deezerImage);
    }

    @SmallTest
    public void testContactTextViewClick() {
        Instrumentation.ActivityMonitor activityMonitor =
                getInstrumentation().addMonitor(new IntentFilter(android.content.Intent.ACTION_CHOOSER), null, false);
        TouchUtils.clickView(this, contactTextView);
        Activity activitySend = activityMonitor.waitForActivityWithTimeout(5000);
        assertNotNull(activitySend);
        activitySend.finish();
    }

/*   @SmallTest
    public void testDeezerImageClick() {
        Instrumentation.ActivityMonitor activityMonitor =
                getInstrumentation().addMonitor(new IntentFilter(android.content.Intent.ACTION_DEFAULT), null, false);
        TouchUtils.clickView(this,rateTextView);
         Activity activitySend = activityMonitor.waitForActivityWithTimeout(8000);
         assertNotNull(activitySend);
        activitySend .finish();
    }*/


}
