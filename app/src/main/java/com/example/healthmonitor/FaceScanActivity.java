package com.example.healthmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 페이스 스캐닝 액티비티
 *
 * 사용 기술스택:
 * - FrameLayout: 카메라 미리보기 + 스캔 레이저 바 겹치기
 * - RadioButton: 정면/측면 스캔 모드 선택
 * - TextView: 스캔 상태, 퍼센트
 * - ProgressBar: 스캔 진행률
 * - TableLayout: 완료/재시도 버튼 2열 배치
 */
public class FaceScanActivity extends AppCompatActivity {

    private ImageView ivFacePreview;
    private View vScanBar;
    private TextView tvScanStatus;
    private TextView tvScanPercent;
    private ProgressBar progressScan;
    private RadioGroup rgScanMode;
    private Button btnRetry;
    private Button btnScanComplete;

    private Handler handler = new Handler();
    private int scanProgress = 0;
    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_scan);

        initViews();
        setupListeners();

        // 실시간 값 반영
        Intent intent = getIntent();
        float avgVal = intent.getFloatExtra("AVG_VAL", 0f);
        TextView tvFaceScanTitle = findViewById(R.id.tvFaceScanTitle);
        if (avgVal > 0) {
            tvFaceScanTitle.setText(String.format("얼굴 인식 (실시간 평균: %.1f cm/s 반영)", avgVal));
        }

        startScanAnimation();
    }

    private void initViews() {
        ivFacePreview  = findViewById(R.id.ivFacePreview);
        vScanBar       = findViewById(R.id.vScanBar);
        tvScanStatus   = findViewById(R.id.tvScanStatus);
        tvScanPercent  = findViewById(R.id.tvScanPercent);
        progressScan   = findViewById(R.id.progressScan);
        rgScanMode     = findViewById(R.id.rgScanMode);
        btnRetry       = findViewById(R.id.btnRetry);
        btnScanComplete = findViewById(R.id.btnScanComplete);
    }

    /**
     * 스캔 레이저 바 위아래 애니메이션 (FrameLayout 활용)
     */
    private void startScanAnimation() {
        isScanning = true;
        vScanBar.setVisibility(View.VISIBLE);

        // FrameLayout 실제 높이 기준으로 끝까지 이동
        android.view.ViewGroup parent = (android.view.ViewGroup) vScanBar.getParent();
        parent.post(() -> {
            int totalHeight = parent.getHeight() - vScanBar.getHeight();
            TranslateAnimation scanAnim = new TranslateAnimation(0, 0, 0, totalHeight);
            scanAnim.setDuration(1800);
            scanAnim.setRepeatCount(TranslateAnimation.INFINITE);
            scanAnim.setRepeatMode(TranslateAnimation.REVERSE);
            vScanBar.startAnimation(scanAnim);
        });

        // 진행률 자동 증가
        handler.postDelayed(progressRunnable, 500);
    }

    /**
     * 진행률 업데이트 Runnable
     */
    private Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (isScanning && scanProgress < 100) {
                scanProgress += 5;
                progressScan.setProgress(scanProgress);
                tvScanPercent.setText(scanProgress + "%");

                if (scanProgress < 100) {
                    tvScanStatus.setText("분석 중...");
                    handler.postDelayed(this, 300);
                } else {
                    // 스캔 완료
                    if (rgScanMode.getCheckedRadioButtonId() == R.id.rbFront) {
                        tvScanStatus.setText("정면 완료, 측면 전환 중...");
                        vScanBar.clearAnimation();
                        vScanBar.setVisibility(View.GONE);
                        // 잠시 대기 후 측면으로 자동 전환
                        handler.postDelayed(() -> {
                            rgScanMode.check(R.id.rbSide); // 이 호출이 onCheckedChanged를 트리거하여 resetScan()이 호출됨
                        }, 1000);
                    } else {
                        tvScanStatus.setText("✓ 분석 완료");
                        vScanBar.clearAnimation();
                        vScanBar.setVisibility(View.GONE);
                    }
                }
            }
        }
    };

    private void setupListeners() {
        // RadioButton: 스캔 모드 변경 → 진행률 리셋
        rgScanMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbFront) {
                ivFacePreview.setImageResource(R.drawable.front_face);
                tvScanStatus.setText("정면 스캔 중...");
            } else if (checkedId == R.id.rbSide) {
                ivFacePreview.setImageResource(R.drawable.side_face);
                tvScanStatus.setText("측면 스캔 중...");
            }
            // 모드 변경 시 진행률 리셋
            resetScan();
        });

        // 재시도 버튼
        btnRetry.setOnClickListener(v -> resetScan());

        // 완료 버튼 → 센서 확인 화면 또는 전체 종합측정 화면으로
        btnScanComplete.setOnClickListener(v -> {
            boolean measureAll = getIntent().getBooleanExtra("MEASURE_ALL", false);
            if (measureAll) {
                Intent intent = new Intent(FaceScanActivity.this, FullMeasureActivity.class);
                intent.putExtra("MEASURE_ALL", true);
                startActivity(intent);
            } else {
                Intent intent = new Intent(FaceScanActivity.this, SensorCheckActivity.class);
                startActivity(intent);
            }
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void resetScan() {
        scanProgress = 0;
        isScanning = true;
        progressScan.setProgress(0);
        tvScanPercent.setText("0%");
        tvScanStatus.setText("분석 중...");
        vScanBar.setVisibility(View.VISIBLE);
        startScanAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(progressRunnable);
    }
}
