package com.cikoapps.deezeralarm.Fragments;

import android.annotation.SuppressLint;
import android.app.Application;
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
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cikoapps.deezeralarm.Activities.AlarmScreenActivity;
import com.cikoapps.deezeralarm.Activities.QuoteActivity;
import com.cikoapps.deezeralarm.Activities.SettingsActivity;
import com.cikoapps.deezeralarm.Activities.DeezerBase;
import com.cikoapps.deezeralarm.HelperClasses.HelperClass;
import com.cikoapps.deezeralarm.HelperClasses.ImageArtworkDownload;
import com.cikoapps.deezeralarm.R;
import com.deezer.sdk.model.AImageOwner;
import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.player.AbstractTrackListPlayer;
import com.deezer.sdk.player.AlbumPlayer;
import com.deezer.sdk.player.PlaylistPlayer;
import com.deezer.sdk.player.event.BufferState;
import com.deezer.sdk.player.event.OnBufferErrorListener;
import com.deezer.sdk.player.event.OnBufferStateChangeListener;
import com.deezer.sdk.player.event.OnPlayerProgressListener;
import com.deezer.sdk.player.event.OnPlayerStateChangeListener;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.event.PlayerWrapperListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.NetworkStateChecker;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;
import com.deezer.sdk.player.networkcheck.WifiOnlyNetworkStateChecker;


public class DeezerListAlarmFragment extends Fragment {

