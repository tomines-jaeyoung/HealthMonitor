package com.example.healthmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

/**
 * 실시간 스캔 액티비티 (3D 인체 Scanning)
 *
 * 사용 기술스택:
 * - Chronometer: 측정 경과 시간 표시
 * - ToggleButton: 측정 시작/정지
 * - RelativeLayout: 전체 레이아웃
 * - FrameLayout: 3D 인체 이미지 + 스캔 바 겹치기
 * - TextView: 실시간 수치, 상태 텍스트 (점 애니메이션)
 */
public class ScanningActivity extends AppCompatActivity {

    private Chronometer chronometerScan;
    private ToggleButton toggleBtnScan;
    private TextView tvScanningText;
    private TextView tvLiveValue;
    private TextView tvCurrentValue;
    private View vBodyScanBar;
    private Button btnScanNext;

    private Handler handler = new Handler();
    private boolean isMeasuring = false;
    private int dotCount = 0;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        initViews();
        setupListeners();
        startBodyScanAnimation();
    }

    private void initViews() {
        chronometerScan = findViewById(R.id.chronometerScan);
        toggleBtnScan   = findViewById(R.id.toggleBtnScan);
        tvScanningText  = findViewById(R.id.tvScanningText);
        tvLiveValue     = findViewById(R.id.tvLiveValue);
        tvCurrentValue  = findViewById(R.id.tvCurrentValue);
        vBodyScanBar    = findViewById(R.id.vBodyScanBar);
        btnScanNext     = findViewById(R.id.btnScanNext);
    }

    /**
     * 스캔 바 위아래 애니메이션 (FrameLayout 활용)
     */
    private void startBodyScanAnimation() {
        TranslateAnimation anim = new TranslateAnimation(0, 0, 0, 300);
        anim.setDuration(2000);
        anim.setRepeatCount(TranslateAnimation.INFINITE);
        anim.setRepeatMode(TranslateAnimation.REVERSE);
        vBodyScanBar.startAnimation(anim);

        // 점 애니메이션 시작
        handler.post(dotAnimRunnable);
    }

    private int lastMeasuredValue = 110;

    /**
     * "scanning..." 점 애니메이션
     */
    private Runnable dotAnimRunnable = new Runnable() {
        @Override
        public void run() {
            dotCount = (dotCount + 1) % 4;
            String dots = ".".repeat(dotCount);
            if (isMeasuring) {
                tvScanningText.setText("scanning" + dots);
            } else {
                tvScanningText.setText("대기 중...");
            }
            handler.postDelayed(this, 400);
        }
    };

    /**
     * 실시간 혈류 수치 시뮬레이션
     */
    private Runnable measureRunnable = new Runnable() {
        @Override
        public void run() {
            if (isMeasuring) {
                // 이전값 기준 ±10~15 범위로 점진적 변화
                int delta = (random.nextInt(11) - 5); // -5 ~ +5
                int value = lastMeasuredValue + delta;
                // 70~135 범위로 클램핑
                value = Math.max(80, Math.min(140, value));
                lastMeasuredValue = value; // 마지막 측정값 저장
                String valueText = value + " cm/s";
                tvLiveValue.setText(valueText);
                tvCurrentValue.setText("혈류 속도: " + valueText);

                // 위험 수치 판단
                if (value < 100) {
                    tvLiveValue.setTextColor(0xFFEF5350);  // 빨간색
                } else {
                    tvLiveValue.setTextColor(0xFF4FC3F7);  // 파란색
                }

                handler.postDelayed(this, 800);
            }
        }
    };

    private void setupListeners() {
        // ToggleButton: 측정 시작/정지
        toggleBtnScan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isMeasuring = isChecked;

            if (isChecked) {
                // 측정 시작
                chronometerScan.setBase(android.os.SystemClock.elapsedRealtime());
                chronometerScan.start();
                handler.post(measureRunnable);
                tvScanningText.setText("scanning...");
            } else {
                // 측정 중지
                chronometerScan.stop();
                handler.removeCallbacks(measureRunnable);
                tvScanningText.setText("측정 완료");
            }
        });

        // 결과 보기 버튼 → 혈류 측정 화면으로
        btnScanNext.setOnClickListener(v -> {
            // 측정 중이면 중지
            if (isMeasuring) {
                toggleBtnScan.setChecked(false);
                isMeasuring = false;
                chronometerScan.stop();
            }
            Intent intent = new Intent(ScanningActivity.this, BloodFlowActivity.class);
            intent.putExtra("MEASURED_VALUE", lastMeasuredValue); // 인텐트로 데이터 전달
            intent.putExtra("MEASURE_ALL", getIntent().getBooleanExtra("MEASURE_ALL", false));
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(dotAnimRunnable);
        handler.removeCallbacks(measureRunnable);
        if (isMeasuring) {
            chronometerScan.stop();
        }
    }
}
