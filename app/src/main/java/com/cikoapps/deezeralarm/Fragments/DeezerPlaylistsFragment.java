package com.cikoapps.deezeralarm.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
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

import com.cikoapps.deezeralarm.Activities.RingtoneActivity;
import com.cikoapps.deezeralarm.HelperClasses.SimpleDividerItemDecoration;
import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.adapters.DeezerPlaylistAdapter;
import com.deezer.sdk.model.AImageOwner;
import com.deezer.sdk.model.Playlist;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;

import java.io.Serializable;
import java.util.ArrayList;


public class DeezerPlaylistsFragment extends Fragment {

    private static final String ARG_POSITION = "position";
    private static final String TAG = "DeezerPlaylistsFrag";
    private static Context context;
    private static ProgressDialog progress;
    private int position;
    ArrayList<com.deezer.sdk.model.Playlist> playlistList;
    Long playListId;
    RecyclerView recyclerView;
    static Activity callingActivity;
    ArrayList<com.cikoapps.deezeralarm.models.Playlist> playlistsArrayList;
    com.cikoapps.deezeralarm.models.Playlist playlistLocal;
    DeezerPlaylistAdapter mAdapter;

    public static Fragment newInstance(int position, Context mContext, Activity activity) {
        DeezerPlaylistsFragment f = new DeezerPlaylistsFragment();
        callingActivity = activity;
        context = mContext;
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        progress = new ProgressDialog(activity);
        progress.setTitle("Loading");
        progress.setMessage("Please wait...");
        return f;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
        //getUserPlayLists();
    }

    public static void updateSelectedRingtone(long id, String name) {
        RingtoneActivity.selectedRingtone.updateDeezerRingtone(1, id, name);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("playlistsArrayList", (Serializable) playlistsArrayList);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            Log.e(TAG, "Saved instance state is not null");
            playlistsArrayList = (ArrayList<com.cikoapps.deezeralarm.models.Playlist>) savedInstanceState.getSerializable("playlistsArrayList");
            mAdapter = new DeezerPlaylistAdapter(context, playlistsArrayList);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.removeAllViews();
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(mAdapter);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
        } else {
            Log.e(TAG, "Saved instance is null");
            if (playlistsArrayList != null && playlistsArrayList.size() > 0) {
                Log.e(TAG, "Getting data from back stack, PlaylistsArrayList size " + playlistsArrayList.size());
                //returning from backstack, data is fine, do nothing
            } else {
                Log.e(TAG, "Getting User Playlists from net,  size ");
                getUserPlayLists();
            }
        }
    }

    public void getUserPlayLists() {
        playlistsArrayList = new ArrayList<>();
        RequestListener requestListener = new JsonRequestListener() {
            public void onResult(Object result, Object requestId) {
                progress.dismiss();
                playlistList = (ArrayList<Playlist>) result;
                if (playlistList.size() < 1) {
                    playlistLocal = new com.cikoapps.deezeralarm.models.Playlist(-1, "No playlists found", "", "", "", "");
                }
                for (Playlist playlist : playlistList) {
                    playListId = playlist.getId();

                    playlistLocal = new com.cikoapps.deezeralarm.models.Playlist(playlist.getId(), playlist.getTitle(),
                            timeConversion(playlist.getDuration()), playlist.getImageUrl(AImageOwner.ImageSize.small), playlist.getImageUrl(AImageOwner.ImageSize.medium),
                            playlist.getImageUrl(AImageOwner.ImageSize.big));
                    playlistsArrayList.add(playlistLocal);
                }
                mAdapter = new DeezerPlaylistAdapter(context, playlistsArrayList);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setAdapter(mAdapter);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
            }

            public void onUnparsedResult(String requestResponse, Object requestId) {
                Log.e(TAG, requestResponse + " Unparesd result");
            }

            public void onException(Exception e, Object requestId) {
                Log.e(TAG, e.getMessage() + " Exception getting playlists");
            }
        };
        DeezerRequest currUserPlaylistRequest = DeezerRequestFactory.requestCurrentUserPlaylists();
        currUserPlaylistRequest.setId("currUserPlayListRequest");
        ((RingtoneActivity) getActivity()).deezerConnect.requestAsync(currUserPlaylistRequest, requestListener);

    }

    private static String timeConversion(int totalSeconds) {

        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        int seconds = totalSeconds % SECONDS_IN_A_MINUTE;
        int totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
        int minutes = totalMinutes % MINUTES_IN_AN_HOUR;
        int hours = totalMinutes / MINUTES_IN_AN_HOUR;
        if (hours > 0) {
            return hours + " h " + minutes + " min " + seconds + " sec";
        } else {
            return minutes + " min " + seconds + " sec";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.deezer_playlists_fragment_layout, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.playlistRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        return rootView;
    }
}
