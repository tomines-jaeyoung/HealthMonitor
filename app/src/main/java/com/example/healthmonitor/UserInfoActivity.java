package com.example.healthmonitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UserInfoActivity extends AppCompatActivity {

    // 페이지 1
    private EditText etName, etAge;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;

    // 페이지 2
    private EditText etHeight, etWeight;
    private TextView tvNormalRange;

    // 페이지 3
    private EditText etContactName, etContactPhone;

    // 레이아웃
    private LinearLayout page1, page2, page3;
    private Button btnNext1, btnNext2, btnFinish;
    private TextView tvStep;

    private int currentPage = 1;
    private static final String PREF = "HealthMonitorPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        initViews();
        loadSaved();
        showPage(1);
    }

    private void initViews() {
        page1 = findViewById(R.id.page1);
        page2 = findViewById(R.id.page2);
        page3 = findViewById(R.id.page3);

        etName     = findViewById(R.id.etName);
        etAge      = findViewById(R.id.etAge);
        rgGender   = findViewById(R.id.rgGender);
        rbMale     = findViewById(R.id.rbMale);
        rbFemale   = findViewById(R.id.rbFemale);

        etHeight   = findViewById(R.id.etHeight);
        etWeight   = findViewById(R.id.etWeight);
        tvNormalRange = findViewById(R.id.tvNormalRange);

        etContactName  = findViewById(R.id.etContactName);
        etContactPhone = findViewById(R.id.etContactPhone);

        btnNext1  = findViewById(R.id.btnNext1);
        btnNext2  = findViewById(R.id.btnNext2);
        btnFinish = findViewById(R.id.btnFinish);
        tvStep    = findViewById(R.id.tvStep);

        btnNext1.setOnClickListener(v -> {
            if (etName.getText().toString().trim().isEmpty()) {
                etName.setError("이름을 입력해주세요"); return;
            }
            if (etAge.getText().toString().trim().isEmpty()) {
                etAge.setError("나이를 입력해주세요"); return;
            }
            showPage(2);
        });

        btnNext2.setOnClickListener(v -> {
            if (etHeight.getText().toString().trim().isEmpty()) {
                etHeight.setError("키를 입력해주세요"); return;
            }
            if (etWeight.getText().toString().trim().isEmpty()) {
                etWeight.setError("몸무게를 입력해주세요"); return;
            }
            updateNormalRange();
            showPage(3);
        });

        btnFinish.setOnClickListener(v -> {
            save();
            Toast.makeText(this, "설정이 완료되었습니다!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MeasurementSelectActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        rgGender.setOnCheckedChangeListener((g, id) -> updateNormalRange());
        etAge.setOnFocusChangeListener((v, f) -> { if (!f) updateNormalRange(); });
    }

    private void showPage(int page) {
        currentPage = page;
        page1.setVisibility(page == 1 ? View.VISIBLE : View.GONE);
        page2.setVisibility(page == 2 ? View.VISIBLE : View.GONE);
        page3.setVisibility(page == 3 ? View.VISIBLE : View.GONE);
        tvStep.setText(page + " / 3");
    }

    private void loadSaved() {
        SharedPreferences p = getSharedPreferences(PREF, MODE_PRIVATE);
        etName.setText(p.getString("user_name", ""));
        etAge.setText(p.getString("user_age", ""));
        etHeight.setText(p.getString("user_height", ""));
        etWeight.setText(p.getString("user_weight", ""));
        etContactName.setText(p.getString("contact1_name", ""));
        etContactPhone.setText(p.getString("contact1_phone", ""));
        if (p.getString("user_gender", "male").equals("female")) rbFemale.setChecked(true);
        else rbMale.setChecked(true);
    }

    private void save() {
        String gender = rbFemale.isChecked() ? "female" : "male";
        getSharedPreferences(PREF, MODE_PRIVATE).edit()
                .putString("user_name",      etName.getText().toString().trim())
                .putString("user_age",       etAge.getText().toString().trim())
                .putString("user_height",    etHeight.getText().toString().trim())
                .putString("user_weight",    etWeight.getText().toString().trim())
                .putString("user_gender",    gender)
                .putString("contact1_name",  etContactName.getText().toString().trim())
                .putString("contact1_phone", etContactPhone.getText().toString().trim())
                .apply();
    }

    private void updateNormalRange() {
        if (tvNormalRange == null) return;
        String ageStr = etAge.getText().toString().trim();
        int age = ageStr.isEmpty() ? 25 : Integer.parseInt(ageStr);
        boolean female = rbFemale.isChecked();
        int min, max;
        if (age < 20)      { min = 55; max = 110; }
        else if (age < 40) { min = 50; max = 100; }
        else if (age < 60) { min = 45; max = 95;  }
        else               { min = 40; max = 90;  }
        if (female) { min -= 5; max -= 5; }
        tvNormalRange.setText(String.format("맞춤 정상 범위: %d ~ %d cm/s", min, max));
        getSharedPreferences(PREF, MODE_PRIVATE).edit()
                .putInt("normal_min", min).putInt("normal_max", max).apply();
    }

    @Override
    public void onBackPressed() {
        if (currentPage > 1) showPage(currentPage - 1);
        else super.onBackPressed();
    }
}
