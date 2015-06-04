package com.cikoapps.deezeralarm.fragments;

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

import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.activities.RingtoneActivity;
import com.cikoapps.deezeralarm.adapters.DeezerRadioAdapter;
import com.cikoapps.deezeralarm.helpers.HelperClass;
import com.cikoapps.deezeralarm.helpers.SimpleDividerItemDecoration;
import com.cikoapps.deezeralarm.models.DeezerRadio;
import com.deezer.sdk.model.AImageOwner;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


@SuppressWarnings("unchecked")
public class DeezerRadioFragment extends Fragment {

    private static final String TAG = "DeezerRadioFragment";
    private static Context context;
    private static boolean onlyWiFi;
    private DeezerRadioAdapter mAdapter;
    private ArrayList<com.deezer.sdk.model.Radio> radioArrayList;
    private ArrayList<DeezerRadio> localDeezerRadioList;
    private RecyclerView recyclerView;
    private ProgressBar progress;
    private boolean enableNoWiFiTextView = false;

    public static Fragment newInstance(Context mContext, boolean onlyWifiConnection) {
        DeezerRadioFragment f = new DeezerRadioFragment();
        context = mContext;
        onlyWiFi = onlyWifiConnection;
        return f;
    }

    public static void updateSelectedRingtone(long id, String name) {
        RingtoneActivity.selectedRingtone.updateDeezerRingtone(RingtoneActivity.RADIO_ID, id, name, "");
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

    // After becoming (in-)visible make app to check weather selected item across all activity has changed
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

    void getUserRadio() {
        radioArrayList = new ArrayList<>();
        localDeezerRadioList = new ArrayList<>();
        RequestListener requestListener = new JsonRequestListener() {

            public void onResult(Object result, Object requestId) {

                 recyclerView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                radioArrayList = (ArrayList<com.deezer.sdk.model.Radio>) result;
                for (com.deezer.sdk.model.Radio radio : radioArrayList) {
                    DeezerRadio deezerRadioLocal = new DeezerRadio(radio.getId(), radio.getTitle().trim()
                            , radio.getImageUrl(AImageOwner.ImageSize.small), radio.getImageUrl(AImageOwner.ImageSize.medium)
                            );
                    localDeezerRadioList.add(deezerRadioLocal);
                }
                if (localDeezerRadioList.size() < 1) {
                    localDeezerRadioList.add(new DeezerRadio(-1, "No radios found",   "", ""));
                }


                Collections.sort(localDeezerRadioList, new Comparator<DeezerRadio>() {
                    @Override
                    public int compare(DeezerRadio lhs, DeezerRadio rhs) {
                        return lhs.title.compareTo(rhs.title);
                    }

                    @Override
                    public boolean equals(Object object) {
                        return false;
                    }
                });

                mAdapter = new DeezerRadioAdapter(context, localDeezerRadioList);
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
        // Request to Deezer API
        DeezerRequest currUserRadioRequest = DeezerRequestFactory.requestRadios();
        currUserRadioRequest.setId(TAG);
        ((RingtoneActivity) getActivity()).deezerConnect.requestAsync(currUserRadioRequest, requestListener);
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
        TextView noWifiTextView = (TextView) rootView.findViewById(R.id.noWifiTextView);
        if (enableNoWiFiTextView) {
            noWifiTextView.setVisibility(View.VISIBLE);
        } else {
            noWifiTextView.setVisibility(View.GONE);
        }
        return rootView;
    }
}
