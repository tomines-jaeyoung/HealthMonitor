package com.example.healthmonitor;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class HeartRateResultActivity extends AppCompatActivity {

    private TextView tvPeak, tvMean, tvCurrent, tvStability, tvJudgement;
    private DataGraphView graphView;
    private Button btnNext, btnBack;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_result);

        int measuredValue = getIntent().getIntExtra("MEASURED_VALUE", 72);

        getSharedPreferences("HealthMonitorPrefs", MODE_PRIVATE).edit()
                .putFloat("last_heart_rate", (float) measuredValue)
                .apply();

        tvPeak      = findViewById(R.id.tvHRPeak);
        tvMean      = findViewById(R.id.tvHRMean);
        tvCurrent   = findViewById(R.id.tvHRCurrent);
        tvStability = findViewById(R.id.tvHRStability);
        tvJudgement = findViewById(R.id.tvHRJudgement);
        graphView   = findViewById(R.id.hrGraphView);
        btnNext     = findViewById(R.id.btnHRNext);
        btnBack     = findViewById(R.id.btnHRBack);

        loadData(measuredValue);

        if (measuredValue < 60 || measuredValue > 100) {
            new AlertDialog.Builder(this)
                    .setTitle("⚠ 비정상 심박수")
                    .setMessage(measuredValue + " BPM — 정상 범위(60~100)를 벗어났습니다.")
                    .setPositiveButton("확인", null).show();
        }

        btnNext.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadData(int value) {
        tvPeak.setText(String.valueOf(value + random.nextInt(10)));
        tvMean.setText(String.valueOf(value - 5 + random.nextInt(10)));
        tvCurrent.setText(value + " BPM");

        if (value < 60) {
            tvJudgement.setText("⚠ 서맥 — 느린 심박");
            tvJudgement.setTextColor(0xFFEF5350);
            tvStability.setText("불량"); tvStability.setTextColor(0xFFEF5350);
        } else if (value > 100) {
            tvJudgement.setText("⚠ 빈맥 — 빠른 심박");
            tvJudgement.setTextColor(0xFFFFA726);
            tvStability.setText("주의"); tvStability.setTextColor(0xFFFFA726);
        } else {
            tvJudgement.setText("● 정상 범위");
            tvJudgement.setTextColor(0xFF69F0AE);
            tvStability.setText("우수"); tvStability.setTextColor(0xFF4FC3F7);
        }

        if (graphView != null) {
            float[] data = new float[12];
            float cur = value;
            for (int i = 0; i < 12; i++) {
                cur = Math.max(50, Math.min(130, cur + (random.nextInt(11) - 5)));
                data[i] = (i == 11) ? value : cur;
            }
            String[] labels = {"11h전","","","8h전","","","5h전","","","2h전","","현재"};
            graphView.setData(data, labels);
        }
    }
}
