package com.example.nhandienbienbao.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.nhandienbienbao.CameraActivity;
import com.example.nhandienbienbao.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    private final List<Uri> imageUris;
    private final Context context;
    private boolean[] selectedFlags;
    private boolean multiSelectMode = false;
    public interface SelectionChangeListener {
        void onMultiSelectStarted();
    }
    private SelectionChangeListener selectionChangeListener;

    public void setSelectionChangeListener(SelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }


    public AlbumAdapter(Context ctx, List<Uri> uris) {
        context = ctx;
        imageUris = uris;
        selectedFlags = new boolean[uris.size()];
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.imageThumbnail);
            checkBox = view.findViewById(R.id.checkboxSelect);
        }
    }

    @Override
    public AlbumAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position >= selectedFlags.length) return;
        Uri uri = imageUris.get(position);
        holder.image.setImageURI(uri);

        holder.checkBox.setVisibility(multiSelectMode ? View.VISIBLE : View.GONE);
        holder.checkBox.setChecked(selectedFlags[position]);

        holder.image.setOnClickListener(v -> {
            if (multiSelectMode) {
                selectedFlags[position] = !selectedFlags[position];
                notifyItemChanged(position);
            } else {
                Intent intent = new Intent(context, CameraActivity.class);
                intent.putExtra("image_uri", uri);
                context.startActivity(intent);
            }
        });

        holder.image.setOnLongClickListener(v -> {
            if (!multiSelectMode) {
                multiSelectMode = true;
                notifyItemRangeChanged(0, imageUris.size());
                if (selectionChangeListener != null) {
                    selectionChangeListener.onMultiSelectStarted();
                }
            }
            return true;
        });



        holder.checkBox.setOnClickListener(v -> {
            selectedFlags[position] = holder.checkBox.isChecked();
        });
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public void selectAll() {
        Arrays.fill(selectedFlags, true);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        Arrays.fill(selectedFlags, false);
        multiSelectMode = false;
        notifyDataSetChanged();
    }

    public List<Uri> getSelectedUris() {
        List<Uri> selected = new ArrayList<>();
        for (int i = 0; i < imageUris.size(); i++) {
            if (selectedFlags[i]) selected.add(imageUris.get(i));
        }
        return selected;
    }

    public boolean isMultiSelectMode() {
        return multiSelectMode;
    }
    public void updateData(List<Uri> newUris) {
        imageUris.clear();
        imageUris.addAll(newUris);
        selectedFlags = new boolean[newUris.size()]; // ✔ đảm bảo cùng size
        notifyDataSetChanged();
    }
    public boolean areAllSelected() {
        for (boolean b : selectedFlags) {
            if (!b) return false; // chỉ cần 1 cái chưa chọn là false
        }
        return true;
    }


}

