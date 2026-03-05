package kr.co.kworks.goodmorning.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import kr.co.kworks.goodmorning.R;

public class CautionLevelNeedleView extends View {

    private Paint whitePaint, needlePaint, needleRoundPaint;
    private Context mContext;
    private int width, height;
    private long duration = 2000L;

    private float needleBaseDiameter;

    private float currentNeedleTheta = 180f;
    private boolean isInit = true;

    public CautionLevelNeedleView(Context context) {
        super(context);
        mContext = context;
    }

    public CautionLevelNeedleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        whitePaint = new Paint();
        whitePaint.setColor(mContext.getColor(R.color.white));
        whitePaint.setStyle(Paint.Style.FILL);
        whitePaint.setAntiAlias(true);

        needlePaint = new Paint();
        needlePaint.setColor(mContext.getColor(R.color.gray_404040));
        needlePaint.setStyle(Paint.Style.FILL);
        needlePaint.setAntiAlias(true);

        needleRoundPaint = new Paint();
        needleRoundPaint.setColor(mContext.getColor(R.color.gray_404040));
        needleRoundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        needleRoundPaint.setStrokeWidth(3f);
        needleRoundPaint.setPathEffect(new CornerPathEffect(50f));
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        width = getWidth();
        height = getHeight();
        needleBaseDiameter = height * 0.37f;
        needleRoundPaint.setPathEffect(new CornerPathEffect(needleBaseDiameter * 0.5f));

        drawNeedle(canvas, currentNeedleTheta);
//        drawNeedle(canvas, 200f);
//        drawNeedle(canvas, 247.5f);
//        drawNeedle(canvas, 292.5f);
//        drawNeedle(canvas, 345f);
    }

    private void drawNeedle(Canvas canvas, float theta) {

        float radius = needleBaseDiameter/2f;
        float x_center = width/2f;
        float y_center = height - needleBaseDiameter/2f;

        setPivotX(x_center);
        setPivotY(y_center);

        canvas.drawCircle(x_center, y_center, radius, whitePaint); // needle base (white)

        float[] point = getPointOnArc(x_center - radius*1.9f, theta);
        float x = x_center + point[0];
        float y = y_center + point[1];

        float[] triangleBottom1 = getPointOnArc(x_center, y_center, radius/2.7f, theta-90);
        float[] triangleBottom2 = getPointOnArc(x_center, y_center, radius/2.7f, theta+90);


        Path path = new Path();
        path.moveTo(triangleBottom1[0], triangleBottom1[1]);
        path.lineTo(x, y);
        path.lineTo(triangleBottom2[0], triangleBottom2[1]);
        path.close();
        canvas.drawPath(path, needleRoundPaint);

        float[] point2 = getPointOnArc(x_center, y_center, x_center - radius*2.4f, theta);
        float x2 = point2[0];
        float y2 = point2[1];
        triangleBottom1 = getPointOnArc(x_center, y_center, radius/2.5f, theta-90);
        triangleBottom2 = getPointOnArc(x_center, y_center, radius/2.5f, theta+90);
        Path path2 = new Path();
        path2.moveTo(triangleBottom1[0], triangleBottom1[1]);
        path2.lineTo(x2, y2);
        path2.lineTo(triangleBottom2[0], triangleBottom2[1]);
        path2.close();
//        canvas.drawPath(path2, needlePaint);
        canvas.drawCircle(x_center, y_center, radius/2.5f, needlePaint);
        canvas.drawCircle(x_center, y_center, radius/2.5f/2f, whitePaint);

        currentNeedleTheta = theta;
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

    public void drawNeedleWithAnimate(float theta) {
        ValueAnimator animator = ValueAnimator.ofFloat(currentNeedleTheta, theta);
        animator.setDuration(duration);
//        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            currentNeedleTheta = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    public void animateNeedleTo(float targetDeg, long durationMs) {
        this.animate()
            .rotation(targetDeg)
            .withLayer()                // 애니 시작~끝 사이에만 HW 레이어 켜줌
            .setDuration(durationMs)
            .start();
    }

}
