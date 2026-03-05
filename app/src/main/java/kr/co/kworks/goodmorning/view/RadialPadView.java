package kr.co.kworks.goodmorning.view;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.lifecycle.MutableLiveData;

import kr.co.kworks.goodmorning.viewmodel.Event;

public class RadialPadView extends View {

    public interface OnPadClickListener {
        void onCenter();
        void onUp();
        void onDown();
        void onLeft();
        void onRight();
    }

    public MutableLiveData<Event<String>> _pressedRegion, _pressEnd;

    public static final int NONE = 0, CENTER = 1, UP = 2, DOWN = 3, LEFT = 4, RIGHT = 5;

    private OnPadClickListener listener;
    private final PointF c = new PointF(); // center
    private float radius;                  // 전체 반지름
    private float centerR;                 // 중앙원 반지름
    private float ringInnerR, ringOuterR;  // 링의 안/바깥 반지름
    private int pressedRegion = NONE;

    public RadialPadView(Context ctx) { super(ctx); init(); }
    public RadialPadView(Context ctx, AttributeSet a) { super(ctx, a); init(); }
    public RadialPadView(Context ctx, AttributeSet a, int def) { super(ctx, a, def); init(); }

    private void init() {
        _pressedRegion = new MutableLiveData<>();
        _pressEnd = new MutableLiveData<>();
        setClickable(true);
    }

    public void setOnPadClickListener(OnPadClickListener l) {
        this.listener = l;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        radius = Math.min(w, h) / 2f;
        c.set(w / 2f, h / 2f);

        // 비율은 그림에 맞춰 적당히 조정
        centerR    = radius * 0.33f;  // 중앙 원 크기
        ringInnerR = radius * 0.52f;  // 링 안쪽 경계
        ringOuterR = radius * 0.98f;  // 링 바깥 경계
    }

    private int regionAt(float x, float y) {
        float dx = x - c.x;
        float dy = y - c.y;
        float r  = (float) Math.hypot(dx, dy);

        if (r <= centerR) return CENTER;
        if (r >= ringInnerR && r <= ringOuterR) {
            // atan2: 0° = 오른쪽, 90° = 아래, 시계방향 증가 (스크린 좌표계)
            double deg = Math.toDegrees(Math.atan2(dy, dx));
            if (deg < 0) deg += 360; // 0..360

            if (deg >= 45 && deg < 135)   return DOWN;
            if (deg >= 135 && deg < 225)  return LEFT;
            if (deg >= 225 && deg < 315)  return UP;
            return RIGHT; // 나머지(315~360, 0~45)
        }
        return NONE;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                pressedRegion = regionAt(e.getX(), e.getY());
                _pressedRegion.postValue(new Event<>(String.valueOf(pressedRegion)));
                return pressedRegion != NONE;

            case MotionEvent.ACTION_MOVE:
                // 필요하면 누른 영역 밖으로 나가면 취소 처리
                return true;

            case MotionEvent.ACTION_UP:
                int up = regionAt(e.getX(), e.getY());
                if (up == pressedRegion && listener != null) {
                    switch (up) {
                        case CENTER: listener.onCenter(); break;
                        case UP:     listener.onUp();     break;
                        case DOWN:   listener.onDown();   break;
                        case LEFT:   listener.onLeft();   break;
                        case RIGHT:  listener.onRight();  break;
                    }
                }

                if (up != CENTER) _pressEnd.postValue(new Event<>("end"));
                pressedRegion = NONE;
                return true;

            case MotionEvent.ACTION_CANCEL:
                pressedRegion = NONE;
                return true;
        }
        return super.onTouchEvent(e);
    }
}