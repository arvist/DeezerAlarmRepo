package com.cikoapps.deezeralarm.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cikoapps.deezeralarm.R;

public class NoNetworkConnectionFragment extends Fragment {

    public static Fragment newInstance() {
        return new NoNetworkConnectionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.no_network_connection_fragment_layout, container, false);
    }
}
