package com.example.healthmonitor;

import android.os.Bundle;
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

    private android.widget.TableRow createTableRow(String dateStr, int value, boolean isDanger) {
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
        tvValue.setText(value + " cm/s");
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
        List<Integer> valuesToShow = new ArrayList<>();

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
                    valuesToShow.add(80 + r.nextInt(20));
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
                    valuesToShow.add(isDanger ? 80 + r.nextInt(20) : 100 + r.nextInt(21));

                    addedCount++;
                }
                tempCal.add(Calendar.DAY_OF_YEAR, -1);
            }
        }

        // 테이블에 행 추가
        for (int i = 0; i < datesToShow.size(); i++) {
            String dateStr = sdf.format(datesToShow.get(i));
            boolean isDanger = isDangerList.get(i);
            int value = valuesToShow.get(i);
            android.widget.TableRow row = createTableRow(dateStr, value, isDanger);
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
            android.content.Intent intent = new android.content.Intent(
                    HistoryActivity.this, MainActivity.class);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
}