package com.example.healthmonitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MeasurementSelectActivity extends AppCompatActivity {

    private static final String PREF = "HealthMonitorPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement_select);

        // 혈류속도 선택
        findViewById(R.id.cardBloodFlow).setOnClickListener(v -> {
            saveType("blood_flow");
            goMain();
        });

        // 심박수 선택
        findViewById(R.id.cardHeartRate).setOnClickListener(v -> {
            saveType("heart_rate");
            goMain();
        });

        // 산소포화도 선택
        findViewById(R.id.cardSpO2).setOnClickListener(v -> {
            saveType("spo2");
            goMain();
        });

        // 체온 선택
        findViewById(R.id.cardTemperature).setOnClickListener(v -> {
            saveType("temperature");
            goMain();
        });

        // 전체 측정 → 혈류속도 스캔부터 시작
        findViewById(R.id.cardAll).setOnClickListener(v -> {
            saveType("all");
            Intent intent = new Intent(this, FaceScanActivity.class);
            intent.putExtra("MEASURE_ALL", true);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void saveType(String type) {
        getSharedPreferences(PREF, MODE_PRIVATE).edit()
                .putString("selected_measure_type", type)
                .apply();
    }

    private void goMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // 뒤로가기 막기 (선택 필수)
    @Override
    public void onBackPressed() {
        // 선택 없이 나가면 혈류속도 기본값
        saveType("blood_flow");
        goMain();
    }
}
