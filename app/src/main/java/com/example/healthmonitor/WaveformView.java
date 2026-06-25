package com.example.healthmonitor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class WaveformView extends View {
    private Paint linePaint;
    private Path wavePath;
    private float phase = 0f;
    private boolean isAnimating = true;

    public WaveformView(Context context) {
        super(context);
        init();
    }

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(0xFF4FC3F7); // 파란색 (정상/기본)
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(6f);
        linePaint.setAntiAlias(true);
        wavePath = new Path();
    }

    public void setAnimating(boolean animating) {
        this.isAnimating = animating;
        if (animating) {
            invalidate();
        }
    }

    public void setLineColor(int color) {
        linePaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int centerY = height / 2;

        wavePath.reset();

        float frequency = 1.0f; // 파형 주기 개수 (화면에 1개의 파동)
        float amplitude = height / 3f;

        wavePath.moveTo(0, centerY);
        for (int i = 0; i <= width; i += 5) {
            float x = i;
            // PPG (광용적맥파) 와 유사한 파형 수학 모델: 
            // 기본파 + 2고조파 + 3고조파를 조합하여 수축기 최고점과 중복맥파(Dicrotic Notch) 구현
            float t = 2 * (float)Math.PI * frequency * (x / width) + phase;
            float basePulse = (float) (Math.sin(t) + 0.45 * Math.sin(2 * t - 1.2) + 0.25 * Math.sin(3 * t - 0.5));
            
            // LIVE 모드일 때만 미세한 노이즈(떨림) 추가
            float noise = isAnimating ? (float) ((Math.random() - 0.5) * 6) : 0;
            
            // Y축 좌표계는 아래로 갈수록 증가하므로 마이너스를 적용하여 파형을 위로 솟게 함
            float y = centerY - (amplitude * basePulse) + noise;
            wavePath.lineTo(x, y);
        }

        canvas.drawPath(wavePath, linePaint);

        if (isAnimating) {
            phase -= 0.15f; // 이동 속도
            postInvalidateDelayed(16); // 약 60fps 애니메이션
        }
    }
}
