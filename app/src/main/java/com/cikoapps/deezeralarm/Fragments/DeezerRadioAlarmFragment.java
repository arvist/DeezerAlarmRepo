package com.cikoapps.deezeralarm.Fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cikoapps.deezeralarm.Activities.AlarmScreenActivity;
import com.cikoapps.deezeralarm.Activities.QuoteActivity;
import com.cikoapps.deezeralarm.HelperClasses.HelperClass;
import com.cikoapps.deezeralarm.HelperClasses.ImageArtworkDownload;
import com.cikoapps.deezeralarm.R;
import com.deezer.sdk.model.AImageOwner;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.player.RadioPlayer;
import com.deezer.sdk.player.event.OnPlayerProgressListener;
import com.deezer.sdk.player.event.OnPlayerStateChangeListener;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.event.RadioPlayerListener;
import com.deezer.sdk.player.networkcheck.NetworkStateChecker;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;
import com.deezer.sdk.player.networkcheck.WifiOnlyNetworkStateChecker;

public class DeezerRadioAlarmFragment extends Fragment {
    private static final String TAG = "DeezerRadioAlarmFrag";
    private boolean WiFiBool;
    Typeface robotoRegular;
    long id;
    MediaPlayer mPlayer;
    SeekBar seekBar;
    RadioPlayer radioPlayer;
    TextView artistTextView;
    TextView songTextView;
    ImageView songImageView;
    ImageButton controlButton;
    ImageButton nextSongButton;
    ImageButton prevSongButton;
    CardView dismissButton;
    private Animation a;
    ImageArtworkDownload imageArtworkDownload;
    int playing; // 0 - playing, 1 - paused, 2 - loading, 3 - playlist finished
    private Handler mHandler = new Handler();
    int type;
    boolean WiFiConnected;
    boolean allowToConnect = false;
    AudioManager audioManager;
    NetworkStateChecker networkStateChecker;

    public DeezerRadioAlarmFragment() {
        super();
    }

    @SuppressLint("ValidFragment")
    public DeezerRadioAlarmFragment(long id, int type, boolean WifiBool, Context context) {
        super();
        this.id = id;
        this.type = type;
        this.WiFiBool = WifiBool;
        imageArtworkDownload = new ImageArtworkDownload(getActivity());
        if (WiFiBool) {
            WiFiConnected = (new HelperClass(context)).isWifiConnected();
            if (!WiFiConnected) {
                Log.e(TAG, "Default ringtone, Wifi not Connected");
                allowToConnect = false;
            } else {
                Log.e(TAG, "Playing only on WiFi, WiFi is Connected");
                networkStateChecker = new WifiOnlyNetworkStateChecker();
                allowToConnect = true;
            }
        } else {
            networkStateChecker = new WifiAndMobileNetworkStateChecker();
            allowToConnect = true;
            Log.e(TAG, "Playing list if any network is available");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.alarm_player_fragment, container, false);
        initViews(view);
        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioPlayer != null) {
                    radioPlayer.stop();
                    radioPlayer.release();
                }
                if (mPlayer != null) {
                    mPlayer.stop();
                    mPlayer.release();
                }
                Intent intent = new Intent((getActivity()), QuoteActivity.class);
                ((AlarmScreenActivity) getActivity()).finishApp();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        try {
            if (!allowToConnect) {
                Toast.makeText(getActivity(), "Your phone is not connected to WiFi", Toast.LENGTH_LONG).show();
                throw new DeezerError("Your phone is not connected to WiFi");
            }
            radioPlayer = new RadioPlayer(getActivity().getApplication(), ((AlarmScreenActivity) getActivity()).deezerConnect, networkStateChecker);
            radioPlayer.setStereoVolume(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            initArtistRadioPlayer();
            if (type == 3) {
                radioPlayer.playRadio(RadioPlayer.RadioType.ARTIST, id);
            }
            if (type == 4) {
                radioPlayer.playRadio(RadioPlayer.RadioType.RADIO, id);
            }
            initControlButtons();
        } catch (Exception e) {
            e.printStackTrace();
            playDefaultRingtone(audioManager);
        }

        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        return view;
    }

