package com.example.healthmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class TemperatureActivity extends AppCompatActivity {

    private TextView tvTemp, tvTempStatus, tvTempGuide, tvThermometer;
    private Button btnStart, btnStop, btnFinish, btnBack;
    private Handler handler = new Handler();
    private Random random = new Random();
    private boolean isMeasuring = false;
    private float currentTemp = 36.5f;
    private boolean measureAll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        measureAll = getIntent().getBooleanExtra("MEASURE_ALL", false);

        tvTemp        = findViewById(R.id.tvTemp);
        tvTempStatus  = findViewById(R.id.tvTempStatus);
        tvTempGuide   = findViewById(R.id.tvTempGuide);
        tvThermometer = findViewById(R.id.tvThermometer);
        btnStart      = findViewById(R.id.btnTempStart);
        btnStop       = findViewById(R.id.btnTempStop);
        btnFinish     = findViewById(R.id.btnTempFinish);
        btnBack       = findViewById(R.id.btnTempBack);

        btnStart.setOnClickListener(v -> startMeasure());
        btnStop.setOnClickListener(v -> stopMeasure());
        btnFinish.setOnClickListener(v -> {
            if (measureAll) {
                new AlertDialog.Builder(this)
                        .setTitle("✅ 전체 측정 완료")
                        .setMessage("모든 항목 측정이 완료되었습니다.")
                        .setPositiveButton("결과 보기", (d, w) -> {
                            Intent intent = new Intent(this, TemperatureResultActivity.class);
                            intent.putExtra("MEASURED_VALUE", currentTemp);
                            startActivity(intent);
                        }).show();
            } else {
                Intent intent = new Intent(this, TemperatureResultActivity.class);
                intent.putExtra("MEASURED_VALUE", currentTemp);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        btnBack.setOnClickListener(v -> finish());
    }

    private void startMeasure() {
        isMeasuring = true;
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
        tvTempGuide.setText("체온계를 올바른 위치에 대주세요.");
        handler.post(measureRunnable);
    }

    private void stopMeasure() {
        isMeasuring = false;
        handler.removeCallbacks(measureRunnable);
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);

        if (currentTemp >= 38f) {
            new AlertDialog.Builder(this)
                    .setTitle("⚠ 발열 감지")
                    .setMessage(String.format("%.1f°C — 발열 상태입니다.\n충분한 휴식을 취하고 의료진 상담을 권장합니다.", currentTemp))
                    .setPositiveButton("확인", null).show();
        }
    }

    private final Runnable measureRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isMeasuring) return;
            float delta = (random.nextFloat() * 0.4f) - 0.2f;
            currentTemp = Math.max(35.0f, Math.min(40.0f, currentTemp + delta));
            tvTemp.setText(String.format("%.1f°C", currentTemp));

            // 온도계 이모지 높이로 시각화
            int bars = (int) ((currentTemp - 35f) / 5f * 10);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bars; i++) sb.append("█");
            tvThermometer.setText("🌡 " + sb.toString());

            if (currentTemp >= 38f) {
                tvTempStatus.setText("⚠ 발열");
                tvTempStatus.setTextColor(0xFFEF5350);
                tvTemp.setTextColor(0xFFEF5350);
                tvThermometer.setTextColor(0xFFEF5350);
            } else if (currentTemp >= 37.5f) {
                tvTempStatus.setText("△ 미열");
                tvTempStatus.setTextColor(0xFFFFA726);
                tvTemp.setTextColor(0xFFFFA726);
                tvThermometer.setTextColor(0xFFFFA726);
            } else {
                tvTempStatus.setText("● 정상");
                tvTempStatus.setTextColor(0xFF69F0AE);
                tvTemp.setTextColor(0xFF4FC3F7);
                tvThermometer.setTextColor(0xFF4FC3F7);
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
