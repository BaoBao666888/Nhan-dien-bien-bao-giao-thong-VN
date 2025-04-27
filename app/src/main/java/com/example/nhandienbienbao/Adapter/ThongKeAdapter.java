package com.example.nhandienbienbao.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhandienbienbao.Models.ThongKeItem;
import com.example.nhandienbienbao.R;

import java.io.File;
import java.util.List;

public class ThongKeAdapter extends RecyclerView.Adapter<ThongKeAdapter.ThongKeViewHolder> {

    private final List<ThongKeItem> thongKeList;
    private final Context context;

    public ThongKeAdapter(Context context, List<ThongKeItem> thongKeList) {
        this.context = context;
        this.thongKeList = thongKeList;
    }

    @NonNull
    @Override
    public ThongKeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_thongke, parent, false);
        return new ThongKeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThongKeViewHolder holder, int position) {
        ThongKeItem item = thongKeList.get(position);

        holder.textLabel.setText(item.label);
        holder.textAccuracy.setText("Độ chính xác: " + item.accuracy);
        holder.textTime.setText(item.time);

        File imageFile = new File(context.getExternalFilesDir("Cropped"), item.imageName);
        if (imageFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            holder.imgCropped.setImageBitmap(bitmap);
        } else {
            holder.imgCropped.setImageResource(R.drawable.ic_placeholder);
        }
    }


    @Override
    public int getItemCount() {
        return thongKeList.size();
    }

    public static class ThongKeViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCropped;
        TextView textLabel, textAccuracy, textTime;

        public ThongKeViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCropped = itemView.findViewById(R.id.imgCropped);
            textLabel = itemView.findViewById(R.id.textLabel);
            textAccuracy = itemView.findViewById(R.id.textAccuracy);
            textTime = itemView.findViewById(R.id.textTime);
        }
    }
}