package com.example.healthmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import java.util.Random;

public class HeartRateActivity extends AppCompatActivity {

    private TextView tvBPM, tvStatus, tvGuide;
    private ImageView ivHeart;
    private Button btnStart, btnStop, btnNext;
    private Handler handler = new Handler();
    private Random random = new Random();
    private boolean isMeasuring = false;
    private int currentBPM = 72;
    private boolean measureAll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate);
        measureAll = getIntent().getBooleanExtra("MEASURE_ALL", false);

        tvBPM    = findViewById(R.id.tvBPM);
        tvStatus = findViewById(R.id.tvHeartStatus);
        tvGuide  = findViewById(R.id.tvHeartGuide);
        ivHeart  = findViewById(R.id.ivHeart);
        btnStart = findViewById(R.id.btnHeartStart);
        btnStop  = findViewById(R.id.btnHeartStop);
        btnNext  = findViewById(R.id.btnHeartNext);

        btnStart.setOnClickListener(v -> startMeasure());
        btnStop.setOnClickListener(v -> stopMeasure());
        btnNext.setOnClickListener(v -> {
            if (measureAll) {
                Intent intent = new Intent(this, SpO2Activity.class);
                intent.putExtra("MEASURE_ALL", true);
                startActivity(intent);
            } else {
                goResult();
            }
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        findViewById(R.id.btnHeartBack).setOnClickListener(v -> finish());
    }

    private void startMeasure() {
        isMeasuring = true;
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
        tvGuide.setText("손가락을 센서에 올려주세요.");
        handler.post(measureRunnable);
        startHeartAnim();
    }

    private void stopMeasure() {
        isMeasuring = false;
        handler.removeCallbacks(measureRunnable);
        handler.removeCallbacks(animRunnable);
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);

        if (currentBPM > 100 || currentBPM < 60) {
            showDangerDialog(currentBPM > 100 ? "빠른 심박수" : "느린 심박수",
                    currentBPM + " BPM — 정상 범위(60~100)를 벗어났습니다.");
        }
    }

    private void goResult() {
        Intent intent = new Intent(this, HeartRateResultActivity.class);
        intent.putExtra("MEASURED_VALUE", currentBPM);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void showDangerDialog(String title, String msg) {
        new AlertDialog.Builder(this)
                .setTitle("⚠ " + title)
                .setMessage(msg)
                .setPositiveButton("확인", null)
                .show();
    }

    private final Runnable measureRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isMeasuring) return;
            int delta = random.nextInt(7) - 3;
            currentBPM = Math.max(50, Math.min(120, currentBPM + delta));
            tvBPM.setText(String.valueOf(currentBPM));

            if (currentBPM < 60) {
                tvStatus.setText("⚠ 서맥 — 느린 심박");
                tvStatus.setTextColor(0xFFEF5350);
                tvBPM.setTextColor(0xFFEF5350);
            } else if (currentBPM > 100) {
                tvStatus.setText("⚠ 빈맥 — 빠른 심박");
                tvStatus.setTextColor(0xFFFFA726);
                tvBPM.setTextColor(0xFFFFA726);
            } else {
                tvStatus.setText("● 정상");
                tvStatus.setTextColor(0xFF69F0AE);
                tvBPM.setTextColor(0xFF4FC3F7);
            }
            handler.postDelayed(this, 800);
        }
    };

    private final Runnable animRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isMeasuring) return;
            ScaleAnimation anim = new ScaleAnimation(1f, 1.3f, 1f, 1.3f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(300);
            anim.setRepeatCount(1);
            anim.setRepeatMode(Animation.REVERSE);
            ivHeart.startAnimation(anim);
            long interval = currentBPM > 0 ? (60000 / currentBPM) : 800;
            handler.postDelayed(this, interval);
        }
    };

    private void startHeartAnim() {
        handler.post(animRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
