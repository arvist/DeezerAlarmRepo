package com.cikoapps.deezeralarm.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cikoapps.deezeralarm.HelperClasses.DeezerBase;
import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.models.DeviceRingtone;

import java.util.ArrayList;

public class SettingsActivity extends DeezerBase {
    private static final String TAG = "SettingsActivity";
    Toolbar toolbar;
    Context context;
    private RadioButton tempRadioButton;
    private RadioButton windRadioButton;
    private RadioButton radioButtonWifi;
    private ImageButton ringtoneButton;
    private ImageButton refreshRateButton;
    private TextView disconnectTextView;
    private TextView textTimeSelected;
    private TextView textRingtoneInfo;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean windSelected = false;
    boolean tempSelected = false;
    int refreshTime = -1;
    int selectedRingtone = -1;
    String[] ringtoneElements;
    final String elements[] = {"5 minutes ", "10 minutes", "15 minutes ", "20 minutes", "25 minutes", "30 minutes",
            "35 minutes", "40 minutes", "45 minutes", "50 minutes", "55 minutes", "60 minutes", "Do not refresh automatically"};
    private ArrayList<DeviceRingtone> ringtoneList;
    private boolean wifiSelected;
    AlertDialog.Builder refreshRateDialogBuilder;
    Dialog refreshRateDialog;
    AlertDialog.Builder ringtoneDialogBuilder;
    Dialog ringtoneDialog;
    RingtoneAcquire ringtoneAcquire;
    private Activity thisActivity;
    private ProgressBar ringtoneProgress;
    private boolean gettingRingtoneListFinished = false;
    private ProgressBar refreshProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        thisActivity = this;
        setContentView(R.layout.settings_activity_layout);
        appBarActions();
        toolbar = (Toolbar) findViewById(R.id.appBar);
        setSupportActionBar(toolbar);
        initViews();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
        if (ringtoneAcquire == null) {
            ringtoneAcquire = new RingtoneAcquire();
            Log.e(TAG, "Executing ringtone acquiring");
            ringtoneAcquire.execute();
        }
    }

    private void initViews() {
        tempRadioButton = (RadioButton) findViewById(R.id.layoutTemp).findViewById(R.id.radioButtonTemp);
        windRadioButton = (RadioButton) findViewById(R.id.layoutWind).findViewById(R.id.radioButtonWind);
        radioButtonWifi = (RadioButton) findViewById(R.id.layoutWiFi).findViewById(R.id.radioButtonWifi);
        disconnectTextView = (TextView) findViewById(R.id.layoutAccount).findViewById(R.id.disconnect);
        textTimeSelected = (TextView) findViewById(R.id.layoutRefresh).findViewById(R.id.textTimeSelected);
        textRingtoneInfo = (TextView) findViewById(R.id.layoutRingtone).findViewById(R.id.textRingtoneInfo);
        ringtoneButton = (ImageButton) findViewById(R.id.layoutRingtone).findViewById(R.id.buttonRingtone);
        ringtoneProgress = (ProgressBar) findViewById(R.id.layoutRingtone).findViewById(R.id.cover_progress);
        ringtoneProgress.setVisibility(View.GONE);
        ringtoneButton.setVisibility(View.VISIBLE);
        refreshRateButton = (ImageButton) findViewById(R.id.layoutRefresh).findViewById(R.id.buttonRate);
        refreshProgress = (ProgressBar) findViewById(R.id.layoutRefresh).findViewById(R.id.cover_progress_refresh);
        refreshProgress.setVisibility(View.GONE);
        setValues();
        setListeners();
    }

    private void buildDialogs() {
        refreshRateDialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
        refreshRateDialogBuilder.setSingleChoiceItems(elements, refreshTime, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                refreshTime = which;
            }
        });
        refreshRateDialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textTimeSelected.setText(elements[refreshTime]);
                editor.putInt("selectedInterval", refreshTime);
            }
        });
        refreshRateDialog = refreshRateDialogBuilder.create();
        ringtoneDialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
        ringtoneElements = new String[ringtoneList.size()];
        int i = 0;
        for (DeviceRingtone dr : ringtoneList) {
            ringtoneElements[i] = dr.title;
            i++;
        }
        ringtoneDialogBuilder.setSingleChoiceItems(ringtoneElements, selectedRingtone, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedRingtone = which;
            }
        });
        ringtoneDialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textRingtoneInfo.setText(ringtoneElements[selectedRingtone]);
                editor.putInt("selectedRingtone", selectedRingtone);
                editor.putString("selectedRingtoneTitle", ringtoneElements[selectedRingtone]);
                editor.putString("selectedRingtoneUri", ringtoneList.get(selectedRingtone).Uri);
            }
        });
        ringtoneDialog = ringtoneDialogBuilder.create();
        ringtoneDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ringtoneProgress.setVisibility(View.GONE);
                ringtoneButton.setVisibility(View.VISIBLE);
            }
        });
        refreshRateDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                refreshProgress.setVisibility(View.GONE);
                refreshRateButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setValues() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean windMiles = preferences.getBoolean("windMilesBool", false);
        boolean tempFBool = preferences.getBoolean("tempFBool", false);
        boolean wifiBool = preferences.getBoolean("wifiSelected", false);
        windRadioButton.setChecked(windMiles);
        tempRadioButton.setChecked(tempFBool);
        radioButtonWifi.setChecked(wifiBool);
        windSelected = windMiles;
        tempSelected = tempFBool;
        wifiSelected = wifiBool;
        textTimeSelected.setText(elements[preferences.getInt("selectedInterval", 1)]);
        refreshTime = preferences.getInt("selectedInterval", 1);
        textRingtoneInfo.setText(preferences.getString("selectedRingtoneTitle", "Not Selected"));
        selectedRingtone = preferences.getInt("selectedRingtone", 1);
    }

    private void setListeners() {
        View.OnClickListener tempClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "temp on click listener");
                Log.e(TAG, tempSelected + " ");
                if (tempSelected) {
                    tempRadioButton.setChecked(false);
                    tempSelected = false;
                } else {
                    tempRadioButton.setChecked(true);
                    tempSelected = true;
                }
                if (tempSelected) {
                    editor.putBoolean("tempFBool", true);
                } else {
                    editor.putBoolean("tempFBool", false);
                }
            }
        };
        tempRadioButton.setOnClickListener(tempClickListener);
        windRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (windSelected) {
                    windRadioButton.setChecked(false);
                    windSelected = false;
                } else {
                    windRadioButton.setChecked(true);
                    windSelected = true;
                }
                if (windSelected) {
                    editor.putBoolean("windMilesBool", true);
                } else {
                    editor.putBoolean("windMilesBool", false);
                }
            }
        });
        radioButtonWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiSelected) {
                    radioButtonWifi.setChecked(false);
                    wifiSelected = false;
                } else {
                    radioButtonWifi.setChecked(true);
                    wifiSelected = true;
                }
                if (wifiSelected) {
                    editor.putBoolean("wifiSelected", true);
                } else {
                    editor.putBoolean("wifiSelected", false);
                }
            }
        });
        disconnectTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutDeezer();
                Toast.makeText(context, "Your Deezer account is no longer connected to app", Toast.LENGTH_SHORT).show();
            }
        });
        refreshRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gettingRingtoneListFinished) {
                    refreshProgress.setVisibility(View.VISIBLE);
                    refreshRateButton.setVisibility(View.GONE);
                }
                new CountDownTimer(250, 80) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        if (refreshRateDialog != null) {
                            if (!refreshRateDialog.isShowing() && !ringtoneDialog.isShowing()) {
                                refreshRateDialog.show();
                            }
                        } else {
                            new CountDownTimer(250, 80) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                }

                                @Override
                                public void onFinish() {
                                    refreshRateButton.callOnClick();
                                }
                            }.start();
                        }
                    }
                }.start();
            }
        });

        ringtoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gettingRingtoneListFinished) {
                    ringtoneProgress.setVisibility(View.VISIBLE);
                    ringtoneButton.setVisibility(View.GONE);
                }
                new CountDownTimer(250, 80) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        if (ringtoneDialog != null) {
                            if (!ringtoneDialog.isShowing() && !refreshRateDialog.isShowing()) {
                                ringtoneDialog.show();
                            }
                        } else {
                            new CountDownTimer(250, 80) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                }

                                @Override
                                public void onFinish() {
                                    ringtoneButton.callOnClick();
                                }
                            }.start();
                        }
                    }
                }.start();
            }
        });
    }

    private void appBarActions() {
        ImageButton backButton = (ImageButton) findViewById(R.id.app_bar_back_btn);
        ImageButton saveButton = (ImageButton) findViewById(R.id.app_bar_save);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.commit();
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    class RingtoneAcquire extends AsyncTask<Void, Integer, String> {
        @Override
        protected String doInBackground(Void... params) {
            getRingtones();
            return "Post execute";
        }

        public void getRingtones() {
            ringtoneList = new ArrayList<>();
            RingtoneManager ringtoneMgr = new RingtoneManager(context);
            ringtoneMgr.setType(RingtoneManager.TYPE_ALARM);
            Cursor alarmsCursor = ringtoneMgr.getCursor();
            if (alarmsCursor.moveToFirst()) {
                do {
                    DeviceRingtone deviceRingtone = new DeviceRingtone();
                    int currentPosition = alarmsCursor.getPosition();
                    deviceRingtone.title = ringtoneMgr.getRingtone(currentPosition).getTitle(context);
                    deviceRingtone.Uri = ringtoneMgr.getRingtoneUri(currentPosition).toString();
                    ringtoneList.add(deviceRingtone);
                } while (alarmsCursor.moveToNext());
                alarmsCursor.close();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            buildDialogs();
            gettingRingtoneListFinished = true;
        }
    }
}
