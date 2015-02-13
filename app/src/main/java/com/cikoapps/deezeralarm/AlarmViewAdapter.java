package com.cikoapps.deezeralarm;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.support.v7.widget.SwitchCompat;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlarmViewAdapter extends RecyclerView.Adapter<AlarmViewAdapter.AlarmViewHolder> {
    private LayoutInflater inflater;
    List<Alarm> alarmList = Collections.emptyList();
    Context context;
    static Typeface notoRegular;
    static Typeface notoBold;


    public AlarmViewAdapter(Context context, List<Alarm> alarmList) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.alarmList = alarmList;
        notoRegular = Typeface.createFromAsset(context.getAssets(), "NotoSerif-Regular.ttf");
        notoBold = Typeface.createFromAsset(context.getAssets(), "NotoSerif-Bold.ttf");

    }

    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.alarm_row, viewGroup, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlarmViewHolder alarmViewHolder, int position) {

        Alarm alarm = alarmList.get(position);
        alarmViewHolder.titleTextView.setText(alarm.title);
        alarmViewHolder.titleTextView.setTypeface(notoRegular);
        alarmViewHolder.titleTextView.setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
        alarmViewHolder.timeTextView.setText(alarm.hour + " : " + alarm.minute);
        alarmViewHolder.timeTextView.setTypeface(notoRegular);
        alarmViewHolder.timeTextView.setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
        alarmViewHolder.alarmSwitch.setWillNotDraw(false);

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion < Build.VERSION_CODES.LOLLIPOP) {
            alarmViewHolder.alarmSwitch.setThumbResource(R.drawable.apptheme_switch_thumb_holo_light);
            alarmViewHolder.alarmSwitch.setTrackResource(R.drawable.apptheme_switch_track_holo_light);
        }

        alarmViewHolder.alarmSwitch.setChecked(alarm.enabled);
        if (areAllFalse(alarm.repeatingDays)) {

            for (int i = 0; i < alarmViewHolder.daysTextViewList.size(); i++) {
                TextView dayTextView = alarmViewHolder.daysTextViewList.get(i);
                dayTextView.setTextColor(context.getResources().getColor(R.color.colorTransparent));
            }
        } else {
            for (int i = 0; i < alarmViewHolder.daysTextViewList.size(); i++) {

                TextView dayTextView = alarmViewHolder.daysTextViewList.get(i);
                dayTextView.setTextColor(context.getResources().getColor(R.color.colorPrimaryText));

                if (alarm.repeatingDays[i]) {
                    dayTextView.setTypeface(notoBold);
                    dayTextView.setTextColor(context.getResources().getColor(R.color.colorAccent));
                } else {
                    dayTextView.setTypeface(notoRegular);
                }
            }
        }
        if (position == alarmList.size() - 1) {
            alarmViewHolder.titleTextView.setTextColor(context.getResources().getColor(R.color.colorTransparent));
            alarmViewHolder.timeTextView.setTextColor(context.getResources().getColor(R.color.colorTransparent));
            alarmViewHolder.alarmSwitch.setWillNotDraw(true);
            for (int i = 0; i < alarmViewHolder.daysTextViewList.size(); i++) {
                TextView dayTextView =  alarmViewHolder.daysTextViewList.get(i);
                dayTextView.setTextColor(context.getResources().getColor(R.color.colorTransparent));
            }

        }
        else {
            alarmViewHolder.rowItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, ((TextView) v.findViewById(R.id.titleTextView)).getText(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    public static boolean areAllFalse(boolean[] array) {
        for (boolean b : array) if (b) return false;
        return true;
    }

    @Override
    public int getItemCount() {
        return this.alarmList.size();
    }


    class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView timeTextView;
        SwitchCompat alarmSwitch;
        LinearLayout daysLinearLayout;
        List<TextView> daysTextViewList;
        View rowItem;

        public AlarmViewHolder(View itemView) {
            super(itemView);
            this.rowItem = itemView;
            daysTextViewList = new ArrayList<>();
            titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
            timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
            alarmSwitch = (SwitchCompat) itemView.findViewById(R.id.alarmSwitch);
            daysLinearLayout = (LinearLayout) itemView.findViewById(R.id.daysLinearLayout);

            daysTextViewList.add(0, (TextView) daysLinearLayout.findViewById(R.id.moTextView));
            daysTextViewList.add(1, (TextView) daysLinearLayout.findViewById(R.id.tuTextView));
            daysTextViewList.add(2, (TextView) daysLinearLayout.findViewById(R.id.weTextView));
            daysTextViewList.add(3, (TextView) daysLinearLayout.findViewById(R.id.thTextView));
            daysTextViewList.add(4, (TextView) daysLinearLayout.findViewById(R.id.frTextView));
            daysTextViewList.add(5, (TextView) daysLinearLayout.findViewById(R.id.saTextView));
            daysTextViewList.add(6, (TextView) daysLinearLayout.findViewById(R.id.suTextView));

        }
    }
}