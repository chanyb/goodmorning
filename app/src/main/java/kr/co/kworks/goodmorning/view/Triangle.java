package kr.co.kworks.goodmorning.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import kr.co.kworks.goodmorning.R;

public class Triangle extends View {
    private static final int ORIENTATION_NORMAL = 0;
    private static final int ORIENTATION_REVERSE = 1;
    private Listener listener;
    private Paint paintPrimary, paintWhite, paintGrayF0F0F0, paintGrayDBDBDB, paintPrimaryOutline, paintText;
    private Context mContext;
    private int width, height;
    private float radius;
    private int orientation;


    public Triangle(Context context) {
        super(context);
        mContext = context;
    }

    public Triangle(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
        getAttrs(attrs);
    }

    public Triangle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        mContext = context;
        init();
        getAttrs(attrs, defStyle);
    }

    private void init() {
        orientation = 0;
        paintPrimary = new Paint();
        paintPrimary.setColor(mContext.getColor(R.color.background));
        paintPrimary.setStyle(Paint.Style.FILL);
        paintPrimary.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = getWidth();
        height = getHeight();
        radius = height*0.5f;
        drawTriangle(canvas);
    }

    private void getAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.Triangle);
        setTypeArray(typedArray);
    }


    private void getAttrs(AttributeSet attrs, int defStyle) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.Triangle, defStyle, 0);
        setTypeArray(typedArray);
    }

    private void setTypeArray(TypedArray typedArray) {
//        int textColor = typedArray.getColor(R.styleable.CompassView_textColor, 0);
        int color = typedArray.getColor(R.styleable.Circle_color, mContext.getColor(R.color.black));
        if( color != mContext.getColor(R.color.background)) {
            if(paintPrimary == null) {
                paintPrimary = new Paint();
            }
            paintPrimary.setColor(color);
        }
        orientation = typedArray.getInt(R.styleable.Triangle_orientation, 0);
        Log.i("this", "orientation: " + orientation);
        typedArray.recycle();
    }

    private void drawTriangle(Canvas canvas) {
        Path path = new Path();
        if(orientation == ORIENTATION_NORMAL) {
            path.moveTo(width / 2f, 0f);
            path.lineTo(0f, height);
            path.lineTo(width, height);
        } else {
            path.moveTo(width, 0f);
            path.lineTo(width/2f, height);
            path.lineTo(0f,0f);
        }

        path.close();
        canvas.drawPath(path, paintPrimary);
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
}