    private static final String TAG = "AlarmFragment";
    private final Handler mHandler = new Handler();
    private final Runnable mUpdateTimeTask = new Runnable() {
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
    private int maxVolume;
    private Context context;
    private DeezerConnect deezerConnect = null;
    private long id;
    private MediaPlayer mPlayer;
    private SeekBar seekBar;
    private AbstractTrackListPlayer player;
    private TextView artistTextView;
    private TextView songTextView;
    private ImageView songImageView;
    private ImageButton controlButton;
    private ImageButton nextSongButton;
    private ImageButton prevSongButton;
    private ImageArtworkDownload imageArtworkDownload;
    private CardView dismissButton;
    private int playlistSize = 0;
    private int type;  /*  1 - Playlist, 2 - Album*/
    private int trackPos = 1;
    private boolean imageSet = false;
    private NetworkStateChecker networkStateChecker;
    private AudioManager audioManager;
    private Application myApp;
    private ProgressBar controlProgress;
    private String currentPlayerState;

    @SuppressLint("ValidFragment")
    public DeezerListAlarmFragment(long id, int type, final boolean wiFiBool, final Context context, Application application) {
        this.id = id;
        this.type = type;
        this.context = context;
        this.myApp = application;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(((AlarmScreenActivity) context));
        maxVolume = preferences.getInt(SettingsActivity.MAX_ALARM_VOLUME, 8);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_SHOW_UI);
        imageArtworkDownload = new ImageArtworkDownload(((AlarmScreenActivity) context));
        boolean allowToConnect;
        if (wiFiBool) {
            boolean wiFiConnected = (new HelperClass(context)).isWifiConnected();
            if (!wiFiConnected) {
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

        // Restore or Login Deezer Account
        SessionStore sessionStore = new SessionStore();
        deezerConnect = new DeezerConnect(context.getApplicationContext(), DeezerBase.APP_ID);
        if (sessionStore.restore(deezerConnect, context.getApplicationContext())) {
            // Play album or playlist
            if (!allowToConnect) {
                try {
                    throw new DeezerError("Your phone is not connected to WiFi");
                } catch (DeezerError deezerError) {
                    deezerError.printStackTrace();
                    Log.e(TAG, deezerError.getMessage());
                    playDefaultRingtone(audioManager);
                }
            } else {
                playAlarm();
            }
        } else {
            String[] permissions = new String[]{
                    Permissions.BASIC_ACCESS,
                    Permissions.MANAGE_LIBRARY,
                    Permissions.LISTENING_HISTORY};
            // The listener for authentication events
            DialogListener listener = new DialogListener() {
                public void onComplete(Bundle values) {
                    SessionStore sessionStore = new SessionStore();
                    sessionStore.save(deezerConnect, myApp);
                    playAlarm();
                }

                public void onCancel() {
                    playDefaultRingtone(audioManager);
                }

                public void onException(Exception e) {
                    playDefaultRingtone(audioManager);
                }
            };
            // Launches the authentication process
            deezerConnect.authorize((AlarmScreenActivity) context, permissions, listener);
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
        initializeLayoutViews(view);
        initializeDismissButton();
        initControlButtons();
        initPlaylistPlayer();
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        if (player != null) {
            player.stop();
            player.release();
        }
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
        }
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
                Intent intent = new Intent((((AlarmScreenActivity) context)), QuoteActivity.class);
                (((AlarmScreenActivity) context)).finishApp();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void repeatListAlarm() {
        if (type == 1) {
            ((PlaylistPlayer) player).playPlaylist(id, 0);
            trackPos = 0;
        } else if (type == 2) {
            ((AlbumPlayer) player).playAlbum(id, 0);
            trackPos = 0;
        }
    }

    private void playAlarm() {
        Log.e(TAG, type + " is Type called PlayListAlarm");
        if (type == 1) {
            try {
                player = new PlaylistPlayer(myApp, deezerConnect, networkStateChecker);
                //initPlaylistPlayer();
            } catch (TooManyPlayersExceptions | DeezerError tooManyPlayersExceptions) {
                Log.e(TAG, "Deezer Error " + tooManyPlayersExceptions.getMessage());
                tooManyPlayersExceptions.printStackTrace();
                playDefaultRingtone(audioManager);
            }
            // Play playlist
            ((PlaylistPlayer) player).playPlaylist(id);
            Log.e(TAG, "Play playlist with id " + id);
            player.setStereoVolume(maxVolume, maxVolume);
        } else if (type == 2) {
            try {
                player = new AlbumPlayer(myApp, deezerConnect, networkStateChecker);
                //initPlaylistPlayer();
            } catch (TooManyPlayersExceptions tooManyPlayersExceptions) {
                tooManyPlayersExceptions.printStackTrace();
                playDefaultRingtone(audioManager);
                Log.e(TAG, "Deezer Error " + tooManyPlayersExceptions.getMessage());
            } catch (DeezerError deezerError) {
                Log.e(TAG, "Deezer Error " + deezerError.getMessage());
                deezerError.printStackTrace();
                playDefaultRingtone(audioManager);
            }
            // Play album
            player.setStereoVolume(maxVolume, maxVolume);
            ((AlbumPlayer) player).playAlbum(id, 0);
        }
    }

    private void playDefaultRingtone(AudioManager audioManager) {
        mPlayer = new MediaPlayer();
        mPlayer.setVolume(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        String tone;
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(((AlarmScreenActivity) context));
            tone = preferences.getString(SettingsActivity.SELECTED_RINGTONE_URI, "");
            if (tone.equalsIgnoreCase("")) {
                RingtoneManager ringtoneMgr = new RingtoneManager(((AlarmScreenActivity) context));
                ringtoneMgr.setType(RingtoneManager.TYPE_ALARM);
                Cursor alarmsCursor = ringtoneMgr.getCursor();
                int alarmsCount = alarmsCursor.getCount();
                if (alarmsCount == 0 && !alarmsCursor.moveToFirst()) {
                    alarmsCursor.close();
                } else {
                    int currentPosition = alarmsCursor.getPosition();
                    tone = ringtoneMgr.getRingtoneUri(currentPosition).toString();
                    alarmsCursor.close();
                }
            }
            Uri toneUri = Uri.parse(tone);
            mPlayer.setDataSource(((AlarmScreenActivity) context), toneUri);
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mPlayer.setLooping(true);
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception f) {
            f.printStackTrace();
        }
    }

    void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    void initControlButtons() {

        controlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPlayerState.equalsIgnoreCase("PLAYING") || currentPlayerState.equalsIgnoreCase("LOADING")) {
                    player.pause();
                } else if (currentPlayerState.equalsIgnoreCase("PAUSED")) {
                    player.play();
                } else if (currentPlayerState.equalsIgnoreCase("RELEASED")) {
                    playAlarm();
                    trackPos = 1;
                } else if (currentPlayerState.equalsIgnoreCase("STOPPED")) {
                    repeatListAlarm();
                    trackPos = 1;
                }
            }
        });
        nextSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackPos++;
                player.skipToNextTrack();
                nextSongButton.setEnabled(false);
                nextSongButton.setClickable(false);
                nextSongButton.setImageResource(R.drawable.ic_av_cant_skip_next);
                new CountDownTimer(2000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        nextSongButton.setEnabled(true);
                        nextSongButton.setClickable(true);
                        if (trackPos >= playlistSize) {
                            trackPos = playlistSize;
                            nextSongButton.setImageResource(R.drawable.ic_av_cant_skip_next);
                        } else {
                            nextSongButton.setImageResource(R.drawable.ic_av_skip_next);
                        }
                    }
                }.start();

            }
        });
        prevSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackPos--;
                player.skipToPreviousTrack();
                prevSongButton.setEnabled(false);
                prevSongButton.setClickable(false);
                prevSongButton.setImageResource(R.drawable.ic_av_cant_skip_previous);
                new CountDownTimer(2000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        prevSongButton.setEnabled(true);
                        prevSongButton.setClickable(true);
                        prevSongButton.setImageResource(R.drawable.ic_av_skip_previous);
                        if (trackPos <= 1) {
                            trackPos = 1;
                            prevSongButton.setImageResource(R.drawable.ic_av_cant_skip_previous);
                        }
                    }
                }.start();
                if (trackPos <= 1) {
                    trackPos = 1;
                    prevSongButton.setImageResource(R.drawable.ic_av_cant_skip_previous);
                }
            }
        });
    }

    private void initPlaylistPlayer() {
        PlayerWrapperListener playerWrapperListener = new PlayerWrapperListener() {
            @Override
            public void onAllTracksEnded() {
                songTextView.setText("");
                artistTextView.setText("");
                controlButton.setImageResource(R.drawable.ic_av_play_arrow);
            }

            @Override
            public void onPlayTrack(Track track) {
                Log.e(TAG, "playing track " + track.getTitle());
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
                    imageArtworkDownload.getPlaylistImage(track.getAlbum().getImageUrl(AImageOwner.ImageSize.big), songImageView);
                }
            }

            @Override
            public void onTrackEnded(Track track) {
                Log.e(TAG, track.getTitle() + " have Ended");
                songTextView.setText("");
                artistTextView.setText("");
                trackPos++;
            }

            @Override
            public void onRequestException(Exception e, Object o) {
                Log.e(TAG, "RequestError " + e.getMessage());
            }

        };
        player.addOnBufferErrorListener(new OnBufferErrorListener() {
            @Override
            public void onBufferError(Exception e, double v) {
                Log.e(TAG, "Buffer Error " + e.getMessage());
            }
        });
        player.addOnBufferStateChangeListener(new OnBufferStateChangeListener() {
            @Override
            public void onBufferStateChange(BufferState bufferState, double v) {
                Log.e(TAG, "BufferState" + bufferState.name());
            }
        });
        player.addOnPlayerStateChangeListener(new OnPlayerStateChangeListener() {
            @Override
            public void onPlayerStateChange(PlayerState playerState, long l) {
                Log.e(TAG, "PlayerStateChanged to - " + playerState.name());
                if (playerState.compareTo(PlayerState.valueOf("PLAYING")) == 0) {
                    ((AlarmScreenActivity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            controlButton.setVisibility(View.VISIBLE);
                            controlProgress.setVisibility(View.INVISIBLE);
                            controlButton.setImageResource(R.drawable.ic_av_pause);
                            currentPlayerState = "PLAYING";
                        }
                    });
                    updateProgressBar();
                    seekBar.setMax((int) player.getTrackDuration());
                } else if (playerState.compareTo(PlayerState.valueOf("WAITING_FOR_DATA")) == 0) {
                    ((AlarmScreenActivity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            controlButton.setVisibility(View.INVISIBLE);
                            controlProgress.setVisibility(View.VISIBLE);
                            currentPlayerState = "LOADING";
                        }
                    });
                } else if (playerState.compareTo(PlayerState.valueOf("INITIALIZING")) == 0) {
                    currentPlayerState = "LOADING";
                } else if (playerState.compareTo(PlayerState.valueOf("PAUSED")) == 0) {
                    ((AlarmScreenActivity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            controlButton.setImageResource(R.drawable.ic_av_play_arrow);
                        }
                    });
                    currentPlayerState = "PAUSED";
                } else if (playerState.compareTo(PlayerState.valueOf("PLAYBACK_COMPLETED")) == 0) {
                    currentPlayerState = "STOPPED";
                } else if (playerState.compareTo(PlayerState.valueOf("READY")) == 0) {
                    currentPlayerState = "LOADING";
                } else if (playerState.compareTo(PlayerState.valueOf("RELEASED")) == 0) {
                    ((AlarmScreenActivity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            controlButton.setVisibility(View.VISIBLE);
                            controlProgress.setVisibility(View.INVISIBLE);
                            controlButton.setImageResource(R.drawable.ic_av_play_arrow);
                            currentPlayerState = "RELEASED";
                        }
                    });
                } else if (playerState.compareTo(PlayerState.valueOf("STARTED")) == 0) {
                    currentPlayerState = "LOADING";
                } else if (playerState.compareTo(PlayerState.valueOf("STOPPED")) == 0) {
                    ((AlarmScreenActivity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            controlButton.setImageResource(R.drawable.ic_av_play_arrow);
                            currentPlayerState = "STOPPED";
                        }
                    });
                }
            }
        });
        player.addPlayerListener(playerWrapperListener);
    }

    private void initializeLayoutViews(View view) {
        Typeface robotoRegular = Typeface.createFromAsset(((AlarmScreenActivity) context).getAssets(), "Roboto-Regular.ttf");
        artistTextView = (TextView) view.findViewById(R.id.artistTextView);
        songTextView = (TextView) view.findViewById(R.id.songTextView);
        artistTextView.setTypeface(robotoRegular);
        songTextView.setTypeface(robotoRegular);
        songImageView = (ImageView) view.findViewById(R.id.songImage);
        controlButton = (ImageButton) (view.findViewById(R.id.controlButtonLayout)).findViewById(R.id.controlButton);
        controlProgress = (ProgressBar) (view.findViewById(R.id.controlButtonLayout)).findViewById(R.id.control_progress);
        controlButton.setVisibility(View.INVISIBLE);
        controlProgress.setVisibility(View.VISIBLE);
        nextSongButton = (ImageButton) view.findViewById(R.id.nextSongButton);
        prevSongButton = (ImageButton) view.findViewById(R.id.prevSongButton);
        dismissButton = (CardView) view.findViewById(R.id.stopAlarmButton);
        TextView buttonText = (TextView) dismissButton.findViewById(R.id.buttonText);
        buttonText.setTypeface(robotoRegular);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setProgress(0);
        seekBar.setMax(100);
    }

}
