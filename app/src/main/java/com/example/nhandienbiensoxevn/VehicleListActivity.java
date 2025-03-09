package com.example.nhandienbiensoxevn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class VehicleListActivity extends AppCompatActivity {
    private RecyclerView rvVehicleList;
    private VehicleAdapter adapter;
    private List<String> vehicleList;
    private Button btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_list);

        rvVehicleList = findViewById(R.id.rvVehicleList);
        btnExit = findViewById(R.id.btnExit);

        // Giả lập dữ liệu
        vehicleList = getIntent().getStringArrayListExtra("licensePlates");
        if (vehicleList == null) {
            vehicleList = new ArrayList<>();
            vehicleList.add("51A-12345");
            vehicleList.add("30B-67890");
            vehicleList.add("65C-54321");
        }
//        if (vehicleList == null) {
//            vehicleList = new ArrayList<>();
//        }

        rvVehicleList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VehicleAdapter(this, vehicleList);
        rvVehicleList.setAdapter(adapter);

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VehicleListActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
