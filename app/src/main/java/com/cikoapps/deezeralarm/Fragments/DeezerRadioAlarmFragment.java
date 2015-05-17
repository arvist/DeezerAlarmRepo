package com.cikoapps.deezeralarm.fragments;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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
import android.widget.Toast;

import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.activities.AlarmScreenActivity;
import com.cikoapps.deezeralarm.activities.DeezerBase;
import com.cikoapps.deezeralarm.activities.QuoteActivity;
import com.cikoapps.deezeralarm.helpers.HelperClass;
import com.cikoapps.deezeralarm.helpers.ImageArtworkDownload;
import com.deezer.sdk.model.AImageOwner;
import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.player.RadioPlayer;
import com.deezer.sdk.player.event.BufferState;
import com.deezer.sdk.player.event.OnBufferErrorListener;
import com.deezer.sdk.player.event.OnBufferStateChangeListener;
import com.deezer.sdk.player.event.OnPlayerProgressListener;
import com.deezer.sdk.player.event.OnPlayerStateChangeListener;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.event.RadioPlayerListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.NetworkStateChecker;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;
import com.deezer.sdk.player.networkcheck.WifiOnlyNetworkStateChecker;

public class DeezerRadioAlarmFragment extends Fragment {
    private static final String TAG = "DeezerRadioAlarmFrag";
    private Typeface robotoRegular;
    private long id;
    private MediaPlayer mPlayer;
    private SeekBar seekBar;
    private RadioPlayer radioPlayer;
    private TextView artistTextView;
    private TextView songTextView;
    private ImageView songImageView;
    private ImageButton controlButton;
    private ImageButton nextSongButton;
    private ImageButton prevSongButton;
    private CardView dismissButton;
    private ImageArtworkDownload imageArtworkDownload;
    private String currentPlayerState;
    private int type;
    private boolean WiFiConnected;
    private boolean allowToConnect = false;
    private AudioManager audioManager;
    private NetworkStateChecker networkStateChecker;
    private Context context;
    private Application myApp;
    private final Handler mHandler = new Handler();
    private DeezerConnect deezerConnect;
    private final Runnable mUpdateTimeTask = new Runnable() {
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
    private ProgressBar controlProgress;
    private int maxVolume;
    DialogListener listener = new DialogListener() {
        public void onComplete(Bundle values) {
            SessionStore sessionStore = new SessionStore();
            sessionStore.save(deezerConnect, myApp);
            playRadioRingtone();
            try {
                if (mPlayer != null) {
                    mPlayer.release();
                }
            } catch (IllegalStateException ignored) {
            }
        }

        public void onCancel() {
        }

        public void onException(Exception e) {
        }
    };
    CountDownTimer countDownTimer = new CountDownTimer(30 * 1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            if (radioPlayer == null) {
                Log.e(TAG, "Player is null, playing default ringtone;");
                playDefaultRingtone();
            } else {
                Log.e(TAG, "Countdown timer finished not playing default ringtone, list player is playing");
            }
        }
    };
    String[] permissions = new String[]{
            Permissions.BASIC_ACCESS,
            Permissions.MANAGE_LIBRARY,
            Permissions.LISTENING_HISTORY};

    public DeezerRadioAlarmFragment() {
        super();
    }

    @SuppressLint("ValidFragment")
    public DeezerRadioAlarmFragment(long id, int type, boolean WifiBool, Context context, Application application) {
        super();
        this.id = id;
        this.type = type;
        this.myApp = application;
        this.context = context;
        audioManager = (AudioManager) ((AlarmScreenActivity) context).getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_PLAY_SOUND);
        imageArtworkDownload = new ImageArtworkDownload(((AlarmScreenActivity) context));
        boolean allowedToConnect = isAllowedToConnect(WifiBool, context);
        // Restore or Login Deezer Account

