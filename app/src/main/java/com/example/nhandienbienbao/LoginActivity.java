package com.example.nhandienbienbao;

import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedLanguage();
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);

        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> login());

        findViewById(R.id.register_button).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        findViewById(R.id.forgotPassword).setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, getString(R.string.nhap_email_truoc_khi_dat_lai_mat_khau), Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, getString(R.string.da_gui_email_xac_thuc), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }
    private void applySavedLanguage() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String languageCode = prefs.getString("app_language", "vi"); // mặc định "vi"
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void login() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.vui_long_nhap_email_va_mat_khau), Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, getString(R.string.dang_nhap_thanh_cong), Toast.LENGTH_SHORT).show();
                        handleLoginSuccess();
                    } else {
                        Toast.makeText(this, getString(R.string.dang_nhap_that_bai) + ": " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleLoginSuccess() {
        String lastUser = prefs.getString("last_user", "");
        String currentUser = mAuth.getCurrentUser().getUid();

        if (!lastUser.equals(currentUser)) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.xoa_du_lieu_cu))
                    .setMessage(getString(R.string.canh_bao_xoa_lich_su_nhan_dien_va_album))
                    .setPositiveButton(getString(R.string.co), (dialog, which) -> {
                        clearOldData();
                        prefs.edit().putString("last_user", currentUser).apply();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    })
                    .setNegativeButton(getString(R.string.khong), (dialog, which) -> {
                        prefs.edit().putString("last_user", currentUser).apply();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    })
                    .show();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
    private void clearOldData() {
        // Xóa file thống kê
        File thongKeFile = new File(getExternalFilesDir(null), "thongke.csv");
        if (thongKeFile.exists()) {
            thongKeFile.delete();
        }

        // Xóa thư mục Cropped
        File croppedFolder = new File(getExternalFilesDir(null), "Cropped");
        if (croppedFolder.exists() && croppedFolder.isDirectory()) {
            for (File file : croppedFolder.listFiles()) {
                file.delete();
            }
            croppedFolder.delete();
        }

        // Xóa ảnh trong gallery (Pictures/BienBaoVN)
        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
        String[] selectionArgs = new String[]{"%Pictures/BienBaoVN%"};

        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                getContentResolver().delete(uri, null, null);
            }
            cursor.close();
        }
    }

}
