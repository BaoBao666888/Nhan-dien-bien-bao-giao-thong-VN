package com.example.nhandienbiensoxevn;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.camera.view.PreviewView;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private ActionBarDrawerToggle toggle;
    private PreviewView previewView;
    private Button btnCapture;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo UI
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        mAuth = FirebaseAuth.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Cập nhật menu dựa vào trạng thái đăng nhập
        updateMenuItems();

        // Xử lý khi chọn menu
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Toast.makeText(this, "Trang chủ", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_vehicle_list) {
                startActivity(new Intent(this, VehicleListActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.nav_about) {
                startActivity(new Intent(this, AboutActivity.class));
            } else if (id == R.id.nav_login) {
                startActivity(new Intent(this, LoginActivity.class));
            } else if (id == R.id.nav_logout) {
                mAuth.signOut();
                Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                updateMenuItems(); // Cập nhật lại menu sau khi đăng xuất
                invalidateOptionsMenu();
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // Kiểm tra quyền camera trước khi mở
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera(); // Nếu có quyền, bắt đầu CameraX
        }

        // CameraX
        previewView = findViewById(R.id.previewView);
        btnCapture = findViewById(R.id.btnCapture);
        cameraExecutor = Executors.newSingleThreadExecutor();

        btnCapture.setOnClickListener(v -> captureImage());
    }

    // Cập nhật hiển thị menu dựa vào trạng thái đăng nhập
    private void updateMenuItems() {
        Menu menu = navigationView.getMenu();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) { // Đã đăng nhập
            menu.findItem(R.id.nav_login).setVisible(false);  // Ẩn "Đăng nhập"
            menu.findItem(R.id.nav_logout).setVisible(true);   // Hiện "Đăng xuất"
        } else { // Chưa đăng nhập
            menu.findItem(R.id.nav_login).setVisible(true);   // Hiện "Đăng nhập"
            menu.findItem(R.id.nav_logout).setVisible(false); // Ẩn "Đăng xuất"
        }

        navigationView.invalidate(); // ⚡️ Bắt buộc NavigationView cập nhật lại ngay
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateMenuItems(); // Cập nhật menu khi quay lại app
    }

    private void startCamera() {
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(this).get();
                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureImage() {
        File file = new File(getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(file).build();

        imageCapture.takePicture(options, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Ảnh đã lưu!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Lỗi chụp ảnh!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera(); // Người dùng cho phép -> Bật Camera
            } else {
                Toast.makeText(this, "Ứng dụng cần quyền Camera để hoạt động!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
