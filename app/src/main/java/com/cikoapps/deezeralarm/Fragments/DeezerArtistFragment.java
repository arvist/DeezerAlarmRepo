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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cikoapps.deezeralarm.Activities.RingtoneActivity;
import com.cikoapps.deezeralarm.HelperClasses.SimpleDividerItemDecoration;
import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.adapters.DeezerArtistAdapter;
import com.deezer.sdk.model.AImageOwner;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;

import java.util.ArrayList;

/**
 * Created by arvis.taurenis on 2/16/2015.
 */
public class DeezerArtistFragment extends Fragment {

    static Activity callingActivity;
    private static final String ARG_POSITION = "position";
    private static Context context;
    private static ProgressDialog progress;
    DeezerArtistAdapter mAdapter;
    private int position;
    ArrayList<com.deezer.sdk.model.Artist> artistArrayList;

    RecyclerView recyclerView;

    public static Fragment newInstance(int position, Context mContext, Activity activity) {
        DeezerArtistFragment f = new DeezerArtistFragment();
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
                if (artistArrayList == null) {
                    progress.show();
                }
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
        getUserArtists();
    }

    public static void updateSelectedRingtone(long id, String name) {
        RingtoneActivity.selectedRingtone.updateDeezerRingtone(3, id, name);
    }

    public void getUserArtists() {
        artistArrayList = new ArrayList<>();
        final ArrayList<com.cikoapps.deezeralarm.models.Artist> localArtistList = new ArrayList<>();
        RequestListener requestListener = new JsonRequestListener() {

            public void onResult(Object result, Object requestId) {
                progress.dismiss();
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
                mAdapter = new DeezerArtistAdapter(context, localArtistList);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setAdapter(mAdapter);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
            }

            public void onUnparsedResult(String requestResponse, Object requestId) {
            }

            public void onException(Exception e, Object requestId) {
            }
        };

        DeezerRequest currUserArtistRequest = DeezerRequestFactory.requestCurrentUserArtists();
        currUserArtistRequest.setId("currUserArtistRequest");
        ((RingtoneActivity) getActivity()).deezerConnect.requestAsync(currUserArtistRequest, requestListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.deezer_artists_fragment_layout, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.artistRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        return rootView;
    }
}
