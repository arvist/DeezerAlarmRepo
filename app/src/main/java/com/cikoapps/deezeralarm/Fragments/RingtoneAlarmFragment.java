package com.cikoapps.deezeralarm.Fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cikoapps.deezeralarm.R;

/**
 * Created by arvis.taurenis on 2/19/2015.
 */
public class RingtoneAlarmFragment extends Fragment {


    TextView alarmTitleTextView;
    Typeface robotoRegular;
    Typeface robotoBlack;
    Typeface robotoItalic;
    String name;
    String tone;
    MediaPlayer mPlayer;

    @SuppressLint("ValidFragment")
    public RingtoneAlarmFragment(String name, String tone) {
        super();
        this.name = name;
        this.tone = tone;
    }

    public RingtoneAlarmFragment() {
        this.name = "My Alarm";
        this.tone = "content://media/internal/audio/media/7";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ringtone_alarm_fragment_layout,
                container, false);
        robotoRegular = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Regular.ttf");
        robotoBlack = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Black.ttf");
        robotoItalic = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Italic.ttf");
        alarmTitleTextView = (TextView) view.findViewById(R.id.alarmTitle);
        alarmTitleTextView.setText(name);
        alarmTitleTextView.setTypeface(robotoRegular);
        CardView dismissButton = (CardView) view.findViewById(R.id.alarm_screen_button);
        TextView buttonText = (TextView) dismissButton.findViewById(R.id.buttonText);
        TextView quoteTextView = (TextView) view.findViewById(R.id.quoteTextView);

        quoteTextView.setTypeface(robotoRegular);
        TextView quoteAuthorTextView = (TextView) view.findViewById(R.id.quoteAuthorTextView);
        quoteAuthorTextView.setTypeface(robotoItalic);

        buttonText.setTypeface(robotoRegular);
        dismissButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mPlayer.stop();
                getActivity().finish();
            }
        });

        //Play alarm tone
        mPlayer = new MediaPlayer();
        try {
            if (tone.equalsIgnoreCase("null") || tone.length() < 2 || tone.equalsIgnoreCase("") || tone == null) {
                tone = "content://media/internal/audio/media/7";
            }
            Uri toneUri = Uri.parse(tone);
            if (toneUri != null) {
                mPlayer.setDataSource(getActivity(), toneUri);
                mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mPlayer.setLooping(true);
                mPlayer.prepare();
                mPlayer.start();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return view;
    }
}
