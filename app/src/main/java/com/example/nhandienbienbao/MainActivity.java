package com.example.nhandienbienbao;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import com.example.nhandienbienbao.Fragments.AlbumFragment;
import com.example.nhandienbienbao.Fragments.InstructFragment;
import com.example.nhandienbienbao.Fragments.ThongKeFragment;
import com.example.nhandienbienbao.Fragments.SettingFragment;
import android.app.Activity;
import android.content.ContentUris;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;


import com.example.nhandienbienbao.Adapter.AlbumAdapter;


import java.util.ArrayList;
import java.util.Stack;
import java.util.List;
import com.example.nhandienbienbao.Helper.BottomNavHelper;
public class MainActivity extends AppCompatActivity {
    private Stack<Integer> fragmentBackStack = new Stack<>();
    private Uri selectedImageUri;
    private int currentTabId;


    // ActivityResultLauncher để chọn ảnh
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                        intent.putExtra("image_uri", selectedImageUri);
                        startActivity(intent);  // Gửi ảnh sang CameraActivity để nhận diện
                    } else {
                        Toast.makeText(this, "Lỗi khi lấy ảnh!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int savedMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedMode);

        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        replaceFragment(new AlbumFragment(), R.id.bntAlbum);

        findViewById(R.id.bntAlbum).setOnClickListener(v -> {
            if (currentTabId != R.id.bntAlbum) {
                replaceFragment(new AlbumFragment(), R.id.bntAlbum);
                highlightBottomNav(R.id.bntAlbum);
            }
        });


        findViewById(R.id.bntHuongDan).setOnClickListener(v -> {
            if (currentTabId != R.id.bntHuongDan){
                replaceFragment(new InstructFragment(), R.id.bntHuongDan);
                highlightBottomNav(R.id.bntHuongDan);
            }
        });

        findViewById(R.id.bntThongKe).setOnClickListener(v -> {
            if (currentTabId != R.id.bntThongKe) {
                replaceFragment(new ThongKeFragment(), R.id.bntThongKe);
                highlightBottomNav(R.id.bntThongKe);
            }
        });

        findViewById(R.id.bnttructiep).setOnClickListener(v -> {
            // Chưa làm gì, nếu muốn có highlight thì:
//            highlightBottomNav(R.id.bnttructiep);
        });

        findViewById(R.id.bntThoat).setOnClickListener(v -> {
//            highlightBottomNav(R.id.bntThoat);
            BottomNavHelper.setupBottomNav(this, R.id.bntThoat, R.id.text);
            // Xử lý logout
        });

        findViewById(R.id.bntSetting).setOnClickListener(v -> replaceFragment(new SettingFragment(), R.id.bntSetting));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!fragmentBackStack.isEmpty()) {
                    int lastTabId = fragmentBackStack.pop();
                    replaceFragment(new AlbumFragment(), R.id.bntAlbum);
                } else {
                    // Nếu đang ở Album thì thoát app
                    if (currentTabId == R.id.bntAlbum) {
                        finish();
                    } else {
                        replaceFragment(new AlbumFragment(), R.id.bntAlbum);
                    }
                }
            }
        });


    }


    private void replaceFragment(Fragment fragment, int newTabId) {
        if (newTabId != currentTabId) {
            boolean isRight = newTabId > currentTabId;

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(
                    isRight ? R.anim.slide_in_right : R.anim.slide_out_left,
                    isRight ? R.anim.slide_out_left : R.anim.slide_out_right
            );
            transaction.replace(R.id.fragmentContainer, fragment).commit();

            //  Nếu không phải Album thì mới push vào fragmentBackStack
            if (currentTabId != R.id.bntAlbum) {
                fragmentBackStack.clear();  // 👉 Chỉ cho 1 bước back
                fragmentBackStack.push(currentTabId);
            }

            highlightBottomNav(newTabId);

            ImageView settingBtn = findViewById(R.id.bntSetting);
            settingBtn.setVisibility(fragment instanceof SettingFragment ? View.GONE : View.VISIBLE);

            currentTabId = newTabId;
        }
    }


    private List<Uri> loadImagesFromGallery() {
        List<Uri> imageUris = new ArrayList<>();
        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
        String[] selectionArgs = new String[]{"%Pictures/BienBaoVN%"};

        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Images.Media.DATE_ADDED + " DESC");

        if (cursor != null) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                imageUris.add(uri);
            }
            cursor.close();
        }

        return imageUris;
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK) {
            onResume(); // reload lại album sau khi xử lý xong
        }
    }

    private void highlightBottomNav(int activeId) {
        int[] buttonIds = {
                R.id.bntHuongDan,
                R.id.bntThongKe,
                R.id.bntAlbum,
                R.id.bnttructiep,
                R.id.bntThoat
        };

        for (int id : buttonIds) {
            LinearLayout btn = findViewById(id);
            ImageView icon = (ImageView) btn.getChildAt(0);
            TextView text = (TextView) btn.getChildAt(1);

            if (id == activeId) {
                text.setTextColor(getResources().getColor(R.color.purple_500));
                text.setTypeface(null, android.graphics.Typeface.BOLD);
                if (icon != null) icon.setColorFilter(getResources().getColor(R.color.purple_500));
            } else {
                text.setTextColor(getResources().getColor(android.R.color.black));
                text.setTypeface(null, android.graphics.Typeface.NORMAL);
                if (icon != null)
                    icon.setColorFilter(getResources().getColor(android.R.color.black));
            }
        }
    }
}