    private void playDefaultRingtone(AudioManager audioManager) {
        mPlayer = new MediaPlayer();
        mPlayer.setVolume(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        String tone;
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            tone = preferences.getString("selectedRingtoneUri", "");
            if (tone.equalsIgnoreCase("")) {
                RingtoneManager ringtoneMgr = new RingtoneManager(getActivity());
                ringtoneMgr.setType(RingtoneManager.TYPE_ALARM);
                Cursor alarmsCursor = ringtoneMgr.getCursor();
                int alarmsCount = alarmsCursor.getCount();
                if (alarmsCount == 0 && !alarmsCursor.moveToFirst()) {
                    alarmsCursor.close();
                } else {
                    while (!alarmsCursor.isAfterLast() && alarmsCursor.moveToNext()) {
                        int currentPosition = alarmsCursor.getPosition();
                        tone = ringtoneMgr.getRingtoneUri(currentPosition).toString();
                        break;
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
        } catch (Exception f) {
            f.printStackTrace();
        }
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    public void initControlButtons() {

        controlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playing == 0 || playing == 2) {
                    controlButton.setImageResource(R.drawable.ic_av_play_arrow);
                    radioPlayer.pause();
                    playing = 1;
                } else if (playing == 1) {
                    controlButton.setImageResource(R.drawable.ic_av_pause);
                    radioPlayer.play();
                    playing = 0;
                } else if (playing == 4) {
                    controlButton.setImageResource(R.drawable.ic_av_pause);
                    if (type == 3) {
                        radioPlayer.playRadio(RadioPlayer.RadioType.ARTIST, id);
                    } else if (type == 4) {
                        radioPlayer.playRadio(RadioPlayer.RadioType.RADIO, id);
                    }
                    playing = 0;
                }
            }
        });
        nextSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean canSkip = radioPlayer.skipToNextTrack();
                if (canSkip) {
                    controlButton.setImageResource(R.drawable.ic_av_pause);
                    playing = 0;
                }
            }
        });
        prevSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            radioPlayer.addOnPlayerProgressListener(new OnPlayerProgressListener() {
                @Override
                public void onPlayerProgress(long l) {
                    seekBar.setProgress((int) l);
                }
            });
            mHandler.postDelayed(this, 100);
        }
    };

    void initViews(View view) {
        robotoRegular = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Regular.ttf");
        artistTextView = (TextView) view.findViewById(R.id.artistTextView);
        songTextView = (TextView) view.findViewById(R.id.songTextView);
        artistTextView.setTypeface(robotoRegular);
        songTextView.setTypeface(robotoRegular);
        songImageView = (ImageView) view.findViewById(R.id.songImage);
        songImageView.setImageResource(R.drawable.ic_no_song_image);
        controlButton = (ImageButton) view.findViewById(R.id.controlButton);
        nextSongButton = (ImageButton) view.findViewById(R.id.nextSongButton);
        prevSongButton = (ImageButton) view.findViewById(R.id.prevSongButton);
        dismissButton = (CardView) view.findViewById(R.id.quoteTextView);
        TextView buttonText = (TextView) dismissButton.findViewById(R.id.buttonText);
        buttonText.setTypeface(robotoRegular);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setProgress(0);
        seekBar.setMax(100);
    }


    void initArtistRadioPlayer() {

        radioPlayer.addOnPlayerStateChangeListener(new OnPlayerStateChangeListener() {
            @Override
            public void onPlayerStateChange(PlayerState playerState, long l) {
                Log.e(TAG, "Player state is " + playerState.toString());
                if (playerState.compareTo(PlayerState.valueOf("PLAYING")) == 0) {
                    updateProgressBar();
                    seekBar.setMax((int) radioPlayer.getTrackDuration());
                } else if (playerState.compareTo(PlayerState.valueOf("WAITING_FOR_DATA")) == 0) {
                    Toast.makeText(getActivity(), "Waiting for data...", Toast.LENGTH_SHORT).show();
                }

            }
        });

        radioPlayer.addPlayerListener(new RadioPlayerListener() {
            @Override
            public void onAllTracksEnded() {
                playing = 3;
                songTextView.setText("");
                artistTextView.setText("");
                controlButton.setImageResource(R.drawable.ic_av_stop);
            }

            @Override
            public void onPlayTrack(Track track) {
                seekBar.setProgress(0);
                prevSongButton.setImageResource(R.drawable.ic_av_cant_skip_previous);
                playing = 0;
                songTextView.setText(track.getTitle());
                artistTextView.setText(track.getArtist().getName());
                songImageView.setImageResource(R.drawable.ic_no_song_image);
                imageArtworkDownload.cancelImageLoadTask();
                imageArtworkDownload.getPlaylistImage(track.getAlbum().getImageUrl(AImageOwner.ImageSize.medium), songImageView);
            }

            @Override
            public void onTrackEnded(Track track) {
                playing = 2;
                songTextView.setText("");
                artistTextView.setText("");
            }

            @Override
            public void onRequestException(Exception e, Object o) {
                Log.e(TAG, e.getMessage());
            }

            @Override
            public void onTooManySkipsException() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity(), "Error! Too many Skips", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


}
