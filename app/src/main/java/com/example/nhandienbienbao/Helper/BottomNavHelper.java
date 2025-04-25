package com.example.nhandienbienbao.Helper;

import android.app.Activity;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.nhandienbienbao.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BottomNavHelper {
    public static void setupBottomNav(Activity activity, int idThoat, int idTextView) {
        LinearLayout thoatBtn = activity.findViewById(idThoat);
        TextView thoatText = thoatBtn.findViewById(idTextView);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        thoatText.setText(user == null ? "Đăng nhập" : "Đăng xuất");

        thoatBtn.setOnClickListener(v -> {
            if (user == null) {
                activity.startActivity(new Intent(activity, LoginActivity.class));
                activity.finish();
            } else {
                new AlertDialog.Builder(activity)
                        .setTitle("Đăng xuất")
                        .setMessage("Bạn có muốn đăng xuất không?")
                        .setPositiveButton("Đăng xuất", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            activity.startActivity(new Intent(activity, LoginActivity.class));
                            activity.finish();
                        })
                        .setNegativeButton("Huỷ", null)
                        .show();
            }
        });
    }
}
