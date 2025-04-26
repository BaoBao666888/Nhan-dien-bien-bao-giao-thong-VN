package com.example.nhandienbienbao.Fragments;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhandienbienbao.Adapter.AlbumAdapter;
import com.example.nhandienbienbao.CameraActivity;
import com.example.nhandienbienbao.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AlbumFragment extends Fragment {
    private List<Uri> oldImageUris = new ArrayList<>();
    private AlbumAdapter adapter;
    private LinearLayout actionBar;
    private RecyclerView recyclerView;
    private Button btnSelectAll;
    private Button btnDelete;
    private Button btnCancel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        recyclerView = view.findViewById(R.id.recyclerAlbum);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        adapter = new AlbumAdapter(getContext(), loadImagesFromGallery(getContext()));
        recyclerView.setAdapter(adapter);

        actionBar = view.findViewById(R.id.bottomActionBar);
        btnSelectAll = view.findViewById(R.id.btnSelectAll);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnCancel = view.findViewById(R.id.btnCancel);

        setupButtons();

        adapter.setSelectionChangeListener(() -> {
            actionBar.setVisibility(View.VISIBLE);
        });

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 100);
        });

        FrameLayout frameChonAnh = view.findViewById(R.id.frame_chon_anh);
        frameChonAnh.setOnClickListener(v -> openGallery());

        return view;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, 101);
    }

    private List<Uri> loadImagesFromGallery(Context context) {
        List<Uri> imageUris = new ArrayList<>();
        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
        String[] selectionArgs = new String[]{"%Pictures/BienBaoVN%"};

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Images.Media.DATE_ADDED + " DESC");

        if (cursor != null) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                imageUris.add(uri);
            }
            cursor.close();
        }

        return imageUris;
    }

    private void setupButtons(View view) {
        view.findViewById(R.id.btnSelectAll).setOnClickListener(v -> {
            boolean selectAll = !adapter.areAllSelected();
            if (selectAll) {
                adapter.selectAll();
            } else {
                adapter.clearSelection();
            }
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            adapter.clearSelection();
            actionBar.setVisibility(View.GONE);
        });

        view.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            List<Uri> selected = adapter.getSelectedUris();
            if (selected.isEmpty()) return;

            for (Uri uri : selected) {
                requireContext().getContentResolver().delete(uri, null, null);
            }
            Toast.makeText(getContext(), "Đã xoá ảnh", Toast.LENGTH_SHORT).show();
            adapter.updateData(loadImagesFromGallery(getContext()));
            actionBar.setVisibility(View.GONE);
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        refreshAlbumIfNeeded();
    }



    private void setupButtons() {
        btnSelectAll.setOnClickListener(v -> {
            boolean selectAll = !adapter.areAllSelected();
            if (selectAll) {
                adapter.selectAll();
                btnSelectAll.setText("Bỏ chọn tất cả");
                actionBar.setVisibility(View.VISIBLE);
            } else {
                adapter.clearSelection();
                btnSelectAll.setText("Chọn tất cả");
                actionBar.setVisibility(View.GONE);
            }
        });

        btnCancel.setOnClickListener(v -> {
            adapter.clearSelection();
            actionBar.setVisibility(View.GONE);
            btnSelectAll.setText("Chọn tất cả");
        });

        btnDelete.setOnClickListener(v -> {
            List<Uri> selected = adapter.getSelectedUris();
            if (selected.isEmpty()) return;

            new AlertDialog.Builder(requireContext())
                    .setTitle("Xoá ảnh")
                    .setMessage("Bạn có chắc muốn xoá " + selected.size() + " ảnh?")
                    .setPositiveButton("Xoá", (dialog, which) -> {
                        for (Uri uri : selected) {
                            requireContext().getContentResolver().delete(uri, null, null);
                        }
                        Toast.makeText(getContext(), "Đã xoá ảnh", Toast.LENGTH_SHORT).show();
                        refreshAlbumIfNeeded();
                        adapter.clearSelection();
                        actionBar.setVisibility(View.GONE);
                        btnSelectAll.setText("Chọn tất cả");
                    })
                    .setNegativeButton("Huỷ", null)
                    .show();
        });
    }

    private void refreshAlbumIfNeeded() {
        List<Uri> newUris = loadImagesFromGallery(getContext());
        if (!newUris.equals(oldImageUris)) {
            adapter.updateData(newUris);
            oldImageUris = new ArrayList<>(newUris);
        }

        recyclerView.postDelayed(() -> {
            if (adapter.isMultiSelectMode()) {
                actionBar.setVisibility(View.VISIBLE);
            } else {
                actionBar.setVisibility(View.GONE);
            }
        }, 300);
    }



}
