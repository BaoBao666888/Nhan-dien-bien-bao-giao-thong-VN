package com.example.nhandienbienbao.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.nhandienbienbao.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ThongKeFragment extends Fragment {

    private TextView textStatsContent;
    private Button btnClearStats;
    private File fileThongKe;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thongke, container, false);

        textStatsContent = view.findViewById(R.id.textStatsContent);
        btnClearStats = view.findViewById(R.id.btnClearStats);

        fileThongKe = new File(requireContext().getExternalFilesDir(null), "thongke.csv");

        // 👉 Gọi hàm load file
        loadThongKeFile();

        btnClearStats.setOnClickListener(v -> confirmClearStats());

        return view;
    }

    private void loadThongKeFile() {
        if (fileThongKe.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(fileThongKe));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
                reader.close();
                textStatsContent.setText(builder.toString());
            } catch (IOException e) {
                e.printStackTrace();
                textStatsContent.setText("Không thể đọc file thống kê.");
            }
        } else {
            textStatsContent.setText("Chưa có thống kê nào.");
        }
    }

    private void confirmClearStats() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xoá thống kê")
                .setMessage("Bạn có chắc muốn xoá toàn bộ dữ liệu thống kê không?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    if (fileThongKe.exists() && fileThongKe.delete()) {
                        textStatsContent.setText("Chưa có thống kê nào.");
                        Toast.makeText(requireContext(), "Đã xoá thống kê!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Không thể xoá!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }
}
