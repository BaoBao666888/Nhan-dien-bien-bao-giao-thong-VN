package com.example.nhandienbienbao;

import android.app.Activity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhandienbienbao.Adapter.AlbumAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Uri selectedImageUri;
    private AlbumAdapter adapter;

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
        RecyclerView recycler = findViewById(R.id.recyclerAlbum);
        recycler.setLayoutManager(new GridLayoutManager(this, 3));
        List<Uri> imageList = loadImagesFromGallery();
        AlbumAdapter adapter = new AlbumAdapter(this, imageList);
        recycler.setAdapter(adapter);


        // FAB mở camera
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivityForResult(intent, 123);
        });

        // Các menu chuyển trang
        findViewById(R.id.bntThongKe).setOnClickListener(v ->
                startActivity(new Intent(this, ThongKeActivity.class)));

        findViewById(R.id.bntHuongDan).setOnClickListener(v ->
                startActivity(new Intent(this, InstructActivity.class)));

        findViewById(R.id.bntSetting).setOnClickListener(v ->
                startActivity(new Intent(this, SettingActivity.class)));

        findViewById(R.id.bntThoat).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận thoát")
                    .setMessage("Bạn có muốn thoát khỏi ứng dụng không?")
                    .setPositiveButton("Thoát", (dialog, which) -> finishAffinity())
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // Xử lý khi bấm vào khung chọn ảnh
        FrameLayout frameChonAnh = findViewById(R.id.frame_chon_anh);
        frameChonAnh.setOnClickListener(v -> openGallery());
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


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Uri> imageList = loadImagesFromGallery();
        RecyclerView recycler = findViewById(R.id.recyclerAlbum);
        if (adapter == null) {
            adapter = new AlbumAdapter(this, imageList);
            adapter.setSelectionChangeListener(() -> {
                LinearLayout actionBar = findViewById(R.id.bottomActionBar);
                actionBar.setVisibility(View.VISIBLE);
            });
            recycler.setAdapter(adapter);
        } else {
            adapter.updateData(imageList);
        }


        LinearLayout actionBar = findViewById(R.id.bottomActionBar);
        Button btnSelectAll = findViewById(R.id.btnSelectAll);

        btnSelectAll.setOnClickListener(v -> {
            boolean selectAll = !adapter.areAllSelected();
            if (selectAll) {
                adapter.selectAll();
                btnSelectAll.setText("Bỏ chọn tất cả");
                actionBar.setVisibility(View.VISIBLE);
            } else {
                adapter.clearSelection();
                btnSelectAll.setText("Chọn tất cả");
                actionBar.setVisibility(View.GONE);
            }
        });


        findViewById(R.id.btnCancel).setOnClickListener(v -> {
            adapter.clearSelection();
            actionBar.setVisibility(View.GONE);
            btnSelectAll.setText("Chọn tất cả");
        });


        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            List<Uri> selected = adapter.getSelectedUris();
            if (selected.isEmpty()) return;

            new AlertDialog.Builder(this)
                    .setTitle("Xoá ảnh")
                    .setMessage("Bạn có chắc muốn xoá " + selected.size() + " ảnh?")
                    .setPositiveButton("Xoá", (dialog, which) -> {
                        for (Uri uri : selected) {
                            getContentResolver().delete(uri, null, null);
                        }
                        Toast.makeText(this, "Đã xoá ảnh", Toast.LENGTH_SHORT).show();
                        onResume(); // reload lại ảnh
                    })
                    .setNegativeButton("Huỷ", null)
                    .show();
        });

        // Hiện actionBar nếu đang multiSelect
        recycler.postDelayed(() -> {
            if (adapter.isMultiSelectMode()) {
                actionBar.setVisibility(View.VISIBLE);
            } else {
                actionBar.setVisibility(View.GONE);
            }
        }, 300);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK) {
            onResume(); // reload lại album sau khi xử lý xong
        }
    }

}