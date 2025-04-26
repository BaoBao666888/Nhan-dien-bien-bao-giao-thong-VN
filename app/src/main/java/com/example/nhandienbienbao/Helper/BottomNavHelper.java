package com.example.nhandienbienbao.Helper;

import android.app.Activity;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.nhandienbienbao.LoginActivity;
import com.example.nhandienbienbao.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BottomNavHelper {
    public static void setupBottomNav(Activity activity, int idThoat, int idTextView) {
        LinearLayout thoatBtn = activity.findViewById(idThoat);
        TextView thoatText = thoatBtn.findViewById(idTextView);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        thoatText.setText(user == null ? activity.getString(R.string.dang_nhap) : activity.getString(R.string.dang_xuat));

        thoatBtn.setOnClickListener(v -> {
            if (user == null) {
                activity.startActivity(new Intent(activity, LoginActivity.class));
                activity.finish();
            } else {
                new AlertDialog.Builder(activity)
                        .setTitle(activity.getString(R.string.dang_xuat))
                        .setMessage(activity.getString(R.string.ban_co_muon_dang_xuat_khong))
                        .setPositiveButton(activity.getString(R.string.dang_xuat), (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            activity.startActivity(new Intent(activity, LoginActivity.class));
                            activity.finish();
                        })
                        .setNegativeButton(activity.getString(R.string.huy), null)
                        .show();
            }
        });
    }
}
