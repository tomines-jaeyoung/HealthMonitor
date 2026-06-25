package com.example.healthmonitor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class DataGraphView extends View {
    private float[] dataPoints = new float[0];
    private String[] xLabels = new String[0];
    
    private Paint linePaint;
    private Paint dotPaint;
    private Paint textPaint;
    private Paint bgLinePaint;
    private Path graphPath;

    public DataGraphView(Context context) {
        super(context);
        init();
    }

    public DataGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0xFF4FC3F7); // 파란색 그래프 선
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(6f);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(0xFFFFFFFF); // 흰색 데이터 점
        dotPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF90CAF9); // 연한 파란색 텍스트
        textPaint.setTextSize(22f); // 글씨 크기 약간 축소
        textPaint.setTextAlign(Paint.Align.CENTER);

        bgLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgLinePaint.setColor(0x33FFFFFF); // 반투명 흰색 가이드라인
        bgLinePaint.setStyle(Paint.Style.STROKE);
        bgLinePaint.setStrokeWidth(2f);

        graphPath = new Path();
    }

    public void setData(float[] points, String[] labels) {
        this.dataPoints = points;
        this.xLabels = labels;
        invalidate(); // 화면 다시 그리기
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dataPoints.length == 0) return;

        int width = getWidth();
        int height = getHeight();
        
        float paddingLeft = 60f;
        float paddingRight = 40f;
        float paddingTop = 40f;
        float paddingBottom = 40f;

        float graphWidth = width - paddingLeft - paddingRight;
        float graphHeight = height - paddingTop - paddingBottom;

        // Y축 최대/최소값 계산
        float min = dataPoints[0]; 
        float max = dataPoints[0]; 
        for (float v : dataPoints) {
            if (v < min) min = v;
            if (v > max) max = v;
        }
        // 여유 공간 추가
        min -= 10f;
        max += 10f;
        if(min < 0) min = 0; // 음수 방지

        // 배경 가이드라인 및 Y축 레이블 그리기 (4칸으로 분할)
        textPaint.setTextAlign(Paint.Align.RIGHT);
        for (int i = 0; i <= 4; i++) {
            float y = paddingTop + graphHeight * (i / 4f);
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, bgLinePaint);
            
            float val = max - (max - min) * (i / 4f);
            canvas.drawText(String.valueOf((int)val), paddingLeft - 20f, y + 10f, textPaint);
        }

        // 그래프 선 그리기
        graphPath.reset();
        for (int i = 0; i < dataPoints.length; i++) {
            float x = paddingLeft + (graphWidth / (dataPoints.length - 1)) * i;
            float y = paddingTop + graphHeight - ((dataPoints[i] - min) / (max - min)) * graphHeight;
            
            if (i == 0) {
                graphPath.moveTo(x, y);
            } else {
                graphPath.lineTo(x, y);
            }
        }
        canvas.drawPath(graphPath, linePaint);

        // 데이터 점 및 X축 레이블 그리기
        textPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < dataPoints.length; i++) {
            float x = paddingLeft + (graphWidth / (dataPoints.length - 1)) * i;
            float y = paddingTop + graphHeight - ((dataPoints[i] - min) / (max - min)) * graphHeight;
            
            canvas.drawCircle(x, y, 8f, dotPaint);
            
            if (xLabels.length > i && !xLabels[i].isEmpty()) {
                // 라벨이 비어있지 않은 경우에만 그리기 (겹침 방지)
                canvas.drawText(xLabels[i], x, height - 10f, textPaint);
            }
        }
    }
}
