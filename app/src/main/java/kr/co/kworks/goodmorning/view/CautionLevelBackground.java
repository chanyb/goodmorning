package kr.co.kworks.goodmorning.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import kr.co.kworks.goodmorning.R;

public class CautionLevelBackground extends View {

    private Paint whitePaint, bluePaint, yellowPaint, orangePaint, redPaint, blackTextPaint;
    private Context mContext;
    private int width, height;

    private float needleBaseDiameter;

    public CautionLevelBackground(Context context) {
        super(context);
        mContext = context;
    }

    public CautionLevelBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        whitePaint = new Paint();
        whitePaint.setColor(mContext.getColor(R.color.white));
        whitePaint.setStyle(Paint.Style.FILL);
        whitePaint.setAntiAlias(true);

        bluePaint = new Paint();
        bluePaint.setColor(mContext.getColor(R.color.communicate_blue));
        bluePaint.setStyle(Paint.Style.FILL);
        bluePaint.setAntiAlias(true);

        yellowPaint = new Paint();
        yellowPaint.setColor(mContext.getColor(R.color.yellow));
        yellowPaint.setStyle(Paint.Style.FILL);
        yellowPaint.setAntiAlias(true);

        orangePaint = new Paint();
        orangePaint.setColor(mContext.getColor(R.color.communicate_orange));
        orangePaint.setStyle(Paint.Style.FILL);
        orangePaint.setAntiAlias(true);

        redPaint = new Paint();
        redPaint.setColor(mContext.getColor(R.color.red));
        redPaint.setStyle(Paint.Style.FILL);
        redPaint.setAntiAlias(true);

        Typeface typeface = ResourcesCompat.getFont(mContext, R.font.pretendard_bold);
        blackTextPaint = new Paint();
        blackTextPaint.setAntiAlias(true); // 계단현상 방지
        blackTextPaint.setColor(mContext.getColor(R.color.black)); // 글자색
        blackTextPaint.setTextSize(70f); // 글자 크기 (px 단위)
        blackTextPaint.setTypeface(typeface);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        width = getWidth();
        height = getHeight();
        needleBaseDiameter = height * 0.37f;
        drawGaugeBackground(canvas);
        drawText(canvas);
    }

    private void drawGaugeBackground(Canvas canvas) {
        // 180도 흰 배경 arc
        float x_center = width/2f;
        float y_center = height - needleBaseDiameter/2f;
        float whiteArcBaseRadius = x_center - needleBaseDiameter;

        for (float theta=180; theta<=360; theta+=0.05f) {
            float[] point = getPointOnArc(whiteArcBaseRadius, theta);
            float x = x_center + point[0];
            float y = y_center + point[1];
            canvas.drawCircle(x,y, needleBaseDiameter/2f, whitePaint);
        }

        float[] point = getPointOnArc(whiteArcBaseRadius, 180);
        float colorRadius = needleBaseDiameter/2f * 0.75f;
        canvas.drawCircle(x_center + point[0], y_center + point[1], colorRadius, bluePaint);

        float startTheta = 180f;
        float attentionTheta = 225f;
        float cautionTheta = 270f;
        float guardTheta = 315f;
        float seriousTheta = 360f;

        drawLevelBackground(canvas, startTheta, attentionTheta, whiteArcBaseRadius, colorRadius, bluePaint);
        drawLevelBackground(canvas, attentionTheta, cautionTheta, whiteArcBaseRadius, colorRadius, yellowPaint);
        drawLevelBackground(canvas, cautionTheta, guardTheta, whiteArcBaseRadius, colorRadius, orangePaint);
        drawLevelBackground(canvas, guardTheta, seriousTheta, whiteArcBaseRadius, colorRadius, redPaint);

        float[] endPoint = getPointOnArc(whiteArcBaseRadius, 360f);
        canvas.drawCircle(x_center + endPoint[0], y_center + endPoint[1], colorRadius, redPaint);
    }

    private void drawLevelBackground(Canvas canvas, float startTheta, float endTheta, float baseLength, float halfSize, Paint paint) {
        float x_center = width/2f;
        float y_center = height - needleBaseDiameter/2f;

        Path path = new Path();
        for (float theta=startTheta; theta<=endTheta; theta+=0.01f) {
            float[] point2 = getPointOnArc(baseLength-halfSize, theta);
            if (theta == startTheta) {
                path.moveTo(x_center + point2[0], y_center + point2[1]);
            } else {
                path.lineTo(x_center + point2[0], y_center + point2[1]);
            }
        }

        for (float theta=endTheta; theta>=startTheta; theta-=0.01f) {
            float[] point2 = getPointOnArc(baseLength+halfSize, theta);
            path.lineTo(x_center + point2[0], y_center + point2[1]);
        }

        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawText(Canvas canvas) {
        float x_center = width/2f;
        float y_center = height - needleBaseDiameter/2f;

        float[] point = getPointOnArc(x_center, y_center, width/2f - needleBaseDiameter - (width*0.012f), 185f);
        canvas.save();
        canvas.rotate(-65, point[0], point[1]);
        canvas.drawText("관심", point[0], point[1], blackTextPaint);
        canvas.restore();

        point = getPointOnArc(x_center, y_center, width/2f - needleBaseDiameter - (width*0.012f), 235f);
        canvas.save();
        canvas.rotate(-20, point[0], point[1]);
        canvas.drawText("주의", point[0], point[1], blackTextPaint);
        canvas.restore();

        point = getPointOnArc(x_center, y_center, width/2f - needleBaseDiameter - (width*0.012f), 280f);
        canvas.save();
        canvas.rotate(20, point[0], point[1]);
        canvas.drawText("경계", point[0], point[1], blackTextPaint);
        canvas.restore();

        point = getPointOnArc(x_center, y_center, width/2f - needleBaseDiameter - (width*0.03f), 330f);
        canvas.save();
        canvas.rotate(65, point[0], point[1]);
        canvas.drawText("심각", point[0], point[1], blackTextPaint);
        canvas.restore();
    }

    private float[] getPointOnArc(float radius, float theta) {
        // init
        float[] point = new float[2];
        point[0] = 0f;
        point[1] = 0f;

        double radian = Math.toRadians(theta);
        point[0] = radius * (float)Math.cos(radian);
        point[1] = radius * (float)Math.sin(radian);

        return point;
    }

    private float[] getPointOnArc(float x, float y, float radius, float theta) {
        // init
        float[] point = new float[2];
        point[0] = 0f;
        point[1] = 0f;

        double radian = Math.toRadians(theta);
        point[0] = x + radius * (float)Math.cos(radian);
        point[1] = y + radius * (float)Math.sin(radian);

        return point;
    }

    private int dpToPx(float dp) {
        Resources resources = mContext.getResources();
        float density = resources.getDisplayMetrics().density;
        int px = (int) Math.ceil(dp * density);
        return px;
    }
}
