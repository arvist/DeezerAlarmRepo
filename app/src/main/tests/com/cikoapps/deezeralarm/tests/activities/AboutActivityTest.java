package com.cikoapps.deezeralarm.tests.activities;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.IntentFilter;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.ImageView;
import android.widget.TextView;

import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.activities.AboutActivity;
import com.robotium.solo.Solo;


@SuppressWarnings("ALL")
public class AboutActivityTest extends ActivityInstrumentationTestCase2<AboutActivity> {

    AboutActivity activity;
    TextView contactTextView;
    TextView rateTextView;
    ImageView deezerImage;
    private Solo solo;

    public AboutActivityTest() {
        super(AboutActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo  = new Solo(getInstrumentation(), getActivity());
        activity = getActivity();
        contactTextView = (TextView) activity.findViewById(R.id.contact);
        rateTextView = (TextView) activity.findViewById(R.id.rate);
        deezerImage = (ImageView) activity.findViewById(R.id.deezerLogoImage);
    }


    @SmallTest
    public void test0_contactTextViewClick() {
        Instrumentation.ActivityMonitor activityMonitor =
                getInstrumentation().addMonitor(new IntentFilter(android.content.Intent.ACTION_CHOOSER), null, false);
        TouchUtils.clickView(this, contactTextView);
        Activity activitySend = activityMonitor.waitForActivityWithTimeout(5000);
        assertNotNull(activitySend);
        activitySend.finish();
    }
    public void test1_rateViewClick(){
        solo.clickOnView(rateTextView);
    }
    public void test2_deezerImageClick(){
        solo.clickOnView(deezerImage);
    }
}
