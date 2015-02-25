package com.cikoapps.deezeralarm.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cikoapps.deezeralarm.Activities.RingtoneActivity;
import com.cikoapps.deezeralarm.HelperClasses.SimpleDividerItemDecoration;
import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.adapters.DeviceRingtoneAdapter;
import com.cikoapps.deezeralarm.models.DeviceRingtone;

import java.io.Serializable;
import java.util.ArrayList;

public class DeviceRingtoneFragment extends Fragment {

    private static final String ARG_POSITION = "position";
    private static Context context;
    private static ProgressDialog progress;

    private int position;
    RecyclerView recyclerView;
    ArrayList<DeviceRingtone> ringtoneList;
    MediaPlayer mediaPlayer;
    DeviceRingtoneAdapter mAdapter;

    public static Fragment newInstance(int position, Context mContext, Activity activity) {
        DeviceRingtoneFragment f = new DeviceRingtoneFragment();
        context = mContext;
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        progress = new ProgressDialog(activity);
        progress.setTitle("Loading");
        progress.setMessage("Please wait...");
        return f;
    }

    public static void updateSelectedRingtone(String uri, String name) {
        RingtoneActivity.selectedRingtone.updateRingtone(0, uri, name);
    }

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
            } else if (isVisibleToUser) {
                if (mAdapter != null) {
                    if (mAdapter.selectedPosition >= 0) {
                        mAdapter.notifyItemChanged(mAdapter.selectedPosition);
                    }
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("ringtoneList", (Serializable) ringtoneList);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            ringtoneList = (ArrayList<DeviceRingtone>) savedInstanceState.getSerializable("ringtoneList");
            if (ringtoneList != null) {
                if (ringtoneList.size() > 0) {
                    initializeRecyclerView();
                }
            } else {
                progress.show();
                new RingtoneAcquire().execute();
            }
        } else {
            if (ringtoneList != null) {
            } else {
                progress.show();
                new RingtoneAcquire().execute();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.device_ringone_fragment_layout, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.ringtoneRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        return rootView;

    }

    public void initializeRecyclerView() {

        mAdapter = new DeviceRingtoneAdapter(context, ringtoneList, mediaPlayer);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));

    }

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
            int alarmsCount = alarmsCursor.getCount();
            if (alarmsCount == 0 && !alarmsCursor.moveToFirst()) {
                alarmsCursor.close();
            } else {

                while (!alarmsCursor.isAfterLast() && alarmsCursor.moveToNext()) {
                    DeviceRingtone deviceRingtone = new DeviceRingtone();
                    int currentPosition = alarmsCursor.getPosition();
                    deviceRingtone.title = ringtoneMgr.getRingtone(currentPosition).getTitle(context);
                    deviceRingtone.Uri = ringtoneMgr.getRingtoneUri(currentPosition).toString();
                    ringtoneList.add(deviceRingtone);
                }
                alarmsCursor.close();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            progress.dismiss();
            super.onPostExecute(s);
            initializeRecyclerView();
        }
    }
}