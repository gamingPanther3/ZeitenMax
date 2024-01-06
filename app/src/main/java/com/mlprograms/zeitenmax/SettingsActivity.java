package com.mlprograms.zeitenmax;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * SettingsActivity
 * @author Max Lemberg
 * @version 1.0.5
 * @date 18.12.2023
 */
public class SettingsActivity extends AppCompatActivity {

    /**
     * Instance of SharedPreferences for storing and retrieving small amounts of primitive data as key-value pairs.
     */
    SharedPreferences prefs = null;

    // Declare a DataManager object
    DataManager dataManager;
    // Declare a static MainActivity object
    @SuppressLint("StaticFieldLeak")
    private static ClockActivity mainActivity;

    /**
     * The `savedInstanceState` Bundle contains data that was saved in {@link #onSaveInstanceState}
     * when the activity was previously destroyed. This data can be used to restore the activity's
     * state when it is re-initialized. If the activity is being created for the first time,
     * this Bundle is null.
     *
     * @param savedInstanceState The Bundle containing the saved state, or null if the activity is
     *                         being created for the first time.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        dataManager = new DataManager();
        //dataManager.deleteJSON(getApplicationContext());
        dataManager.createJSON(getApplicationContext());
        //resetReleaseNoteConfig(getApplicationContext());
        setUpListeners();
        appendSpaceToSwitches(findViewById(R.id.settingsUI));

        // Declare a Spinner object
        Spinner spinner = findViewById(R.id.settings_display_mode_spinner);
        Integer num = getSelectedSettingPosition();
        if(num != null) {
            spinner.setSelection(num);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSpinner(parent);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

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
     * Checks the dark mode setting.
     * It switches the display mode based on the current night mode.
     */
    private void checkDarkmodeSetting() {
        switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
    }

