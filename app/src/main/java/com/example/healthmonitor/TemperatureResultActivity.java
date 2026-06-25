package com.example.healthmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class TemperatureResultActivity extends AppCompatActivity {

    private TextView tvTempPeak, tvTempMean, tvTempCurrent, tvTempStability, tvTempJudgement;
    private DataGraphView graphView;
    private Button btnNext, btnBack;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_result);

        float measuredValue = getIntent().getFloatExtra("MEASURED_VALUE", 36.5f);

        getSharedPreferences("HealthMonitorPrefs", MODE_PRIVATE).edit()
                .putFloat("last_temp", measuredValue)
                .apply();

        tvTempPeak      = findViewById(R.id.tvTempPeak);
        tvTempMean      = findViewById(R.id.tvTempMean);
        tvTempCurrent   = findViewById(R.id.tvTempCurrent);
        tvTempStability = findViewById(R.id.tvTempStability);
        tvTempJudgement = findViewById(R.id.tvTempJudgement);
        graphView       = findViewById(R.id.tempGraphView);
        btnNext         = findViewById(R.id.btnTempRNext);
        btnBack         = findViewById(R.id.btnTempRBack);

        loadData(measuredValue);

        if (measuredValue >= 38f) {
            new AlertDialog.Builder(this)
                    .setTitle("⚠ 발열 감지")
                    .setMessage(String.format("%.1f°C — 충분한 휴식을 취하고 의료진 상담을 권장합니다.", measuredValue))
                    .setPositiveButton("확인", null).show();
        }

        btnNext.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadData(float value) {
        tvTempPeak.setText(String.format("%.1f°C", Math.min(40f, value + 0.3f)));
        tvTempMean.setText(String.format("%.1f°C", value - 0.2f));
        tvTempCurrent.setText(String.format("%.1f°C", value));

        if (value >= 38f) {
            tvTempJudgement.setText("⚠ 발열");
            tvTempJudgement.setTextColor(0xFFEF5350);
            tvTempStability.setText("위험"); tvTempStability.setTextColor(0xFFEF5350);
        } else if (value >= 37.5f) {
            tvTempJudgement.setText("△ 미열");
            tvTempJudgement.setTextColor(0xFFFFA726);
            tvTempStability.setText("주의"); tvTempStability.setTextColor(0xFFFFA726);
        } else {
            tvTempJudgement.setText("● 정상");
            tvTempJudgement.setTextColor(0xFF69F0AE);
            tvTempStability.setText("우수"); tvTempStability.setTextColor(0xFF4FC3F7);
        }

        if (graphView != null) {
            float[] data = new float[12];
            float cur = value;
            for (int i = 0; i < 12; i++) {
                cur = Math.max(35f, Math.min(40f, cur + (random.nextFloat() * 0.6f - 0.3f)));
                data[i] = (i == 11) ? value : cur;
            }
            String[] labels = {"11h전","","","8h전","","","5h전","","","2h전","","현재"};
            graphView.setData(data, labels);
        }
    }
}
