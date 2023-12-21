package com.mlprograms.zeitenmax;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
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
 * @version 1.0.0
 * @date 03.12.2023
 */
public class SettingsActivity extends AppCompatActivity {

    // Declare a DataManager object
    DataManager dataManager;
    // Declare a static MainActivity object
    @SuppressLint("StaticFieldLeak")
    private static MainActivity mainActivity;

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

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switchDisplayMode(currentNightMode);

        @SuppressLint("CutPasteId") Button button = findViewById(R.id.settings_return_button);
        button.setOnClickListener(v -> returnToCalculator());

        findViewById(R.id.settingsUI);

        @SuppressLint({"CutPasteId", "UseSwitchCompatOrMaterialCode"}) Switch settingsReleaseNotesSwitch = findViewById(R.id.settings_release_notes);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch settingsTrueDarkMode = findViewById(R.id.settings_true_darkmode);

        updateSwitchState(settingsReleaseNotesSwitch, "settingReleaseNotesSwitch");
        updateSwitchState(settingsTrueDarkMode, "settingsTrueDarkMode");

        appendSpaceToSwitches(findViewById(R.id.settingsUI));
        final String setRelNotSwitch= dataManager.readFromJSON("settingReleaseNotesSwitch", getMainActivityContext());

        if (setRelNotSwitch != null) {
            settingsReleaseNotesSwitch.setChecked(setRelNotSwitch.equals("true"));
        }

        settingsReleaseNotesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dataManager.saveToJSON("settingReleaseNotesSwitch", isChecked, getMainActivityContext());
            dataManager.saveToJSON("showPatchNotes", isChecked, getMainActivityContext());
            dataManager.saveToJSON("disablePatchNotesTemporary", isChecked, getMainActivityContext());
            Log.d("Settings", "settingReleaseNotesSwitch=" + dataManager.readFromJSON("settingReleaseNotesSwitch", getMainActivityContext()));
        });
        settingsTrueDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dataManager.saveToJSON("settingsTrueDarkMode", isChecked, getMainActivityContext());
            Log.d("Settings", "settingsTrueDarkMode=" + dataManager.readFromJSON("settingsTrueDarkMode", getMainActivityContext()));

            dataManager = new DataManager();
            int currentNightMode1 = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            updateSpinner(findViewById(R.id.settings_display_mode_spinner));
            @SuppressLint("CutPasteId") Button backbutton = findViewById(R.id.settings_return_button);

            String trueDarkMode = dataManager.readFromJSON("settingsTrueDarkMode", getMainActivityContext());
            if(currentNightMode1 == Configuration.UI_MODE_NIGHT_YES) {
                if (trueDarkMode != null && trueDarkMode.equals("true") && (getSelectedSetting().equals("Dunkelmodus") || getSelectedSetting().equals("Systemstandard"))) {
                    updateUI(R.color.darkmode_black, R.color.darkmode_white);
                    backbutton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_true_darkmode));
                } else if (trueDarkMode != null && trueDarkMode.equals("false") && (getSelectedSetting().equals("Dunkelmodus") || getSelectedSetting().equals("Systemstandard"))) {
                    updateUI(R.color.black, R.color.white);
                    backbutton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_light));
                }
            } else if(currentNightMode1 == Configuration.UI_MODE_NIGHT_NO) {
                if (trueDarkMode != null && trueDarkMode.equals("true") && (getSelectedSetting().equals("Dunkelmodus") || getSelectedSetting().equals("Systemstandard"))) {
                    updateUI(R.color.darkmode_black, R.color.darkmode_white);
                    backbutton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_true_darkmode));
                } else if (trueDarkMode != null && trueDarkMode.equals("false") && (getSelectedSetting().equals("Dunkelmodus") || getSelectedSetting().equals("Systemstandard"))) {
                    updateUI(R.color.black, R.color.white);
                    backbutton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_light));
                }
                ScrollView settingsScrollView = findViewById(R.id.settings_sroll_textview);
                LinearLayout settingsLayout = findViewById(R.id.settings_layout);
                @SuppressLint("CutPasteId") Button settingsReturnButton = findViewById(R.id.settings_return_button);

                TextView settingsTitle = findViewById(R.id.settings_title);
                @SuppressLint("CutPasteId") TextView settingsReleaseNotes = findViewById(R.id.settings_release_notes);
                TextView settingsReleaseNotesText = findViewById(R.id.settings_release_notes_text);
                TextView settingsTrueDarkModeText = findViewById(R.id.settings_true_darkmode_text);
                TextView settingsCredits = findViewById(R.id.credits_view);

                settingsLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
                settingsReturnButton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_light));
                settingsReturnButton.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
                settingsTitle.setTextColor(ContextCompat.getColor(this, R.color.white));
                settingsTitle.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
                settingsScrollView.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
                settingsReleaseNotes.setTextColor(ContextCompat.getColor(this, R.color.white));
                settingsReleaseNotesText.setTextColor(ContextCompat.getColor(this, R.color.white));
                settingsTrueDarkMode.setTextColor(ContextCompat.getColor(this, R.color.white));
                settingsTrueDarkModeText.setTextColor(ContextCompat.getColor(this, R.color.white));
                settingsCredits.setTextColor(ContextCompat.getColor(this, R.color.white));
                settingsCredits.setBackgroundColor(ContextCompat.getColor(this, R.color.black));

                switchDisplayMode(Configuration.UI_MODE_NIGHT_NO);
            }
        });
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
    public static void setMainActivityContext(MainActivity activity) {
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
        Button settingsReturnButton = findViewById(R.id.settings_return_button);

        TextView settingsTitle = findViewById(R.id.settings_title);
        TextView settingsReleaseNotes = findViewById(R.id.settings_release_notes);
        TextView settingsReleaseNotesText = findViewById(R.id.settings_release_notes_text);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch settingsTrueDarkMode = findViewById(R.id.settings_true_darkmode);
        TextView settingsTrueDarkModeText = findViewById(R.id.settings_true_darkmode_text);
        TextView settingsDisplayModeText = findViewById(R.id.settings_display_mode_text);
        TextView settingsDisplayModeTitle = findViewById(R.id.settings_display_mode_title);
        TextView settingsCredits = findViewById(R.id.credits_view);

        Spinner spinner = findViewById(R.id.settings_display_mode_spinner);
        updateSpinner2(spinner);
        @SuppressLint("CutPasteId") Button backbutton = findViewById(R.id.settings_return_button);

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

                                if(backbutton != null) {
                                    backbutton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_light));
                                }
                            } else {
                                updateUI(R.color.darkmode_black, R.color.darkmode_white);

                                if(backbutton != null) {
                                    backbutton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_true_darkmode));
                                }
                            }
                        } else {
                            updateUI(R.color.black, R.color.white);
                        }
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        // Nightmode is not activated

                        // Set the colors for the Button and the TextView
                        settingsLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                        settingsReturnButton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24));
                        settingsReturnButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
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
                settingsLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                settingsReturnButton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24));
                settingsReturnButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
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

                        if(backbutton != null) {
                            backbutton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_light));
                        }
                    } else {
                        updateUI(R.color.darkmode_black, R.color.darkmode_white);
                        updateSpinner2(findViewById(R.id.settings_display_mode_spinner));

                        if(backbutton != null) {
                            backbutton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_true_darkmode));
                        }
                    }
                } else {
                    updateUI(R.color.black, R.color.white);
                    updateSpinner2(findViewById(R.id.settings_display_mode_spinner));
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
        Button settingsReturnButton = findViewById(R.id.settings_return_button);
        TextView settingsTitle = findViewById(R.id.settings_title);
        TextView settingsReleaseNotes = findViewById(R.id.settings_release_notes);
        TextView settingsReleaseNotesText = findViewById(R.id.settings_release_notes_text);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch settingsTrueDarkMode = findViewById(R.id.settings_true_darkmode);
        TextView settingsTrueDarkModeText = findViewById(R.id.settings_true_darkmode_text);
        TextView settingsDisplayModeText = findViewById(R.id.settings_display_mode_text);
        TextView settingsDisplayModeTitle = findViewById(R.id.settings_display_mode_title);
        TextView settingsCredits = findViewById(R.id.credits_view);

        settingsLayout.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
        settingsReturnButton.setForeground(getDrawable(R.drawable.baseline_arrow_back_24_light));
        settingsReturnButton.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));
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
        returnToCalculator();
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
     * This method returns to the calculator by starting the MainActivity.
     */
    public void returnToCalculator() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}