    /**
     * Sets up the listeners for each button in the application
     */
    private void setUpListeners() {
        setButtonListener(R.id.settings, this::switchToSettingsAction);
        setButtonListener(R.id.stopwatch, this::switchToStopwatchAction);
        setButtonListener(R.id.timer, this::switchToTimerAction);
        setButtonListener(R.id.alarm, this::switchToAlarmAction);
        setButtonListener(R.id.clock, this::switchToClockAction);

        setButtonListener(R.id.okay_button, this::patchNotesOkayButtonAction);
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
     * This method updates the display mode and text color of a spinner based on the selected setting.
     * It first checks the current night mode of the device.
     * Then it reads the selected setting from the spinner.
     * If the selected setting is "Dunkelmodus", it saves "Dark" to the JSON file, switches the display mode, and sets the text color to white or darkmode_white based on the "settingsTrueDarkMode" value in the JSON file.
     * If the selected setting is "Tageslichtmodus", it saves "Light" to the JSON file, switches the display mode, and sets the text color to black.
     * If the selected setting is "Systemstandard", it saves "System" to the JSON file, switches the display mode, and sets the text color to white, darkmode_white, or black based on the current night mode and the "settingsTrueDarkMode" value in the JSON file.
     *
     * @param parent The AdapterView where the selection happened. This is used to get the selected setting and the TextView object.
     */
    public void updateSpinner(AdapterView<?> parent) {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        final String readselectedSetting = parent.getSelectedItem().toString();

        // Check if the TextView object is null before calling methods on it
        TextView textView = null;
        if(parent.getChildAt(0) instanceof TextView) {
            textView = (TextView) parent.getChildAt(0);
        }

        if(textView != null) {
            textView.setTextSize(20);
            switch (readselectedSetting) {
                case "Dunkelmodus":
                    dataManager.saveToJSON("selectedSpinnerSetting", "Dark", getMainActivityContext());
                    switchDisplayMode(currentNightMode);
                    if(dataManager.readFromJSON("settingsTrueDarkMode", getApplicationContext()).equals("true")) {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.darkmode_white));
                    } else {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.white));
                    }
                    break;
                case "Tageslichtmodus":
                    dataManager.saveToJSON("selectedSpinnerSetting", "Light", getMainActivityContext());
                    textView.setTextColor(ContextCompat.getColor(this, R.color.black));
                    switchDisplayMode(currentNightMode);
                    break;
                case "Systemstandard":
                    dataManager.saveToJSON("selectedSpinnerSetting", "System", getMainActivityContext());
                    if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                        if(dataManager.readFromJSON("settingsTrueDarkMode", getApplicationContext()).equals("true")) {
                            textView.setTextColor(ContextCompat.getColor(this, R.color.darkmode_white));
                        } else {
                            textView.setTextColor(ContextCompat.getColor(this, R.color.white));
                        }
                    } else if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.black));
                    }
                    switchDisplayMode(currentNightMode);
                    break;
            }
        }
    }

    /**
     * This method updates the second spinner based on the selected setting.
     * @param parent The AdapterView where the selection happened.
     */
    public void updateSpinner2(AdapterView<?> parent) {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        final String readselectedSetting = parent.getSelectedItem().toString();

        // Check if the TextView object is null before calling methods on it
        TextView textView = null;
        if(parent.getChildAt(0) instanceof TextView) {
            textView = (TextView) parent.getChildAt(0);
        }

        if(textView != null) {
            textView.setTextSize(20);
            switch (readselectedSetting) {
                case "Dunkelmodus":
                    dataManager.saveToJSON("selectedSpinnerSetting", "Dark", getMainActivityContext());
                    if(dataManager.readFromJSON("settingsTrueDarkMode", getApplicationContext()).equals("true")) {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.darkmode_white));
                    } else {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.white));
                    }
                    break;
                case "Tageslichtmodus":
                    dataManager.saveToJSON("selectedSpinnerSetting", "Light", getMainActivityContext());
                    textView.setTextColor(ContextCompat.getColor(this, R.color.black));
                    break;
                case "Systemstandard":
                    dataManager.saveToJSON("selectedSpinnerSetting", "System", getMainActivityContext());
                    if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                        if(dataManager.readFromJSON("settingsTrueDarkMode", getApplicationContext()).equals("true")) {
                            textView.setTextColor(ContextCompat.getColor(this, R.color.darkmode_white));
                        } else {
                            textView.setTextColor(ContextCompat.getColor(this, R.color.white));
                        }
                    } else if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.black));
                    }
                    break;
            }
        }
    }

    /**
     * This method gets the position of the selected setting.
     * @return The position of the selected setting.
     */
    public Integer getSelectedSettingPosition() {
        Integer num = null;
        final String readselectedSetting = dataManager.readFromJSON("selectedSpinnerSetting", getMainActivityContext());

        if(readselectedSetting != null) {
            switch (readselectedSetting) {
                case "System":
                    num = 0;
                    break;
                case "Light":
                    num = 1;
                    break;
                case "Dark":
                    num = 2;
                    break;
            }
        }
        return num;
    }

    /**
     * This method gets the selected setting.
     * @return The selected setting.
     */
    public String getSelectedSetting() {
        final String setting = dataManager.readFromJSON("selectedSpinnerSetting", getMainActivityContext());

        if(setting != null) {
            switch (setting) {
                case "System":
                    return "Systemstandard";
                case "Dark":
                    return "Dunkelmodus";
                case "Light":
                    return "Tageslichtmodus";
            }
        }
        return null;
    }

    /**
     * This method updates the state of a switch view based on a key.
     * @param switchView The switch view to update.
     * @param key The key to use to get the value.
     */
    private void updateSwitchState(@SuppressLint("UseSwitchCompatOrMaterialCode") Switch switchView, String key) {
        String value = dataManager.readFromJSON(key, this);
        if (value != null) {
            switchView.setChecked(Boolean.parseBoolean(value));
        } else {
            Log.e("Settings", "Failed to read value for key: " + key);
        }
    }

    /**
     * This static method sets the context of the MainActivity.
     * @param activity The MainActivity whose context is to be set.
     */
    public static void setMainActivityContext(ClockActivity activity) {
        mainActivity = activity;
    }

    /**
     * This method gets the context of the MainActivity.
     * @return The context of the MainActivity.
     */
    public Context getMainActivityContext() {
        return mainActivity;
    }

    /**
     * This method is called when the configuration of the device changes.
     * @param newConfig The new device configuration.
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int currentNightMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switchDisplayMode(currentNightMode);
    }

    /**
     * This method switches the display mode based on the current night mode.
     * @param currentNightMode The current night mode.
     */
    @SuppressLint({"ResourceType", "UseCompatLoadingForDrawables"})
    private void switchDisplayMode(int currentNightMode) {
        ScrollView settingsScrollView = findViewById(R.id.settings_sroll_textview);
        LinearLayout settingsLayout = findViewById(R.id.settings_layout);

        TextView settingsTitle = findViewById(R.id.settings_title);
        TextView settingsReleaseNotes = findViewById(R.id.settings_release_notes);
        TextView settingsReleaseNotesText = findViewById(R.id.settings_release_notes_text);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch settingsTrueDarkMode = findViewById(R.id.settings_true_darkmode);
        TextView settingsTrueDarkModeText = findViewById(R.id.settings_true_darkmode_text);
        TextView settingsDisplayModeText = findViewById(R.id.settings_display_mode_text);
        TextView settingsDisplayModeTitle = findViewById(R.id.settings_display_mode_title);
        TextView settingsCredits = findViewById(R.id.credits_view);

        TextView stopwatch = findViewById(R.id.stopwatch);
        TextView timer = findViewById(R.id.timer);
        TextView alarm = findViewById(R.id.alarm);
        TextView settings = findViewById(R.id.settings);
        TextView clock = findViewById(R.id.clock);

        Spinner spinner = findViewById(R.id.settings_display_mode_spinner);
        updateSpinner2(spinner);

        if(getSelectedSetting() != null) {
            if(getSelectedSetting().equals("Systemstandard")) {
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        // Nightmode is activated
                        dataManager = new DataManager();
                        String trueDarkMode = dataManager.readFromJSON("settingsTrueDarkMode", getMainActivityContext());

                        if (trueDarkMode != null) {
                            if (trueDarkMode.equals("false")) {
                                updateUI(R.color.black, R.color.white);

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
                            } else {
                                updateUI(R.color.darkmode_black, R.color.darkmode_white);

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
                            }
                        } else {
                            updateUI(R.color.black, R.color.white);

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
                        }
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        // Nightmode is not activated

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

                        // Set the colors for the Button and the TextView
                        settingsLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                        settingsTitle.setTextColor(ContextCompat.getColor(this, R.color.black));
                        settingsTitle.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                        settingsScrollView.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                        settingsReleaseNotes.setTextColor(ContextCompat.getColor(this, R.color.black));
                        settingsReleaseNotesText.setTextColor(ContextCompat.getColor(this, R.color.black));
                        settingsTrueDarkMode.setTextColor(ContextCompat.getColor(this, R.color.black));
                        settingsTrueDarkModeText.setTextColor(ContextCompat.getColor(this, R.color.black));
                        settingsDisplayModeText.setTextColor(ContextCompat.getColor(this, R.color.black));
                        settingsDisplayModeTitle.setTextColor(ContextCompat.getColor(this, R.color.black));
                        settingsCredits.setTextColor(ContextCompat.getColor(this, R.color.black));
                        settingsCredits.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                        break;
                }
            } else if (getSelectedSetting().equals("Tageslichtmodus")) {
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

                settingsLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                settingsTitle.setTextColor(ContextCompat.getColor(this, R.color.black));
                settingsTitle.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                settingsScrollView.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                settingsReleaseNotes.setTextColor(ContextCompat.getColor(this, R.color.black));
                settingsReleaseNotesText.setTextColor(ContextCompat.getColor(this, R.color.black));
                settingsTrueDarkMode.setTextColor(ContextCompat.getColor(this, R.color.black));
                settingsTrueDarkModeText.setTextColor(ContextCompat.getColor(this, R.color.black));
                settingsDisplayModeText.setTextColor(ContextCompat.getColor(this, R.color.black));
                settingsDisplayModeTitle.setTextColor(ContextCompat.getColor(this, R.color.black));
                settingsCredits.setTextColor(ContextCompat.getColor(this, R.color.black));
                settingsCredits.setBackgroundColor(ContextCompat.getColor(this, R.color.white));

            } else if (getSelectedSetting().equals("Dunkelmodus")) {
                dataManager = new DataManager();
                String trueDarkMode = dataManager.readFromJSON("settingsTrueDarkMode", getMainActivityContext());

                if (trueDarkMode != null) {
                    if (trueDarkMode.equals("false")) {
                        updateUI(R.color.black, R.color.white);
                        updateSpinner2(findViewById(R.id.settings_display_mode_spinner));

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
                    } else {
                        updateUI(R.color.darkmode_black, R.color.darkmode_white);
                        updateSpinner2(findViewById(R.id.settings_display_mode_spinner));

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
                    }
                } else {
                    updateUI(R.color.black, R.color.white);
                    updateSpinner2(findViewById(R.id.settings_display_mode_spinner));

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
            }
        }
    }

    /**
     * This method updates the UI elements with the given background color and text color.
     * @param backgroundColor The color to be used for the background.
     * @param textColor The color to be used for the text.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateUI(int backgroundColor, int textColor) {
        ScrollView settingsScrollView = findViewById(R.id.settings_sroll_textview);
        LinearLayout settingsLayout = findViewById(R.id.settings_layout);
        TextView settingsTitle = findViewById(R.id.settings_title);
        TextView settingsReleaseNotes = findViewById(R.id.settings_release_notes);
        TextView settingsReleaseNotesText = findViewById(R.id.settings_release_notes_text);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch settingsTrueDarkMode = findViewById(R.id.settings_true_darkmode);
        TextView settingsTrueDarkModeText = findViewById(R.id.settings_true_darkmode_text);
        TextView settingsDisplayModeText = findViewById(R.id.settings_display_mode_text);
        TextView settingsDisplayModeTitle = findViewById(R.id.settings_display_mode_title);
        TextView settingsCredits = findViewById(R.id.credits_view);

        settingsLayout.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingsTitle.setTextColor(ContextCompat.getColor(this, textColor));
        settingsTitle.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingsScrollView.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingsReleaseNotes.setTextColor(ContextCompat.getColor(this, textColor));
        settingsReleaseNotesText.setTextColor(ContextCompat.getColor(this, textColor));
        settingsTrueDarkMode.setTextColor(ContextCompat.getColor(this, textColor));
        settingsTrueDarkModeText.setTextColor(ContextCompat.getColor(this, textColor));
        settingsDisplayModeText.setTextColor(ContextCompat.getColor(this, textColor));
        settingsDisplayModeTitle.setTextColor(ContextCompat.getColor(this, textColor));
        settingsCredits.setTextColor(ContextCompat.getColor(this, textColor));
        settingsCredits.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
    }

    /**
     * This method is called when the back button is pressed.
     * It overrides the default behavior and returns to the calculator.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        switchToClockAction();
    }

    /**
     * This method appends spaces to the text of all Switch views in a layout.
     * @param layout The layout containing the Switch views.
     */
    private void appendSpaceToSwitches(ViewGroup layout) {
        if (layout != null) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View v = layout.getChildAt(i);
                if (v instanceof Switch) {
                    String switchText = ((Switch) v).getText().toString();
                    // Define a string of spaces
                    String space = "                                                      ";
                    switchText = switchText + space;
                    ((Switch) v).setText(switchText);
                }
                else if (v instanceof ViewGroup) {
                    appendSpaceToSwitches((ViewGroup) v);
                }
            }
        }
    }

    /**
     * Switches to the stopwatch activity.
     * It creates a new StopwatchActivity, sets the main activity context, and starts the activity.
     */
    private void switchToStopwatchAction() {
        StopwatchActivity.setMainActivityContext((ClockActivity) getMainActivityContext());
        Intent intent = new Intent(this, StopwatchActivity.class);
        startActivity(intent);
    }

    /**
     * Switches to the timer activity.
     * It creates a new TimerActivity, sets the main activity context, and starts the activity.
     */
    private void switchToTimerAction() {
        TimerActivity.setMainActivityContext(((ClockActivity) getMainActivityContext()));
        Intent intent = new Intent(this, TimerActivity.class);
        startActivity(intent);
    }

    /**
     * Switches to the alarm activity.
     * It creates a new AlarmActivity, sets the main activity context, and starts the activity.
     */
    private void switchToAlarmAction() {
        AlarmActivity.setMainActivityContext((ClockActivity) getMainActivityContext());
        Intent intent = new Intent(this, AlarmActivity.class);
        startActivity(intent);
    }

    /**
     * Switches to the clock activity.
     */
    private void switchToClockAction() {
        Intent intent = new Intent(this, ClockActivity.class);
        startActivity(intent);
    }

    /**
     * Switches to the settings activity.
     * It creates a new SettingsActivity, sets the main activity context, and starts the activity.
     */
    private void switchToSettingsAction() {
        SettingsActivity.setMainActivityContext((ClockActivity) getMainActivityContext());
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}