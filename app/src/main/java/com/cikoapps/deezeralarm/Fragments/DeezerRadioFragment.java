package com.cikoapps.deezeralarm.Fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cikoapps.deezeralarm.Activities.RingtoneActivity;
import com.cikoapps.deezeralarm.HelperClasses.HelperClass;
import com.cikoapps.deezeralarm.HelperClasses.SimpleDividerItemDecoration;
import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.adapters.DeezerRadioAdapter;
import com.cikoapps.deezeralarm.models.Radio;
import com.deezer.sdk.model.AImageOwner;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class DeezerRadioFragment extends Fragment {

    private static final String TAG = "DeezerRadioFragment";
    static Activity callingActivity;
    private static Context context;
    DeezerRadioAdapter mAdapter;
    ArrayList<com.deezer.sdk.model.Radio> radioArrayList;
    ArrayList<com.cikoapps.deezeralarm.models.Radio> localRadioList;
    RecyclerView recyclerView;
    private ProgressBar progress;
    private static boolean onlyWiFi;
    private TextView noWifiTextView;
    private boolean enableNoWiFiTextView = false;

    public static Fragment newInstance(Context mContext, Activity activity, boolean onlyWifiConnection) {
        DeezerRadioFragment f = new DeezerRadioFragment();
        callingActivity = activity;
        context = mContext;
        onlyWiFi = onlyWifiConnection;
        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean WiFiConnected = (new HelperClass(context)).isWifiConnected();
        if ((onlyWiFi && WiFiConnected) || !onlyWiFi) {
            getUserRadio();
        } else {
            enableNoWiFiTextView = true;
        }
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible()) {
            if (!isVisibleToUser) {
                if (mAdapter != null) {
                    if (DeezerRadioAdapter.selectedPosition >= 0) {
                        mAdapter.notifyItemChanged(DeezerRadioAdapter.selectedPosition);
                    }
                }
            } else {
                if (mAdapter != null) {
                    if (DeezerRadioAdapter.selectedPosition >= 0) {
                        mAdapter.notifyItemChanged(DeezerRadioAdapter.selectedPosition);
                    }
                }
            }
        }
    }

    public void getUserRadio() {
        radioArrayList = new ArrayList<>();
        localRadioList = new ArrayList<>();
        RequestListener requestListener = new JsonRequestListener() {

            public void onResult(Object result, Object requestId) {

                //noinspection unchecked
                recyclerView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                radioArrayList = (ArrayList<com.deezer.sdk.model.Radio>) result;
                for (com.deezer.sdk.model.Radio radio : radioArrayList) {
                    com.cikoapps.deezeralarm.models.Radio radioLocal = new com.cikoapps.deezeralarm.models.Radio(radio.getId(), radio.getTitle().trim()
                            , radio.getPictureUrl(), radio.getImageUrl(AImageOwner.ImageSize.small), radio.getImageUrl(AImageOwner.ImageSize.medium),
                            radio.getImageUrl(AImageOwner.ImageSize.big));
                    localRadioList.add(radioLocal);
                }
                if (localRadioList.size() < 1) {
                    localRadioList.add(new com.cikoapps.deezeralarm.models.Radio(-1, "No radios found", "", "", "", ""));
                }


                Collections.sort(localRadioList, new Comparator<Radio>() {
                    @Override
                    public int compare(Radio lhs, Radio rhs) {
                        return lhs.title.compareTo(rhs.title);
                    }

                    @Override
                    public boolean equals(Object object) {
                        return false;
                    }
                });

                mAdapter = new DeezerRadioAdapter(context, localRadioList);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setAdapter(mAdapter);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
            }

            public void onUnparsedResult(String requestResponse, Object requestId) {
                Log.e(TAG, "Unparsed Result");
            }

            public void onException(Exception e, Object requestId) {
                Log.e(TAG, "Error getting radios " + e.getMessage());
            }
        };
        DeezerRequest currUserRadioRequest = DeezerRequestFactory.requestRadios();
        currUserRadioRequest.setId(TAG);
        ((RingtoneActivity) getActivity()).deezerConnect.requestAsync(currUserRadioRequest, requestListener);
    }

    public static void updateSelectedRingtone(long id, String name) {
        RingtoneActivity.selectedRingtone.updateDeezerRingtone(RingtoneActivity.RADIO_ID, id, name, "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.deezer_radio_fragment_layout, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.radioRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setVisibility(View.INVISIBLE);
        progress = (ProgressBar) rootView.findViewById(R.id.cover_progress);
        progress.setVisibility(View.VISIBLE);
        noWifiTextView = (TextView) rootView.findViewById(R.id.noWifiTextView);
        if (enableNoWiFiTextView) {
            noWifiTextView.setVisibility(View.VISIBLE);
        } else {
            noWifiTextView.setVisibility(View.GONE);
        }
        return rootView;
    }
}
