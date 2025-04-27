package com.example.nhandienbienbao.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.nhandienbienbao.R;
import android.widget.Toast;
import java.util.Locale;

public class SettingFragment extends Fragment {

    private String initialLanguage;
    private String selectedLanguage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        Spinner themeSpinner = view.findViewById(R.id.theme_spinner);
        Spinner languageSpinner = view.findViewById(R.id.language_spinner);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        // Lưu ngôn ngữ hiện tại
        initialLanguage = prefs.getString("app_language", "vi");
        selectedLanguage = initialLanguage;

        // Set theme mode cho Spinner theme
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

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: selectedLanguage = "vi"; break;
                    case 1: selectedLanguage = "zh"; break;
                    case 2: selectedLanguage = "en"; break;
                    case 3: selectedLanguage = "ja"; break;
                    case 4: selectedLanguage = "fr"; break;
                    case 5: selectedLanguage = "es"; break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Đặt spinner theo ngôn ngữ hiện tại
        switch (initialLanguage) {
            case "zh": languageSpinner.setSelection(1); break;
            case "en": languageSpinner.setSelection(2); break;
            case "ja": languageSpinner.setSelection(3); break;
            case "fr": languageSpinner.setSelection(4); break;
            case "es": languageSpinner.setSelection(5); break;
            default: languageSpinner.setSelection(0);
        }

        Switch notificationSwitch = view.findViewById(R.id.notification_switch);

        // Load trạng thái từ SharedPreferences
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
        notificationSwitch.setChecked(notificationsEnabled);

        // Gán sự kiện khi bật/tắt
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
            Toast.makeText(getContext(), isChecked ? "Thông báo đã bật" : "Thông báo đã tắt", Toast.LENGTH_SHORT).show();
        });

        Button updateButton = view.findViewById(R.id.update_button);
        updateButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Kiểm tra cập nhật")
                    .setMessage("Bạn đang sử dụng phiên bản mới nhất.")
                    .setPositiveButton("OK", null)
                    .show();
        });

        Button accessPermissionButton = view.findViewById(R.id.access_permission_button);
        accessPermissionButton.setOnClickListener(v -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
            startActivity(intent);
        });

        Button resetSettingsButton = view.findViewById(R.id.reset_settings_button);
        resetSettingsButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Đặt lại cài đặt")
                    .setMessage("Bạn có chắc muốn đặt lại toàn bộ cài đặt về mặc định không?")
                    .setPositiveButton("Đặt lại", (dialog, which) -> resetSettings())
                    .setNegativeButton("Huỷ", null)
                    .show();
        });


        return view;
    }
    private void resetSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("app_language", "vi"); // Trả ngôn ngữ về tiếng Việt
        editor.putInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM); // Giao diện theo hệ thống
        editor.putBoolean("notifications_enabled", false); // Tắt thông báo
        editor.apply();

        Toast.makeText(requireContext(), "Đã đặt lại cài đặt mặc định", Toast.LENGTH_SHORT).show();

        requireActivity().recreate(); // Reload lại giao diện app (không cần exit app luôn)
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!initialLanguage.equals(selectedLanguage)) {
            applyNewLanguage(selectedLanguage);
        }

    }

    private void applyNewLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        requireContext().getResources().updateConfiguration(config, requireContext().getResources().getDisplayMetrics());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        prefs.edit().putString("app_language", languageCode).apply();

        requireActivity().recreate();
    }
}