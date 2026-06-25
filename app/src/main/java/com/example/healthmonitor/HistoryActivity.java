package com.example.healthmonitor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * 건강 데이터 히스토리 액티비티 (수정본)
 */
public class HistoryActivity extends AppCompatActivity {

    private AutoCompleteTextView actSearchDate;
    private CheckBox cbDangerFilter;
    private TextView tvTotalCount;
    private CalendarView calendarView;
    private Button btnHistoryToMain;

    // 테이블 날짜 및 수치/상태 업데이트를 위한 뷰 주소 바인딩
    private TextView tvDate1, tvDate2, tvDate3, tvDate4;
    private TextView tvValue1, tvValue2, tvValue3, tvValue4;
    private TextView tvStatus1, tvStatus2, tvStatus3, tvStatus4;
    private android.widget.TableRow row1, row2, row3, row4;

    private android.widget.TableLayout tableHistory;
    private Date currentBaseDate;

    // 전체측정 리포트 관련 뷰
    private android.widget.ScrollView scrollReport;
    private android.widget.ScrollView scrollTableHistory;
    private android.widget.LinearLayout layoutFilter;
    private TextView tvReportBloodFlow, tvReportHeartRate, tvReportTemp, tvReportSpO2;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
    private SimpleDateFormat monthFmt = new SimpleDateFormat("yyyy-MM", Locale.KOREA);
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initViews();
        setupAutoComplete();
        setupListeners();
    }

    private void initViews() {
        actSearchDate    = findViewById(R.id.actSearchDate);
        cbDangerFilter   = findViewById(R.id.cbDangerFilter);
        tvTotalCount     = findViewById(R.id.tvTotalCount);
        calendarView     = findViewById(R.id.calendarView);
        btnHistoryToMain = findViewById(R.id.btnHistoryToMain);

        // 날짜 및 수치 변경을 위해 추가 매칭
        tvDate1 = findViewById(R.id.tvDate1);
        tvDate2 = findViewById(R.id.tvDate2);
        tvDate3 = findViewById(R.id.tvDate3);
        tvDate4 = findViewById(R.id.tvDate4);

        tvValue1 = findViewById(R.id.tvValue1);
        tvValue2 = findViewById(R.id.tvValue2);
        tvValue3 = findViewById(R.id.tvValue3);
        tvValue4 = findViewById(R.id.tvValue4);

        row1 = findViewById(R.id.row1);
        row2 = findViewById(R.id.row2);
        row3 = findViewById(R.id.row3);
        row4 = findViewById(R.id.row4);

        tvStatus1 = findViewById(R.id.tvStatus1);
        tvStatus2 = findViewById(R.id.tvStatus2);
        tvStatus3 = findViewById(R.id.tvStatus3);
        tvStatus4 = findViewById(R.id.tvStatus4);

        tableHistory = findViewById(R.id.tableHistory);

        // 전체측정 리포트 관련 뷰 초기화
        scrollReport       = findViewById(R.id.scrollReport);
        scrollTableHistory = findViewById(R.id.scrollTableHistory);
        layoutFilter       = findViewById(R.id.layoutFilter);
        tvReportBloodFlow  = findViewById(R.id.tvReportBloodFlow);
        tvReportHeartRate  = findViewById(R.id.tvReportHeartRate);
        tvReportTemp       = findViewById(R.id.tvReportTemp);
        tvReportSpO2       = findViewById(R.id.tvReportSpO2);

        // 미래 날짜 선택 차단
        calendarView.setMaxDate(System.currentTimeMillis());

        // 최초 실행 시에는 오늘 날짜 기준으로 하단 테이블 리스트 채우기
        Calendar now = Calendar.getInstance();
        currentBaseDate = now.getTime();
        refreshTableData();

        actSearchDate.setHint("날짜 검색 (예: " + monthFmt.format(now.getTime()) + ")");
    }

    /**
     * [핵심 수정] 기준 날짜를 던져주면 그 날짜 및 과거 1일 전, 2일 전, 3일 전 데이터로
     * 하단 TableLayout 리스트의 날짜와 더미 수치를 동적으로 변경해 주는 함수 (호환성 유지용)
     */
    private void updateTableDataByBaseDate(Date baseDate) {
        // 기존 코드와의 호환성을 위해 남겨두되, 신규 로직인 refreshTableData()를 사용하도록 위임합니다.
        currentBaseDate = baseDate;
        refreshTableData();
    }

    // 연도+월 기반으로 그 달의 위험일 5개를 고정 생성
    private java.util.Set<Integer> getDangerDaysForMonth(int year, int month) {
        java.util.Set<Integer> days = new java.util.HashSet<>();
        // year+month를 seed로 사용 → 같은 달은 항상 같은 5일
        java.util.Random r = new java.util.Random((long) year * 100 + month);
        while (days.size() < 5) {
            days.add(1 + r.nextInt(28)); // 1~28 (모든 달에 존재)
        }
        return days;
    }

    private boolean isDangerDate(String dateStr) {
        try {
            int year  = Integer.parseInt(dateStr.substring(0, 4));
            int month = Integer.parseInt(dateStr.substring(5, 7));
            int day   = Integer.parseInt(dateStr.substring(8));
            return getDangerDaysForMonth(year, month).contains(day);
        } catch (Exception e) {
            return false;
        }
    }

    private android.widget.TableRow createTableRow(String dateStr, double value, boolean isDanger, String type) {
        android.widget.TableRow row = new android.widget.TableRow(this);
        android.widget.TableRow.LayoutParams rowParams = new android.widget.TableRow.LayoutParams(
                android.widget.TableRow.LayoutParams.MATCH_PARENT,
                android.widget.TableRow.LayoutParams.WRAP_CONTENT
        );
        rowParams.topMargin = (int) (2 * getResources().getDisplayMetrics().density);
        row.setLayoutParams(rowParams);

        // CheckBox
        CheckBox cb = new CheckBox(this);
        cb.setLayoutParams(new android.widget.TableRow.LayoutParams(
                android.widget.TableRow.LayoutParams.WRAP_CONTENT,
                android.widget.TableRow.LayoutParams.WRAP_CONTENT
        ));
        cb.setButtonTintList(android.content.res.ColorStateList.valueOf(isDanger ? 0xFFEF5350 : 0xFF4FC3F7));
        cb.setChecked(isDanger);
        cb.setPadding((int) (8 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density));

        // Date TextView
        TextView tvDate = new TextView(this);
        android.widget.TableRow.LayoutParams dateParams = new android.widget.TableRow.LayoutParams(
                0,
                android.widget.TableRow.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        tvDate.setLayoutParams(dateParams);
        tvDate.setText(dateStr);
        tvDate.setTextColor(isDanger ? 0xFFEF5350 : 0xFFFFFFFF);
        tvDate.setTextSize(13);
        tvDate.setPadding((int) (8 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density));

        // Value TextView
        TextView tvValue = new TextView(this);
        tvValue.setLayoutParams(new android.widget.TableRow.LayoutParams(
                android.widget.TableRow.LayoutParams.WRAP_CONTENT,
                android.widget.TableRow.LayoutParams.WRAP_CONTENT
        ));
        
        String formattedVal;
        switch (type) {
            case "heart_rate":
                formattedVal = String.format(Locale.KOREA, "%.0f BPM", value);
                break;
            case "spo2":
                formattedVal = String.format(Locale.KOREA, "%.0f%%", value);
                break;
            case "temperature":
                formattedVal = String.format(Locale.KOREA, "%.1f°C", value);
                break;
            default: // blood_flow
                formattedVal = String.format(Locale.KOREA, "%.0f cm/s", value);
                break;
        }
        tvValue.setText(formattedVal);
        tvValue.setTextColor(isDanger ? 0xFFEF5350 : 0xFFFFFFFF);
        tvValue.setTextSize(13);
        if (isDanger) {
            tvValue.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        tvValue.setPadding((int) (8 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density));

        // Status TextView
        TextView tvStatus = new TextView(this);
        tvStatus.setLayoutParams(new android.widget.TableRow.LayoutParams(
                android.widget.TableRow.LayoutParams.WRAP_CONTENT,
                android.widget.TableRow.LayoutParams.WRAP_CONTENT
        ));
        tvStatus.setText(isDanger ? "⚠ 위험" : "정상");
        tvStatus.setTextColor(isDanger ? 0xFFEF5350 : 0xFF69F0AE);
        tvStatus.setTextSize(13);
        if (isDanger) {
            tvStatus.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        tvStatus.setPadding((int) (8 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density));

        row.addView(cb);
        row.addView(tvDate);
        row.addView(tvValue);
        row.addView(tvStatus);

        return row;
    }

    private void refreshTableData() {
        if (tableHistory == null) return;

        // 헤더 제외하고 기존 행 지우기
        int childCount = tableHistory.getChildCount();
        if (childCount > 1) {
            tableHistory.removeViews(1, childCount - 1);
        }

        // 선택된 측정 항목 읽기
        String type = getSharedPreferences("HealthMonitorPrefs", MODE_PRIVATE)
                .getString("selected_measure_type", "blood_flow");

        if ("all".equals(type)) {
            actSearchDate.setVisibility(View.GONE);
            layoutFilter.setVisibility(View.GONE);
            calendarView.setVisibility(View.GONE);
            scrollTableHistory.setVisibility(View.GONE);
            scrollReport.setVisibility(View.VISIBLE);
            if (btnHistoryToMain != null) {
                btnHistoryToMain.setText("측정 항목 선택으로 돌아가기");
            }

            displayHealthReport();
            return;
        } else {
            actSearchDate.setVisibility(View.VISIBLE);
            layoutFilter.setVisibility(View.VISIBLE);
            calendarView.setVisibility(View.VISIBLE);
            scrollTableHistory.setVisibility(View.VISIBLE);
            scrollReport.setVisibility(View.GONE);
            if (btnHistoryToMain != null) {
                btnHistoryToMain.setText("상세 건강 리포트 확인");
            }
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(currentBaseDate);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        List<Date> datesToShow = new ArrayList<>();
        List<Boolean> isDangerList = new ArrayList<>();
        List<Double> valuesToShow = new ArrayList<>();

        if (cbDangerFilter.isChecked()) {
            // 해당 월의 위험일 5개 가져오기
            java.util.Set<Integer> dangerDays = getDangerDaysForMonth(year, month);
            // 역순(최신순) 정렬
            List<Integer> sortedDays = new ArrayList<>(dangerDays);
            java.util.Collections.sort(sortedDays, java.util.Collections.reverseOrder());

            for (int day : sortedDays) {
                Calendar checkCal = Calendar.getInstance();
                checkCal.set(year, month - 1, day, 0, 0, 0);
                checkCal.set(Calendar.MILLISECOND, 0);

                if (!checkCal.after(today)) {
                    datesToShow.add(checkCal.getTime());
                    isDangerList.add(true);

                    String dateStr = sdf.format(checkCal.getTime());
                    long seed = dateStr.hashCode();
                    Random r = new Random(seed);
                    
                    double val;
                    switch (type) {
                        case "heart_rate":
                            // 위험 심박수 (서맥 40~59 또는 빈맥 101~120)
                            val = r.nextBoolean() ? 40 + r.nextInt(20) : 101 + r.nextInt(20);
                            break;
                        case "spo2":
                            // 위험 산소포화도 (88~94%)
                            val = 88 + r.nextInt(7);
                            break;
                        case "temperature":
                            // 위험 체온 (37.5~39.5°C)
                            val = 37.5 + r.nextDouble() * 2.0;
                            break;
                        default: // blood_flow
                            // 위험 혈류속도 (80~99 cm/s)
                            val = 80 + r.nextInt(20);
                            break;
                    }
                    valuesToShow.add(val);
                }
            }
        } else {
            // 현재 기준일로부터 과거로 4일 치 데이터 생성
            Calendar tempCal = (Calendar) cal.clone();
            int addedCount = 0;
            for (int i = 0; i < 30 && addedCount < 4; i++) {
                if (!tempCal.after(today)) {
                    datesToShow.add(tempCal.getTime());
                    String dateStr = sdf.format(tempCal.getTime());
                    boolean isDanger = isDangerDate(dateStr);
                    isDangerList.add(isDanger);

                    long seed = dateStr.hashCode();
                    Random r = new Random(seed);
                    
                    double val;
                    if (isDanger) {
                        switch (type) {
                            case "heart_rate":
                                val = r.nextBoolean() ? 40 + r.nextInt(20) : 101 + r.nextInt(20);
                                break;
                            case "spo2":
                                val = 88 + r.nextInt(7);
                                break;
                            case "temperature":
                                val = 37.5 + r.nextDouble() * 2.0;
                                break;
                            default: // blood_flow
                                val = 80 + r.nextInt(20);
                                break;
                        }
                    } else {
                        switch (type) {
                            case "heart_rate":
                                // 정상 심박수 (60~100 BPM)
                                val = 60 + r.nextInt(41);
                                break;
                            case "spo2":
                                // 정상 산소포화도 (95~100%)
                                val = 95 + r.nextInt(6);
                                break;
                            case "temperature":
                                // 정상 체온 (36.0~37.4°C)
                                val = 36.0 + r.nextDouble() * 1.4;
                                break;
                            default: // blood_flow
                                // 정상 혈류속도 (100~120 cm/s)
                                val = 100 + r.nextInt(21);
                                break;
                        }
                    }
                    valuesToShow.add(val);

                    addedCount++;
                }
                tempCal.add(Calendar.DAY_OF_YEAR, -1);
            }
        }

        // 테이블에 행 추가
        for (int i = 0; i < datesToShow.size(); i++) {
            String dateStr = sdf.format(datesToShow.get(i));
            boolean isDanger = isDangerList.get(i);
            double value = valuesToShow.get(i);
            android.widget.TableRow row = createTableRow(dateStr, value, isDanger, type);
            tableHistory.addView(row);
        }

        // 토탈 건수 표시
        tvTotalCount.setText(datesToShow.size() + "건");
        if (cbDangerFilter.isChecked()) {
            tvTotalCount.setTextColor(0xFFEF5350); // 빨간색 강조
        } else {
            tvTotalCount.setTextColor(0xFFFFFFFF); // 흰색
        }
    }

    private void setupAutoComplete() {
        Calendar cal = Calendar.getInstance();
        List<String> hintList = new ArrayList<>();
        SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        SimpleDateFormat mFmt   = new SimpleDateFormat("yyyy-MM", Locale.KOREA);

        for (int i = 0; i < 8; i++) {
            hintList.add(dayFmt.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        cal = Calendar.getInstance();
        for (int i = 0; i < 3; i++) {
            hintList.add(mFmt.format(cal.getTime()));
            cal.add(Calendar.MONTH, -1);
        }

        String[] dateHints = hintList.toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                dateHints
        );
        actSearchDate.setAdapter(adapter);

        // 1. 자동완성 드롭다운 리스트 항목 선택 시 검색 실행
        actSearchDate.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDate = (String) parent.getItemAtPosition(position);
            filterByDate(selectedDate);
        });

        // 2. 검색창 직접 타이핑 후 키보드에서 '돋보기(검색)' 또는 '엔터' 클릭 시 검색 실행
        actSearchDate.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {

                String inputText = actSearchDate.getText().toString().trim();
                if (!inputText.isEmpty()) {
                    filterByDate(inputText);
                }
                return true; // 이벤트 소비 처리
            }
            return false;
        });
    }

    private void setupListeners() {
        cbDangerFilter.setOnCheckedChangeListener((buttonView, isChecked) -> {
            refreshTableData();
        });

        // 달력에서 날짜(4월 등) 클릭했을 때의 리스너
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = year + "-" + String.format("%02d", month + 1)
                    + "-" + String.format("%02d", dayOfMonth);
            actSearchDate.setText(selectedDate);
            filterByDate(selectedDate); // ➔ 선택한 날짜 필터링 메커니즘 가동
        });

        btnHistoryToMain.setOnClickListener(v -> {
            String type = getSharedPreferences("HealthMonitorPrefs", MODE_PRIVATE)
                    .getString("selected_measure_type", "blood_flow");
            android.content.Intent intent;
            if ("all".equals(type)) {
                intent = new android.content.Intent(HistoryActivity.this, MeasurementSelectActivity.class);
            } else {
                intent = new android.content.Intent(HistoryActivity.this, IndividualReportActivity.class);
            }
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    /**
     * [버그 수정 완료] 날짜 쿼리를 전달받아 데이터 리스트를 동적으로 바꿉니다.
     */
    private void filterByDate(String dateStr) {
        try {
            Date parsedDate;
            Calendar targetCal = Calendar.getInstance();

            // 월 단위 검색(yyyy-MM, 예: 2026-03) 처리 보정
            if (dateStr.length() == 7) {
                parsedDate = monthFmt.parse(dateStr);
                targetCal.setTime(parsedDate);
                // 🚨 핵심 디버깅: 1일로 세팅되어 전 달로 튕기는 것을 막기 위해 해당 월의 '마지막 날'로 지정
                int maxDay = targetCal.getActualMaximum(Calendar.DAY_OF_MONTH);
                targetCal.set(Calendar.DAY_OF_MONTH, maxDay);
            } else {
                // 일 단위 검색(yyyy-MM-dd) 처리
                parsedDate = sdf.parse(dateStr);
                targetCal.setTime(parsedDate);
            }

            // 오늘 이후의 날짜인 경우 오늘로 제한 (Capping)
            Calendar today = Calendar.getInstance();
            if (targetCal.after(today)) {
                targetCal.setTime(today.getTime());
                // 입력 필드의 텍스트도 오늘 날짜로 업데이트
                if (dateStr.length() == 7) {
                    actSearchDate.setText(monthFmt.format(today.getTime()));
                } else {
                    actSearchDate.setText(sdf.format(today.getTime()));
                }
            }

            currentBaseDate = targetCal.getTime();
            refreshTableData();

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void displayHealthReport() {
        SharedPreferences p = getSharedPreferences("HealthMonitorPrefs", MODE_PRIVATE);
        float bloodFlow = p.getFloat("all_blood_flow", 110.0f);
        float heartRate = p.getFloat("all_heart_rate", 75.0f);
        float temp = p.getFloat("all_temp", 36.6f);
        float spo2 = p.getFloat("all_spo2", 98.0f);

        // 1. Blood Flow opinion
        String bloodFlowOpinion;
        if (bloodFlow >= 100f && bloodFlow <= 120f) {
            bloodFlowOpinion = "정상 범위입니다. 건강한 혈액 순환 상태를 유지하기 위해 규칙적인 유산소 운동과 충분한 수분 섭취";
        } else {
            bloodFlowOpinion = "정상 범위를 벗어났습니다. 식습관 개선(저염식)과 무리한 운동 자제가 필요하며 전문 의료진과의 상담";
        }
        tvReportBloodFlow.setText(String.format(Locale.KOREA, 
                "현재 혈류속도는 %.0f cm/s 이며, %s이 권장됩니다.", bloodFlow, bloodFlowOpinion));

        // 2. Heart Rate opinion
        String heartRateOpinion;
        if (heartRate >= 60f && heartRate <= 100f) {
            heartRateOpinion = "안정적입니다. 유산소 운동을 통해 적절한 심폐 지구력을 꾸준히 유지하는 것";
        } else {
            heartRateOpinion = "비정상(서맥 혹은 빈맥) 상태입니다. 가슴 두근거림이나 어지러움 증상이 있을 수 있으므로 충분한 안정을 취하고 정밀 심전도 검사";
        }
        tvReportHeartRate.setText(String.format(Locale.KOREA, 
                "현재 심박수는 %.0f BPM 이며, %s이 권장됩니다.", heartRate, heartRateOpinion));

        // 3. Temp opinion
        String tempOpinion;
        if (temp >= 36.0f && temp <= 37.4f) {
            tempOpinion = "정상 체온입니다. 급격한 외부 온도 변화에 유의하며 면역력을 보존하는 것";
        } else {
            tempOpinion = "고열 상태가 의심됩니다. 미온수로 몸을 닦아주거나 해열제를 복용하고 충분한 휴식을 취하는 것이 좋으며, 증상이 지속될 경우 신속한 내원";
        }
        tvReportTemp.setText(String.format(Locale.KOREA, 
                "현재 체온은 %.1f °C 이며, %s이 권장됩니다.", temp, tempOpinion));

        // 4. SpO2 opinion
        String spo2Opinion;
        if (spo2 >= 95f && spo2 <= 100f) {
            spo2Opinion = "매우 안정적입니다. 신체 산소 공급이 원활하므로 현재 상태를 유지하기 위해 맑은 공기를 자주 환기하는 것";
        } else {
            spo2Opinion = "정상치 미만(저산소 구간)입니다. 두통이나 호흡 곤란이 발생할 수 있으므로 깊게 숨을 쉬고 즉시 환기가 잘 되는 곳으로 이동하는 것";
        }
        tvReportSpO2.setText(String.format(Locale.KOREA, 
                "현재 산소포화도는 %.0f %% 이며, %s이 권장됩니다.", spo2, spo2Opinion));
    }
}