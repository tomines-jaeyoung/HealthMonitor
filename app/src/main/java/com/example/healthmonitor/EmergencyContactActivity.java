package com.example.healthmonitor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EmergencyContactActivity extends AppCompatActivity {

    private EditText etContact1Name, etContact1Phone;
    private EditText etContact2Name, etContact2Phone;
    private Switch switchAutoAlert;
    private TextView tvAlertStatus, tvLastAlertTime;
    private Button btnSave, btnTest, btnBack;

    private static final String PREF = "HealthMonitorPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);
        initViews();
        loadSaved();
        setupListeners();
    }

    private void initViews() {
        etContact1Name  = findViewById(R.id.etContact1Name);
        etContact1Phone = findViewById(R.id.etContact1Phone);
        etContact2Name  = findViewById(R.id.etContact2Name);
        etContact2Phone = findViewById(R.id.etContact2Phone);
        switchAutoAlert = findViewById(R.id.switchAutoAlert);
        tvAlertStatus   = findViewById(R.id.tvAlertStatus);
        tvLastAlertTime = findViewById(R.id.tvLastAlertTime);
        btnSave         = findViewById(R.id.btnSaveContacts);
        btnTest         = findViewById(R.id.btnTestAlert);
        btnBack         = findViewById(R.id.btnEmergencyBack);
    }

    private void loadSaved() {
        SharedPreferences p = getSharedPreferences(PREF, MODE_PRIVATE);
        etContact1Name.setText(p.getString("contact1_name", ""));
        etContact1Phone.setText(p.getString("contact1_phone", ""));
        etContact2Name.setText(p.getString("contact2_name", ""));
        etContact2Phone.setText(p.getString("contact2_phone", ""));
        boolean auto = p.getBoolean("auto_alert", false);
        switchAutoAlert.setChecked(auto);
        updateStatus(auto);
        tvLastAlertTime.setText("마지막 알림: " + p.getString("last_alert_time", "없음"));
    }

    private void setupListeners() {
        switchAutoAlert.setOnCheckedChangeListener((btn, on) -> {
            updateStatus(on);
            getSharedPreferences(PREF, MODE_PRIVATE).edit().putBoolean("auto_alert", on).apply();
        });

        btnSave.setOnClickListener(v -> {
            if (!validate()) return;
            saveContacts();
            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
        });

        btnTest.setOnClickListener(v -> {
            String name  = etContact1Name.getText().toString().trim();
            String phone = etContact1Phone.getText().toString().trim();
            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "연락처 1을 먼저 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("⚠ 긴급 알림 테스트")
                    .setMessage(name + " (" + phone + ") 에게\n위험 수치 감지 알림을 전송합니다.\n\n[테스트] 혈류 속도 이상 감지 — 즉시 확인 바랍니다.")
                    .setPositiveButton("전송", (d, w) -> {
                        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(new Date());
                        getSharedPreferences(PREF, MODE_PRIVATE).edit().putString("last_alert_time", now).apply();
                        tvLastAlertTime.setText("마지막 알림: " + now);
                        Toast.makeText(this, "테스트 알림 전송 완료", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private boolean validate() {
        if (etContact1Name.getText().toString().trim().isEmpty())  { etContact1Name.setError("이름 입력"); return false; }
        if (etContact1Phone.getText().toString().trim().isEmpty()) { etContact1Phone.setError("전화번호 입력"); return false; }
        return true;
    }

    private void saveContacts() {
        getSharedPreferences(PREF, MODE_PRIVATE).edit()
                .putString("contact1_name",  etContact1Name.getText().toString().trim())
                .putString("contact1_phone", etContact1Phone.getText().toString().trim())
                .putString("contact2_name",  etContact2Name.getText().toString().trim())
                .putString("contact2_phone", etContact2Phone.getText().toString().trim())
                .apply();
    }

    private void updateStatus(boolean on) {
        if (on) { tvAlertStatus.setText("● 자동 알림 활성화"); tvAlertStatus.setTextColor(0xFF69F0AE); }
        else    { tvAlertStatus.setText("● 자동 알림 비활성화"); tvAlertStatus.setTextColor(0xFF546E7A); }
    }
}
