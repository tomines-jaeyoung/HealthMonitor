package com.example.healthmonitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class IndividualReportActivity extends AppCompatActivity {

    private TextView tvReportTitle, tvReportSubTitle, tvReportValue, tvReportStatus;
    private TextView tvDetailedAnalysis, tvDietRecommendation, tvSupplementsRecommendation;
    private Button btnReportToSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_report);

        initViews();
        loadReportData();
    }

    private void initViews() {
        tvReportTitle               = findViewById(R.id.tvReportTitle);
        tvReportSubTitle            = findViewById(R.id.tvReportSubTitle);
        tvReportValue               = findViewById(R.id.tvReportValue);
        tvReportStatus              = findViewById(R.id.tvReportStatus);
        tvDetailedAnalysis          = findViewById(R.id.tvDetailedAnalysis);
        tvDietRecommendation        = findViewById(R.id.tvDietRecommendation);
        tvSupplementsRecommendation = findViewById(R.id.tvSupplementsRecommendation);
        btnReportToSelect           = findViewById(R.id.btnReportToSelect);

        btnReportToSelect.setOnClickListener(v -> {
            Intent intent = new Intent(IndividualReportActivity.this, MeasurementSelectActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void loadReportData() {
        SharedPreferences p = getSharedPreferences("HealthMonitorPrefs", MODE_PRIVATE);
        String type = p.getString("selected_measure_type", "blood_flow");

        switch (type) {
            case "heart_rate":
                float hr = p.getFloat("last_heart_rate", 72.0f);
                tvReportTitle.setText("심박수 상세 리포트");
                tvReportSubTitle.setText("최근 측정 심박수");
                tvReportValue.setText(String.format(Locale.KOREA, "%.0f BPM", hr));
                
                if (hr >= 60f && hr <= 100f) {
                    tvReportStatus.setText("● 정상");
                    tvReportStatus.setTextColor(0xFF69F0AE);
                    tvReportValue.setTextColor(0xFF4FC3F7);
                    
                    tvDetailedAnalysis.setText("현재 심박수는 매우 안정적인 상태를 보여주고 있습니다. 자율신경계가 균형 있게 반응하고 있으며, 스트레스 관리 상태가 우수합니다. 일상적인 활동 중 심장에 가해지는 부담이 적어 이상적인 컨디션입니다.");
                    tvDietRecommendation.setText("• 통곡물(현미, 오트밀 등) 및 시금치, 아몬드와 같은 마그네슘이 풍부한 식품을 섭취해 보십시오. 심장 근육의 이완을 돕고 안정적인 박동을 유지하게 해 줍니다.\n• 칼륨이 풍부한 토마토나 바나나는 나트륨 배출을 유도하여 혈압과 심장 부담을 낮추는 데 효과적입니다.\n• 카페인이 과도한 고함량 음료나 알코올의 섭취는 순간적인 맥박 변동을 초래하므로 자제해 주는 것이 좋습니다.");
                    tvSupplementsRecommendation.setText("• 마그네슘: 심장 근육의 과도한 수축을 예방하고 자율신경 안정을 지원합니다. 매일 저녁 식후 섭취를 추천합니다.\n• L-테아닌: 스트레스로 인한 심박수 일시 상승을 효과적으로 억제해 줍니다.\n• CoQ10: 심장 세포 에너지 생성을 원활하게 하여 기본적인 심폐력 관리에 필수적입니다.");
                } else {
                    tvReportStatus.setText("⚠ 위험 (서맥 혹은 빈맥)");
                    tvReportStatus.setTextColor(0xFFEF5350);
                    tvReportValue.setTextColor(0xFFEF5350);
                    
                    tvDetailedAnalysis.setText("현재 측정된 심박수가 정상 범위(60~100 BPM)를 벗어난 상태입니다. 맥박이 너무 느리면 어지러움이나 피로감이 발생할 수 있고, 너무 빠르면 심장에 과부하가 걸려 두근거림이나 호흡 불편이 생길 수 있으니 충분한 안정이 절대적으로 필요합니다.");
                    tvDietRecommendation.setText("• 소화 과정에서 심장에 부담이 가기 쉬우므로, 자극적이고 기름진 과식을 지양해 주십시오.\n• 자율신경을 자극할 수 있는 짠 음식이나 맵고 뜨거운 보양식 위주의 섭취를 중단하고 담백한 야채죽이나 수프 위주의 편안한 식단을 구성해 보십시오.\n• 인공 감미료와 카페인이 포함된 음료는 부정맥 등의 위험을 높이므로 금해 주는 것이 좋습니다.");
                    tvSupplementsRecommendation.setText("• 칼륨 & 마그네슘 복합체: 심장 근육의 긴장과 흥분을 빠르게 진정시키는 데 가장 최우선적으로 권장됩니다.\n• 타우린: 심장 세포막을 안정시키고 맥박 조절 작향에 긍정적인 도움을 제공합니다.\n• (주의) 고카페인성 에너지 부스터나 정제 형태의 각성 성분 영양제는 절대 섭취를 금하십시오.");
                }
                break;

            case "spo2":
                float spo2 = p.getFloat("last_spo2", 98.0f);
                tvReportTitle.setText("산소포화도 상세 리포트");
                tvReportSubTitle.setText("최근 측정 산소포화도");
                tvReportValue.setText(String.format(Locale.KOREA, "%.0f%%", spo2));
                
                if (spo2 >= 95f) {
                    tvReportStatus.setText("● 정상");
                    tvReportStatus.setTextColor(0xFF69F0AE);
                    tvReportValue.setTextColor(0xFF4FC3F7);
                    
                    tvDetailedAnalysis.setText("신체 내 세포로 산소가 매우 원활하고 풍부하게 공급되고 있습니다. 폐 기능과 혈액 공급계가 조화롭게 기능하고 있으며, 신선한 산소 전달력이 우수하여 정상적인 피로 저항력을 유지하고 계십니다.");
                    tvDietRecommendation.setText("• 조혈 작용에 필수적인 시금치, 케일 등의 녹색 채소와 철분이 풍부한 붉은 고기를 고루 섭취하여 적혈구 생산을 계속해서 지원해 주십시오.\n• 철분의 흡수율을 드라마틱하게 높여주는 레몬, 오렌지 등의 비타민 C 함유 과일을 식사 후에 섭취하시면 더욱 도움이 됩니다.\n• 혈관을 맑게 유지할 수 있는 깨끗한 물을 하루에 1.5리터 이상 자주 나누어 섭취하는 것이 좋습니다.");
                    tvSupplementsRecommendation.setText("• 철분제 (액상 또는 캡슐): 적혈구 생성의 핵심 원료로, 비타민 C와 함께 섭취하면 흡수 시너지 효과가 나타납니다.\n• 엽산 & 비타민 B12: 새로운 적혈구가 성숙하는 과정을 자극하여 양질의 산소 캐리어를 활성화합니다.\n• 스피룰리나: 엽록소가 풍부하여 혈액 내 산소 교환 메커니즘을 지원하고 면역을 보강합니다.");
                } else {
                    tvReportStatus.setText("⚠ 위험 (저산소 위험)");
                    tvReportStatus.setTextColor(0xFFEF5350);
                    tvReportValue.setTextColor(0xFFEF5350);
                    
                    tvDetailedAnalysis.setText("현재 산소포화도가 안정권인 95% 미만으로 떨어져, 일시적인 저산소증 상태가 발생할 가능성이 있습니다. 두통, 현기증, 혹은 가벼운 호흡 곤란을 동반할 수 있으므로, 즉시 깊은 호흡을 유도하고 실내 공기를 빠르게 환기하여 주십시오.");
                    tvDietRecommendation.setText("• 혈관 수축을 유발하는 고칼로리 기름진 육류 및 튀김류 식습관을 피하고 혈액을 맑게 돕는 신선한 미역, 다시마 등의 해조류를 식단에 대폭 추가해 보십시오.\n• 산소 결합력을 저해할 수 있는 가공 육가공품(소시지, 햄류의 아질산나트륨)의 섭취는 철저히 제한하십시오.\n• 혈류 촉진을 유도하도록 따뜻한 미온수를 수시로 마셔 혈액 점도를 낮춰 주십시오.");
                    tvSupplementsRecommendation.setText("• 액상 철분제: 흡수율이 빨라 산소 운반 저하 증상 완화에 직접적인 역할을 수행합니다.\n• 클로렐라/스피룰리나 고함량 제품: 혈행 개선 및 세포 대사에 산소 공급을 지원하는 보완 영양제입니다.\n• 비타민 E: 적혈구 막의 산화를 억제하여 적혈구가 파괴되는 것을 예방하고 수명을 연장시켜 줍니다.");
                }
                break;

            case "temperature":
                float temp = p.getFloat("last_temp", 36.5f);
                tvReportTitle.setText("체온 상세 리포트");
                tvReportSubTitle.setText("최근 측정 체온");
                tvReportValue.setText(String.format(Locale.KOREA, "%.1f°C", temp));
                
                if (temp < 37.5f) {
                    tvReportStatus.setText("● 정상");
                    tvReportStatus.setTextColor(0xFF69F0AE);
                    tvReportValue.setTextColor(0xFF4FC3F7);
                    
                    tvDetailedAnalysis.setText("기초 대사량이 정상 범주에 머물고 있으며 체온 조절 능력이 잘 가동되고 있습니다. 면역 세포들이 활성화되기 가장 최적화된 이상적인 심부 온도를 완벽하게 유지하고 계신 컨디션입니다.");
                    tvDietRecommendation.setText("• 체온 향상과 혈행 안정에 기여하는 생강차, 대추차, 홍삼 등의 따뜻한 성질의 한방 식품을 섭취해 보십시오.\n• 면역세포 생성의 든든한 원료인 단백질(계란, 닭가슴살, 두부)과 체온 발열 대사를 지원할 질 좋은 탄수화물 식단을 규칙적으로 챙기십시오.\n• 신진대사를 순조롭게 활성화하도록 찬 음료보다는 체온과 유사한 온수를 자주 마시는 습관이 유리합니다.");
                    tvSupplementsRecommendation.setText("• 비타민 C & 아연: 기본적인 면역 방어 체계를 확립해 면역력을 보강합니다.\n• 유산균 (Probiotics): 면역 세포의 약 70%가 집중된 장내 환경을 개선하여 근본적인 면역 체계와 체온 저항력을 지원합니다.\n• 비타민 D: 림프구 활성화를 돕고 사계절 신체 면역 항상성 유지를 위해 필수로 추천됩니다.");
                } else {
                    tvReportStatus.setText("⚠ 위험 (발열)");
                    tvReportStatus.setTextColor(0xFFEF5350);
                    tvReportValue.setTextColor(0xFFEF5350);
                    
                    tvDetailedAnalysis.setText("체온이 37.5°C 이상으로 상승한 발열 상태가 감지되었습니다. 신체 내 염증 반응이나 바이러스 침투에 대응하기 위해 면역계가 긴급 가동 중인 신호일 수 있으니, 무리한 실외 활동을 전면 즉각 중단하고 시원한 그늘이나 실내에서 안정 조치를 취하십시오.");
                    tvDietRecommendation.setText("• 소화 에너지를 최소화하고 수분을 집중 보충할 수 있는 묽은 흰죽이나 오이, 수박 등의 수분 중심 저자극 유동식을 지향해 주십시오.\n• 발열 증상을 더욱 심화시키거나 염증을 악화할 수 있는 밀가루, 당도가 매우 높은 단 음식을 식단에서 완벽히 차단하십시오.\n• 미온수를 꾸준히 섭취해 땀으로 유실되는 탈수 증상을 반드시 방지해야 합니다.");
                    tvSupplementsRecommendation.setText("• 비타민 C 메가도스: 활성산소를 억제하고 염증 대처 면역 작용을 강력하게 백업합니다.\n• 아연 단독 영양제: 급격한 세포 분열 및 활성화를 지원해 회복 골든타임을 단축시킵니다.\n• (주의) 고열 상태가 하루 이상 지속되거나 오한이 극심해지는 경우에는 약물 오남용을 피하고 즉시 전문 의료기관에 내원하십시오.");
                }
                break;

            default: // blood_flow
                float bf = p.getFloat("last_blood_flow", 110.0f);
                tvReportTitle.setText("혈류속도 상세 리포트");
                tvReportSubTitle.setText("최근 측정 혈류속도");
                tvReportValue.setText(String.format(Locale.KOREA, "%.0f cm/s", bf));
                
                if (bf >= 100f && bf <= 120f) {
                    tvReportStatus.setText("● 정상 범위");
                    tvReportStatus.setTextColor(0xFF69F0AE);
                    tvReportValue.setTextColor(0xFFFFFFFF);
                    
                    tvDetailedAnalysis.setText("혈액이 혈관벽에 적절한 탄성을 유지하면서 말초 혈관까지 막힘없이 매우 건강한 속도로 주행하고 있습니다. 심박과의 리듬도 훌륭하며 뇌와 중요 장기로의 원활한 영양 공급이 잘 보장되는 이상적인 컨디션입니다.");
                    tvDietRecommendation.setText("• 고등어, 삼치, 연어 등 혈행 개선 기능성이 뚜렷하게 인증된 불포화지방산(오메가-3) 성분이 풍부한 등푸른 생선 식습관을 지속해 보십시오.\n• 알리신 성분이 혈전을 방지하고 혈관을 확장해 주는 마늘과 양파를 구워 한식 식단에 함께 풍부하게 곁들여 먹는 것이 권장됩니다.\n• 혈관의 탄력과 산화 방지를 보조하는 루틴 성분이 풍부한 메밀차나 신선한 채소 샐러드를 섭취하십시오.");
                    tvSupplementsRecommendation.setText("• 오메가-3 (EPA 및 DHA 함유): 혈중 중성지질 및 혈행 개선에 전 세계적으로 가장 효과가 입증된 최우선 권장 영양제입니다.\n• 은행잎 추출물 (Ginkgo Biloba): 뇌 및 미세 모세혈관 등의 말초 혈액 흐름 장애 완화에 도움을 줍니다.\n• CoQ10: 혈관 벽 산화 손상을 줄여주고 높은 혈압을 감소시켜 고른 속도의 안정적 혈류 주행을 지원합니다.");
                } else {
                    tvReportStatus.setText("⚠ 위험 (혈류 속도 이상)");
                    tvReportStatus.setTextColor(0xFFEF5350);
                    tvReportValue.setTextColor(0xFFEF5350);
                    
                    tvDetailedAnalysis.setText("최근 측정된 혈류 속도가 표준 수치 범위(100~120 cm/s)를 과도하게 초과하거나 크게 미달해 있습니다. 혈관의 점성이 높아졌거나 탄성이 저하되는 등 말초 혈류 부하 현상이 의심되므로 심장에 불필요한 과부하가 축적되기 전, 정밀 혈류 매개 지표 검사를 권장합니다.");
                    tvDietRecommendation.setText("• 혈관 통로를 좁히고 혈전을 유도하기 쉬운 튀김, 붉은 육류의 과도한 동물성 포화지방 섭취를 엄격히 제한하십시오.\n• 혈압 상승의 주범이자 혈액 점도를 높이는 염분 배출을 돕기 위해 나트륨 함량이 높은 찌개, 탕류 국물을 먹지 말고 깨끗한 채식 쌈밥 식단을 구성해 보십시오.\n• 혈관 내 찌꺼기 산화를 촉진하는 인스턴트 가공식품과 흡연을 철저히 금하십시오.");
                    tvSupplementsRecommendation.setText("• 고함량 rTG 오메가-3: 혈행 개선 긴급 지원을 위해 흡수율이 우수한 rTG 형태의 영양제 복용이 강력 권장됩니다.\n• 낫토키나제: 혈관 내 노폐물과 혈전 물질을 용해하고 생성 억제를 돕는 검증된 보조 성분입니다.\n• 레시틴: 혈관 벽에 엉겨 붙어 있는 노폐 지방질을 유화하여 안전하게 씻어내는 정화 활성 작용을 보강해 줍니다.");
                }
                break;
        }
    }
}
