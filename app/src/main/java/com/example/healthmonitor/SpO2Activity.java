package com.example.healthmonitor;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class SpO2Activity extends AppCompatActivity {

    private TextView tvSpO2, tvSpO2Status, tvSpO2Guide;
    private ProgressBar gaugeBar;
    private Button btnStart, btnStop, btnNext, btnBack;
    private Handler handler = new Handler();
    private Random random = new Random();
    private boolean isMeasuring = false;
    private float currentSpO2 = 98f;
    private boolean measureAll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spo2);
        measureAll = getIntent().getBooleanExtra("MEASURE_ALL", false);

        tvSpO2       = findViewById(R.id.tvSpO2);
        tvSpO2Status = findViewById(R.id.tvSpO2Status);
        tvSpO2Guide  = findViewById(R.id.tvSpO2Guide);
        gaugeBar     = findViewById(R.id.gaugeBar);
        btnStart     = findViewById(R.id.btnSpO2Start);
        btnStop      = findViewById(R.id.btnSpO2Stop);
        btnNext      = findViewById(R.id.btnSpO2Next);
        btnBack      = findViewById(R.id.btnSpO2Back);

        btnStart.setOnClickListener(v -> startMeasure());
        btnStop.setOnClickListener(v -> stopMeasure());
        btnNext.setOnClickListener(v -> {
            if (measureAll) {
                Intent intent = new Intent(this, TemperatureActivity.class);
                intent.putExtra("MEASURE_ALL", true);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, SpO2ResultActivity.class);
                intent.putExtra("MEASURED_VALUE", currentSpO2);
                startActivity(intent);
            }
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        btnBack.setOnClickListener(v -> finish());
    }

    private void startMeasure() {
        isMeasuring = true;
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
        tvSpO2Guide.setText("손가락을 센서에 올려주세요.");
        handler.post(measureRunnable);
    }

    private void stopMeasure() {
        isMeasuring = false;
        handler.removeCallbacks(measureRunnable);
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        if (currentSpO2 < 95f) {
            new AlertDialog.Builder(this)
                    .setTitle("⚠ 산소포화도 저하")
                    .setMessage(String.format("%.1f%% — 정상 범위(95~100%%) 미만입니다.\n즉시 의료진 상담을 권장합니다.", currentSpO2))
                    .setPositiveButton("확인", null).show();
        }
    }

    private final Runnable measureRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isMeasuring) return;
            float delta = (random.nextFloat() * 2f) - 1f;
            currentSpO2 = Math.max(88f, Math.min(100f, currentSpO2 + delta));
            tvSpO2.setText(String.format("%.1f%%", currentSpO2));

            // 게이지 업데이트
            int progress = (int) ((currentSpO2 - 88f) / 12f * 100);
            ValueAnimator anim = ValueAnimator.ofInt(gaugeBar.getProgress(), progress);
            anim.setDuration(400);
            anim.addUpdateListener(a -> gaugeBar.setProgress((int) a.getAnimatedValue()));
            anim.start();

            if (currentSpO2 < 95f) {
                tvSpO2Status.setText("⚠ 저산소증 위험");
                tvSpO2Status.setTextColor(0xFFEF5350);
                tvSpO2.setTextColor(0xFFEF5350);
            } else if (currentSpO2 < 97f) {
                tvSpO2Status.setText("△ 주의 구간");
                tvSpO2Status.setTextColor(0xFFFFA726);
                tvSpO2.setTextColor(0xFFFFA726);
            } else {
                tvSpO2Status.setText("● 정상");
                tvSpO2Status.setTextColor(0xFF69F0AE);
                tvSpO2.setTextColor(0xFF4FC3F7);
            }
            handler.postDelayed(this, 800);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
