package com.example.healthmonitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UserInfoActivity extends AppCompatActivity {

    private EditText etName, etAge, etHeight, etWeight;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private TextView tvNormalRange;
    private Button btnSave, btnNext;

    private static final String PREF = "HealthMonitorPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        initViews();
        loadSaved();
        setupListeners();
    }

    private void initViews() {
        etName       = findViewById(R.id.etName);
        etAge        = findViewById(R.id.etAge);
        etHeight     = findViewById(R.id.etHeight);
        etWeight     = findViewById(R.id.etWeight);
        rgGender     = findViewById(R.id.rgGender);
        rbMale       = findViewById(R.id.rbMale);
        rbFemale     = findViewById(R.id.rbFemale);
        tvNormalRange = findViewById(R.id.tvNormalRange);
        btnSave      = findViewById(R.id.btnSaveUserInfo);
        btnNext      = findViewById(R.id.btnUserInfoNext);
    }

    private void loadSaved() {
        SharedPreferences p = getSharedPreferences(PREF, MODE_PRIVATE);
        etName.setText(p.getString("user_name", ""));
        etAge.setText(p.getString("user_age", ""));
        etHeight.setText(p.getString("user_height", ""));
        etWeight.setText(p.getString("user_weight", ""));
        if (p.getString("user_gender", "male").equals("female")) rbFemale.setChecked(true);
        else rbMale.setChecked(true);
        updateNormalRange();
    }

    private void setupListeners() {
        rgGender.setOnCheckedChangeListener((g, id) -> updateNormalRange());
        etAge.setOnFocusChangeListener((v, f) -> { if (!f) updateNormalRange(); });

        btnSave.setOnClickListener(v -> {
            if (!validate()) return;
            save();
            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
        });

        btnNext.setOnClickListener(v -> {
            if (!validate()) return;
            save();
            Intent intent = new Intent(this, FaceScanActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private boolean validate() {
        if (etName.getText().toString().trim().isEmpty()) { etName.setError("이름 입력"); return false; }
        if (etAge.getText().toString().trim().isEmpty())  { etAge.setError("나이 입력");  return false; }
        if (etHeight.getText().toString().trim().isEmpty()){ etHeight.setError("키 입력"); return false; }
        if (etWeight.getText().toString().trim().isEmpty()){ etWeight.setError("몸무게 입력"); return false; }
        return true;
    }

    private void save() {
        String gender = rbFemale.isChecked() ? "female" : "male";
        getSharedPreferences(PREF, MODE_PRIVATE).edit()
                .putString("user_name",   etName.getText().toString().trim())
                .putString("user_age",    etAge.getText().toString().trim())
                .putString("user_height", etHeight.getText().toString().trim())
                .putString("user_weight", etWeight.getText().toString().trim())
                .putString("user_gender", gender)
                .apply();
    }

    private void updateNormalRange() {
        String ageStr = etAge.getText().toString().trim();
        int age = ageStr.isEmpty() ? 25 : Integer.parseInt(ageStr);
        boolean female = rbFemale.isChecked();
        int min, max;
        if (age < 20)      { min = 55; max = 110; }
        else if (age < 40) { min = 50; max = 100; }
        else if (age < 60) { min = 45; max = 95;  }
        else               { min = 40; max = 90;  }
        if (female) { min -= 5; max -= 5; }
        tvNormalRange.setText(String.format("개인화 정상 범위: %d ~ %d cm/s", min, max));
        tvNormalRange.setTextColor(0xFF69F0AE);
        getSharedPreferences(PREF, MODE_PRIVATE).edit()
                .putInt("normal_min", min).putInt("normal_max", max).apply();
    }
}
