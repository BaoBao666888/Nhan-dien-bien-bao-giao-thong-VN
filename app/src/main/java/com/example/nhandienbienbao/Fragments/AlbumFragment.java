package com.example.nhandienbienbao.Fragments;

import android.app.Activity;
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
    private ActivityResultLauncher<Intent> pickImageLauncher;

    private Button btnCancel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        recyclerView = view.findViewById(R.id.recyclerAlbum);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        actionBar = view.findViewById(R.id.bottomActionBar);
        btnSelectAll = view.findViewById(R.id.btnSelectAll);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnCancel = view.findViewById(R.id.btnCancel);

        //chưa gán adapter, chỉ để khung RecyclerView

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshAlbumIfNeeded();
            swipeRefreshLayout.setRefreshing(false); //  nhớ tắt refreshing
        });


        loadImagesAsync(); //tải hình nền

        setupButtons();
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            Intent intent = new Intent(getContext(), CameraActivity.class);
                            intent.putExtra("image_uri", selectedImage);
                            startActivity(intent);
                        }
                    }
                }
        );

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CameraActivity.class);
            startActivity(intent);
        });

        FrameLayout frameChonAnh = view.findViewById(R.id.frame_chon_anh);
        frameChonAnh.setOnClickListener(v -> {
            openGallery();
        });


        return view;
    }

    private void loadImagesAsync() {
        new Thread(() -> {
            List<Uri> imageUris = loadImagesFromGallery(getContext());

            if (isAdded()) { // check Fragment còn attach không
                requireActivity().runOnUiThread(() -> {
                    adapter = new AlbumAdapter(getContext(), imageUris);
                    recyclerView.setAdapter(adapter);

                    adapter.setSelectionChangeListener(() -> {
                        actionBar.setVisibility(View.VISIBLE);
                    });
                });
            }
        }).start();
    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
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
                btnSelectAll.setText(getString(R.string.bo_chon_tat_ca));
                actionBar.setVisibility(View.VISIBLE);
            } else {
                adapter.clearSelection();
                btnSelectAll.setText(getString(R.string.chon_tat_ca));
                actionBar.setVisibility(View.GONE);
            }
        });

        btnCancel.setOnClickListener(v -> {
            adapter.clearSelection();
            actionBar.setVisibility(View.GONE);
            btnSelectAll.setText(getString(R.string.chon_tat_ca));
        });

        btnDelete.setOnClickListener(v -> {
            List<Uri> selected = adapter.getSelectedUris();
            if (selected.isEmpty()) return;

            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.xoa_anh))
                    .setMessage(getString(R.string.ban_co_chac_chan_muon_xoa)+ " " + selected.size() + " " + getString(R.string.anh_h))
                    .setPositiveButton(getString(R.string.xoa), (dialog, which) -> {
                        for (Uri uri : selected) {
                            requireContext().getContentResolver().delete(uri, null, null);
                        }
                        Toast.makeText(getContext(), getString(R.string.da_xoa_anh), Toast.LENGTH_SHORT).show();
                        refreshAlbumIfNeeded();
                        adapter.clearSelection();
                        actionBar.setVisibility(View.GONE);
                        btnSelectAll.setText(getString(R.string.chon_tat_ca));
                    })
                    .setNegativeButton(getString(R.string.huy), null)
                    .show();
        });
    }

    private void refreshAlbumIfNeeded() {
        if (adapter == null) {
            loadImagesAsync();
            return;
        }

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
        }, 100);
    }


}
