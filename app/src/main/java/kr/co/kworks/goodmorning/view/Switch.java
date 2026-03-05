package kr.co.kworks.goodmorning.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.MutableLiveData;

import kr.co.kworks.goodmorning.R;

public class Switch extends View {

    private Listener listener;
    private Paint paintPrimary, paintWhite30, paintGrayF0F0F0, paintWhite30Stroke, paintPrimaryOutline, paintTextBlack, paintTextWhite;
    private Context mContext;
    private int width, height;
    private float radius;
    private int startCount, maxCount, minCount;
    private float sweepAngle;
    private MutableLiveData<String> exerciseStatus;
    private ValueAnimator animator;
    private float circleX, circleY;
    private boolean isOn, isMoving, drawTextOn;

    public Switch(Context context) {
        super(context);
        mContext = context;
    }

    public Switch(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        paintPrimary = new Paint();
        paintPrimary.setColor(mContext.getColor(R.color.communicate_green));
        paintPrimary.setStyle(Paint.Style.FILL);
        paintPrimary.setAntiAlias(true);

        paintWhite30 = new Paint();
        paintWhite30.setColor(mContext.getColor(R.color.white_30));
        paintWhite30.setStyle(Paint.Style.FILL);
        paintWhite30.setAntiAlias(true);

        paintGrayF0F0F0 = new Paint();
        paintGrayF0F0F0.setColor(mContext.getColor(R.color.gray_f0f0f0));
        paintGrayF0F0F0.setStyle(Paint.Style.FILL);
        paintGrayF0F0F0.setAntiAlias(true);

        paintWhite30Stroke = new Paint();
        paintWhite30Stroke.setColor(mContext.getColor(R.color.white_30));
        paintWhite30Stroke.setStyle(Paint.Style.STROKE);
        paintWhite30Stroke.setStrokeWidth(2);
        paintWhite30Stroke.setAntiAlias(true);

        paintPrimaryOutline = new Paint();
        paintPrimaryOutline.setColor(mContext.getColor(R.color.communicate_green));
        paintPrimaryOutline.setStyle(Paint.Style.STROKE);
        paintPrimaryOutline.setStrokeWidth(2);
        paintPrimaryOutline.setAntiAlias(true);

        paintTextBlack = new Paint();
        paintTextBlack.setTextSize(dpToPx(20));
        paintTextBlack.setColor(mContext.getColor(R.color.black));
        paintTextBlack.setStyle(Paint.Style.FILL);
        paintTextBlack.setTypeface(ResourcesCompat.getFont(mContext, R.font.pretendard_bold));
        paintTextBlack.setTextAlign(Paint.Align.CENTER);

        paintTextWhite = new Paint();
        paintTextWhite.setTextSize(dpToPx(20));
        paintTextWhite.setColor(mContext.getColor(R.color.white));
        paintTextWhite.setStyle(Paint.Style.FILL);
        paintTextWhite.setTextAlign(Paint.Align.CENTER);
        paintTextWhite.setTypeface(ResourcesCompat.getFont(mContext, R.font.pretendard_bold));


        circleX = -1f;

        isOn = false;
        isMoving = false;
        drawTextOn = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = getWidth();
        height = getHeight();
        radius = height*0.5f;
        float textSize = radius * 0.55f;
        paintTextBlack.setTextSize(textSize);
        paintTextWhite.setTextSize(textSize);
        drawRect(canvas);
        drawCircle(canvas);
        drawText(canvas);
    }

    private void drawText2(Canvas canvas) {
        if(!drawTextOn) return;
        if(isMoving) return;
        if(isOn) {
            if (circleX == -1f) canvas.drawText("on", radius-radius - dpToPx(2), (height/2f)+dpToPx(2), paintTextBlack);
            else canvas.drawText("on", circleX-radius - dpToPx(2), (height/2f)+dpToPx(2), paintTextBlack);
        } else {
            if (circleX == -1f) canvas.drawText("off", radius+radius + dpToPx(2), (height/2f)+dpToPx(2), paintTextWhite);
            else canvas.drawText("off", circleX+radius + dpToPx(2), (height/2f)+dpToPx(2), paintTextWhite);
        }
    }

