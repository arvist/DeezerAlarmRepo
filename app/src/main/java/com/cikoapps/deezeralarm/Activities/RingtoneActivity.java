package com.cikoapps.deezeralarm.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

    private static final String TAG = "RingtoneActivity";
    public static final int RINGTONE_ID = 0;
    public static final int PLAYLIST_ID = 1;
    public static final int ALBUM_ID = 2;
    public static final int ARTIST_ID = 3;
    public static final int RADIO_ID = 4;
    public static SelectedRingtone selectedRingtone;

    Toolbar toolbar;
    PagerSlidingTabStrip tabs;
    Activity activity;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.ringtone_activity_layout);
        appBarActions();
        activity = this;
        toolbar = (Toolbar) findViewById(R.id.ringtoneAppBar);
        setSupportActionBar(toolbar);

        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());

        final ViewPager pager = (ViewPager) findViewById(R.id.ringtone_pager);

        pager.setOffscreenPageLimit(5);
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
                returnIntent.putExtra("artist", selectedRingtone.artist);
                pager.removeAllViews();
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
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

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean wifiBool = preferences.getBoolean("wifiSelected", false);

            HelperClass helperClass = new HelperClass(getApplicationContext());
            boolean haveNetworkConnection = helperClass.haveNetworkConnection();
            if (position == 0) {
                Log.e(TAG, "DeviceRingtoneFragment");
                return DeviceRingtoneFragment.newInstance(position, getApplicationContext());
            } else if (position == 1) {
                Log.e(TAG, "PlaylistFragment");
                if (haveNetworkConnection) {
                    return DeezerPlaylistsFragment.newInstance(getApplicationContext(), activity, wifiBool);
                } else {
                    return NoNetworkConnectionFragment.newInstance(position);
                }
            } else if (position == 2) {
                Log.e(TAG, "AlbumFragment");
                if (haveNetworkConnection) {
                    return DeezerAlbumFragment.newInstance(getApplicationContext(), activity, wifiBool);
                } else {
                    return NoNetworkConnectionFragment.newInstance(position);
                }
            } else if (position == 3) {
                Log.e(TAG, "ArtistFragment");

                if (haveNetworkConnection) {
                    return DeezerArtistFragment.newInstance(position, getApplicationContext(), activity, wifiBool);
                } else {
                    return NoNetworkConnectionFragment.newInstance(position);
                }
            } else if (position == 4) {
                Log.e(TAG, "RadioFragment");

                if (haveNetworkConnection) {
                    return DeezerRadioFragment.newInstance(getApplicationContext(), activity, wifiBool);
                } else {
                    return NoNetworkConnectionFragment.newInstance(position);
                }
            }
            return DeviceRingtoneFragment.newInstance(position, getApplicationContext());
        }
    }

    public class SelectedRingtone {
        public int type;
        long id;
        String uri;
        String name;
        String artist;

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

        public void updateDeezerRingtone(int t, long id, String name, String artist) {
            this.type = t;
            this.id = id;
            this.name = name;
            this.uri = null;
            this.artist = artist;
        }
    }
}
