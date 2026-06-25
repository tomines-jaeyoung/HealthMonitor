package com.example.healthmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 센서 확인 액티비티
 *
 * 사용 기술스택:
 * - TableLayout: 체크리스트 정렬 (XML에서 사용)
 * - CheckBox: 각 확인 항목
 * - TextView: 상태 메시지
 * - Button: 다음으로
 * - LinearLayout: 기본 레이아웃
 */
public class SensorCheckActivity extends AppCompatActivity {

    private CheckBox cbSensorAttached;
    private CheckBox cbBluetooth;
    private CheckBox cbPermission;
    private CheckBox cbSkinContact;
    private TextView tvChecklistStatus;
    private TextView tvConnectionSuccess;
    private Button btnSensorNext;
    private ProgressBar progressLoading;

    private Handler handler = new Handler();
    private Runnable loadingRunnable;
    private int loadingDotCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_check);

        initViews();
        startAutoCheckSequence();
    }

    private void initViews() {
        cbSensorAttached = findViewById(R.id.cbSensorAttached);
        cbBluetooth      = findViewById(R.id.cbBluetooth);
        cbPermission     = findViewById(R.id.cbPermission);
        cbSkinContact    = findViewById(R.id.cbSkinContact);
        tvChecklistStatus = findViewById(R.id.tvChecklistStatus);
        tvConnectionSuccess = findViewById(R.id.tvConnectionSuccess);
        btnSensorNext    = findViewById(R.id.btnSensorNext);
        progressLoading  = findViewById(R.id.progressLoading);

        // 사용자가 수동으로 체크하지 못하도록 막음
        cbSensorAttached.setClickable(false);
        cbBluetooth.setClickable(false);
        cbPermission.setClickable(false);
        cbSkinContact.setClickable(false);

        btnSensorNext.setEnabled(false);
        btnSensorNext.setAlpha(0.5f);

        // 버튼 클릭 시 수동 전환 (자동 전환 전이라도 누를 수 있음)
        btnSensorNext.setOnClickListener(v -> navigateToNext());
    }

    private void startAutoCheckSequence() {
        tvChecklistStatus.setText("시스템 자동 점검 중...");
        
        // 1초 뒤 첫 번째 체크
        handler.postDelayed(() -> {
            cbSensorAttached.setChecked(true);
        }, 1000);

        // 1.8초 뒤 두 번째 체크
        handler.postDelayed(() -> {
            cbBluetooth.setChecked(true);
        }, 1800);

        // 2.6초 뒤 세 번째 체크
        handler.postDelayed(() -> {
            cbPermission.setChecked(true);
        }, 2600);

        // 3.4초 뒤 네 번째 체크 & 완료 처리
        handler.postDelayed(() -> {
            cbSkinContact.setChecked(true);
            
            if (tvConnectionSuccess != null) {
                tvConnectionSuccess.setVisibility(View.VISIBLE);
            }
            
            // 모든 체크 완료 후
            btnSensorNext.setEnabled(true);
            btnSensorNext.setAlpha(1.0f);
            btnSensorNext.setText("확인 완료");
            
            startLoadingAnimation();
            
        }, 3400);
    }

    private void startLoadingAnimation() {
        tvChecklistStatus.setTextColor(0xFF69F0AE); // 초록색
        progressLoading.setVisibility(View.VISIBLE); // 동그라미 로딩 표시

        loadingRunnable = new Runnable() {
            @Override
            public void run() {
                loadingDotCount = (loadingDotCount + 1) % 4;
                StringBuilder dots = new StringBuilder();
                for (int i = 0; i < loadingDotCount; i++) dots.append(".");
                tvChecklistStatus.setText("시스템 준비 중" + dots.toString());
                handler.postDelayed(this, 400);
            }
        };
        handler.post(loadingRunnable);

        // 2.5초 뒤 자동 전환
        handler.postDelayed(this::navigateToNext, 2500);
    }

    private void navigateToNext() {
        handler.removeCallbacksAndMessages(null);
        Intent intent = new Intent(SensorCheckActivity.this, ScanningActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish(); // 자동 전환 후 뒤로가기 시 다시 이 화면이 나오지 않도록 종료
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
