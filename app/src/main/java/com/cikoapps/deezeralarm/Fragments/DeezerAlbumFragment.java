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
import com.cikoapps.deezeralarm.adapters.DeezerAlbumAdapter;
import com.cikoapps.deezeralarm.helpers.HelperClass;
import com.cikoapps.deezeralarm.helpers.SimpleDividerItemDecoration;
import com.cikoapps.deezeralarm.models.DeezerAlbum;
import com.deezer.sdk.model.AImageOwner;
import com.deezer.sdk.model.Album;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DeezerAlbumFragment extends Fragment {

    private static final String TAG = "DeezerAlbumFragment";
    private static Context context;
    private static ProgressBar progress;
    private static boolean onlyWiFi;
    private DeezerAlbumAdapter mAdapter;
    private ArrayList<Album> albumsArrayList;
    private RecyclerView recyclerView;
    private boolean enableNoWiFiTextView = false;

    public static Fragment newInstance(Context mContext, boolean onlyWifiConnection) {
        DeezerAlbumFragment fragment = new DeezerAlbumFragment();
        context = mContext;
        onlyWiFi = onlyWifiConnection;
        return fragment;
    }

    public static void updateSelectedRingtone(long id, String name, String artist) {
        RingtoneActivity.selectedRingtone.updateDeezerRingtone(RingtoneActivity.ALBUM_ID, id, name, artist);
    }

    // After becoming (in-)visible make app to check weather selected item across all activity has changed
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible()) {
            if (!isVisibleToUser) {
                if (mAdapter != null) {
                    if (DeezerAlbumAdapter.selectedPosition >= 0) {
                        mAdapter.notifyItemChanged(DeezerAlbumAdapter.selectedPosition);
                    }
                }
            } else {

                if (mAdapter != null) {
                    if (DeezerAlbumAdapter.selectedPosition >= 0)
                        mAdapter.notifyItemChanged(DeezerAlbumAdapter.selectedPosition);
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean WiFiConnected = (new HelperClass(context)).isWifiConnected();
        // Decide between data download and internet usage warning text
        if ((onlyWiFi && WiFiConnected) || !onlyWiFi) {
            getUserAlbums();
        } else {
            enableNoWiFiTextView = true;
        }
    }

    void getUserAlbums() {
        albumsArrayList = new ArrayList<>();
        final ArrayList<DeezerAlbum> localDeezerAlbumList = new ArrayList<>();
        RequestListener requestListener = new JsonRequestListener() {
            public void onResult(Object result, Object requestId) {
                recyclerView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                //noinspection unchecked
                albumsArrayList = (ArrayList<Album>) result;
                for (Album album : albumsArrayList) {
                    DeezerAlbum deezerAlbumLocal = new DeezerAlbum(album.getId(), album.getTitle(), album.getArtist().getName()
                            , album.getImageUrl(AImageOwner.ImageSize.medium));
                    localDeezerAlbumList.add(deezerAlbumLocal);
                }
                if (localDeezerAlbumList.size() < 1) {
                    localDeezerAlbumList.add(new DeezerAlbum(-1, "No albums found", "", ""));
                }
                // Sort by name
                Collections.sort(localDeezerAlbumList, new Comparator<DeezerAlbum>() {
                    @Override
                    public int compare(DeezerAlbum lhs, DeezerAlbum rhs) {
                        return lhs.title.compareTo(rhs.title);
                    }
                });
                mAdapter = new DeezerAlbumAdapter(context, localDeezerAlbumList);
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
                Log.e(TAG, "Getting user album error - " + e.getMessage());
            }
        };
        // Data request to Deezer API
        DeezerRequest currUserAlbumRequest = DeezerRequestFactory.requestCurrentUserAlbums();
        currUserAlbumRequest.setId(TAG);
        ((RingtoneActivity) getActivity()).deezerConnect.requestAsync(currUserAlbumRequest, requestListener);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.deezer_albums_fragment_layout, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.albumRecyclerView);
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
