package com.example.nhandienbiensoxevn;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {
    private List<String> vehicleList;
    private Context context;

    // Constructor
    public VehicleAdapter(Context context, List<String> vehicleList) {
        this.context = context;
        this.vehicleList = vehicleList;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        String vehicle = vehicleList.get(position);
        holder.tvLicensePlate.setText(vehicle);
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    // ViewHolder class
    public static class VehicleViewHolder extends RecyclerView.ViewHolder {
        TextView tvLicensePlate;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLicensePlate = itemView.findViewById(R.id.tvLicensePlate);
        }
    }
}
