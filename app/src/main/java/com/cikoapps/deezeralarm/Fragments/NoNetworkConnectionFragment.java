package com.cikoapps.deezeralarm.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cikoapps.deezeralarm.R;

public class NoNetworkConnectionFragment extends Fragment {
    private static final String ARG_POSITION = "position";

    public static Fragment newInstance(int position) {
        NoNetworkConnectionFragment f = new NoNetworkConnectionFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.no_network_connection_fragment_layout, container, false);
    }
}
