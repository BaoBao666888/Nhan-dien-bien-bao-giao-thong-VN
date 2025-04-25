package com.example.nhandienbienbao;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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

        //thêm
        TextView textStatsContent = findViewById(R.id.textStatsContent);

        File file = new File(getExternalFilesDir(null), "thongke.csv");
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
                reader.close();
                textStatsContent.setText(builder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            textStatsContent.setText("Chưa có thống kê nào.");
        }

        //thêm
        Button btnClear = findViewById(R.id.btnClearStats);

        btnClear.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xoá thống kê")
                    .setMessage("Bạn có chắc muốn xoá toàn bộ dữ liệu thống kê không?")
                    .setPositiveButton("Xoá", (dialog, which) -> {
                        if (file.exists() && file.delete()) {
                            textStatsContent.setText("Chưa có thống kê nào.");
                            Toast.makeText(this, "Đã xoá thống kê!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Không thể xoá!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });


    }
}
