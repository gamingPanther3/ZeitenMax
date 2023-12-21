package com.mlprograms.zeitenmax;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;

public class MainActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_main);

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
    }

    /**
     * Sets up the listeners for each button in the application
     */
    private void setUpListeners() {
        setButtonListener(R.id.settings, this::switchToSettingsAction);
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
        setContentView(R.layout.activity_main);
        checkDarkmodeSetting();
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
     * Switches to the settings activity.
     * It creates a new SettingsActivity, sets the main activity context, and starts the activity.
     */
    public void switchToSettingsAction() {
        SettingsActivity.setMainActivityContext(this);
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Checks the dark mode setting.
     * It switches the display mode based on the current night mode.
     */
    public void checkDarkmodeSetting() {
        switchDisplayMode(getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
    }

    private void switchDisplayMode(int currentNightMode) {
        // check if darkmode and set application to darkmode
    }
}