package com.cikoapps.deezeralarm.tests.activities;

import android.test.ActivityInstrumentationTestCase2;

import com.cikoapps.deezeralarm.activities.QuoteActivity;
import com.robotium.solo.Solo;

public class QuoteActivityTest extends ActivityInstrumentationTestCase2<QuoteActivity> {


    private Solo solo;

    public QuoteActivityTest() {
        super(QuoteActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void test0_testViews(){
        assertNotNull(solo.getView("quoteTextView"));
        assertNotNull(solo.getView("authorTextView"));
    }

}
