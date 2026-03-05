package kr.co.kworks.goodmorning.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import kr.co.kworks.goodmorning.R;

public class ProgressView extends View {

    private Paint whitePaint, greenPaint;
    private Context mContext;
    private int width, height;
    private float rightPx, currentFillPercent, fillPercent;
    private AnimationFinished listener;
    private long duration;

    public ProgressView(Context context) {
        super(context);
        mContext = context;
    }

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        whitePaint = new Paint();
        whitePaint.setColor(mContext.getColor(R.color.white_10));
        whitePaint.setStyle(Paint.Style.FILL);
        whitePaint.setAntiAlias(true);

        greenPaint = new Paint();
        greenPaint.setColor(mContext.getColor(R.color.communicate_green));
        greenPaint.setStyle(Paint.Style.FILL);
        greenPaint.setAntiAlias(true);
        currentFillPercent = fillPercent = 0f;
        duration = 500;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = getWidth();
        height = getHeight();
        drawGauge(canvas);
    }

    private void drawGauge(Canvas canvas) {
        float left = 0;
        float top = 0;
        float right = width;
        float bottom = height;

        // 흰 배경
        canvas.drawRoundRect(left, top, right, bottom, dpToPx(15), dpToPx(15), whitePaint);

        // 채우기 표현
        if(fillPercent > 1f) fillPercent = 1f;
        rightPx = right * fillPercent;
        canvas.drawRoundRect(left, top, rightPx, bottom, dpToPx(15), dpToPx(15), greenPaint);
//        canvas.drawRect(left+dpToPx(5), top+dpToPx(5), rightPx, bottom-dpToPx(5), greenPaint);
        currentFillPercent = fillPercent;
    }

    /**
     * Progress를 채우는 Aimation 작동,
     * @param percent 0 ~ 1 사이 소수 값
     */
    public void animateFill(float percent) {
        if(currentFillPercent >= percent) {
            if (listener != null) listener.onFinish();
            return;
        }
        ValueAnimator animator = ValueAnimator.ofFloat(currentFillPercent, percent);

        animator.setDuration(duration); // 애니메이션 기간 설정 (예: 2초)
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                fillPercent = (float) animation.getAnimatedValue();

                if(fillPercent >= percent && percent == 1) {
                    if (listener != null) listener.onFinish();
                }
                invalidate(); // View를 다시 그려서 애니메이션 효과를 반영
            }
        });
        animator.start();
    }

    private int dpToPx(float dp) {
        Resources resources = mContext.getResources();
        float density = resources.getDisplayMetrics().density;
        int px = (int) Math.ceil(dp * density);
        return px;
    }

    public interface AnimationFinished {
        void onFinish();
    }

    public void setOnFinishListener(AnimationFinished animationFinished) {
        this.listener = animationFinished;
    }

    public AnimationFinished getOnFinishListener() {
        return this.listener;
    }

    public void setAnimatorDuration(long milliSeconds) {
        this.duration = milliSeconds;
    }

    public void initFill() {
        fillPercent = 0f;
        invalidate();
    }

}
