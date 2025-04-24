package com.example.nhandienbienbao;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private Uri selectedImageUri;

    // ActivityResultLauncher để chọn ảnh
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    Toast.makeText(this, "Đã chọn ảnh!", Toast.LENGTH_SHORT).show();

                    // Nếu bạn muốn hiện ảnh ra, có thể dùng ImageView và setImageURI(selectedImageUri)
                } else {
                    Toast.makeText(this, "Bạn chưa chọn ảnh nào", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // FAB mở camera
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
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

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }
}