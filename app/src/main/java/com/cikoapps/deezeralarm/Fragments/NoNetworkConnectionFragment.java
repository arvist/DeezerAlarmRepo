package com.cikoapps.deezeralarm.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cikoapps.deezeralarm.R;

/**
 * Created by arvis.taurenis on 2/17/2015.
 */
public class NoNetworkConnectionFragment extends Fragment {
    private static final String ARG_POSITION = "position";
    private static Context context;

    public static Fragment newInstance(int position, Context mContext) {
        NoNetworkConnectionFragment f = new NoNetworkConnectionFragment();
        context = mContext;
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.no_network_connection_fragment_layout, container, false);
        return rootView;

    }
}
