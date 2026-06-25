package com.example.healthmonitor;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

/**
 * 혈류 측정 액티비티 (Blood Flow Velocity)
 *
 * 사용 기술스택:
 * - EditText: Peak/Mean 목표값 직접 입력
 * - ImageButton: 다음 화면으로
 * - ToggleButton: 오름/내림차순 정렬 전환
 * - GridLayout: 수치 2열 배치 (XML에서 사용)
 * - RadioButton: 날짜 탭 (오늘/1주일/1개월)
 * - RelativeLayout, FrameLayout: 레이아웃
 * - TextView: 판정 결과 표시
 */
public class BloodFlowActivity extends AppCompatActivity {

    private EditText etPeakValue;
    private EditText etMeanValue;
    private TextView tvCurrentSpeed;
    private TextView tvStability;
    private TextView tvJudgement;
    private TextView tvGaugeValue;
    private DataGraphView ivFlowGraph;
    private ToggleButton toggleSort;
    private RadioGroup rgDateTab;
    private ImageButton btnBloodFlowNext;
    private ImageButton btnBloodFlowBack;
    private ImageView ivGaugeFill;

    // 정상 기준치
    private static final int NORMAL_MIN = 100;
    private static final int NORMAL_MAX = 120;
    
    private int currentMeasuredValue = 80;
    private Random random = new Random();
    private boolean measureAll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_flow);

        initViews();
        setupListeners();
        
        // 이전 스캔 화면에서 넘어온 실시간 측정값 받기
        currentMeasuredValue = getIntent().getIntExtra("MEASURED_VALUE", 80);
        measureAll = getIntent().getBooleanExtra("MEASURE_ALL", false);

        getSharedPreferences("HealthMonitorPrefs", MODE_PRIVATE).edit()
                .putFloat("last_blood_flow", (float) currentMeasuredValue)
                .apply();
        
        loadTodayData(currentMeasuredValue);
    }

    private void initViews() {
        etPeakValue     = findViewById(R.id.etPeakValue);
        etMeanValue     = findViewById(R.id.etMeanValue);
        tvCurrentSpeed  = findViewById(R.id.tvCurrentSpeed);
        tvStability     = findViewById(R.id.tvStability);
        tvJudgement     = findViewById(R.id.tvJudgement);
        tvGaugeValue    = findViewById(R.id.tvGaugeValue);
        ivFlowGraph     = findViewById(R.id.ivFlowGraph);
        toggleSort      = findViewById(R.id.toggleSort);
        rgDateTab       = findViewById(R.id.rgDateTab);
        btnBloodFlowNext = findViewById(R.id.btnBloodFlowNext);
        btnBloodFlowBack = findViewById(R.id.btnBloodFlowBack);
        ivGaugeFill      = findViewById(R.id.ivGaugeFill);
    }

    private void setupListeners() {
        // EditText: Peak 값 변경 시 실시간 판정
        etPeakValue.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    int value = Integer.parseInt(s.toString());
                    tvGaugeValue.setText(String.valueOf(value));
                    updateJudgement(value);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // EditText: Mean 값 변경 시 안정성 업데이트
        etMeanValue.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    int mean = Integer.parseInt(s.toString());
                    updateStability(mean);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // ToggleButton: 오름/내림차순 정렬 전환
        toggleSort.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tvCurrentSpeed.setText("60 → 80 (오름)");
            } else {
                tvCurrentSpeed.setText("80 → 60 (내림)");
            }
        });

        // RadioGroup: 날짜 탭 선택
        rgDateTab.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbToday) {
                loadTodayData(currentMeasuredValue);
            } else if (checkedId == R.id.rbWeek) {
                loadWeekData();
            } else if (checkedId == R.id.rbMonth) {
                loadMonthData();
            }
        });

        // ImageButton: 다음 화면 (파형 데이터 또는 전체 측정 4분할 화면)
        btnBloodFlowNext.setOnClickListener(v -> {
            if (measureAll) {
                Intent intent = new Intent(BloodFlowActivity.this, FullMeasureActivity.class);
                intent.putExtra("MEASURE_ALL", true);
                startActivity(intent);
            } else {
                Intent intent = new Intent(BloodFlowActivity.this, WaveformActivity.class);
                startActivity(intent);
            }
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 이전 버튼
        btnBloodFlowBack.setOnClickListener(v -> {
            finish();
        });
    }

    /**
     * 수치에 따른 판정 업데이트
     * (정상 범위: 50~100 cm/s)
     */
    private void updateJudgement(int value) {
        if (value < NORMAL_MIN) {
            tvJudgement.setText("⚠ 위험 — 혈류 부족");
            tvJudgement.setTextColor(0xFFEF5350);
        } else if (value > NORMAL_MAX) {
            tvJudgement.setText("⚠ 주의 — 혈류 과다");
            tvJudgement.setTextColor(0xFFFFA726);
        } else {
            tvJudgement.setText("● 정상 범위");
            tvJudgement.setTextColor(0xFF69F0AE);
        }
    }

    /**
     * Mean 값에 따른 안정성 업데이트
     */
    private void updateStability(int mean) {
        if (mean < 80) {
            tvStability.setText("불량");
            tvStability.setTextColor(0xFFEF5350);
        } else if (mean < 100) {
            tvStability.setText("양호");
            tvStability.setTextColor(0xFF69F0AE);
        } else {
            tvStability.setText("우수");
            tvStability.setTextColor(0xFF4FC3F7);
        }
    }

    /** 오늘 데이터 로드 및 애니메이션 */
    private void loadTodayData(int finalValue) {
        // TextWatcher가 tvGaugeValue를 변경하겠지만, 그 직후에 애니메이터가 숫자를 카운트업 합니다.
        etPeakValue.setText(String.valueOf(finalValue));
        etMeanValue.setText(String.valueOf(finalValue - 25)); // Mean은 적당히 Peak보다 낮게 시뮬레이션
        tvCurrentSpeed.setText(String.valueOf(finalValue - 10));
        updateJudgement(finalValue);

        if (ivFlowGraph != null) {
            float[] data = generateMockData(12, finalValue);
            String[] labels = {"11h전", "", "", "8h전", "", "", "5h전", "", "", "2h전", "", "현재"};
            ivFlowGraph.setData(data, labels);
        }

        // 1. 숫자 카운트업 애니메이션
        ValueAnimator countAnimator = ValueAnimator.ofInt(0, finalValue);
        countAnimator.setDuration(1200); // 1.2초 동안
        countAnimator.addUpdateListener(animation -> {
            int val = (int) animation.getAnimatedValue();
            tvGaugeValue.setText(String.valueOf(val));
        });
        countAnimator.start();

        // 2. 원형 게이지 채워지는 페이드인 애니메이션
        if (ivGaugeFill != null) {
            ivGaugeFill.setAlpha(0f);
            ivGaugeFill.animate().alpha(1f).setDuration(1200).start();
        }
    }

    /** 1주일 평균 데이터 로드 */
    private void loadWeekData() {
        etPeakValue.setText("75");
        etMeanValue.setText("50");
        tvCurrentSpeed.setText("63");
        updateJudgement(75);
        if (ivFlowGraph != null) {
            float[] data = generateMockData(12, 75);
            String[] labels = {"11일전", "", "", "8일전", "", "", "5일전", "", "", "2일전", "", "오늘"};
            ivFlowGraph.setData(data, labels);
        }
    }

    /** 1개월 평균 데이터 로드 */
    private void loadMonthData() {
        etPeakValue.setText("72");
        etMeanValue.setText("48");
        tvCurrentSpeed.setText("61");
        updateJudgement(72);
        if (ivFlowGraph != null) {
            float[] data = generateMockData(12, 72);
            String[] labels = {"11달전", "", "", "8달전", "", "", "5달전", "", "", "2달전", "", "이번달"};
            ivFlowGraph.setData(data, labels);
        }
    }

    /** 가짜 데이터 생성 유틸리티 */
    private float[] generateMockData(int count, int targetValue) {
        float[] data = new float[count];
        float current = targetValue;
        for (int i = 0; i < count; i++) {
            if (i == count - 1) {
                data[i] = targetValue;
            } else {
                // 이전값 기준 ±10 점진적 변화
                current = current + (random.nextInt(21) - 10);
                current = Math.max(80, Math.min(140, current));
                data[i] = current;
            }
        }
        return data;
    }
}
