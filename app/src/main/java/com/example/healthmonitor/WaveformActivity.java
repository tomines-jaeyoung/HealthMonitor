package com.example.healthmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Random;

/**
 * 파형 데이터 액티비티 (Velocity Waveform)
 *
 * 사용 기술스택:
 * - RadioButton: Live/정적 모드 탭
 * - GridLayout: 수치 목록 2열 배치 (XML에서 사용)
 * - ScrollView + TextView: 하단 설명 스크롤
 * - ImageView: 경고 아이콘 (위험 수치 시 표시)
 */
public class WaveformActivity extends AppCompatActivity {

    private RadioGroup rgWaveMode;
    private ImageView ivWarningIcon;
    private WaveformView ivWaveformGraph;
    private TextView tvWave1, tvWave2, tvWave3, tvWave4;
    private TextView tvLiveVelocity;
    private TextView tvWaveDescription;
    private Button btnWaveformToHistory;
    private android.widget.ImageButton btnWaveformBack;
    private android.widget.ImageButton btnWaveformNextIcon;

    // 임계값 (이 이하면 위험)
    private static final float DANGER_THRESHOLD = 100.0f;
    private int measuredValue = 120;

    private Handler handler = new Handler();
    private Random random = new Random();
    private boolean isLiveMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waveform);

        measuredValue = getIntent().getIntExtra("MEASURED_VALUE", 120);

        initViews();
        setupListeners();
        loadLiveData();
    }

    private void initViews() {
        rgWaveMode          = findViewById(R.id.rgWaveMode);
        ivWarningIcon       = findViewById(R.id.ivWarningIcon);
        ivWaveformGraph     = findViewById(R.id.ivWaveformGraph);
        tvWave1             = findViewById(R.id.tvWave1);
        tvWave2             = findViewById(R.id.tvWave2);
        tvWave3             = findViewById(R.id.tvWave3);
        tvWave4             = findViewById(R.id.tvWave4);
        tvLiveVelocity      = findViewById(R.id.tvLiveVelocity);
        tvWaveDescription   = findViewById(R.id.tvWaveDescription);
        btnWaveformToHistory = findViewById(R.id.btnWaveformToHistory);
        btnWaveformBack      = findViewById(R.id.btnWaveformBack);
        btnWaveformNextIcon  = findViewById(R.id.btnWaveformNext);
    }

    /**
     * 위험 수치 확인 → 경고 아이콘 표시
     * ImageView 활용 예시
     */
    private void checkForDanger() {
        // 58.3이 DANGER_THRESHOLD 미만이므로 경고 표시
        float dangerValue = 58.3f;
        if (dangerValue < DANGER_THRESHOLD) {
            ivWarningIcon.setVisibility(android.view.View.VISIBLE);
            // 위험 수치 텍스트를 빨간색으로
            tvWave3.setTextColor(0xFFEF5350);
        }
    }

    private void setupListeners() {
        // RadioButton: Live / 정적 모드 전환
        rgWaveMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbLive) {
                isLiveMode = true;
                loadLiveData();
            } else if (checkedId == R.id.rbStatic) {
                isLiveMode = false;
                loadStaticData();
            }
        });

        // 이전 버튼 → 혈류 측정 화면으로 돌아가기
        btnWaveformBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 다음(아이콘) 버튼 → 히스토리 화면으로
        btnWaveformNextIcon.setOnClickListener(v -> {
            Intent intent = new Intent(WaveformActivity.this, HistoryActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 데이터 추이 보기 버튼 → 히스토리 화면으로
        btnWaveformToHistory.setOnClickListener(v -> {
            Intent intent = new Intent(WaveformActivity.this, HistoryActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private Runnable liveUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (isLiveMode) {
                // 33% 확률로 위험(80~99) / 정상(100~120) / 과다(121~140)
                int rand = random.nextInt(3);
                float currentVelocity;
                if (rand == 0) {
                    currentVelocity = 80.0f + random.nextFloat() * 20.0f;  // 위험: 80~99
                } else if (rand == 1) {
                    currentVelocity = 100.0f + random.nextFloat() * 21.0f; // 정상: 100~120
                } else {
                    currentVelocity = 121.0f + random.nextFloat() * 19.0f; // 과다: 121~140
                }
                tvLiveVelocity.setText(String.format(Locale.KOREA, "현재 실시간 속도: %.1f cm/s", currentVelocity));
                
                // 임계값 확인
                if (currentVelocity < DANGER_THRESHOLD) {
                    ivWarningIcon.setVisibility(android.view.View.VISIBLE);
                    tvLiveVelocity.setTextColor(0xFFEF5350);
                } else {
                    ivWarningIcon.setVisibility(android.view.View.GONE);
                    tvLiveVelocity.setTextColor(0xFF4FC3F7);
                }
                handler.postDelayed(this, 500); // 0.5초마다 갱신
            }
        }
    };

    /** 실시간 데이터 로드 */
    private void loadLiveData() {
        tvWave1.setText(String.format(Locale.KOREA, "%.1f", measuredValue + 2.0f));
        tvWave2.setText(String.format(Locale.KOREA, "%.1f", measuredValue - 1.4f));
        tvWave3.setText(String.format(Locale.KOREA, "%.1f", measuredValue - 3.7f));
        tvWave4.setText(String.format(Locale.KOREA, "%.1f", measuredValue + 1.1f));
        
        tvLiveVelocity.setVisibility(android.view.View.VISIBLE);
        ivWaveformGraph.setAnimating(true);
        ivWaveformGraph.setLineColor(0xFF4FC3F7); // 파란색 애니메이션 파형
        tvWaveDescription.setText("LIVE 측정 중입니다. 안정적인 측정을 위해 움직임을 최소화해주세요.");
        
        handler.removeCallbacks(liveUpdateRunnable);
        handler.post(liveUpdateRunnable);
    }

    /** 정적 데이터 로드 */
    private void loadStaticData() {
        handler.removeCallbacks(liveUpdateRunnable);
        tvLiveVelocity.setVisibility(android.view.View.GONE);
        ivWaveformGraph.setAnimating(false);
        ivWaveformGraph.setLineColor(0xFF90CAF9); // 정적일 때는 연한 파란색 정지 파형
        
        tvWaveDescription.setText("데이터 분석 중...");
        
        // 1.5초 후 분석 결과 도출
        handler.postDelayed(() -> {
            float meanData = measuredValue;
            tvWave1.setText(String.format(Locale.KOREA, "%.1f", meanData + 3.2f));
            tvWave2.setText(String.format(Locale.KOREA, "%.1f", meanData - 1.5f));
            tvWave3.setText(String.format(Locale.KOREA, "%.1f", meanData + 2.1f));
            tvWave4.setText(String.format(Locale.KOREA, "%.1f", meanData - 0.8f));
            
            ivWarningIcon.setVisibility(android.view.View.GONE);
            tvWave1.setTextColor(0xFFFFFFFF);
            tvWave2.setTextColor(0xFFFFFFFF);
            tvWave3.setTextColor(0xFFFFFFFF);
            tvWave4.setTextColor(0xFFFFFFFF);

            String newDescription = "혈류 파형 분석 결과 (갱신됨):\n\n"
                    + "측정된 전체 구간의 혈류 속도 평균은 " + meanData + " cm/s 로 정상 범위에 속합니다.\n"
                    + "파형 분석 결과, 수축기 및 확장기 비율이 안정적이며 중복맥파(Dicrotic Notch)가 뚜렷하게 관찰되어 혈관 탄성도가 매우 양호한 상태입니다.\n\n"
                    + "이전 대비 특이사항 없음.";
            tvWaveDescription.setText(newDescription);
        }, 1500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
