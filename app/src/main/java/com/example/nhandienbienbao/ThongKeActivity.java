package com.example.nhandienbienbao;

import android.os.Bundle;
import android.content.Intent;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;

public class ThongKeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thongke);

        LinearLayout navAlbum = findViewById(R.id.nav_album);
        navAlbum.setOnClickListener(v -> {
            Intent intent = new Intent(ThongKeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        LinearLayout navhuongDan = findViewById(R.id.nav_HuongDan);
        navhuongDan.setOnClickListener(v -> {
            Intent intent = new Intent(ThongKeActivity.this, InstructActivity.class);
            startActivity(intent);
            finish();
        });

        LinearLayout exitButton = findViewById(R.id.nav_thoat);
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
