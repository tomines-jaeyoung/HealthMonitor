package com.example.healthmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.os.Handler;
import java.util.Random;
import java.util.Locale;
import java.util.Date;
import java.text.SimpleDateFormat;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 메인 대시보드 액티비티 (눈동자 인터랙션)
 *
 * 사용 기술스택:
 * - ViewFlipper: 배경 상태 전환 (정상=파랑, 위험=빨강)
 * - RelativeLayout: 눈동자 중앙 배치
 * - ImageView: 눈동자 이미지
 * - ImageButton: 측정 시작, 햄버거 메뉴
 * - TextView + OnClickListener: 마지막 측정값 클릭 이벤트
 * - SlidingDrawer: 하단 슬라이딩 메뉴
 */
public class MainActivity extends AppCompatActivity {

    // ViewFlipper - 배경 전환
    private ViewFlipper vfBackground;

    // 눈동자 관련 ImageView
    private ImageView ivLeftPupil;
    private ImageView ivRightPupil;
    private android.view.ViewGroup eyeContainer;

    // 눈동자 원래 위치 저장
    private float leftPupilOrigX, leftPupilOrigY;
    private float rightPupilOrigX, rightPupilOrigY;

    // 눈 흰자 (깜빡임용)
    private ImageView ivLeftEyeWhite;
    private ImageView ivRightEyeWhite;

    // 상태 표시 TextView
    private TextView tvLastMeasureValue;
    private TextView tvLastMeasureLabel;
    private TextView tvStatusLabel;
    private TextView tvStatsLabel;

    // SlidingDrawer
    private SlidingDrawer slidingDrawer;

    // 현재 상태 (true=정상, false=위험)
    private boolean isNormal = true;

    // 실시간 로직
    private Handler handler = new Handler();
    private Random random = new Random();
    private int stateTimer = 0;
    private float minVal = 999f, maxVal = 0f, sumVal = 0f;
    private int countVal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupEyeTracking();
        setupClickListeners();
        setupSlidingDrawer();

