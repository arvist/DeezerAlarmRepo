package com.cikoapps.deezeralarm;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;

/**
 * Created by arvis.taurenis on 2/13/2015.
 */
public class RingtoneActivity extends ActionBarActivity {

    Toolbar toolbar;
    private MyPagerAdapter adapter;
    PagerSlidingTabStrip tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ringtone_activity_layout);

        toolbar = (Toolbar) findViewById(R.id.ringtoneAppBar);
        setSupportActionBar(toolbar);
        // create our manager instance after the content view is set


        adapter = new MyPagerAdapter(getSupportFragmentManager());
        ViewPager pager = (ViewPager) findViewById(R.id.ringtone_pager);
        pager.setAdapter(adapter);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.ringtone_tabs);
        tabs.setViewPager(pager);
        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);

        tabs.setOnTabReselectedListener(new PagerSlidingTabStrip.OnTabReselectedListener() {
            @Override
            public void onTabReselected(int position) {
               Toast.makeText(RingtoneActivity.this, "Tab reselected: " + position, Toast.LENGTH_SHORT).show();
               }
           }
        );
    }


    public class MyPagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {"DEVICE RINGTONES","PLAYLISTS", "ALBUMS", "FAVORITE ARTISTS", "RADIO"};

        public MyPagerAdapter(FragmentManager fm) {

            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            return DeviceRingtoneFragment.newInstance(position);
        }
    }

}
