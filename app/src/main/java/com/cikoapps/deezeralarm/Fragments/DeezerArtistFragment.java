package com.cikoapps.deezeralarm.Fragments;

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
import com.cikoapps.deezeralarm.adapters.DeezerArtistAdapter;
import com.cikoapps.deezeralarm.models.Artist;
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

    public void getUserArtists() {
        artistArrayList = new ArrayList<>();
        final ArrayList<com.cikoapps.deezeralarm.models.Artist> localArtistList = new ArrayList<>();
        RequestListener requestListener = new JsonRequestListener() {

            public void onResult(Object result, Object requestId) {
                recyclerView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                //noinspection unchecked
                artistArrayList = (ArrayList<com.deezer.sdk.model.Artist>) result;
                for (com.deezer.sdk.model.Artist artist : artistArrayList) {
                    com.cikoapps.deezeralarm.models.Artist artistLocal = new com.cikoapps.deezeralarm.models.Artist(artist.getId(), artist.getName()
                            , artist.getPictureUrl(), artist.getImageUrl(AImageOwner.ImageSize.small), artist.getImageUrl(AImageOwner.ImageSize.medium),
                            artist.getImageUrl(AImageOwner.ImageSize.big), artist.hasRadio());
                    localArtistList.add(artistLocal);
                }
                if (localArtistList.size() < 1) {
                    localArtistList.add(new com.cikoapps.deezeralarm.models.Artist(-1, "No artists found", "", "", "", "", false));
                }
                Collections.sort(localArtistList, new Comparator<Artist>() {
                    @Override
                    public int compare(Artist lhs, Artist rhs) {
                        return lhs.name.compareTo(rhs.name);
                    }
                });
                mAdapter = new DeezerArtistAdapter(context, localArtistList);
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
