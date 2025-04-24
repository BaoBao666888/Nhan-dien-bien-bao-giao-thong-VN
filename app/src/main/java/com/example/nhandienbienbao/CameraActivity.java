package com.example.nhandienbienbao;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private ImageView imageView;
    private LinearLayout resultsContainer;
    private Interpreter tflite;
    private final int INPUT_SIZE = 1280;
    private final float CONF_THRESHOLD = 0.3f;
    private Uri photoUri;
    private File photoFile;

    private List<String> classNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageView = findViewById(R.id.capturedImage);
        resultsContainer = findViewById(R.id.resultsContainer);

        try {
            tflite = new Interpreter(loadModelFile("best_float32.tflite"));
            loadClassNames("classes_vie.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            photoFile = File.createTempFile("temp_image", ".jpg", getExternalCacheDir());
            photoUri = FileProvider.getUriForFile(CameraActivity.this, getPackageName() + ".provider", photoFile);
            Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(openCameraIntent, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadClassNames(String filename) throws IOException {
        classNames.clear();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(getAssets().open(filename)));
        String line;
        while ((line = reader.readLine()) != null) {
            classNames.add(line.trim());
        }
        reader.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && photoFile != null && photoFile.exists()) {
            Bitmap photo = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            imageView.setImageBitmap(photo);
            runTFLite(photo);
        }
    }

    private MappedByteBuffer loadModelFile(String filename) throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd(filename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        ByteBuffer buffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4);
        buffer.order(ByteOrder.nativeOrder());
        buffer.rewind();
        for (int y = 0; y < INPUT_SIZE; y++) {
            for (int x = 0; x < INPUT_SIZE; x++) {
                int px = resized.getPixel(x, y);
                buffer.putFloat(((px >> 16) & 0xFF) / 255.0f);
                buffer.putFloat(((px >> 8) & 0xFF) / 255.0f);
                buffer.putFloat((px & 0xFF) / 255.0f);
            }
        }
        buffer.rewind();
        return buffer;
    }

    private void runTFLite(Bitmap photo) {
        ByteBuffer input = convertBitmapToByteBuffer(photo);
        float[][][] output = new float[1][300][6];
        tflite.run(input, output);

        int w = photo.getWidth();
        int h = photo.getHeight();

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
                    e.printStackTrace(); // ignore crop error
                }
            }
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
        String label = (cls < classNames.size()) ? classNames.get(cls) : "Không rõ";
        labelView.setText("Biển báo: " + label + "\nĐộ tin cậy: " + String.format("%.2f", score));
        labelView.setPadding(0, 10, 0, 40);
        labelView.setTextSize(16);
        labelView.setTextColor(Color.BLACK);

        resultsContainer.addView(signView);
        resultsContainer.addView(labelView);
    }
}