        handler.post(updateRunnable);
        handler.postDelayed(blinkRunnable, 3000);
    }

    /**
     * 뷰 초기화
     */
    private void initViews() {
        vfBackground   = findViewById(R.id.vfBackground);
        ivLeftPupil    = findViewById(R.id.ivLeftPupil);
        ivRightPupil   = findViewById(R.id.ivRightPupil);
        ivLeftEyeWhite = findViewById(R.id.ivLeftEyeWhite);
        ivRightEyeWhite= findViewById(R.id.ivRightEyeWhite);
        eyeContainer   = findViewById(R.id.eyeContainer);
        tvLastMeasureValue = findViewById(R.id.tvLastMeasureValue);
        tvLastMeasureLabel = findViewById(R.id.tvLastMeasureLabel);
        tvStatusLabel      = findViewById(R.id.tvStatusLabel);
        tvStatsLabel       = findViewById(R.id.tvStatsLabel);
        slidingDrawer      = findViewById(R.id.slidingDrawer);

        // 눈알 흰자 모서리 클리핑 → 가장자리 배경색 제거
        ivLeftEyeWhite.setClipToOutline(true);
        ivRightEyeWhite.setClipToOutline(true);
    }

    /**
     * 눈동자 터치 트래킹 설정
     * 사용자의 터치 좌표에 따라 눈동자 이동
     */
    private void setupEyeTracking() {
        View rootView = findViewById(R.id.mainRoot);

        rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE
                    || event.getAction() == MotionEvent.ACTION_DOWN) {

                float touchX = event.getX();
                float touchY = event.getY();

                // 눈 컨테이너 중심 기준으로 눈동자 이동 (최대 20dp 범위)
                int[] containerLoc = new int[2];
                eyeContainer.getLocationOnScreen(containerLoc);

                float centerX = containerLoc[0] + eyeContainer.getWidth() / 2f;
                float centerY = containerLoc[1] + eyeContainer.getHeight() / 2f;

                float dx = (touchX - centerX) * 0.15f;
                float dy = (touchY - centerY) * 0.15f;

                // 최대 이동 범위 제한 (±20dp)
                float maxOffset = 20f;
                dx = Math.max(-maxOffset, Math.min(maxOffset, dx));
                dy = Math.max(-maxOffset, Math.min(maxOffset, dy));

                // 왼쪽 눈동자 이동
                ivLeftPupil.setTranslationX(dx);
                ivLeftPupil.setTranslationY(dy);

                // 오른쪽 눈동자 이동
                ivRightPupil.setTranslationX(dx);
                ivRightPupil.setTranslationY(dy);
            }
            return true;
        });
    }

    /**
     * 클릭 리스너 설정
     * TextView OnClickListener, ImageButton 등
     */
    private void setupClickListeners() {

        // 측정 시작 버튼 → 페이스 스캔으로 이동
        ImageButton btnStart = findViewById(R.id.btnStartMeasure);
        btnStart.setOnClickListener(v -> {            String type = getSharedPreferences("HealthMonitorPrefs", MODE_PRIVATE)
                    .getString("selected_measure_type", "blood_flow");
            Intent intent;
            switch (type) {
                case "heart_rate":
                    intent = new Intent(MainActivity.this, HeartRateActivity.class); break;
                case "spo2":
                    intent = new Intent(MainActivity.this, SpO2Activity.class); break;
                case "temperature":
                    intent = new Intent(MainActivity.this, TemperatureActivity.class); break;
                default:
                    intent = new Intent(MainActivity.this, FaceScanActivity.class);
                    float avgVal = countVal > 0 ? sumVal / countVal : 0;
                    intent.putExtra("AVG_VAL", avgVal);
                    intent.putExtra("MIN_VAL", minVal);
                    intent.putExtra("MAX_VAL", maxVal);
                    break;
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 항목 변경 버튼
        TextView btnChangeItem = findViewById(R.id.btnChangeItem);
        btnChangeItem.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MeasurementSelectActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 자동 상태 전환이 적용되므로 클릭을 통한 수동 배경 토글은 제거합니다.

        // 햄버거 메뉴 버튼 → SlidingDrawer 열기
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> {
            if (slidingDrawer.isOpened()) {
                slidingDrawer.animateClose();
            } else {
                slidingDrawer.animateOpen();
            }
        });
    }

    /**
     * SlidingDrawer 메뉴 설정
     * 각 메뉴 항목 클릭 시 해당 액티비티로 이동
     */
    private void setupSlidingDrawer() {

        // 메인 화면 메뉴
        TextView menuMain = findViewById(R.id.menuMain);
        menuMain.setOnClickListener(v -> slidingDrawer.animateClose());

        // 페이스 스캔 메뉴
        TextView menuFaceScan = findViewById(R.id.menuFaceScan);
        menuFaceScan.setOnClickListener(v -> {
            slidingDrawer.animateClose();
            startActivity(new Intent(this, FaceScanActivity.class));
        });

        // 센서 확인 메뉴
        TextView menuSensorCheck = findViewById(R.id.menuSensorCheck);
        menuSensorCheck.setOnClickListener(v -> {
            slidingDrawer.animateClose();
            startActivity(new Intent(this, SensorCheckActivity.class));
        });

        // 혈류 측정 메뉴
        TextView menuBloodFlow = findViewById(R.id.menuBloodFlow);
        menuBloodFlow.setOnClickListener(v -> {
            slidingDrawer.animateClose();
            startActivity(new Intent(this, BloodFlowActivity.class));
        });

        // 데이터 추이 메뉴
        TextView menuHistory = findViewById(R.id.menuHistory);
        menuHistory.setOnClickListener(v -> {
            slidingDrawer.animateClose();
            startActivity(new Intent(this, HistoryActivity.class));
        });

        TextView menuUserInfo = findViewById(R.id.menuUserInfo);
        menuUserInfo.setOnClickListener(v -> {
            slidingDrawer.animateClose();
            startActivity(new Intent(this, UserInfoActivity.class));
        });

        TextView menuEmergency = findViewById(R.id.menuEmergency);
        menuEmergency.setOnClickListener(v -> {
            slidingDrawer.animateClose();
            startActivity(new Intent(this, EmergencyContactActivity.class));
        });

        TextView menuMeasureSelect = findViewById(R.id.menuMeasureSelect);
        menuMeasureSelect.setOnClickListener(v -> {
            slidingDrawer.animateClose();
            startActivity(new Intent(this, MeasurementSelectActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            stateTimer++;
            if (stateTimer >= 15) {
                stateTimer = 0;
                isNormal = !isNormal;
            }

            // 선택된 측정 항목 읽기
            String type = getSharedPreferences("HealthMonitorPrefs", MODE_PRIVATE)
                    .getString("selected_measure_type", "blood_flow");

            float currentVal;
            String unit;
            float normalMin, normalMax;

            switch (type) {
                case "heart_rate":
                    normalMin = 60f; normalMax = 100f;
                    currentVal = isNormal
                            ? 60f + random.nextFloat() * 41f   // 60~100 BPM
                            : random.nextBoolean()
                                ? 40f + random.nextFloat() * 20f  // 40~59 (서맥)
                                : 101f + random.nextFloat() * 20f; // 101~120 (빈맥)
                    unit = " BPM";
                    break;
                case "spo2":
                    normalMin = 95f; normalMax = 100f;
                    currentVal = isNormal
                            ? 95f + random.nextFloat() * 5f    // 95~100%
                            : 88f + random.nextFloat() * 7f;   // 88~94%
                    unit = "%";
                    break;
                case "temperature":
                    normalMin = 36f; normalMax = 37.5f;
                    currentVal = isNormal
                            ? 36f + random.nextFloat() * 1.5f  // 36~37.5°C
                            : 37.5f + random.nextFloat() * 2f; // 37.5~39.5°C
                    unit = "°C";
                    break;
                default: // blood_flow
                    normalMin = 100f; normalMax = 120f;
                    currentVal = isNormal
                            ? 100f + random.nextFloat() * 21f  // 100~120
                            : 80f + random.nextFloat() * 20f;  // 80~99
                    unit = " cm/s";
                    break;
            }

            boolean danger = currentVal < normalMin || currentVal > normalMax;

            if (!danger) {
                vfBackground.setDisplayedChild(0);
                eyeContainer.setBackgroundColor(0x00000000);
                tvStatusLabel.setText("● 정상");
                tvStatusLabel.setTextColor(getColor(android.R.color.holo_blue_light));
                getWindow().setStatusBarColor(0xFF0D1B3E);
                getWindow().setNavigationBarColor(0xFF0D1B3E);
            } else {
                vfBackground.setDisplayedChild(1);
                eyeContainer.setBackgroundColor(0x00000000);
                tvStatusLabel.setText("⚠ 위험");
                tvStatusLabel.setTextColor(getColor(android.R.color.holo_red_light));
                getWindow().setStatusBarColor(0xFF31040E);
                getWindow().setNavigationBarColor(0xFF31040E);
            }

            if (currentVal < minVal) minVal = currentVal;
            if (currentVal > maxVal) maxVal = currentVal;
            sumVal += currentVal;
            countVal++;
            float avgVal = sumVal / countVal;

            tvLastMeasureValue.setText(String.format(Locale.KOREA,
                    "%.1f%s — %s", currentVal, unit, danger ? "위험" : "정상"));
            tvStatsLabel.setText(String.format(Locale.KOREA,
                    "최고: %.1f | 최저: %.1f | 평균: %.1f", maxVal, minVal, avgVal));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            tvLastMeasureLabel.setText("실시간 측정값 (" + sdf.format(new Date()) + ")");

            handler.postDelayed(this, 1000);
        }
    };

    private Runnable blinkRunnable = new Runnable() {
        @Override
        public void run() {
            long duration = 150;
            ivLeftEyeWhite.animate().scaleY(0.1f).setDuration(duration).withEndAction(() -> ivLeftEyeWhite.animate().scaleY(1.0f).setDuration(duration).start()).start();
            ivRightEyeWhite.animate().scaleY(0.1f).setDuration(duration).withEndAction(() -> ivRightEyeWhite.animate().scaleY(1.0f).setDuration(duration).start()).start();
            ivLeftPupil.animate().scaleY(0.1f).setDuration(duration).withEndAction(() -> ivLeftPupil.animate().scaleY(1.0f).setDuration(duration).start()).start();
            ivRightPupil.animate().scaleY(0.1f).setDuration(duration).withEndAction(() -> ivRightPupil.animate().scaleY(1.0f).setDuration(duration).start()).start();

            handler.postDelayed(this, 3000 + random.nextInt(2000));
        }
    };
}
