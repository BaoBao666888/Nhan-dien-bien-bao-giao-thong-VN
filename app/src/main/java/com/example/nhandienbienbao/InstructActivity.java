package com.example.nhandienbienbao;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class InstructActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruct);

        LinearLayout navAlbum = findViewById(R.id.album);
        navAlbum.setOnClickListener(v -> {
            Intent intent = new Intent(InstructActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        LinearLayout navThongKe = findViewById(R.id.Thongke);
        navThongKe.setOnClickListener(v -> {
            Intent intent = new Intent(InstructActivity.this, ThongKeActivity.class);
            startActivity(intent);
            finish();
        });

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