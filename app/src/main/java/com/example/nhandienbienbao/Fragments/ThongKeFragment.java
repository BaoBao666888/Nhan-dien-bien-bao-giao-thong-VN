package com.example.nhandienbienbao.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhandienbienbao.Adapter.ThongKeAdapter;
import com.example.nhandienbienbao.Models.ThongKeItem;
import com.example.nhandienbienbao.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class ThongKeFragment extends Fragment {

    private RecyclerView recyclerThongKe;
    private FloatingActionButton btnClearStats;
    private EditText searchEditText;
    private File fileThongKe;
    private ThongKeAdapter adapter;
    private List<ThongKeItem> thongKeItems = new ArrayList<>(); // Data gốc (full)


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thongke, container, false);

        recyclerThongKe = view.findViewById(R.id.recyclerThongKe);
        btnClearStats = view.findViewById(R.id.btnClearStats);
        searchEditText = view.findViewById(R.id.searchEditText);

        recyclerThongKe.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ThongKeAdapter(requireContext(), thongKeItems);
        recyclerThongKe.setAdapter(adapter);

        fileThongKe = new File(requireContext().getExternalFilesDir(null), "thongke.csv");

        loadThongKeFile();

        btnClearStats.setOnClickListener(v -> confirmClearStats());

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String text = v.getText().toString();
                filterThongKe(text);
                return true; // đã xử lý
            }
            return false;
        });


        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterThongKe(s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });


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
        adapter.updateData(new ArrayList<>(thongKeItems)); // update hiển thị
    }


    private void filterThongKe(String text) {
        if (text.trim().isEmpty()) {
            adapter.updateData(new ArrayList<>(thongKeItems)); // <-- reset từ gốc thongKeItems
            return;
        }

        String normalizedSearch = normalize(text);

        List<ThongKeItem> filteredList = new ArrayList<>();
        for (ThongKeItem item : thongKeItems) { // luôn search từ thongKeItems gốc
            String normalizedLabel = normalize(item.getTenBienBao());
            if (normalizedLabel.contains(normalizedSearch)) {
                filteredList.add(item);
            }
        }

        adapter.updateData(filteredList); // <-- không cần currentItems lung tung gì nữa
    }



    // Hàm normalize: chuyển chữ thường + xóa dấu + xóa dấu câu
    private String normalize(String input) {
        if (input == null) return "";

        // Chuyển thành chữ thường
        input = input.toLowerCase();

        // Xóa dấu tiếng Việt
        input = Normalizer.normalize(input, Normalizer.Form.NFD);
        input = input.replaceAll("\\p{M}", ""); // xóa ký tự dấu

        // Xóa dấu câu (ví dụ: , . ! ? ' " ...)
        input = input.replaceAll("[\\p{Punct}]", "");

        // Xóa khoảng trắng thừa
        input = input.trim();

        return input;
    }


    private void confirmClearStats() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xoá thống kê")
                .setMessage("Bạn có chắc muốn xoá toàn bộ dữ liệu thống kê không?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    if (fileThongKe.exists() && fileThongKe.delete()) {
                        File croppedFolder = new File(requireContext().getExternalFilesDir(null), "cropped");
                        deleteFolder(croppedFolder);
                        thongKeItems.clear();
                        adapter.updateData(new ArrayList<>(thongKeItems));
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