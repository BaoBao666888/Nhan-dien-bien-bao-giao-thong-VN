package com.example.nhandienbienbao.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.nhandienbienbao.Models.Violation;
import com.example.nhandienbienbao.R;

import java.util.List;

public class ViolationAdapter extends RecyclerView.Adapter<ViolationAdapter.ViolationViewHolder> {

    private final List<Violation> violations;

    public ViolationAdapter(List<Violation> violations) {
        this.violations = violations;
    }

    @Override
    public ViolationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_violation, parent, false);
        return new ViolationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViolationViewHolder holder, int position) {
        Violation v = violations.get(position);
        holder.textBienSo.setText("🚗 Biển số: " + v.bienKiemSoat);
        holder.textThoiGian.setText("📅 Thời gian: " + v.thoiGian);
        holder.textDiaDiem.setText("📍 Địa điểm: " + v.diaDiem);
        holder.textHanhVi.setText("🔧 Hành vi: " + (v.hanhVi == null ? "Không rõ" : v.hanhVi));
        holder.textTrangThai.setText("🛑 Trạng thái: " + v.trangThai);
        holder.textMucPhat.setText("💵 Mức phạt: " + (v.mucPhat == null ? "Không rõ" : v.mucPhat));
    }

    @Override
    public int getItemCount() {
        return violations.size();
    }

    public static class ViolationViewHolder extends RecyclerView.ViewHolder {
        TextView textBienSo, textThoiGian, textDiaDiem, textHanhVi, textTrangThai, textMucPhat;

        public ViolationViewHolder(View view) {
            super(view);
            textBienSo = view.findViewById(R.id.textBienSo);
            textThoiGian = view.findViewById(R.id.textThoiGian);
            textDiaDiem = view.findViewById(R.id.textDiaDiem);
            textHanhVi = view.findViewById(R.id.textHanhVi);
            textTrangThai = view.findViewById(R.id.textTrangThai);
            textMucPhat = view.findViewById(R.id.textMucPhat);
        }
    }
}
