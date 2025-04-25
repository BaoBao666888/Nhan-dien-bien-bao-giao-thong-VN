package com.example.nhandienbienbao;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nhandienbienbao.Helper.BottomNavHelper;

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

        BottomNavHelper.setupBottomNav(this, R.id.bntThoat, R.id.text);
    }
}