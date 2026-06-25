# HealthMonitor v2.0 — 통합 헬스케어 모니터

> 2학년 1학기 Android 프로그래밍 과제 (v1.0 확장판)

## 📱 앱 소개

혈류 속도 측정 앱(v1.0)을 기반으로 **심박수 · 산소포화도 · 체온** 측정 기능을 추가한 **통합 헬스케어 Android 앱**입니다.  
스플래시 → 사용자 정보 입력 → 측정 항목 선택 → 각 항목별 측정 → 결과 분석 → 데이터 추이의 완결된 흐름을 제공합니다.

---

## 🆕 v1.0 대비 추가된 기능

| 기능 | 설명 |
|------|------|
| 스플래시 화면 | 앱 실행 시 로고 표시 |
| 사용자 정보 입력 | 이름/나이/성별/키/몸무게/보호자 연락처 (3단계) |
| 측정 항목 선택 | 혈류속도·심박수·산소포화도·체온·전체측정 선택 |
| 심박수 측정 | 심장 박동 애니메이션 + BPM 실시간 측정 |
| 산소포화도 측정 | ProgressBar 게이지 + SpO₂ 실시간 측정 |
| 체온 측정 | 온도계 바 시각화 + 체온 실시간 측정 |
| 각 항목별 결과 | Peak/Mean/그래프/판정 |
| 상세 리포트 | 정밀 상태 분석 + 추천 건강 식단 + 맞춤 영양제 |
| 긴급 연락처 | 위험 수치 감지 시 보호자 알림 |
| 항목 변경 버튼 | 메인에서 측정 항목 즉시 전환 |

---

## 🖥️ 주요 화면 흐름

```
SplashActivity
    → UserInfoActivity (1/3 이름·나이·성별)
    → UserInfoActivity (2/3 키·몸무게)
    → UserInfoActivity (3/3 보호자 연락처)
    → MeasurementSelectActivity (항목 선택)
    → MainActivity (실시간 측정값 표시)
         ├── [혈류속도] FaceScanActivity → SensorCheckActivity
         │       → ScanningActivity → BloodFlowActivity
         │       → WaveformActivity → HistoryActivity
         ├── [심박수] HeartRateActivity → HeartRateResultActivity → HistoryActivity
         ├── [산소포화도] SpO2Activity → SpO2ResultActivity → HistoryActivity
         └── [체온] TemperatureActivity → TemperatureResultActivity → HistoryActivity
```

---

## ⚙️ 사용 기술

- **언어**: Java
- **UI**: ViewFlipper, SlidingDrawer, Chronometer, ToggleButton, RadioButton, CalendarView, TableLayout, RecyclerView, ProgressBar, GridLayout, ScrollView
- **커스텀 뷰**: `DataGraphView`, `WaveformView` (Canvas 직접 드로잉)
- **애니메이션**: ScaleAnimation (심박수), TranslateAnimation (스캔바), ValueAnimator
- **데이터**: SharedPreferences, Intent 양방향 전송, onActivityResult
- **알림**: NotificationChannel, AlertDialog
- **Activity**: 총 15개

---

## 📊 Activity 목록

| Activity | 역할 |
|----------|------|
| SplashActivity | 앱 시작 로고 |
| UserInfoActivity | 사용자 정보 3단계 입력 |
| MeasurementSelectActivity | 측정 항목 선택 |
| MainActivity | 메인 대시보드 + 실시간 측정값 |
| FaceScanActivity | 얼굴 인식 |
| SensorCheckActivity | 센서 착용 확인 |
| ScanningActivity | 혈류속도 실시간 스캔 |
| BloodFlowActivity | 혈류속도 결과 |
| WaveformActivity | 혈류 파형 분석 |
| HistoryActivity | 건강 데이터 추이 |
| HeartRateActivity | 심박수 측정 |
| HeartRateResultActivity | 심박수 결과 |
| SpO2Activity | 산소포화도 측정 |
| SpO2ResultActivity | 산소포화도 결과 |
| TemperatureActivity | 체온 측정 |
| TemperatureResultActivity | 체온 결과 |
| UserInfoActivity | 사용자 정보 관리 |
| EmergencyContactActivity | 긴급 연락처 |

---

## 🔗 링크

- **GitHub (v1.0 원본)**: https://github.com/tomines-jaeyoung/javaprojects2
- **피그마**: https://www.figma.com/design/7CS9LMoIeERXiyg69nuQql/JAVA-PROJECT

---

## 👤 개발자

- **학과**: 인공지능소프트웨어학과
- **학번**: 2501110200
- **이름**: 김재영
