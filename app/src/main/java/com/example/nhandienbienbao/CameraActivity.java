package com.example.nhandienbienbao;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.content.ContentValues;
import android.provider.MediaStore.Images;
import java.io.OutputStream;

public class CameraActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final float CONF_THRESHOLD = 0.3f;
    private static final int INPUT_SIZE = 1280;

    private ImageView imageView;
    private LinearLayout resultsContainer;
    private Interpreter tflite;
    private File photoFile;
    private Uri photoUri;
    private Bitmap currentBitmap;
    private List<String> classNames = new ArrayList<>();
    private boolean isFromAlbum = false;
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Intent intent = getIntent();
        isFromAlbum = intent.hasExtra("image_uri");
        imageView = findViewById(R.id.capturedImage);
        resultsContainer = findViewById(R.id.resultsContainer);
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        try {
            tflite = new Interpreter(loadModelFile("best_float32.tflite"));
            loadClassNames("classes_vie.txt");
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }

        String source = getIntent().getStringExtra("source");

        if (getIntent().hasExtra("image_uri")) {
            Uri imageUri = getIntent().getParcelableExtra("image_uri");
            imageView.setImageURI(imageUri);
            currentBitmap = getBitmapFromUri(imageUri);

            ProgressBar loading = findViewById(R.id.progressBar);
            loading.setVisibility(View.VISIBLE);
            isProcessing = true;

            imageView.postDelayed(() -> {
                if (currentBitmap != null) runTFLite(currentBitmap);
                loading.setVisibility(View.GONE);
                isProcessing = false;
            }, 500);

        } else {
            // Mặc định: luôn mở camera nếu không nhận ảnh (kể cả quên "fab")
            try {
                photoFile = File.createTempFile("temp_image", ".jpg", getExternalCacheDir());
                photoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException e) {
                e.printStackTrace();
                finish();
            }
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isProcessing) {
                    new AlertDialog.Builder(CameraActivity.this)
                            .setTitle("Đang nhận diện")
                            .setMessage("Ứng dụng đang nhận diện ảnh. Bạn chắc chắn muốn thoát?")
                            .setPositiveButton("Thoát", (dialog, which) -> {
                                isProcessing = false;
                                setResult(RESULT_OK);
                                finish();
                            })
                            .setNegativeButton("Huỷ", null)
                            .show();
                } else {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && photoFile != null && photoFile.exists()) {
            currentBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath()); // 🛠 THÊM DÒNG NÀY!!!
            imageView.setImageBitmap(currentBitmap);

            ProgressBar loading = findViewById(R.id.progressBar);
            loading.setVisibility(View.VISIBLE);
            isProcessing = true;

            imageView.postDelayed(() -> {
                if (currentBitmap != null) {
                    runTFLite(currentBitmap);
                }
                loading.setVisibility(View.GONE);
                isProcessing = false;
            }, 500);
        } else {
            finish();
        }
    }



    private MappedByteBuffer loadModelFile(String filename) throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd(filename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,
                fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
    }

    private void loadClassNames(String filename) throws IOException {
        classNames.clear();
        BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(filename)));
        String line;
        while ((line = reader.readLine()) != null) {
            classNames.add(line.trim());
        }
        reader.close();
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        ByteBuffer buffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4);
        buffer.order(ByteOrder.nativeOrder());
        for (int y = 0; y < INPUT_SIZE; y++) {
            for (int x = 0; x < INPUT_SIZE; x++) {
                int px = resized.getPixel(x, y);
                buffer.putFloat(((px >> 16) & 0xFF) / 255.0f);
                buffer.putFloat(((px >> 8) & 0xFF) / 255.0f);
                buffer.putFloat((px & 0xFF) / 255.0f);
            }
        }
        return buffer;
    }

    private void runTFLite(Bitmap photo) {
        if (photo == null || tflite == null) return;
        ByteBuffer input = convertBitmapToByteBuffer(photo);
        float[][][] output = new float[1][300][6];
        tflite.run(input, output);

        int w = photo.getWidth();
        int h = photo.getHeight();
        resultsContainer.removeAllViews();

        for (float[] result : output[0]) {
            float x1 = result[0] * w;
            float y1 = result[1] * h;
            float x2 = result[2] * w;
            float y2 = result[3] * h;
            float score = result[4];
            int cls = (int) result[5];

            if (score > CONF_THRESHOLD) {
                try {
                    Rect rect = new Rect((int)x1, (int)y1, (int)x2, (int)y2);
                    Bitmap cropped = Bitmap.createBitmap(photo, rect.left, rect.top, rect.width(), rect.height());
                    addResultView(cropped, cls, score);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (!isFromAlbum) {
            saveImageToGallery(photo);
        }

    }

    private void addResultView(Bitmap cropped, int cls, float score) {
        ImageView signView = new ImageView(this);
        signView.setImageBitmap(cropped);
        signView.setAdjustViewBounds(true);
        signView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView labelView = new TextView(this);
        String label = (cls < classNames.size()) ? classNames.get(cls) : getString(R.string.khong_ro);
        labelView.setText(getString(R.string.bien_bao_2c) + " " + label);
        labelView.setTextSize(18);
        labelView.setTextColor(Color.BLACK);
        labelView.setPadding(0, 10, 0, 4);
        labelView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        TextView accView = new TextView(this);
        accView.setText(String.format(getString(R.string.do_chinh_xac)+" %.1f%%", score * 100));
        accView.setTextSize(14);
        accView.setTextColor(Color.GRAY);
        accView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        accView.setPadding(0, 0, 0, 40);

        resultsContainer.addView(signView);
        resultsContainer.addView(labelView);
        resultsContainer.addView(accView);
        String savedImageName = saveCroppedImage(cropped);
        if (savedImageName != null) {
            saveDetectionLog(label, score, savedImageName);
        }

    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveImageToGallery(Bitmap bitmap) {
        ContentValues values = new ContentValues();
        String filename = "bienbao_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
        values.put(Images.Media.DISPLAY_NAME, filename);
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        values.put(Images.Media.RELATIVE_PATH, "Pictures/BienBaoVN");

        Uri uri = getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void saveDetectionLog(String label, float score, String imageName) {
        String filename = "thongke.csv";
        String line = String.format(Locale.getDefault(), "%s,%s,%.1f%%,%s\n",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()),
                label,
                score * 100,
                imageName);

        try {
            File file = new File(getExternalFilesDir(null), filename);
            boolean append = file.exists();
            FileOutputStream fos = new FileOutputStream(file, append);
            fos.write(line.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        setResult(RESULT_OK); // cho biết vừa nhận diện
        super.finish();
    }

    private String saveCroppedImage(Bitmap croppedBitmap) {
        try {
            File croppedDir = getExternalFilesDir("Cropped");
            if (croppedDir != null && !croppedDir.exists()) {
                croppedDir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "cropped_" + timestamp + "_" + System.currentTimeMillis() + ".jpg";
            File file = new File(croppedDir, fileName);

            FileOutputStream out = new FileOutputStream(file);
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
