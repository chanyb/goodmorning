package kr.co.kworks.goodmorning.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import kr.co.kworks.goodmorning.R;


public class Circle extends View {

    private Paint paint_active, paint_inactive;
    private Context mContext;
    private int width, height, r;
    private int color;

    public Circle(Context context) {
        super(context);
        mContext = context;
    }

    public Circle(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
        getAttrs(attrs);
    }

    public Circle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        mContext = context;
        init();
        getAttrs(attrs, defStyle);
    }

    private void getAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.Circle);
        setTypeArray(typedArray);
    }

    private void getAttrs(AttributeSet attrs, int defStyle) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.Circle, defStyle, 0);
        setTypeArray(typedArray);
    }

    private void setTypeArray(TypedArray typedArray) {
//        int textColor = typedArray.getColor(R.styleable.CompassView_textColor, 0);
        color = typedArray.getColor(R.styleable.Circle_color, mContext.getColor(R.color.black));
        if( color != mContext.getColor(R.color.black)) {
            if(paint_active == null) {
                paint_active = new Paint();
            }
            paint_active.setColor(color);
            setSelected(true);
        }

        typedArray.recycle();
    }

    private void init() {
        if (paint_active == null) {
            paint_active = new Paint();
            paint_active.setColor(mContext.getColor(R.color.red));
        }

        paint_active.setStyle(Paint.Style.FILL);
        paint_active.setAntiAlias(true);

        if (paint_inactive == null) {
            paint_inactive = new Paint();
            paint_inactive.setColor(mContext.getColor(R.color.gray_dbdbdb));
        }
        paint_inactive = new Paint();
        paint_inactive.setColor(mContext.getColor(R.color.gray_dbdbdb));
        paint_inactive.setStyle(Paint.Style.FILL);
        paint_inactive.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = getWidth();
        height = getHeight();
        r = width/2;
        drawCircle(canvas);
    }

    private int dpToPx(float dp) {
        Resources resources = mContext.getResources();
        float density = resources.getDisplayMetrics().density;
        int px = (int) Math.ceil(dp * density);
        return px;
    }

    private void drawCircle(Canvas canvas) {
        if(isSelected()) canvas.drawCircle(r, r, r, paint_active);
        else canvas.drawCircle(r, r, r, paint_inactive);
    }

    public void setActiveColor(int color) {
        paint_active.setColor(color);
        invalidate();
    }

    public void setInactiveColor(int color) {
        paint_inactive.setColor(color);
        invalidate();
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        invalidate();
    }

    public int getActiveColor() {
        return paint_active.getColor();
    }
}
