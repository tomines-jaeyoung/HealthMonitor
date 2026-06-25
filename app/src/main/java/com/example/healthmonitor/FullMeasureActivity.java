package com.example.healthmonitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Random;

public class FullMeasureActivity extends AppCompatActivity {

    private TextView tvFullBloodFlow, tvFullHeartRate, tvFullTemp, tvFullSpO2;
    private TextView tvFullBloodFlowStatus, tvFullHeartRateStatus, tvFullTempStatus, tvFullSpO2Status;
    private ImageView ivGaugeFill;
    private ProgressBar progressSpO2;
    private ImageView ivFullHeart;
    private TextView tvThermometerIcon;
    private TextView tvFullStatus;
    private Button btnFullAction;

    private Handler handler = new Handler();
    private Random random = new Random();

    private boolean isMeasuring = false;
    private int measureTime = 0;
    private static final int TOTAL_MEASURE_TIME = 10; // 5 seconds (500ms intervals)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_measure);

        initViews();
    }

    private void initViews() {
        tvFullBloodFlow       = findViewById(R.id.tvFullBloodFlow);
        tvFullHeartRate       = findViewById(R.id.tvFullHeartRate);
        tvFullTemp            = findViewById(R.id.tvFullTemp);
        tvFullSpO2            = findViewById(R.id.tvFullSpO2);

        tvFullBloodFlowStatus = findViewById(R.id.tvFullBloodFlowStatus);
        tvFullHeartRateStatus = findViewById(R.id.tvFullHeartRateStatus);
        tvFullTempStatus      = findViewById(R.id.tvFullTempStatus);
        tvFullSpO2Status      = findViewById(R.id.tvFullSpO2Status);

        ivGaugeFill       = findViewById(R.id.ivGaugeFill);
        progressSpO2      = findViewById(R.id.progressSpO2);
        ivFullHeart       = findViewById(R.id.ivFullHeart);
        tvThermometerIcon = findViewById(R.id.tvThermometerIcon);
        tvFullStatus      = findViewById(R.id.tvFullStatus);
        btnFullAction     = findViewById(R.id.btnFullAction);

        btnFullAction.setOnClickListener(v -> {
            if (isMeasuring) {
                // Do nothing
            } else if (btnFullAction.getText().toString().equals("결과 보고서 확인")) {
                // Go to report screen
                Intent intent = new Intent(FullMeasureActivity.this, HistoryActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                startFullMeasure();
            }
        });
    }

    private void startFullMeasure() {
        isMeasuring = true;
        btnFullAction.setEnabled(false);
        tvFullStatus.setText("종합 측정 중입니다. 잠시만 기다려주세요...");
        measureTime = 0;

        // Heart beat animation
        startHeartAnim();

        // Run measurement loop
        handler.post(measureRunnable);
    }

    private void startHeartAnim() {
        if (!isMeasuring) return;
        ScaleAnimation scale = new ScaleAnimation(
                1f, 1.25f, 1f, 1.25f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scale.setDuration(350);
        scale.setRepeatCount(Animation.INFINITE);
        scale.setRepeatMode(Animation.REVERSE);
        ivFullHeart.startAnimation(scale);
    }

    private final Runnable measureRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isMeasuring) return;

            measureTime++;
            if (measureTime < TOTAL_MEASURE_TIME) {
                // Generate fluctuating mock values
                int bloodFlow = 80 + random.nextInt(50); // 80~130
                int heartRate = 60 + random.nextInt(50); // 60~110
                double temp = 35.8 + random.nextDouble() * 2.5; // 35.8~38.3
                int spo2 = 90 + random.nextInt(11); // 90~100

                // Update text views
                tvFullBloodFlow.setText(String.format(Locale.KOREA, "%d", bloodFlow));
                tvFullHeartRate.setText(String.format(Locale.KOREA, "%d", heartRate));
                tvFullTemp.setText(String.format(Locale.KOREA, "%.1f°C", temp));
                tvFullSpO2.setText(String.format(Locale.KOREA, "%d%%", spo2));

                // Update blood flow circular gauge fill
                ivGaugeFill.setScaleY(bloodFlow / 150f);

                // Update SpO2 progress bar (88% to 100% mapped to 0-100)
                int spo2Progress = (int) ((spo2 - 88f) / 12f * 100);
                progressSpO2.setProgress(Math.max(0, Math.min(100, spo2Progress)));

                // Update thermometer bar level
                int bars = (int) ((temp - 35f) / 5f * 10);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < bars; i++) sb.append("█");
                tvThermometerIcon.setText("🌡 " + sb.toString());

                // Update statuses & text colors dynamically to match standalone UIs (without orange warnings)
                // 1. Blood Flow
                if (bloodFlow < 100) {
                    tvFullBloodFlowStatus.setText("⚠ 위험 — 혈류 부족");
                    tvFullBloodFlowStatus.setTextColor(0xFFEF5350);
                } else if (bloodFlow > 120) {
                    tvFullBloodFlowStatus.setText("⚠ 위험 — 혈류 과다");
                    tvFullBloodFlowStatus.setTextColor(0xFFEF5350);
                } else {
                    tvFullBloodFlowStatus.setText("● 정상 범위");
                    tvFullBloodFlowStatus.setTextColor(0xFF69F0AE);
                }

                // 2. Heart Rate
                if (heartRate < 60) {
                    tvFullHeartRateStatus.setText("⚠ 서맥 — 느린 심박");
                    tvFullHeartRateStatus.setTextColor(0xFFEF5350);
                    tvFullHeartRate.setTextColor(0xFFEF5350);
                } else if (heartRate > 100) {
                    tvFullHeartRateStatus.setText("⚠ 빈맥 — 빠른 심박");
                    tvFullHeartRateStatus.setTextColor(0xFFEF5350);
                    tvFullHeartRate.setTextColor(0xFFEF5350);
                } else {
                    tvFullHeartRateStatus.setText("● 정상");
                    tvFullHeartRateStatus.setTextColor(0xFF69F0AE);
                    tvFullHeartRate.setTextColor(0xFF4FC3F7);
                }

                // 3. Temp (>= 37.5 is danger/fever)
                if (temp >= 37.5) {
                    tvFullTempStatus.setText("⚠ 발열");
                    tvFullTempStatus.setTextColor(0xFFEF5350);
                    tvFullTemp.setTextColor(0xFFEF5350);
                    tvThermometerIcon.setTextColor(0xFFEF5350);
                } else {
                    tvFullTempStatus.setText("● 정상");
                    tvFullTempStatus.setTextColor(0xFF69F0AE);
                    tvFullTemp.setTextColor(0xFF4FC3F7);
                    tvThermometerIcon.setTextColor(0xFF4FC3F7);
                }

                // 4. SpO2 (< 95 is danger/low SpO2)
                if (spo2 < 95) {
                    tvFullSpO2Status.setText("⚠ 저산소증 위험");
                    tvFullSpO2Status.setTextColor(0xFFEF5350);
                    tvFullSpO2.setTextColor(0xFFEF5350);
                } else {
                    tvFullSpO2Status.setText("● 정상");
                    tvFullSpO2Status.setTextColor(0xFF69F0AE);
                    tvFullSpO2.setTextColor(0xFF4FC3F7);
                }

                handler.postDelayed(this, 500);
            } else {
                finishMeasure();
            }
        }
    };

    private void finishMeasure() {
        isMeasuring = false;
        ivFullHeart.clearAnimation();

        // Generate final stable values (normal/healthy range to make report look good)
        double finalBloodFlow = 100 + random.nextInt(21); // 100~120 cm/s
        double finalHeartRate = 60 + random.nextInt(41);   // 60~100 BPM
        double finalTemp = 36.0 + random.nextDouble() * 1.4; // 36.0~37.4 °C
        double finalSpO2 = 95 + random.nextInt(6);      // 95~100 %

        // Update UI
        tvFullBloodFlow.setText(String.format(Locale.KOREA, "%.0f", finalBloodFlow));
        tvFullHeartRate.setText(String.format(Locale.KOREA, "%.0f", finalHeartRate));
        tvFullTemp.setText(String.format(Locale.KOREA, "%.1f°C", finalTemp));
        tvFullSpO2.setText(String.format(Locale.KOREA, "%.0f%%", finalSpO2));

        ivGaugeFill.setScaleY((float) (finalBloodFlow / 150f));
        int finalSpo2Progress = (int) ((finalSpO2 - 88f) / 12f * 100);
        progressSpO2.setProgress(Math.max(0, Math.min(100, finalSpo2Progress)));

        int bars = (int) ((finalTemp - 35f) / 5f * 10);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bars; i++) sb.append("█");
        tvThermometerIcon.setText("🌡 " + sb.toString());

        // Update statuses & text colors dynamically for final values (without orange warnings)
        // 1. Blood Flow
        if (finalBloodFlow < 100) {
            tvFullBloodFlowStatus.setText("⚠ 위험 — 혈류 부족");
            tvFullBloodFlowStatus.setTextColor(0xFFEF5350);
        } else if (finalBloodFlow > 120) {
            tvFullBloodFlowStatus.setText("⚠ 위험 — 혈류 과다");
            tvFullBloodFlowStatus.setTextColor(0xFFEF5350);
        } else {
            tvFullBloodFlowStatus.setText("● 정상 범위");
            tvFullBloodFlowStatus.setTextColor(0xFF69F0AE);
        }

        // 2. Heart Rate
        if (finalHeartRate < 60) {
            tvFullHeartRateStatus.setText("⚠ 서맥 — 느린 심박");
            tvFullHeartRateStatus.setTextColor(0xFFEF5350);
            tvFullHeartRate.setTextColor(0xFFEF5350);
        } else if (finalHeartRate > 100) {
            tvFullHeartRateStatus.setText("⚠ 빈맥 — 빠른 심박");
            tvFullHeartRateStatus.setTextColor(0xFFEF5350);
            tvFullHeartRate.setTextColor(0xFFEF5350);
        } else {
            tvFullHeartRateStatus.setText("● 정상");
            tvFullHeartRateStatus.setTextColor(0xFF69F0AE);
            tvFullHeartRate.setTextColor(0xFF4FC3F7);
        }

        // 3. Temp (>= 37.5 is danger/fever)
        if (finalTemp >= 37.5) {
            tvFullTempStatus.setText("⚠ 발열");
            tvFullTempStatus.setTextColor(0xFFEF5350);
            tvFullTemp.setTextColor(0xFFEF5350);
            tvThermometerIcon.setTextColor(0xFFEF5350);
        } else {
            tvFullTempStatus.setText("● 정상");
            tvFullTempStatus.setTextColor(0xFF69F0AE);
            tvFullTemp.setTextColor(0xFF4FC3F7);
            tvThermometerIcon.setTextColor(0xFF4FC3F7);
        }

        // 4. SpO2 (< 95 is danger/low SpO2)
        if (finalSpO2 < 95) {
            tvFullSpO2Status.setText("⚠ 저산소증 위험");
            tvFullSpO2Status.setTextColor(0xFFEF5350);
            tvFullSpO2.setTextColor(0xFFEF5350);
        } else {
            tvFullSpO2Status.setText("● 정상");
            tvFullSpO2Status.setTextColor(0xFF69F0AE);
            tvFullSpO2.setTextColor(0xFF4FC3F7);
        }

        tvFullStatus.setText("✓ 종합 스캔 완료!");
        btnFullAction.setText("결과 보고서 확인");
        btnFullAction.setEnabled(true);

        // Save measured values to SharedPreferences
        SharedPreferences p = getSharedPreferences("HealthMonitorPrefs", MODE_PRIVATE);
        p.edit()
                .putFloat("all_blood_flow", (float) finalBloodFlow)
                .putFloat("all_heart_rate", (float) finalHeartRate)
                .putFloat("all_temp", (float) finalTemp)
                .putFloat("all_spo2", (float) finalSpO2)
                .apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(measureRunnable);
    }
}
