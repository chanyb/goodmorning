package kr.co.kworks.goodmorning.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import kr.co.kworks.goodmorning.R;

public class GridView extends View {

    private Paint whitePaint, thickPrimaryPaint;
    private Context mContext;
    private int widthCount, heightCount;
    private int emptyCountInWidth, emptyCountInHeight;
    private int emptySizePx, outSideEmptyPx;
    private float eachWidth, eachHeight;

    public GridView(Context context) {
        super(context);
        mContext = context;
    }

    public GridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        whitePaint = new Paint();
        whitePaint.setColor(mContext.getColor(R.color.white));
        whitePaint.setStyle(Paint.Style.FILL);
        whitePaint.setAntiAlias(true);

        thickPrimaryPaint = new Paint();
        thickPrimaryPaint.setColor(mContext.getColor(R.color.thick_primary));
        thickPrimaryPaint.setStyle(Paint.Style.FILL);
        thickPrimaryPaint.setAntiAlias(true);

        widthCount = 8;
        heightCount = 4;

        outSideEmptyPx = 30;

        // 이 View의 상태 저장을 허용
        setSaveEnabled(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        emptyCountInWidth = widthCount - 1;
        emptyCountInHeight = heightCount - 1;
        emptySizePx = 20;

        eachWidth = ((float) (getWidth() - (emptyCountInWidth * emptySizePx) - (2 * outSideEmptyPx))) / widthCount;
        eachHeight = ((float) (getHeight() - (emptyCountInHeight * emptySizePx) - (2 * outSideEmptyPx))) / heightCount;
        drawRect(canvas);
    }

    private void drawRect(Canvas canvas) {
        float left=emptySizePx, top=emptySizePx, right=0, bottom=0;


        for (int n=0; n<heightCount; n++) {
            top = eachHeight * n + emptySizePx * n + outSideEmptyPx;
            bottom = top + eachHeight;
            for (int m=0; m<widthCount; m++) {
                left = eachWidth*m + emptySizePx*m + outSideEmptyPx;
                right = left + eachWidth;
                canvas.drawRect(left, top, right, bottom, thickPrimaryPaint);
            }
        }
    }

    public void setWidthCount(int widthCount) {
        this.widthCount = widthCount;
    }

    public void setHeightCount(int heightCount) {
        this.heightCount = heightCount;
    }

    // --- 상태 저장/복원 ---
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.widthCount = this.widthCount;
        ss.heightCount = this.heightCount;
        ss.emptySizePx = this.emptySizePx;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        this.widthCount = ss.widthCount;
        this.heightCount = ss.heightCount;
        this.emptySizePx = ss.emptySizePx;

        // 복원 후 다시 그리기
        invalidate();
        requestLayout();
    }

    static class SavedState extends BaseSavedState {
        int widthCount;
        int heightCount;
        int emptySizePx;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            widthCount = in.readInt();
            heightCount = in.readInt();
            emptySizePx = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(widthCount);
            out.writeInt(heightCount);
            out.writeInt(emptySizePx);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override public SavedState createFromParcel(Parcel in) { return new SavedState(in); }
            @Override public SavedState[] newArray(int size) { return new SavedState[size]; }
        };
    }
}
