package com.cikoapps.deezeralarm.fragments;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.activities.RingtoneActivity;
import com.cikoapps.deezeralarm.adapters.DeviceRingtoneAdapter;
import com.cikoapps.deezeralarm.helpers.SimpleDividerItemDecoration;
import com.cikoapps.deezeralarm.models.DeviceRingtone;

import java.util.ArrayList;

public class DeviceRingtoneFragment extends Fragment {

    private static final String TAG = "DeviceRingtoneFragment";
    private static Context context;
    private static MediaPlayer mediaPlayer;
    private RecyclerView recyclerView;
    private ArrayList<DeviceRingtone> ringtoneList;
    private DeviceRingtoneAdapter mAdapter;
    private ProgressBar progress;

    public static Fragment newInstance(Context mContext) {
        DeviceRingtoneFragment fragment = new DeviceRingtoneFragment();
        context = mContext;
        return fragment;
    }

    public static void updateSelectedRingtone(String uri, String name) {
        RingtoneActivity.selectedRingtone.updateRingtone(RingtoneActivity.RINGTONE_ID, uri, name);
    }

    // After becoming (in-)visible make app to check weather selected item across all activity has changed
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible()) {
            if (!isVisibleToUser) {
                if (mAdapter != null) {
                    if (mAdapter.selectedPosition >= 0) {
                        mAdapter.notifyItemChanged(mAdapter.selectedPosition);
                    }
                }
            } else {
                if (mAdapter != null) {
                    if (mAdapter.selectedPosition >= 0) {
                        mAdapter.notifyItemChanged(mAdapter.selectedPosition);
                    }
                }
            }
        }
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Launch async task to get Device Ringtone obj
        new RingtoneAcquire().execute();
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.device_ringone_fragment_layout, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.ringtoneRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setVisibility(View.INVISIBLE);
        progress = (ProgressBar) rootView.findViewById(R.id.cover_progress);
        progress.setVisibility(View.VISIBLE);
        return rootView;
    }


    void initializeRecyclerView() {

        mAdapter = new DeviceRingtoneAdapter(context, ringtoneList, mediaPlayer);
        try {
            recyclerView.setAdapter(mAdapter);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
        } catch (NullPointerException ignored) {
        }
        recyclerView.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
    }

    // Ringtone Acquire async task
    class RingtoneAcquire extends AsyncTask<Void, Integer, String> {

        @Override
        protected String doInBackground(Void... params) {
            getRingtones();
            return "Post execute";
        }

        public void getRingtones() {
            ringtoneList = new ArrayList<>();
            RingtoneManager ringtoneMgr = new RingtoneManager(context);
            ringtoneMgr.setType(RingtoneManager.TYPE_ALARM);
            Cursor alarmsCursor = ringtoneMgr.getCursor();
            if (alarmsCursor.moveToFirst()) {
                do {
                    int currentPosition = alarmsCursor.getPosition();
                    DeviceRingtone deviceRingtone = new DeviceRingtone(ringtoneMgr.getRingtoneUri(currentPosition).toString(), ringtoneMgr.getRingtone(currentPosition).getTitle(context), false);
                    ringtoneList.add(deviceRingtone);
                } while (alarmsCursor.moveToNext());
                alarmsCursor.close();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            initializeRecyclerView();
        }
    }
}