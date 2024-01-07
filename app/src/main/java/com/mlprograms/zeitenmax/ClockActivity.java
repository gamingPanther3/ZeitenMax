package com.mlprograms.zeitenmax;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

public class ClockActivity extends AppCompatActivity {

    private final DataManager dataManager = new DataManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clock);

        checkAndSetTabLayoutPos();
        setUpListeners();
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
        final int value = Integer.parseInt(dataManager.readFromJSON("selectedTab", getApplicationContext()));
        if(value != -1) {
            switch (value) {
                case 0:
                    setContentView(R.layout.alarm);
                    break;
                case 1:
                    setContentView(R.layout.clock);
                    break;
                case 2:
                    setContentView(R.layout.timer);
                    break;
                case 3:
                    setContentView(R.layout.stopwatch);
                    break;
                case 4:
                    setContentView(R.layout.settings);
                    break;
            }
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
            TabLayout.Tab tab = tabLayout.getTabAt(value);
            assert tab != null;
            tab.select();
            setUpListeners();
        }
    }
}