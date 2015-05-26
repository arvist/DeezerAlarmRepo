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
import com.cikoapps.deezeralarm.adapters.DeezerPlaylistAdapter;
import com.cikoapps.deezeralarm.helpers.HelperClass;
import com.cikoapps.deezeralarm.helpers.SimpleDividerItemDecoration;
import com.cikoapps.deezeralarm.models.DeezerPlaylist;
import com.deezer.sdk.model.AImageOwner;
import com.deezer.sdk.model.Playlist;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class DeezerPlaylistsFragment extends Fragment {

    private static final String TAG = "DeezerPlaylistsFragment";
    private static Context context;
    private static boolean onlyWiFi;
    private ArrayList<Playlist> playlistList;
    private Long playListId;
    private RecyclerView recyclerView;
    private ArrayList<DeezerPlaylist> playlistsArrayList;
    private DeezerPlaylist deezerPlaylistLocal;
    private DeezerPlaylistAdapter mAdapter;
    private ProgressBar progress;
    private boolean enableNoWiFiTextView = false;

    public static Fragment newInstance(Context mContext, boolean onlyWifiConnection) {
        DeezerPlaylistsFragment fragment = new DeezerPlaylistsFragment();
        context = mContext;
        onlyWiFi = onlyWifiConnection;
        return fragment;
    }

    public static void updateSelectedRingtone(long id, String name) {
        RingtoneActivity.selectedRingtone.updateDeezerRingtone(RingtoneActivity.PLAYLIST_ID, id, name, "");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible()) {
            if (!isVisibleToUser) {
                if (mAdapter != null) {
                    if (DeezerPlaylistAdapter.selectedPosition >= 0) {
                        mAdapter.notifyItemChanged(DeezerPlaylistAdapter.selectedPosition);
                    }
                }
            } else {
                if (mAdapter != null) {
                    if (DeezerPlaylistAdapter.selectedPosition >= 0) {
                        mAdapter.notifyItemChanged(DeezerPlaylistAdapter.selectedPosition);
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // if user is allowed to connect to internet fetch user playlist list
        // otherwise set true warning text variable
        boolean wifiConnected = (new HelperClass(context)).isWifiConnected();
        if ((onlyWiFi && wifiConnected) || !onlyWiFi) {
            getUserPlayLists();
        } else {
            enableNoWiFiTextView = true;
        }
    }

    void getUserPlayLists() {
        playlistsArrayList = new ArrayList<>();
        RequestListener requestListener = new JsonRequestListener() {
            public void onResult(Object result, Object requestId) {
                recyclerView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);

                //noinspection unchecked
                playlistList = (ArrayList<Playlist>) result;
                if (playlistList.size() < 1) {
                    deezerPlaylistLocal = new DeezerPlaylist(-1, "No playlists found", "", "");
                }
                for (Playlist playlist : playlistList) {
                    playListId = playlist.getId();
                    deezerPlaylistLocal = new DeezerPlaylist(playlist.getId(), playlist.getTitle(),
                            HelperClass.timeConversion(playlist.getDuration()), playlist.getImageUrl(AImageOwner.ImageSize.medium)
                    );
                    playlistsArrayList.add(deezerPlaylistLocal);
                }
                Collections.sort(playlistsArrayList, new Comparator<DeezerPlaylist>() {
                    @Override
                    public int compare(DeezerPlaylist lhs, DeezerPlaylist rhs) {
                        return lhs.title.compareTo(rhs.title);
                    }
                });
                mAdapter = new DeezerPlaylistAdapter(context, playlistsArrayList);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setAdapter(mAdapter);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
                Log.e(TAG, "getUserPlaylists finish");
            }

            public void onUnparsedResult(String requestResponse, Object requestId) {
                Log.e(TAG, requestResponse + " Unparsed result");
            }

            public void onException(Exception e, Object requestId) {
                Log.e(TAG, e.getMessage() + " Exception getting playlists");
            }
        };
        DeezerRequest currUserPlaylistRequest = DeezerRequestFactory.requestCurrentUserPlaylists();
        currUserPlaylistRequest.setId(TAG);
        ((RingtoneActivity) getActivity()).deezerConnect.requestAsync(currUserPlaylistRequest, requestListener);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.deezer_playlists_fragment_layout, container, false);
        // inflate fragment into view
        recyclerView = (RecyclerView) rootView.findViewById(R.id.playlistRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setVisibility(View.GONE);
        // enable loading animation
        progress = (ProgressBar) rootView.findViewById(R.id.cover_progress);
        progress.setVisibility(View.VISIBLE);
        TextView noWifiTextView = (TextView) rootView.findViewById(R.id.noWifiTextView);
        //if user is not allowed to connect to internet show warning message
        if (enableNoWiFiTextView) {
            noWifiTextView.setVisibility(View.VISIBLE);
        } else {
            noWifiTextView.setVisibility(View.GONE);
        }
        return rootView;
    }
}
