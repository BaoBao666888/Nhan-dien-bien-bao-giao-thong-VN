package com.example.nhandienbienbao.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.example.nhandienbienbao.R;

public class SettingFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        Spinner themeSpinner = view.findViewById(R.id.theme_spinner);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

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

        themeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
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
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        return view;
    }
}
