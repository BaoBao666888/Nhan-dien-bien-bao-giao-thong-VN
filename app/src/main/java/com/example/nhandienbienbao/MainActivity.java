package com.example.nhandienbienbao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Cài đặt padding tránh overlap hệ thống
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Thiết lập xử lý khi bấm nút FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        });

        // Thiết lập xử lý khi bấm nút ThongKe
        LinearLayout statisticsButton = findViewById(R.id.bntThongKe);
        statisticsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ThongKeActivity.class);
            startActivity(intent);
        });

        // Thiết lập xử lý khi bấm nút HuongDan
        LinearLayout instructButton = findViewById(R.id.bntHuongDan);
        instructButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InstructActivity.class);
            startActivity(intent); // Chuyển sang trang hướng dẫn
        });

        // Thiết lập xử lý khi bấm nút Setting
        ImageView settingButton = findViewById(R.id.bntSetting);
        settingButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent); // Chuyển sang trang hướng dẫn
        });


        // Thiết lập xử lý khi bấm nút Thoát
        LinearLayout exitButton = findViewById(R.id.bntThoat);
        exitButton.setOnClickListener(v -> {
            // Hiển thị hộp thoại xác nhận trước khi thoát
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận thoát")
                    .setMessage("Bạn có muốn thoát khỏi ứng dụng không?")
                    .setPositiveButton("Thoát", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Thoát ứng dụng
                            finishAffinity();  // Hoặc dùng System.exit(0);
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }
}