        if (allowToConnect) {
            SessionStore sessionStore = new SessionStore();
            deezerConnect = new DeezerConnect(myApp, DeezerBase.APP_ID);
            if (sessionStore.restore(deezerConnect, context.getApplicationContext())) {
                playRadioRingtone();

            } else {
                Log.e(TAG, "Deezer session restore failed");
                // Launches the authentication process
                Log.e(TAG, "Authorizing deezer account");
                deezerConnect.authorize(((AlarmScreenActivity) context), permissions, listener);
                countDownTimer.start();
            }
        } else {
            playDefaultRingtone();
        }
    }

    private boolean isAllowedToConnect(boolean WifiBool, Context context) {
        if (WifiBool) {
            WiFiConnected = (new HelperClass(context)).isWifiConnected();
            if (!WiFiConnected) {
                allowToConnect = false;
            } else {
                networkStateChecker = new WifiOnlyNetworkStateChecker();
                allowToConnect = true;
            }
        } else {
            networkStateChecker = new WifiAndMobileNetworkStateChecker();
            allowToConnect = true;
        }
        return allowToConnect;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.alarm_player_fragment, container, false);
        initializeLayoutViews(view);
        initializeDismissButton();
        initControlButtons();
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        return view;
    }


    private void playRadioRingtone() {
        try {
            radioPlayer = new RadioPlayer(myApp, deezerConnect, networkStateChecker);
            initArtistRadioPlayer();
        } catch (TooManyPlayersExceptions tooManyPlayersExceptions) {
            tooManyPlayersExceptions.printStackTrace();
            playDefaultRingtone();
        } catch (DeezerError deezerError) {
            deezerError.printStackTrace();
            playDefaultRingtone();
        }
        radioPlayer.setStereoVolume(maxVolume, maxVolume);
        if (type == 3) {
            radioPlayer.playRadio(RadioPlayer.RadioType.ARTIST, id);
        }
        if (type == 4) {
            radioPlayer.playRadio(RadioPlayer.RadioType.RADIO, id);
        }
    }

    private void initializeDismissButton() {
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioPlayer != null) {
                    radioPlayer.stop();
                    radioPlayer.release();
                }
                try {
                    if (mPlayer != null) {
                        mPlayer = new MediaPlayer();
                        mPlayer.release();
                    }
                } catch (IllegalStateException ignored) {

                }
                Intent intent = new Intent(((AlarmScreenActivity) context), QuoteActivity.class);
                (((AlarmScreenActivity) context)).finishApp();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (radioPlayer != null) {
            radioPlayer.stop();
            radioPlayer.release();
        }
        try {
            if (mPlayer != null) {
                mPlayer = new MediaPlayer();
                mPlayer.release();
            }
        } catch (IllegalStateException ignored) {

        }
    }

    private void playDefaultRingtone() {
        Log.e(TAG, "Playing default ringtone");
        audioManager = (AudioManager) ((AlarmScreenActivity) context).getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_PLAY_SOUND);
        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(context, alert);
            mPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            mPlayer.setLooping(true);
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
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
                    radioPlayer.pause();
                } else if (currentPlayerState.equalsIgnoreCase("PAUSED")) {
                    radioPlayer.play();
                } else if (currentPlayerState.equalsIgnoreCase("RELEASED")) {
                    playRadioRingtone();
                } else if (currentPlayerState.equalsIgnoreCase("STOPPED")) {
                    if (type == 3) {
                        radioPlayer.playRadio(RadioPlayer.RadioType.ARTIST, id);
                    } else if (type == 4) {
                        radioPlayer.playRadio(RadioPlayer.RadioType.RADIO, id);
                    }
                }
            }
        });
        nextSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioPlayer.skipToNextTrack();
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
                        nextSongButton.setImageResource(R.drawable.ic_av_skip_next);
                    }
                }.start();
            }
        });
        prevSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    void initializeLayoutViews(View view) {
        robotoRegular = Typeface.createFromAsset(((AlarmScreenActivity) context).getAssets(), "Roboto-Regular.ttf");
        artistTextView = (TextView) view.findViewById(R.id.artistTextView);
        songTextView = (TextView) view.findViewById(R.id.songTextView);
        artistTextView.setTypeface(robotoRegular);
        songTextView.setTypeface(robotoRegular);
        songImageView = (ImageView) view.findViewById(R.id.songImage);
        songImageView.setImageResource(R.drawable.ic_no_song_image);
        controlButton = (ImageButton) view.findViewById(R.id.controlButton);
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


    void initArtistRadioPlayer() {
        if (radioPlayer != null) {
            addPlayerListener();
        } else {
            new CountDownTimer(3000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    initArtistRadioPlayer();
                }
            }.start();
        }
    }

    private void addPlayerListener() {
        radioPlayer.addOnPlayerStateChangeListener(new OnPlayerStateChangeListener() {
            @Override
            public void onPlayerStateChange(PlayerState playerState, long l) {
                //Log.e(TAG, "PlayerStateChanged to - " + playerState.name());
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
                    seekBar.setMax((int) radioPlayer.getTrackDuration());
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

        radioPlayer.addPlayerListener(new RadioPlayerListener() {
            @Override
            public void onAllTracksEnded() {
                songTextView.setText("");
                artistTextView.setText("");
                controlButton.setImageResource(R.drawable.ic_av_stop);
            }

            @Override
            public void onPlayTrack(Track track) {
                seekBar.setProgress(0);
                prevSongButton.setImageResource(R.drawable.ic_av_cant_skip_previous);
                songTextView.setText(track.getTitle());
                artistTextView.setText(track.getArtist().getName());
                songImageView.setImageResource(R.drawable.ic_no_song_image);
                imageArtworkDownload.cancelImageLoadTask();
                imageArtworkDownload.setPlaylistArtworkImage(track.getAlbum().getImageUrl(AImageOwner.ImageSize.big), songImageView);
            }

            @Override
            public void onTrackEnded(Track track) {
                songTextView.setText("");
                artistTextView.setText("");
            }

            @Override
            public void onRequestException(Exception e, Object o) {
                //Log.e(TAG, e.getMessage());
            }

            @Override
            public void onTooManySkipsException() {
                ((AlarmScreenActivity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(((AlarmScreenActivity) context), "Error! Too many Skips", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        radioPlayer.addOnBufferErrorListener(new OnBufferErrorListener() {
            @Override
            public void onBufferError(Exception e, double v) {
                //Log.e(TAG, "Buffer Error " + e.getMessage());
            }
        });
        radioPlayer.addOnBufferStateChangeListener(new OnBufferStateChangeListener() {
            @Override
            public void onBufferStateChange(BufferState bufferState, double v) {
                //Log.e(TAG, "BufferState" + bufferState.name());
            }
        });
    }


}