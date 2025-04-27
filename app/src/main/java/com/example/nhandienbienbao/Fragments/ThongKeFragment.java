package com.example.nhandienbienbao.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhandienbienbao.Adapter.ThongKeAdapter;
import com.example.nhandienbienbao.Models.ThongKeItem;
import com.example.nhandienbienbao.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ThongKeFragment extends Fragment {

    private RecyclerView recyclerThongKe;
    private Button btnClearStats;
    private File fileThongKe;
    private ThongKeAdapter adapter;
    private List<ThongKeItem> thongKeItems = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thongke, container, false);

        recyclerThongKe = view.findViewById(R.id.recyclerThongKe);
        btnClearStats = view.findViewById(R.id.btnClearStats);

        recyclerThongKe.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ThongKeAdapter(requireContext(), thongKeItems);
        recyclerThongKe.setAdapter(adapter);

        fileThongKe = new File(requireContext().getExternalFilesDir(null), "thongke.csv");

        loadThongKeFile();

        btnClearStats.setOnClickListener(v -> confirmClearStats());

        return view;
    }

    private void loadThongKeFile() {
        thongKeItems.clear();
        if (fileThongKe.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(fileThongKe));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        thongKeItems.add(new ThongKeItem(parts[0], parts[1], parts[2], parts[3]));
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void confirmClearStats() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xoá thống kê")
                .setMessage("Bạn có chắc muốn xoá toàn bộ dữ liệu thống kê không?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    if (fileThongKe.exists() && fileThongKe.delete()) {
                        // Xoá folder cropped
                        File croppedFolder = new File(requireContext().getExternalFilesDir(null), "cropped");
                        deleteFolder(croppedFolder);
                        thongKeItems.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(requireContext(), "Đã xoá thống kê!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Không thể xoá!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deleteFolder(File folder) {
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            folder.delete();
        }
    }
}
