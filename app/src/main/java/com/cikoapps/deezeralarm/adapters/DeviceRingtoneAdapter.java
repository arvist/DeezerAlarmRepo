package com.cikoapps.deezeralarm.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.cikoapps.deezeralarm.Activities.RingtoneActivity;
import com.cikoapps.deezeralarm.Fragments.DeviceRingtoneFragment;
import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.models.DeviceRingtone;

import java.io.IOException;
import java.util.ArrayList;


public class DeviceRingtoneAdapter extends RecyclerView.Adapter<DeviceRingtoneAdapter.DeviceRingtoneViewHolder> {
    private static Typeface robotoRegular;
    private final LayoutInflater inflater;
    private final Context context;
    public int selectedPosition = -1;
    private ArrayList<DeviceRingtone> deviceRingtones = new ArrayList<>();
    private MediaPlayer mediaPlayer;


    public DeviceRingtoneAdapter(Context context, ArrayList<DeviceRingtone> deviceRingtones, MediaPlayer media) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.deviceRingtones = deviceRingtones;
        if (deviceRingtones.get(deviceRingtones.size() - 1) != null)
            this.deviceRingtones.add(null);
        robotoRegular = Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf");
        this.mediaPlayer = media;

    }


    @Override
    public DeviceRingtoneViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.device_ringtone_item, viewGroup, false);
        return new DeviceRingtoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DeviceRingtoneViewHolder deviceRingtoneViewHolder, final int position) {

        final DeviceRingtone deviceRingtone = deviceRingtones.get(position);
        if (deviceRingtone == null) {
            deviceRingtoneViewHolder.deviceRingtoneRadioButton.setWillNotDraw(true);
            deviceRingtoneViewHolder.itemTextView.setWillNotDraw(true);
            deviceRingtoneViewHolder.soundImageButton.setImageDrawable(null);

        } else {
            deviceRingtoneViewHolder.soundImageButton.setImageResource(R.drawable.ic_image_audiotrack);
            deviceRingtoneViewHolder.deviceRingtoneRadioButton.setWillNotDraw(false);
            deviceRingtoneViewHolder.itemTextView.setWillNotDraw(false);
            deviceRingtoneViewHolder.itemTextView.setText(deviceRingtone.title);
            deviceRingtoneViewHolder.itemTextView.setTypeface(robotoRegular);
            if (deviceRingtone.selected && RingtoneActivity.selectedRingtone.type == 0) {
                deviceRingtoneViewHolder.deviceRingtoneRadioButton.setChecked(true);
            } else deviceRingtoneViewHolder.deviceRingtoneRadioButton.setChecked(false);
            deviceRingtoneViewHolder.deviceRingtoneRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedPosition == -1) {
                        deviceRingtone.selected = true;
                        selectedPosition = position;
                        notifyItemChanged(position);
                        DeviceRingtoneFragment.updateSelectedRingtone(deviceRingtone.Uri, deviceRingtone.title);
                    } else if (selectedPosition == position) {
                        deviceRingtone.selected = false;
                        selectedPosition = -1;
                        notifyItemChanged(position);
                        DeviceRingtoneFragment.updateSelectedRingtone(null, "");
                    } else {
                        deviceRingtones.get(selectedPosition).selected = false;
                        notifyItemChanged(selectedPosition);
                        deviceRingtone.selected = true;
                        selectedPosition = position;
                        notifyItemChanged(position);
                        DeviceRingtoneFragment.updateSelectedRingtone(deviceRingtone.Uri, deviceRingtone.title);
                    }
                }
            });
            deviceRingtoneViewHolder.soundImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (mediaPlayer != null)
                            mediaPlayer.release();
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setDataSource(context, Uri.parse(deviceRingtone.Uri));
                        mediaPlayer.prepare();
                        mediaPlayer.setLooping(false);
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(final MediaPlayer mp) {
                                Handler mHandler = new Handler();
                                mp.start();
                                mHandler.postDelayed(new Runnable() {
                                    public void run() {
                                        try {
                                            mp.release();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, 4 * 1000);
                            }
                        });
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mp.reset();
                                mp.release();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return this.deviceRingtones.size();
    }


    class DeviceRingtoneViewHolder extends RecyclerView.ViewHolder {
        final TextView itemTextView;
        final RadioButton deviceRingtoneRadioButton;
        final View rowItem;
        final ImageButton soundImageButton;

        public DeviceRingtoneViewHolder(View itemView) {
            super(itemView);
            this.rowItem = itemView;
            itemTextView = (TextView) itemView.findViewById(R.id.itemTextView);
            deviceRingtoneRadioButton = (RadioButton) itemView.findViewById(R.id.deviceRingtoneRadioButton);
            soundImageButton = (ImageButton) itemView.findViewById(R.id.soundImageButton);
        }
    }

}