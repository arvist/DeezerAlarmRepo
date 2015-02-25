package com.cikoapps.deezeralarm.Fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import com.cikoapps.deezeralarm.Activities.AlarmScreenActivity;
import com.cikoapps.deezeralarm.HelperClasses.HelperClass;
import com.cikoapps.deezeralarm.HelperClasses.ImageArtworkDownload;
import com.cikoapps.deezeralarm.R;
import com.deezer.sdk.model.AImageOwner;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.player.AbstractTrackListPlayer;
import com.deezer.sdk.player.AlbumPlayer;
import com.deezer.sdk.player.PlaylistPlayer;
import com.deezer.sdk.player.event.OnPlayerErrorListener;
import com.deezer.sdk.player.event.OnPlayerProgressListener;
import com.deezer.sdk.player.event.OnPlayerStateChangeListener;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.event.PlayerWrapperListener;
import com.deezer.sdk.player.exception.DeezerPlayerException;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;


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

    @SuppressLint("ValidFragment")
    public DeezerListAlarmFragment(long id, int type) {
        super();
        this.id = id;
        this.type = type;
        imageArtworkDownload = new ImageArtworkDownload(getActivity());
    }

    public DeezerListAlarmFragment() {
        super();
        return;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.alarm_player_fragment,
                container, false);
        initViews(view);
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
                getActivity().finish();
            }
        });


        try {
            HelperClass helperClass = new HelperClass(getActivity());
            if (!helperClass.haveNetworkConnection()) {
                throw new DeezerPlayerException();
            }
            if (type == 1) {
                player = new PlaylistPlayer(getActivity().getApplication(), ((AlarmScreenActivity) getActivity()).deezerConnect, new WifiAndMobileNetworkStateChecker());
                ((PlaylistPlayer) player).playPlaylist(id);
            } else if (type == 2) {
                player = new AlbumPlayer(getActivity().getApplication(), ((AlarmScreenActivity) getActivity()).deezerConnect, new WifiAndMobileNetworkStateChecker());
                ((AlbumPlayer) player).playAlbum(id);
            }
            initPlaylistPlayer();
            initControlButtons();
        } catch (Exception e) {
            Log.e("DEBUG", e.toString());
            mPlayer = new MediaPlayer();
            try {
                // TODO hard coded tone
                String tone = "content://media/internal/audio/media/7";
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
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        return view;
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
                Log.e("DEBUG", "++ Track Pos" + trackPos);
                boolean canSkip = player.skipToNextTrack();
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
                Log.e("DEBUG", "-- Track Pos" + trackPos);
                boolean canSkip = player.skipToPreviousTrack();
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

                Log.e("DEBUG", "TRACK POS " + trackPos);

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
                if (type == 2 && imageSet) {
                    // Do nothing album image is set
                } else {
                    songImageView.setImageResource(R.drawable.ic_no_song_image);
                }
                if (!imageSet && type == 2) {
                    Log.e(TAG, "Downloading album image");
                    imageArtworkDownload.getAlbum(songImageView, type, id);
                    imageSet = true;
                } else if (type == 1) {
                    Log.e(TAG, "Downloading track image");
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
                Log.e("DEBUG", "TRACK ENDED");
            }

            @Override
            public void onRequestException(Exception e, Object o) {
            }
        };
        player.addOnPlayerStateChangeListener(new OnPlayerStateChangeListener() {
            @Override
            public void onPlayerStateChange(PlayerState playerState, long l) {
                Log.e("DEBUG", playerState.name());
                if (playerState.compareTo(PlayerState.valueOf("PLAYING")) == 0) {

                    updateProgressBar();
                    seekBar.setMax((int) player.getTrackDuration());
                }
            }
        });
        player.addOnPlayerErrorListener(new OnPlayerErrorListener() {
            @Override
            public void onPlayerError(Exception e, long l) {
                Log.e("DEBUG", "PLAYER ERROR " + e.getMessage());
            }
        });

        player.addPlayerListener(playerWrapperListener);
    }

    private void initViews(View view) {
        robotoRegular = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Regular.ttf");
        artistTextView = (TextView) view.findViewById(R.id.artistTextView);
        songTextView = (TextView) view.findViewById(R.id.songTextView);
        artistTextView.setTypeface(robotoRegular);
        songTextView.setTypeface(robotoRegular);
        songImageView = (ImageView) view.findViewById(R.id.songImage);
        controlButton = (ImageButton) view.findViewById(R.id.controlButton);
        nextSongButton = (ImageButton) view.findViewById(R.id.nextSongButton);
        prevSongButton = (ImageButton) view.findViewById(R.id.prevSongButton);
        dismissButton = (CardView) view.findViewById(R.id.alarm_screen_button);
        TextView buttonText = (TextView) dismissButton.findViewById(R.id.buttonText);
        buttonText.setTypeface(robotoRegular);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setProgress(0);
        seekBar.setMax(100);
    }

}

