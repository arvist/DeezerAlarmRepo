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
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.deezer.sdk.player.AbstractTrackListPlayer;
import com.deezer.sdk.player.AlbumPlayer;
import com.deezer.sdk.player.PlaylistPlayer;
import com.deezer.sdk.player.event.OnPlayerErrorListener;
import com.deezer.sdk.player.event.OnPlayerProgressListener;
import com.deezer.sdk.player.event.OnPlayerStateChangeListener;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.event.PlayerWrapperListener;
import com.deezer.sdk.player.exception.InvalidStreamTokenException;
import com.deezer.sdk.player.exception.StreamLimitationException;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.NetworkStateChecker;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;
import com.deezer.sdk.player.networkcheck.WifiOnlyNetworkStateChecker;


public class DeezerListAlarmFragment extends Fragment {

    private static final String TAG = "AlarmFragment";
    Typeface robotoRegular;
    long id;
    MediaPlayer mPlayer;
    SeekBar seekBar;
    AbstractTrackListPlayer player;
    TextView artistTextView;
    TextView songTextView;
    ImageView songImageView;
    ImageButton controlButton;
    ImageButton nextSongButton;
    ImageButton prevSongButton;
    ImageArtworkDownload imageArtworkDownload;
    CardView dismissButton;
    int playing; // 0 - playing, 1 - paused, 2 - loading, 3 - playlist finished
    int playlistSize = 0;
    int type;  /*  0 - device Ringtone , 1 - Playlist, 2 - Album, 3 - Artist Radio, 4 - Radio */
    private Handler mHandler = new Handler();
    int trackPos = 1;
    boolean imageSet = false;
    NetworkStateChecker networkStateChecker;
    boolean WiFiBool;
    boolean WiFiConnected;
    boolean allowToConnect = false;
    AudioManager audioManager;
    private int reconnect = 0;
    private int reconnectDeezerLogin = 0;
    private int streamLimitReconnect = 0;
    private int reconnectTry = 0;

