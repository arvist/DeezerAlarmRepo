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
import com.cikoapps.deezeralarm.HelperClasses.HelperClass;
import com.cikoapps.deezeralarm.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class RingtoneActivity extends DeezerBase {

    public static final int RINGTONE_ID = 0;
    public static final int PLAYLIST_ID = 1;
    public static final int ALBUM_ID = 2;
    public static final int ARTIST_ID = 3;
    public static final int RADIO_ID = 4;
    public static final String RINGTONE_ID_STRING = "id";
    public static final String RINGTONE_TYPE = "type";
    public static final String RINGTONE_URI = "uri";
    public static final String RINGTONE_NAME = "name";
    public static final String RINGTONE_ARTIST = "artist";
    private static final String TAG = "RingtoneActivity";
    public static SelectedRingtone selectedRingtone;
    private PagerSlidingTabStrip tabs;
    private Activity activity;
    private Context context;
    private ViewPager pager;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.ringtone_activity_layout);
        initializeAppBarActions();
        activity = this;
        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        Toolbar toolbar = (Toolbar) findViewById(R.id.ringtoneAppBar);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.ringtone_tabs);
        pager = (ViewPager) findViewById(R.id.ringtone_pager);
        setSupportActionBar(toolbar);
        TabsPagerAdapter adapter = new TabsPagerAdapter(getSupportFragmentManager());
        pager.setOffscreenPageLimit(5);
        pager.setAdapter(adapter);
        tabs.setViewPager(pager);
        pager.setPageMargin(pageMargin);
        selectedRingtone = new SelectedRingtone();
        showAds();
        findViewById(R.id.confirmRingtone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(RINGTONE_TYPE, selectedRingtone.type);
                returnIntent.putExtra(RINGTONE_URI, selectedRingtone.uri);
                returnIntent.putExtra(RINGTONE_ID_STRING, selectedRingtone.id);
                returnIntent.putExtra(RINGTONE_NAME, selectedRingtone.name);
                returnIntent.putExtra(RINGTONE_ARTIST, selectedRingtone.artist);
                Log.e(TAG, RINGTONE_ARTIST + " " + selectedRingtone.artist);
                pager.removeAllViews();
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adView != null) {
            adView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adView != null) {
            adView.destroy();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    private void showAds() {
        adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
/*                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("abc")*/
                .build();
        adView.loadAd(adRequest);
    }

    void initializeAppBarActions() {
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
                pager.removeAllViews();
                pager.setAdapter(new TabsPagerAdapter(getSupportFragmentManager()));
                tabs.setViewPager(pager);
            }
        }
    }

    public class TabsPagerAdapter extends android.support.v4.app.FragmentStatePagerAdapter {

        private final String[] TAB_TITLES = {"DEVICE RINGTONES", "PLAYLISTS", "ALBUMS", "FAVORITE ARTISTS", "RADIO"};

        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TAB_TITLES[position];
        }

        @Override
        public int getCount() {
            return TAB_TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean wifiBool = preferences.getBoolean(SettingsActivity.ONLY_WIFI_SELECTED, false);

            HelperClass helperClass = new HelperClass(getApplicationContext());
            boolean haveNetworkConnection = helperClass.haveNetworkConnection();
            if (position == RINGTONE_ID) {
                return DeviceRingtoneFragment.newInstance(getApplicationContext());
            } else if (position == PLAYLIST_ID) {
                if (haveNetworkConnection) {
                    return DeezerPlaylistsFragment.newInstance(getApplicationContext(), wifiBool);
                } else {
                    return NoNetworkConnectionFragment.newInstance();
                }
            } else if (position == ALBUM_ID) {
                if (haveNetworkConnection) {
                    return DeezerAlbumFragment.newInstance(getApplicationContext(), wifiBool);
                } else {
                    return NoNetworkConnectionFragment.newInstance();
                }
            } else if (position == ARTIST_ID) {
                if (haveNetworkConnection) {
                    return DeezerArtistFragment.newInstance(getApplicationContext(), wifiBool);
                } else {
                    return NoNetworkConnectionFragment.newInstance();
                }
            } else if (position == RADIO_ID) {
                if (haveNetworkConnection) {
                    return DeezerRadioFragment.newInstance(getApplicationContext(), wifiBool);
                } else {
                    return NoNetworkConnectionFragment.newInstance();
                }
            }
            return DeviceRingtoneFragment.newInstance(getApplicationContext());
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