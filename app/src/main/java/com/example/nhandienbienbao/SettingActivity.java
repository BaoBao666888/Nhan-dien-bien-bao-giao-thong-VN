package com.example.nhandienbienbao;

import android.os.Bundle;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Spinner themeSpinner = findViewById(R.id.theme_spinner);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Khởi tạo theme spinner
        int savedMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        switch (savedMode) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                themeSpinner.setSelection(1);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                themeSpinner.setSelection(2);
                break;
            default:
                themeSpinner.setSelection(0);
        }

        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int mode;
                switch (position) {
                    case 1:
                        mode = AppCompatDelegate.MODE_NIGHT_NO;
                        break;
                    case 2:
                        mode = AppCompatDelegate.MODE_NIGHT_YES;
                        break;
                    default:
                        mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                }
                AppCompatDelegate.setDefaultNightMode(mode);
                prefs.edit().putInt("theme_mode", mode).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}
