package com.cikoapps.deezeralarm.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.activities.AlarmScreenActivity;
import com.cikoapps.deezeralarm.activities.SettingsActivity;
import com.cikoapps.deezeralarm.helpers.Quotes;

public class RingtoneAlarmFragment extends Fragment {


    private final String name;
    private AudioManager audioManager;
    private String tone;
    private MediaPlayer mPlayer;

    @SuppressLint("ValidFragment")
    public RingtoneAlarmFragment(String name, String tone, Context context) {
        super();
        this.name = name;
        this.tone = tone;
        audioManager = (AudioManager) ((AlarmScreenActivity) context).getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_PLAY_SOUND);
    }

    public RingtoneAlarmFragment() {
        this.name = "My Alarm";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        container.removeAllViews();
        View view = inflater.inflate(R.layout.ringtone_alarm_fragment_layout,
                container, false);
        Typeface robotoRegular = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Regular.ttf");
        TextView alarmTitleTextView = (TextView) view.findViewById(R.id.alarmTitleTextView);
        alarmTitleTextView.setText(name);
        alarmTitleTextView.setTypeface(robotoRegular);
        CardView dismissButton = (CardView) view.findViewById(R.id.stopButton);
        TextView buttonText = (TextView) dismissButton.findViewById(R.id.buttonText);
        TextView quoteTextView = (TextView) view.findViewById(R.id.quoteTextView);
        TextView quoteAuthorTextView = (TextView) view.findViewById(R.id.quoteAuthorTextView);
        Quotes quote = Quotes.getQuote();
        quoteTextView.setText(quote.quote);
        quoteAuthorTextView.setText(quote.author);


        buttonText.setTypeface(robotoRegular);
        dismissButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mPlayer.stop();
                ((AlarmScreenActivity) getActivity()).finishApp();
            }
        });

        //Play alarm tone
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mPlayer = new MediaPlayer();
        mPlayer.setVolume(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        try {
            if (tone.equalsIgnoreCase("null") || tone.length() < 2 || tone.equalsIgnoreCase("") || tone == null) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                tone = preferences.getString(SettingsActivity.SELECTED_RINGTONE_URI, "");
                if (tone.equalsIgnoreCase("")) {
                    RingtoneManager ringtoneMgr = new RingtoneManager(getActivity());
                    ringtoneMgr.setType(RingtoneManager.TYPE_ALARM);
                    Cursor alarmsCursor = ringtoneMgr.getCursor();
                    int alarmsCount = alarmsCursor.getCount();
                    if (alarmsCount == 0 && !alarmsCursor.moveToFirst()) {
                        alarmsCursor.close();
                    } else {
                        int currentPosition = alarmsCursor.getPosition();
                        tone = ringtoneMgr.getRingtoneUri(currentPosition).toString();

                    }
                    alarmsCursor.close();
                }
            }

            Uri toneUri = Uri.parse(tone);
            mPlayer.setDataSource(getActivity(), toneUri);
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mPlayer.setLooping(true);
            mPlayer.prepare();
            mPlayer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }
}
