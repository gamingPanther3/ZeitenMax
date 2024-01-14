package com.mlprograms.zeitenmax;

import java.text.DateFormatSymbols;
import java.util.TimeZone;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClockActivity extends AppCompatActivity {

    private final DataManager dataManager = new DataManager();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private String[] idArray;
    private String selectedTimeZoneId = null;
    private String selectedTimeZoneLocation = null;
    private AutoCompleteTextView addClockAutoComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clock);

        //dataManager.deleteJSON(getApplicationContext());
        dataManager.initializeSettings(getApplicationContext());
        checkAndSetTabLayoutPos();
        setUpListeners();

        startClockUpdates();
        updateTimesInCards();
    }

    private void updateTimesInCards() {
        int clockNumber = Integer.parseInt(dataManager.readFromJSON("clockNumber", getApplicationContext()));

        if(clockNumber >= 1) {
            for (int currentClock = 1; currentClock <= clockNumber; currentClock++) {
                String id = String.valueOf(currentClock);
                String location = dataManager.readFromJSON(id, getApplicationContext());

                if (location != null && !location.isEmpty()) {
                    Calendar calendar = Calendar.getInstance();
                    android.icu.util.TimeZone selectedTimeZone =
                            android.icu.util.TimeZone.getTimeZone(location.replace(", ", "/").replace(" ", "_"));
                    calendar.setTimeZone(selectedTimeZone);

                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH) + 1;
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
                    String minute = String.valueOf(calendar.get(Calendar.MINUTE));
                    String second = String.valueOf(calendar.get(Calendar.SECOND));

                    if (second.length() == 1) {
                        second = "0" + second;
                    }
                    if (minute.length() == 1) {
                        minute = "0" + minute;
                    }
                    if (hour.length() == 1) {
                        hour = "0" + hour;
                    }

                    updateCardTime(id, hour + ":" + minute + ":" + second);
                }
            }
        }
    }

    private void updateCardTime(String cardId, String updatedTime) {
        View cardView = findViewById(Integer.parseInt(cardId));
        if (cardView != null) {
            TextView timeTextView = cardView.findViewById(R.id.card_time);
            if (timeTextView != null) {
                timeTextView.setText(updatedTime);
            }
        }
    }

    private void createSavedClocks() {
        new Thread(() -> {
            Handler handler = new Handler(Looper.getMainLooper());

            handler.post(() -> {
                final int clockNumber = Integer.parseInt(dataManager.readFromJSON("clockNumber", getApplicationContext()));
                int currentClock = 0;
                Log.e("Clock", "ClockNumber:" + clockNumber);
                if (clockNumber >= 1) {
                    for (int x = 0; x < clockNumber; x++) {
                        currentClock++;
                        final String value = dataManager.readFromJSON(String.valueOf(currentClock), getApplicationContext());
                        if (value != null && !value.isEmpty()) {
                            final String location = dataManager.readFromJSON(String.valueOf(currentClock), getApplicationContext());

                            Calendar calendar = Calendar.getInstance();
                            android.icu.util.TimeZone selectedTimeZone =
                                    android.icu.util.TimeZone.getTimeZone(
                                            String.valueOf(location)
                                                    .replace(", ", "/")
                                                    .replace(" ", "_"));

                            Log.d("createSavedClocks", location.replace(", ", "/").replace(" ", "_"));
                            calendar.setTimeZone(selectedTimeZone);

                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH) + 1;
                            int day = calendar.get(Calendar.DAY_OF_MONTH);
                            String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
                            String minute = String.valueOf(calendar.get(Calendar.MINUTE));
                            String second = String.valueOf(calendar.get(Calendar.SECOND));

                            if (second.length() == 1) {
                                second = "0" + second;
                            }
                            if (minute.length() == 1) {
                                minute = "0" + minute;
                            }
                            if (hour.length() == 1) {
                                hour = "0" + hour;
                            }

                            addCardToClockLayout(hour + ":" + minute + ":" + second,
                                    location,
                                    findWeekday(year, month, day) + " " + day + ". " + getMonthAbbreviation(month) + " " + year);
                        }
                    }
                } else {
                    Log.e("card", "no card:" + clockNumber);
                }
            });
        }).start();
    }

    private void startClockUpdates() {
        startTimeUpdates();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            if(findViewById(R.id.location) != null) {
                startLocationUpdates();
            }
        }
    }

    private void updateLocalTime() {
        TextView localTimeTextView = findViewById(R.id.localTime);

        if(localTimeTextView != null) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SS", Locale.getDefault());
            String formattedTime = dateFormat.format(calendar.getTime());
            localTimeTextView.setText(formattedTime);
        }
    }

    private void startTimeUpdates() {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(findViewById(R.id.localTime) != null) {
                    updateLocalTime();
                    setLocalDate(findViewById(R.id.localDate));
                }
                handler.postDelayed(this, 10);
            }
        });
    }

    private void startLocationUpdates() {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateLocation();
                handler.postDelayed(this, 60000);
            }
        });
    }

    private void setLocalDate(TextView textView) {
        if(findViewById(R.id.localDate) != null) {
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("E d. MMM yyyy", Locale.getDefault());

            String formattedDate = dateFormat.format(currentDate);
            textView.setText(formattedDate);
        }
    }

    private void updateLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            updateLocationInfo(location);
                        }
                    });
        } else {
            TextView locationTextView = findViewById(R.id.location);

            if(locationTextView != null) {
                locationTextView.setText(R.string.keinen_standortzugriff);
            }
        }
    }

    private void updateLocationInfo(Location location) {
        if(findViewById(R.id.location) != null) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                assert addresses != null;
                if (!addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String locationString = String.format(Locale.getDefault(),
                            "%s, %s, %s", address.getCountryName(), address.getAdminArea(), address.getLocality());
                    TextView locationTextView = findViewById(R.id.location);

                    if(locationTextView != null) {
                        locationTextView.setText(locationString);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                TextView locationTextView = findViewById(R.id.location);

                if(locationTextView != null) {
                    locationTextView.setText(R.string.keinen_standortzugriff);
                }
            }
        }
    }

    private void setUpListeners() {
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    dataManager.saveToJSON("selectedTab", String.valueOf(tab.getPosition()), getApplicationContext());
                    checkAndSetTabLayoutPos();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }

        Button addNewClockButton = findViewById(R.id.add_new_clock);
        if(addNewClockButton != null) {
            addNewClockButton.setOnClickListener(v -> showPopupWindow(addNewClockButton));
        }
    }

    private void showPopupWindow(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.new_clock_popup_layout, null);

        popupView.setBackgroundColor(Color.TRANSPARENT);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.setFocusable(true);

        popupWindow.showAsDropDown(view, 30, -50);

        addClockAutoComplete = popupView.findViewById(R.id.add_clock_autocomplete);

        Button closePopupButton = popupView.findViewById(R.id.close_popup_button);
        closePopupButton.setOnClickListener(v -> popupWindow.dismiss());

        Button savePopupButton = popupView.findViewById(R.id.save_popup_button);
        savePopupButton.setOnClickListener(v -> {
            saveNewTimeRegion();
            popupWindow.dismiss();
        });

        if (addClockAutoComplete != null) {
            idArray = TimeZone.getAvailableIDs();

            for (int i = 0; i < idArray.length; i++) {
                idArray[i] = idArray[i].replace("/", ", ").replace("_", " ");
            }

            ArrayAdapter<String> idAdapter = new ArrayAdapter<>(
                    getApplicationContext(), android.R.layout.simple_spinner_item, idArray);
            idAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            addClockAutoComplete.setAdapter(idAdapter);

            addClockAutoComplete.setOnItemClickListener((parent, view1, position, id) -> {
                selectedTimeZoneId = parent.getItemAtPosition(position).toString();
                selectedTimeZoneLocation = parent.getItemAtPosition(position).toString();
            });
        }
    }

    private void saveNewTimeRegion() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.new_clock_popup_layout, null);
        AutoCompleteTextView addClock = popupView.findViewById(R.id.add_clock_autocomplete);

        if (addClock != null) {
            Calendar calendar = Calendar.getInstance();

            Log.e("Debug", selectedTimeZoneId);

            android.icu.util.TimeZone selectedTimeZone =
                    android.icu.util.TimeZone.getTimeZone(
                            String.valueOf(selectedTimeZoneId)
                                    .replace(", ", "/")
                                    .replace(" ", "_"));

            Log.d("saveNewTimeRegion", selectedTimeZoneId.replace(", ", "/").replace(" ", "_"));
            calendar.setTimeZone(selectedTimeZone);

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
            String minute = String.valueOf(calendar.get(Calendar.MINUTE));
            String second = String.valueOf(calendar.get(Calendar.SECOND));

            if (second.length() == 1) {
                second = "0" + second;
            }
            if (minute.length() == 1) {
                minute = "0" + minute;
            }
            if (hour.length() == 1) {
                hour = "0" + hour;
            }

            addCardToClockLayout(hour + ":" + minute + ":" + second,
                    selectedTimeZoneLocation,
                    findWeekday(year, month, day) + " " + day + ". " + getMonthAbbreviation(month) + " " + year);

            String currentClockNumber = dataManager.readFromJSON("clockNumber", getApplicationContext());
            int newClockNumber = Integer.parseInt(currentClockNumber) + 1;
            dataManager.saveToJSON("clockNumber", String.valueOf(newClockNumber), getApplicationContext());
            dataManager.saveToJSON(String.valueOf(newClockNumber), selectedTimeZoneLocation, getApplicationContext());
        }
    }


    private void addCardToClockLayout(String time, String location, String date) {
        LinearLayout clockLayout = findViewById(R.id.clock_layout);
        if (clockLayout != null) {
            // Erstelle eine neue CardView basierend auf dem cardtemplate.xml
            CardView newCard = (CardView) getLayoutInflater().inflate(R.layout.cardtemplate, null);

            // Setze die Layout-Parameter für die neue CardView
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(60, 20, 60, 20);
            newCard.setLayoutParams(layoutParams);

            // Setze die IDs für die TextViews in der neuen CardView
            final int clockNumber = Integer.parseInt(dataManager.readFromJSON("clockNumber", getApplicationContext())) + 1;

            newCard.setId(clockNumber);

            TextView timeTextView = newCard.findViewById(R.id.card_time);
            timeTextView.setId(View.generateViewId()); // Verwende View.generateViewId() für eindeutige IDs
            timeTextView.setText(time);

            TextView locationTextView = newCard.findViewById(R.id.card_location);
            locationTextView.setId(View.generateViewId());
            locationTextView.setText(location);

            TextView dateTextView = newCard.findViewById(R.id.card_date);
            dateTextView.setId(View.generateViewId());
            dateTextView.setText(date);

            // Füge die CardView zum ClockLayout hinzu
            clockLayout.addView(newCard);

            // Füge den OnLongClickListener zur CardView hinzu
            newCard.setOnLongClickListener(view -> {
                showDeletePopupWindow(newCard);
                return true;
            });
        }
    }

    private void showDeletePopupWindow(View cardView) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.delete_clock_popup_layout, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.setFocusable(true);

        Button deleteButton = popupView.findViewById(R.id.delete_clock);
        Button cancelButton = popupView.findViewById(R.id.cancel_delete_clock);

        deleteButton.setOnClickListener(v -> {
            // Hier füge die Logik zum Löschen der Card hinzu
            deleteCard(cardView);
            popupWindow.dismiss();
        });

        cancelButton.setOnClickListener(v -> popupWindow.dismiss());

        popupWindow.showAtLocation(cardView, Gravity.CENTER, 0, 0);
    }

    private void deleteCard(View cardView) {
        // Hier füge die Logik zum Entfernen der Card aus dem Layout hinzu
        if (cardView.getParent() instanceof ViewGroup) {
            ViewGroup parentView = (ViewGroup) cardView.getParent();
            parentView.removeView(cardView);
            dataManager.saveToJSON(String.valueOf(cardView.getId()), null, getApplicationContext());
        }
    }

    public static String findWeekday(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);

        SimpleDateFormat sdf = new SimpleDateFormat("EE", Locale.getDefault());
        Date date = calendar.getTime();
        return sdf.format(date);
    }

    private static String getMonthAbbreviation(int monthNumber) {
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] monthAbbreviations = dfs.getShortMonths();

        return monthAbbreviations[monthNumber - 1];
    }

    private void checkAndSetTabLayoutPos() {

        final String value = dataManager.readFromJSON("selectedTab", getApplicationContext());
        if(value != null) {
            switch (value) {
                case "0":
                    setContentView(R.layout.alarm);
                    break;
                case "1":
                    setContentView(R.layout.clock);
                    createSavedClocks();
                    updateTimesInCards();
                    startClockUpdates();
                    break;
                case "2":
                    setContentView(R.layout.timer);
                    break;
                case "3":
                    setContentView(R.layout.stopwatch);
                    break;
                case "4":
                    setContentView(R.layout.settings);
                    break;
            }
            TabLayout tabLayout = findViewById(R.id.tab_layout);
            TabLayout.Tab tab = tabLayout.getTabAt(Integer.parseInt(value));
            assert tab != null;
            tab.select();
            setUpListeners();
        }
    }
}