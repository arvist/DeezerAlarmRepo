package com.cikoapps.deezeralarm.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;

import com.astuetz.PagerSlidingTabStrip;
import com.cikoapps.deezeralarm.Fragments.DeezerAlbumFragment;
import com.cikoapps.deezeralarm.Fragments.DeezerArtistFragment;
import com.cikoapps.deezeralarm.Fragments.DeezerPlaylistsFragment;
import com.cikoapps.deezeralarm.Fragments.DeezerRadioFragment;
import com.cikoapps.deezeralarm.Fragments.DeviceRingtoneFragment;
import com.cikoapps.deezeralarm.Fragments.NoNetworkConnectionFragment;
import com.cikoapps.deezeralarm.HelperClasses.DeezerBase;
import com.cikoapps.deezeralarm.HelperClasses.HelperClass;
import com.cikoapps.deezeralarm.R;


public class RingtoneActivity extends DeezerBase {

    Toolbar toolbar;
    private MyPagerAdapter adapter;
    PagerSlidingTabStrip tabs;
    Activity activity;
    public static SelectedRingtone selectedRingtone;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        loginDeezer();

        this.context = this;

        setContentView(R.layout.ringtone_activity_layout);
        appBarActions();
        activity = this;
        toolbar = (Toolbar) findViewById(R.id.ringtoneAppBar);
        setSupportActionBar(toolbar);

        adapter = new MyPagerAdapter(getSupportFragmentManager());
        ViewPager pager = (ViewPager) findViewById(R.id.ringtone_pager);
        pager.setOffscreenPageLimit(4);

        pager.setAdapter(adapter);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.ringtone_tabs);
        tabs.setViewPager(pager);
        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);

        selectedRingtone = new SelectedRingtone();

        findViewById(R.id.confirmRingtone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("type", selectedRingtone.type);
                returnIntent.putExtra("uri", selectedRingtone.uri);
                returnIntent.putExtra("id", selectedRingtone.id);
                returnIntent.putExtra("name", selectedRingtone.name);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    public void appBarActions() {
        ImageButton backButton = (ImageButton) findViewById(R.id.app_bar_back_btn);
        ImageButton settingsButton = (ImageButton) findViewById(R.id.app_bar_settings);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();
            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SettingsActivity.class);
                startActivityForResult(intent, 2);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {

            } else if (resultCode == RESULT_CANCELED) {

            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public class MyPagerAdapter extends android.support.v4.app.FragmentStatePagerAdapter {

        private final String[] TITLES = {"DEVICE RINGTONES", "PLAYLISTS", "ALBUMS", "FAVORITE ARTISTS", "RADIO"};

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
            HelperClass helperClass = new HelperClass(getApplicationContext());
            boolean haveNetworkConnection = helperClass.haveNetworkConnection();
            if (position == 0) {
                return DeviceRingtoneFragment.newInstance(position, getApplicationContext(), activity);
            } else if (position == 1) {
                if (haveNetworkConnection) {
                    return DeezerPlaylistsFragment.newInstance(position, getApplicationContext(), activity);
                } else {
                    return NoNetworkConnectionFragment.newInstance(position, getApplicationContext());
                }
            } else if (position == 2) {
                if (haveNetworkConnection) {
                    return DeezerAlbumFragment.newInstance(position, getApplicationContext(), activity);
                } else {
                    return NoNetworkConnectionFragment.newInstance(position, getApplicationContext());
                }
            } else if (position == 3) {
                if (haveNetworkConnection) {
                    return DeezerArtistFragment.newInstance(position, getApplicationContext(), activity);
                } else {
                    return NoNetworkConnectionFragment.newInstance(position, getApplicationContext());
                }
            } else if (position == 4) {
                if (haveNetworkConnection) {
                    return DeezerRadioFragment.newInstance(position, getApplicationContext(), activity);
                } else {
                    return NoNetworkConnectionFragment.newInstance(position, getApplicationContext());
                }
            }
            return DeviceRingtoneFragment.newInstance(position, getApplicationContext(), activity);
        }
    }

    public class SelectedRingtone {
        /*
            0 - device Ringtone
            1 - Playlist
            2 - Album
            3 - Artist Radio
            4 - Radio
        */
        public int type;
        long id;
        String uri;
        String name;

        public SelectedRingtone() {
            this.type = -1;
            this.id = -1;
            this.uri = null;
            this.name = "";
        }

        public void updateRingtone(int t, String uri, String name) {
            this.type = t;
            this.uri = uri;
            this.name = name;
            id = -1;
        }

        public void updateDeezerRingtone(int t, long id, String name) {
            this.type = t;
            this.id = id;
            this.name = name;
            this.uri = null;
        }
    }
}
