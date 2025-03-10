package com.example.nhandienbiensoxevn;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.time.Instant;

public class ResultActivity extends AppCompatActivity {
    private ImageView imgCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Button btnExit = findViewById(R.id.btnExit);

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this, VehicleListActivity.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);

                finish();
            }
        });

        imgCar = findViewById(R.id.imgCar);

        // Nhận đường dẫn ảnh từ Intent
        String imagePath = getIntent().getStringExtra("image_path");

        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                imgCar.setImageBitmap(bitmap);
            }
        }
    }
}
