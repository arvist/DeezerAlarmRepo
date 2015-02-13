package com.cikoapps.deezeralarm;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DeviceRingtoneFragment extends Fragment {

    private static final String ARG_POSITION = "position";

    private int position;

    public static Fragment newInstance(int position) {
        DeviceRingtoneFragment f = new DeviceRingtoneFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.device_ringone_fragment_layout,container,false);
        TextView textView1 = (TextView)rootView.findViewById(R.id.textView900);
        textView1.setText("TEXT");
        return rootView;
    }
}