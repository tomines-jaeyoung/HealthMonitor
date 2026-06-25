package com.example.healthmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class SpO2ResultActivity extends AppCompatActivity {

    private TextView tvSpO2Peak, tvSpO2Mean, tvSpO2Current, tvSpO2Stability, tvSpO2Judgement;
    private DataGraphView graphView;
    private Button btnNext, btnBack;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spo2_result);

        float measuredValue = getIntent().getFloatExtra("MEASURED_VALUE", 98f);

        getSharedPreferences("HealthMonitorPrefs", MODE_PRIVATE).edit()
                .putFloat("last_spo2", measuredValue)
                .apply();

        tvSpO2Peak      = findViewById(R.id.tvSpO2Peak);
        tvSpO2Mean      = findViewById(R.id.tvSpO2Mean);
        tvSpO2Current   = findViewById(R.id.tvSpO2Current);
        tvSpO2Stability = findViewById(R.id.tvSpO2Stability);
        tvSpO2Judgement = findViewById(R.id.tvSpO2Judgement);
        graphView       = findViewById(R.id.spo2GraphView);
        btnNext         = findViewById(R.id.btnSpO2RNext);
        btnBack         = findViewById(R.id.btnSpO2RBack);

        loadData(measuredValue);

        if (measuredValue < 95f) {
            new AlertDialog.Builder(this)
                    .setTitle("⚠ 산소포화도 저하")
                    .setMessage(String.format("%.1f%% — 즉시 의료진 상담을 권장합니다.", measuredValue))
                    .setPositiveButton("확인", null).show();
        }

        btnNext.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadData(float value) {
        tvSpO2Peak.setText(String.format("%.1f%%", Math.min(100f, value + 0.5f)));
        tvSpO2Mean.setText(String.format("%.1f%%", value - 0.3f));
        tvSpO2Current.setText(String.format("%.1f%%", value));

        if (value < 95f) {
            tvSpO2Judgement.setText("⚠ 저산소증 위험");
            tvSpO2Judgement.setTextColor(0xFFEF5350);
            tvSpO2Stability.setText("불량"); tvSpO2Stability.setTextColor(0xFFEF5350);
        } else if (value < 97f) {
            tvSpO2Judgement.setText("△ 주의 구간");
            tvSpO2Judgement.setTextColor(0xFFFFA726);
            tvSpO2Stability.setText("양호"); tvSpO2Stability.setTextColor(0xFFFFA726);
        } else {
            tvSpO2Judgement.setText("● 정상");
            tvSpO2Judgement.setTextColor(0xFF69F0AE);
            tvSpO2Stability.setText("우수"); tvSpO2Stability.setTextColor(0xFF4FC3F7);
        }

        if (graphView != null) {
            float[] data = new float[12];
            float cur = value;
            for (int i = 0; i < 12; i++) {
                cur = Math.max(88f, Math.min(100f, cur + (random.nextFloat() * 2f - 1f)));
                data[i] = (i == 11) ? value : cur;
            }
            String[] labels = {"11h전","","","8h전","","","5h전","","","2h전","","현재"};
            graphView.setData(data, labels);
        }
    }
}