    @SuppressLint("ValidFragment")
    public DeezerListAlarmFragment(long id, int type, boolean wiFiBool, Context context) {
        this.id = id;
        this.type = type;
        this.WiFiBool = wiFiBool;
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

    public DeezerListAlarmFragment() {
        super();
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.alarm_player_fragment,
                container, false);
        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        initializeLayoutViews(view);
        initializeDismissButton();
        try {
            if (!allowToConnect) {
                Toast.makeText(getActivity(), "Your phone is not connected to WiFi", Toast.LENGTH_LONG).show();
                throw new DeezerError("Your phone is not connected to WiFi");
            }
            initControlButtons();
            playAlarm();
        } catch (DeezerError deezerError) {
            deezerError.printStackTrace();
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

    private void initializeDismissButton() {
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player != null) {
                    player.stop();
                    player.release();
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
    }

    private void playAlarm() {
        playListAlarm();
        initPlaylistPlayer();
    }

    private void playListAlarm() {
        if (type == 1) {
            try {
                player = new PlaylistPlayer(getActivity().getApplication(), ((AlarmScreenActivity) getActivity()).deezerConnect, networkStateChecker);
            } catch (TooManyPlayersExceptions tooManyPlayersExceptions) {
                tooManyPlayersExceptions.printStackTrace();
            } catch (DeezerError deezerError) {
                deezerError.printStackTrace();
            }
            ((PlaylistPlayer) player).playPlaylist(id);
            player.setStereoVolume(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        } else if (type == 2) {
            try {
                player = new AlbumPlayer(getActivity().getApplication(), ((AlarmScreenActivity) getActivity()).deezerConnect, networkStateChecker);
            } catch (TooManyPlayersExceptions tooManyPlayersExceptions) {
                tooManyPlayersExceptions.printStackTrace();
                playDefaultRingtone(audioManager);
            } catch (DeezerError deezerError) {
                deezerError.printStackTrace();
                playDefaultRingtone(audioManager);
            }
            ((AlbumPlayer) player).playAlbum(id);
        }
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

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            player.addOnPlayerProgressListener(new OnPlayerProgressListener() {
                @Override
                public void onPlayerProgress(long l) {
                    seekBar.setProgress((int) l);
                }
            });
            mHandler.postDelayed(this, 100);
        }
    };

    public void initControlButtons() {

        controlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playing == 0 || playing == 2) {
                    controlButton.setImageResource(R.drawable.ic_av_play_arrow);
                    player.pause();
                    playing = 1;
                } else if (playing == 1) {
                    controlButton.setImageResource(R.drawable.ic_av_pause);
                    player.play();
                    playing = 0;
                } else if (playing == 3) {
                    controlButton.setImageResource(R.drawable.ic_av_pause);
                    player.seek(0);
                    player.play();
                    playing = 0;
                }
            }
        });
        nextSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackPos++;
                player.skipToNextTrack();
                if (trackPos >= playlistSize) {
                    trackPos = playlistSize;
                }

                controlButton.setImageResource(R.drawable.ic_av_pause);
            }
        });
        prevSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackPos--;
                player.skipToPreviousTrack();
                if (trackPos <= 1) {
                    trackPos = 1;
                }
                controlButton.setImageResource(R.drawable.ic_av_pause);
                playing = 0;
            }
        });
    }

    private void initPlaylistPlayer() {
        PlayerWrapperListener playerWrapperListener = new PlayerWrapperListener() {
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
                playlistSize = player.getTracks().size();

                if (trackPos == 1) {
                    prevSongButton.setImageResource(R.drawable.ic_av_cant_skip_previous);
                } else {
                    prevSongButton.setImageResource(R.drawable.ic_av_skip_previous);
                }
                if (trackPos == playlistSize) {
                    nextSongButton.setImageResource(R.drawable.ic_av_cant_skip_next);
                } else {
                    nextSongButton.setImageResource(R.drawable.ic_av_skip_next);
                }
                playing = 0;
                songTextView.setText(track.getTitle());
                artistTextView.setText(track.getArtist().getName());
                if (type != 2 || !imageSet) {
                    songImageView.setImageResource(R.drawable.ic_no_song_image);
                }
                if (!imageSet && type == 2) {
                    imageArtworkDownload.getAlbum(songImageView, type, id);
                    imageSet = true;
                } else if (type == 1) {
                    imageArtworkDownload.cancelImageLoadTask();
                    imageArtworkDownload.getPlaylistImage(track.getAlbum().getImageUrl(AImageOwner.ImageSize.medium), songImageView);
                }
            }

            @Override
            public void onTrackEnded(Track track) {
                playing = 2;
                songTextView.setText("");
                artistTextView.setText("");
                trackPos++;
            }

            @Override
            public void onRequestException(Exception e, Object o) {
                Log.e(TAG, "RequestError " + e.getMessage());
                if (reconnect < 3) {
                    initPlaylistPlayer();
                    reconnect++;
                }

            }
        };
        player.addOnPlayerStateChangeListener(new OnPlayerStateChangeListener() {
            @Override
            public void onPlayerStateChange(PlayerState playerState, long l) {
                Log.e(TAG, "PlayerStateChanged to - " + playerState.name());
                if (playerState.compareTo(PlayerState.valueOf("PLAYING")) == 0) {
                    updateProgressBar();
                    seekBar.setMax((int) player.getTrackDuration());
                } else if (playerState.compareTo(PlayerState.valueOf("WAITING_FOR_DATA")) == 0) {
                    Toast.makeText(getActivity(), "Waiting for data...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        player.addOnPlayerErrorListener(new OnPlayerErrorListener() {
                                            @Override
                                            public void onPlayerError(final Exception e, long l) {
                                                if (e instanceof StreamLimitationException) {
                                                    Log.e(TAG, "Account used on different devices error " + e.getMessage());
                                                    if (streamLimitReconnect < 2) {
                                                        streamLimitReconnect++;
                                                        ((AlarmScreenActivity) getActivity()).loginDeezer();
                                                        Log.e(TAG, "Trying to connect to deezer one more time");
                                                        new CountDownTimer(8000, 1000) {
                                                            @Override
                                                            public void onTick(long millisUntilFinished) {
                                                            }

                                                            @Override
                                                            public void onFinish() {
                                                                Log.e(TAG, "Timer finished");
                                                                playListAlarm();
                                                                initPlaylistPlayer();
                                                            }
                                                        }.start();
                                                    } else {
                                                        playDefaultRingtone((AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE));
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                Toast.makeText(getActivity(), "Error!" + e.getMessage(), Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    }
                                                } else if (e instanceof InvalidStreamTokenException)

                                                {
                                                    if (reconnectDeezerLogin < 1) {
                                                        ((AlarmScreenActivity) getActivity()).loginDeezer();
                                                        initPlaylistPlayer();
                                                        player.play();
                                                        reconnectDeezerLogin++;
                                                    }
                                                    Log.e(TAG, "Player error " + e.getMessage());
                                                }
                                            }
                                        }

        );
        player.addPlayerListener(playerWrapperListener);
    }

    private void initializeLayoutViews(View view) {
        robotoRegular = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Regular.ttf");
        artistTextView = (TextView) view.findViewById(R.id.artistTextView);
        songTextView = (TextView) view.findViewById(R.id.songTextView);
        artistTextView.setTypeface(robotoRegular);
        songTextView.setTypeface(robotoRegular);
        songImageView = (ImageView) view.findViewById(R.id.songImage);
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

}

