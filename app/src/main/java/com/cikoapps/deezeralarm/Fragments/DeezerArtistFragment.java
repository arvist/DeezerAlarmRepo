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
import com.cikoapps.deezeralarm.adapters.DeezerArtistAdapter;
import com.cikoapps.deezeralarm.helpers.HelperClass;
import com.cikoapps.deezeralarm.helpers.SimpleDividerItemDecoration;
import com.cikoapps.deezeralarm.models.DeezerArtist;
import com.deezer.sdk.model.AImageOwner;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DeezerArtistFragment extends Fragment {

    private static final String TAG = "DeezerArtistFragment";
    private static Context context;
    private static ProgressBar progress;
    private static boolean onlyWiFi;
    private DeezerArtistAdapter mAdapter;
    private ArrayList<com.deezer.sdk.model.Artist> artistArrayList;

    private RecyclerView recyclerView;
    private boolean enableNoWiFiTextView = false;

    public static Fragment newInstance(Context mContext, boolean onlyWifiConnection) {
        DeezerArtistFragment fragment = new DeezerArtistFragment();
        context = mContext;
        onlyWiFi = onlyWifiConnection;
        return fragment;
    }

    public static void updateSelectedRingtone(long id, String name) {
        RingtoneActivity.selectedRingtone.updateDeezerRingtone(RingtoneActivity.ARTIST_ID, id, name, "");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible()) {
            if (!isVisibleToUser) {
                if (mAdapter != null) {
                    if (DeezerArtistAdapter.selectedPosition >= 0) {
                        mAdapter.notifyItemChanged(DeezerArtistAdapter.selectedPosition);
                    }
                }
            } else {
                if (mAdapter != null) {
                    if (DeezerArtistAdapter.selectedPosition >= 0) {
                        mAdapter.notifyItemChanged(DeezerArtistAdapter.selectedPosition);
                    }
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean WiFiConnected = (new HelperClass(context)).isWifiConnected();
        if ((onlyWiFi && WiFiConnected) || !onlyWiFi) {
            getUserArtists();
        } else {
            enableNoWiFiTextView = true;
        }
    }

    void getUserArtists() {
        artistArrayList = new ArrayList<>();
        final ArrayList<DeezerArtist> localDeezerArtistList = new ArrayList<>();
        RequestListener requestListener = new JsonRequestListener() {

            public void onResult(Object result, Object requestId) {
                recyclerView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                //noinspection unchecked
                artistArrayList = (ArrayList<com.deezer.sdk.model.Artist>) result;
                for (com.deezer.sdk.model.Artist artist : artistArrayList) {
                    DeezerArtist deezerArtistLocal = new DeezerArtist(artist.getId(), artist.getName()
                            ,    artist.getImageUrl(AImageOwner.ImageSize.medium)
                             , artist.hasRadio());
                    localDeezerArtistList.add(deezerArtistLocal);
                }
                if (localDeezerArtistList.size() < 1) {
                    localDeezerArtistList.add(new DeezerArtist(-1, "No artists found", "", false));
                }
                Collections.sort(localDeezerArtistList, new Comparator<DeezerArtist>() {
                    @Override
                    public int compare(DeezerArtist lhs, DeezerArtist rhs) {
                        return lhs.name.compareTo(rhs.name);
                    }
                });
                mAdapter = new DeezerArtistAdapter(context, localDeezerArtistList);
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
                Log.e(TAG, "Error getting favorite artists " + e.getMessage());
            }
        };

        DeezerRequest currUserArtistRequest = DeezerRequestFactory.requestCurrentUserArtists();
        currUserArtistRequest.setId(TAG);
        ((RingtoneActivity) getActivity()).deezerConnect.requestAsync(currUserArtistRequest, requestListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.deezer_artists_fragment_layout, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.artistRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setVisibility(View.GONE);
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