    private void drawText(Canvas canvas) {
        if (!drawTextOn) return;

        String text = isOn ? "ON" : "OFF";
        Paint paint = isOn ? paintTextBlack : paintTextWhite;

        float cx = (circleX == -1f || circleX == 0.0f)
            ? (isOn ? width - radius : radius)
            : circleX;

        float cy = radius; // 원의 중심 Y

        Paint.FontMetrics fm = paint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        float textBaselineY = cy + (textHeight / 2f) - fm.descent;

        canvas.drawText(text, cx, textBaselineY, paint);
    }

    private void drawRect(Canvas canvas) {
        if(isOn) {
            canvas.drawRoundRect(dpToPx(1),0,width-dpToPx(2),2*radius,radius,radius,paintPrimaryOutline);
        } else {
            canvas.drawRoundRect(dpToPx(1),0,width-dpToPx(2),2*radius,radius,radius,paintWhite30Stroke);
        }
    }

    private void drawCircle(Canvas canvas) {
        if (circleX == -1f || circleX==0.0f) {
            Paint tempPaint = isOn ? paintPrimary : paintWhite30;
            float cx = isOn ? width - radius : radius;
            canvas.drawCircle(cx, radius, radius-dpToPx(3), tempPaint);
            return;
        }

        if (isOn) {
            canvas.drawCircle(circleX, radius, radius-dpToPx(3), paintPrimary);
        } else {
            canvas.drawCircle(circleX, radius, radius-dpToPx(3), paintWhite30);
//            canvas.drawCircle(circleX, radius, radius-dpToPx(1), paintWhite30Stroke);
        }
    }

    public void click() {
        if (isMoving) return;
        if (isOn) {
            // on 상황
            animateOff(0.5f);
        } else {
            animateOn(0.5f);
        }
    }

    public void setOn(boolean selection) {
        if(selection) setOn();
        else setOff();

        isOn = selection;
    }

    private void setOn() {
        animateOn(0f);
    }

    private void setOff() {
        animateOff(0f);
    }

    private void animateOn(float fullFillSecond) {
        isMoving = true;
        animator = ValueAnimator.ofFloat(radius, width-radius);
        animator.setDuration((long) (fullFillSecond*1000L));
        final float[] min = {width-radius};
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                circleX = (float) animation.getAnimatedValue();
                if(circleX < min[0]) {
                    min[0] = circleX;
                    if (listener != null) {
                        listener.onComplete();
                    }
                }
                if(circleX == width-radius) {
                    isMoving = false;
                }
                invalidate(); // View를 다시 그려서 애니메이션 효과를 반영
            }
        });
        animator.start();
        isOn = true;
    }

    private void animateOff(float fullFillSecond) {
        isMoving = true;
        animator = ValueAnimator.ofFloat(width-radius, radius);
        animator.setDuration((long) (fullFillSecond*1000L));
        final float[] max = {radius};
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                circleX = (float) animation.getAnimatedValue();
                if(circleX > max[0]) {
                    max[0] = circleX;
                    if (listener != null) {
                        listener.offComplete();
                    }
                }
                if(circleX == radius) {
                    isMoving = false;
                }
                invalidate(); // View를 다시 그려서 애니메이션 효과를 반영
            }
        });
        animator.start();
        isOn = false;
    }

    private int dpToPx(float dp) {
        Resources resources = mContext.getResources();
        float density = resources.getDisplayMetrics().density;
        int px = (int) Math.ceil(dp * density);
        return px;
    }

    public interface Listener {
        void onComplete();
        void offComplete();
    }

    public void setOnOffListener(Listener listener) {
        this.listener = listener;
    }

    public void removeOnOffListener() {
        this.listener = null;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOnColor(int color) {
        paintPrimaryOutline.setColor(mContext.getColor(color));
        paintPrimary.setColor(mContext.getColor(color));
    }

    public void setDrawText(boolean bool) {
        drawTextOn = bool;
    }
}
