package com.mlprograms.zeitenmax;

import android.Manifest;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clock);

        dataManager.initializeSettings(getApplicationContext());
        checkAndSetTabLayoutPos();
        setUpListeners();

        startLocalTimeUpdates();

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

    private void startLocalTimeUpdates() {
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

    private void checkAndSetTabLayoutPos() {
        final String value = dataManager.readFromJSON("selectedTab", getApplicationContext());
        if(value != null) {
            switch (value) {
                case "0":
                    setContentView(R.layout.alarm);
                    break;
                case "1":
                    setContentView(R.layout.clock);
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
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
            TabLayout.Tab tab = tabLayout.getTabAt(Integer.parseInt(value));
            assert tab != null;
            tab.select();
            setUpListeners();
        }
    }
}