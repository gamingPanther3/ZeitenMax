package com.mlprograms.zeitenmax;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class ClockActivity extends AppCompatActivity {

    /**
     * Instance of DataManager to handle data-related tasks such as saving and retrieving data.
     */
    private DataManager dataManager;

    /**
     * Instance of SharedPreferences for storing and retrieving small amounts of primitive data as key-value pairs.
     */
    SharedPreferences prefs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clock);

        dataManager = new DataManager(this);
        dataManager.createJSON(getApplicationContext());
        dataManager.checkAndCreateFile();
        setUpListeners();

        // If it's the first run of the application
        prefs = getSharedPreferences("com.mlprograms.RechenMax", MODE_PRIVATE);
        if (prefs.getBoolean("firstrun", true)) {
            dataManager.saveToJSON("showPatchNotes", true, getApplicationContext());
            setContentView(R.layout.patchnotes);
            checkDarkmodeSetting();
            prefs.edit().putBoolean("firstrun", false).apply();
        }

        Log.e("MainActivity", "showPatchNotes=" + dataManager.readFromJSON("showPatchNotes", getApplicationContext()));
        Log.e("MainActivity", "disablePatchNotesTemporary=" + dataManager.readFromJSON("disablePatchNotesTemporary", getApplicationContext()));

        final String showPatNot = dataManager.readFromJSON("showPatchNotes", getApplicationContext());
        final String disablePatNotTemp = dataManager.readFromJSON("disablePatchNotesTemporary", getApplicationContext());

        if (showPatNot != null && disablePatNotTemp != null) {
            if (showPatNot.equals("true") && disablePatNotTemp.equals("false")) {
                setContentView(R.layout.patchnotes);
                setUpListeners();
                checkDarkmodeSetting();
            }
        }
        checkDarkmodeSetting();
    }

    /**
     * Sets up the listeners for each button in the application
     */
    private void setUpListeners() {
        setButtonListener(R.id.settings, this::switchToSettingsAction);
        setButtonListener(R.id.stopwatch, this::switchToStopwatchAction);
        setButtonListener(R.id.timer, this::switchToTimerAction);
        setButtonListener(R.id.alarm, this::switchToAlarmAction);

        setButtonListener(R.id.okay_button, this::patchNotesOkayButtonAction);
    }

    /**
     * Handles the action when the okay button in the patch notes is clicked.
     * Depending on whether the checkbox is checked or not, it saves different values to JSON.
     * Then it sets the content view, loads numbers, checks dark mode setting, checks science button state, and sets up listeners.
     */
    public void patchNotesOkayButtonAction() {
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) CheckBox checkBox = findViewById(R.id.checkBox);
        if (checkBox.isChecked()) {
            dataManager.saveToJSON("showPatchNotes", false, getApplicationContext());
            dataManager.saveToJSON("disablePatchNotesTemporary", true, getApplicationContext());
            dataManager.saveToJSON("settingReleaseNotesSwitch", false, getApplicationContext());
        } else {
            dataManager.saveToJSON("showPatchNotes", true, getApplicationContext());
            dataManager.saveToJSON("disablePatchNotesTemporary", true, getApplicationContext());
            dataManager.saveToJSON("settingReleaseNotesSwitch", true, getApplicationContext());
        }
        setContentView(R.layout.clock);
        //checkDarkmodeSetting();
        setUpListeners();
    }

    /**
     * Sets up the listener for all number buttons
     *
     * @param buttonId The ID of the button to which the listener is to be set.
     * @param action The action which belongs to the button.
     */
    private void setButtonListener(int buttonId, Runnable action) {
        Button btn = findViewById(buttonId);
        if(btn != null) {
            btn.setOnClickListener(v -> action.run());
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (dataManager.readFromJSON("disablePatchNotesTemporary", getApplicationContext()).equals("true")) {
            dataManager.saveToJSON("disablePatchNotesTemporary", false, getApplicationContext());
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * Handles configuration changes.
     * It calls the superclass method and switches the display mode based on the current night mode.
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
    }

    /**
     * Switches to the stopwatch activity.
     * It creates a new StopwatchActivity, sets the main activity context, and starts the activity.
     */
    private void switchToStopwatchAction() {
        StopwatchActivity.setMainActivityContext(this);
        Intent intent = new Intent(this, StopwatchActivity.class);
        startActivity(intent);
    }


    /**
     * Switches to the timer activity.
     * It creates a new TimerActivity, sets the main activity context, and starts the activity.
     */
    private void switchToTimerAction() {
        TimerActivity.setMainActivityContext(this);
        Intent intent = new Intent(this, TimerActivity.class);
        startActivity(intent);
    }

    /**
     * Switches to the alarm activity.
     * It creates a new AlarmActivity, sets the main activity context, and starts the activity.
     */
    private void switchToAlarmAction() {
        AlarmActivity.setMainActivityContext(this);
        Intent intent = new Intent(this, AlarmActivity.class);
        startActivity(intent);
    }

    /**
     * Switches to the settings activity.
     * It creates a new SettingsActivity, sets the main activity context, and starts the activity.
     */
    private void switchToSettingsAction() {
        SettingsActivity.setMainActivityContext(this);
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Checks the dark mode setting.
     * It switches the display mode based on the current night mode.
     */
    private void checkDarkmodeSetting() {
        switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void switchDisplayMode(int currentNightMode) {
        final String mode = dataManager.readFromJSON("selectedSpinnerSetting", getApplicationContext());
        final String trueDarkmode = dataManager.readFromJSON("settingsTrueDarkMode", getApplicationContext());

        TextView stopwatch = findViewById(R.id.stopwatch);
        TextView timer = findViewById(R.id.timer);
        TextView alarm = findViewById(R.id.alarm);
        TextView settings = findViewById(R.id.settings);
        TextView clock = findViewById(R.id.clock);

        int newColorBTNBackgroundAccent = 0;
        int newColorBTNForegroundAccent = 0;

        switch (mode) {
            case "System":
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        if (stopwatch != null) {
                            Drawable icon;
                            icon = getDrawable(R.drawable.baseline_access_time_24_light);
                            stopwatch.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                        }
                        if (clock != null) {
                            Drawable icon;
                            icon = getDrawable(R.drawable.baseline_access_time_24_light);
                            clock.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                        }
                        if (timer != null) {
                            Drawable icon = getDrawable(R.drawable.baseline_hourglass_empty_24_light);
                            timer.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                        }
                        if (alarm != null) {
                            Drawable icon = getDrawable(R.drawable.baseline_alarm_24_light);
                            alarm.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                        }
                        if (settings != null) {
                            Drawable icon = getDrawable(R.drawable.baseline_settings_24_light);
                            settings.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                        }

                        if (trueDarkmode != null && trueDarkmode.equals("true")) {
                            newColorBTNForegroundAccent = ContextCompat.getColor(getApplicationContext(), R.color.darkmode_white);
                            newColorBTNBackgroundAccent = ContextCompat.getColor(getApplicationContext(), R.color.darkmode_black);

                            if (stopwatch != null) {
                                Drawable icon;
                                icon = getDrawable(R.drawable.baseline_access_time_24_true_darkmode);
                                stopwatch.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                            }
                            if (clock != null) {
                                Drawable icon;
                                icon = getDrawable(R.drawable.baseline_access_time_24_true_darkmode);
                                clock.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                            }
                            if (timer != null) {
                                Drawable icon = getDrawable(R.drawable.baseline_hourglass_empty_24_true_darkmode);
                                timer.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                            }
                            if (alarm != null) {
                                Drawable icon = getDrawable(R.drawable.baseline_alarm_24_true_darkmode);
                                alarm.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                            }
                            if (settings != null) {
                                Drawable icon = getDrawable(R.drawable.baseline_settings_24_true_darkmode);
                                settings.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                            }
                        } else if (trueDarkmode != null && trueDarkmode.equals("false")) {
                            newColorBTNForegroundAccent = ContextCompat.getColor(getApplicationContext(), R.color.white);
                            newColorBTNBackgroundAccent = ContextCompat.getColor(getApplicationContext(), R.color.black);
                        }
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        newColorBTNBackgroundAccent = ContextCompat.getColor(getApplicationContext(), R.color.white);
                        newColorBTNForegroundAccent = ContextCompat.getColor(getApplicationContext(), R.color.black);

                        if (stopwatch != null) {
                            Drawable icon;
                            icon = getDrawable(R.drawable.baseline_access_time_24);
                            stopwatch.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                        }
                        if (clock != null) {
                            Drawable icon;
                            icon = getDrawable(R.drawable.baseline_access_time_24);
                            clock.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                        }
                        if (timer != null) {
                            Drawable icon = getDrawable(R.drawable.baseline_hourglass_empty_24);
                            timer.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                        }
                        if (alarm != null) {
                            Drawable icon = getDrawable(R.drawable.baseline_alarm_24);
                            alarm.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                        }
                        if (settings != null) {
                            Drawable icon = getDrawable(R.drawable.baseline_settings_24);
                            settings.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                        }
                        break;
                }
                break;
            case "Light":
                newColorBTNBackgroundAccent = ContextCompat.getColor(getApplicationContext(), R.color.white);
                newColorBTNForegroundAccent = ContextCompat.getColor(getApplicationContext(), R.color.black);

                if (stopwatch != null) {
                    Drawable icon;
                    icon = getDrawable(R.drawable.baseline_access_time_24);
                    stopwatch.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                }
                if (clock != null) {
                    Drawable icon;
                    icon = getDrawable(R.drawable.baseline_access_time_24);
                    clock.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                }
                if (timer != null) {
                    Drawable icon = getDrawable(R.drawable.baseline_hourglass_empty_24);
                    timer.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                }
                if (alarm != null) {
                    Drawable icon = getDrawable(R.drawable.baseline_alarm_24);
                    alarm.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                }
                if (settings != null) {
                    Drawable icon = getDrawable(R.drawable.baseline_settings_24);
                    settings.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                }
                break;
            case "Dark":
                if (stopwatch != null) {
                    Drawable icon;
                    icon = getDrawable(R.drawable.baseline_access_time_24_light);
                    stopwatch.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                }
                if (clock != null) {
                    Drawable icon;
                    icon = getDrawable(R.drawable.baseline_access_time_24_light);
                    clock.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                }
                if (timer != null) {
                    Drawable icon = getDrawable(R.drawable.baseline_hourglass_empty_24_light);
                    timer.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                }
                if (alarm != null) {
                    Drawable icon = getDrawable(R.drawable.baseline_alarm_24_light);
                    alarm.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                }
                if (settings != null) {
                    Drawable icon = getDrawable(R.drawable.baseline_settings_24_light);
                    settings.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                }

                if (trueDarkmode != null && trueDarkmode.equals("true")) {
                    newColorBTNForegroundAccent = ContextCompat.getColor(getApplicationContext(), R.color.darkmode_white);
                    newColorBTNBackgroundAccent = ContextCompat.getColor(getApplicationContext(), R.color.darkmode_black);

                    if (stopwatch != null) {
                        Drawable icon;
                        icon = getDrawable(R.drawable.baseline_access_time_24_true_darkmode);
                        stopwatch.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                    }
                    if (clock != null) {
                        Drawable icon;
                        icon = getDrawable(R.drawable.baseline_access_time_24_true_darkmode);
                        clock.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                    }
                    if (timer != null) {
                        Drawable icon = getDrawable(R.drawable.baseline_hourglass_empty_24_true_darkmode);
                        timer.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                    }
                    if (alarm != null) {
                        Drawable icon = getDrawable(R.drawable.baseline_alarm_24_true_darkmode);
                        alarm.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                    }
                    if (settings != null) {
                        Drawable icon = getDrawable(R.drawable.baseline_settings_24_true_darkmode);
                        settings.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                    }
                } else if (trueDarkmode != null && trueDarkmode.equals("false")) {
                    newColorBTNForegroundAccent = ContextCompat.getColor(getApplicationContext(), R.color.white);
                    newColorBTNBackgroundAccent = ContextCompat.getColor(getApplicationContext(), R.color.black);

                    if (stopwatch != null) {
                        Drawable icon;
                        icon = getDrawable(R.drawable.baseline_access_time_24);
                        stopwatch.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                    }
                    if (clock != null) {
                        Drawable icon;
                        icon = getDrawable(R.drawable.baseline_access_time_24);
                        clock.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                    }
                    if (timer != null) {
                        Drawable icon = getDrawable(R.drawable.baseline_hourglass_empty_24);
                        timer.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                    }
                    if (alarm != null) {
                        Drawable icon = getDrawable(R.drawable.baseline_alarm_24);
                        alarm.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                    }
                    if (settings != null) {
                        Drawable icon = getDrawable(R.drawable.baseline_settings_24);
                        settings.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                    }
                }
                break;

        }

        changeTextViewColors(findViewById(R.id.activity_main), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
        changeButtonColors(findViewById(R.id.activity_main), newColorBTNForegroundAccent, newColorBTNBackgroundAccent);
    }

    /**
     * This method is used to change the colors of the buttons in a given layout.
     *
     * @param layout The ViewGroup whose Button children should have their colors changed. This can be any layout containing Buttons.
     * @param foregroundColor The color to be set as the text color of the Buttons. This should be a resolved color, not a resource id.
     * @param backgroundColor The color to be set as the background color of the Buttons and the layout. This should be a resolved color, not a resource id.
     */
    private void changeButtonColors(ViewGroup layout, int foregroundColor, int backgroundColor) {
        if (layout != null) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View v = layout.getChildAt(i);
                v.setBackgroundColor(backgroundColor);

                // If the child is a Button, change the foreground and background colors
                if (v instanceof Button) {
                    ((Button) v).setTextColor(foregroundColor);
                    v.setBackgroundColor(backgroundColor);
                }
                // If the child itself is a ViewGroup (e.g., a layout), call the function recursively
                else if (v instanceof ViewGroup) {
                    changeButtonColors((ViewGroup) v, foregroundColor, backgroundColor);
                }
            }
        }
    }

    /**
     * This method is used to change the colors of the TextViews in a given layout.
     *
     * @param layout The ViewGroup whose TextView children should have their colors changed. This can be any layout containing TextViews.
     * @param foregroundColor The color to be set as the text color of the TextViews. This should be a resolved color, not a resource id.
     * @param backgroundColor The color to be set as the background color of the TextViews and the layout. This should be a resolved color, not a resource id.
     */
    private void changeTextViewColors(ViewGroup layout, int foregroundColor, int backgroundColor) {
        if (layout != null) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View v = layout.getChildAt(i);
                v.setBackgroundColor(backgroundColor);
                System.out.println(v);

                // If the child is a TextView, change the foreground and background colors
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(foregroundColor);
                    v.setBackgroundColor(backgroundColor);
                }
                // If the child itself is a ViewGroup (e.g., a layout), call the function recursively
                else if (v instanceof ViewGroup) {
                    changeTextViewColors((ViewGroup) v, foregroundColor, backgroundColor);
                }
            }
        }
    }
}