package com.cikoapps.deezeralarm.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cikoapps.deezeralarm.HelperClasses.DeezerBase;
import com.cikoapps.deezeralarm.R;

public class SettingsActivity extends DeezerBase {
    private static final String TAG = "SettingsActivity";
    Toolbar toolbar;
    Context context;
    private RadioButton tempRadioButton;
    private RadioButton windRadioButton;
    private RadioButton clockRadioButton;
    private ImageButton ringtoneButton;
    private ImageButton refreshRateButton;
    private TextView connectTextView;
    private TextView disconnectTextView;
    private TextView textTimeSelected;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean windSelected = false;
    boolean tempSelected = false;
    int refreshTime = -1;
    final String elements[] = {"5 minutes ", "10 minutes", "15 minutes ", "20 minutes", "25 minutes", "30 minutes",
            "35 minutes", "40 minutes", "45 minutes", "50 minutes", "55 minutes", "60 minutes"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.settings_activity_layout);
        appBarActions();
        toolbar = (Toolbar) findViewById(R.id.appBar);
        setSupportActionBar(toolbar);
        initViews();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
    }

    private void initViews() {
        tempRadioButton = (RadioButton) ((RelativeLayout) findViewById(R.id.layoutTemp)).findViewById(R.id.radioButtonTemp);
        windRadioButton = (RadioButton) ((RelativeLayout) findViewById(R.id.layoutWind)).findViewById(R.id.radioButtonWind);
        ringtoneButton = (ImageButton) ((RelativeLayout) findViewById(R.id.layoutRingtone)).findViewById(R.id.buttonRingtone);
        connectTextView = (TextView) ((RelativeLayout) findViewById(R.id.layoutAccount)).findViewById(R.id.connect);
        disconnectTextView = (TextView) ((RelativeLayout) findViewById(R.id.layoutAccount)).findViewById(R.id.disconnect);
        textTimeSelected = (TextView) ((RelativeLayout) findViewById(R.id.layoutRefresh)).findViewById(R.id.textTimeSelected);
        refreshRateButton = (ImageButton) ((RelativeLayout) findViewById(R.id.layoutRefresh)).findViewById(R.id.buttonRate);

        setValues();
        setListeners();

    }

    private void setValues() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean windMiles = preferences.getBoolean("windMilesBool", false);
        boolean tempFBool = preferences.getBoolean("tempFBool", false);
        windRadioButton.setChecked(windMiles);
        tempRadioButton.setChecked(tempFBool);
        windSelected = windMiles;
        tempSelected = tempFBool;
        textTimeSelected.setText(elements[preferences.getInt("selectedInterval", 1)]);
        refreshTime = preferences.getInt("selectedInterval", 1);

    }

    private void setListeners() {
        tempRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

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
        connectTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginDeezer();
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
                AlertDialog.Builder refreshRateDialog = new AlertDialog.Builder(SettingsActivity.this);
                refreshRateDialog.setSingleChoiceItems(elements, refreshTime, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        refreshTime = which;
                        Log.e(TAG, refreshTime + "");
                    }
                });
                refreshRateDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        textTimeSelected.setText(elements[refreshTime]);
                        editor.putInt("selectedInterval", refreshTime);
                    }
                });
                refreshRateDialog.show();
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
}
