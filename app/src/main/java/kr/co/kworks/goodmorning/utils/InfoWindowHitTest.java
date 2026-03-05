package kr.co.kworks.goodmorning.utils;

import android.graphics.PointF;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.InfoWindow;

public class InfoWindowHitTest {

    public enum Action {
        NONE,
        BUTTON_LEFT,
        BUTTON_RIGHT
    }

    public static class State {
        // 화면 기준 Rect들
        public final Rect infoWindowRectOnScreen = new Rect();
        public final Rect leftBtnRectOnScreen = new Rect();
        public final Rect rightBtnRectOnScreen = new Rect();

        // 유효 여부
        public boolean valid;
    }

    private final Rect tmp = new Rect();

    /**
     * InfoWindow + 버튼(2개)의 화면 Rect를 갱신한다.
     *
     * @param map          NaverMap
     * @param infoWindow   InfoWindow
     * @param iwPosition   InfoWindow가 표시되는 기준 좌표 (보통 marker.getPosition() 또는 infoWindow.getPosition())
     * @param rootW        InfoWindow root view measured width(px)
     * @param rootH        InfoWindow root view measured height(px)
     * @param anchorX      (0~1) 가로 앵커. 기본값 0.5f 권장 (가운데)
     * @param anchorY      (0~1) 세로 앵커. 기본값 1.0f 권장 (아래 꼭지점)
     * @param offsetXPx    화면 offset X(px)
     * @param offsetYPx    화면 offset Y(px)  (마커 위로 띄우려면 음수 방향이 아니라, 계산 방식상 여기서는 "추가로 이동" 개념)
     * @param leftBtnRectInRoot   루트뷰 기준 왼쪽 버튼 Rect (left/top/right/bottom)
     * @param rightBtnRectInRoot  루트뷰 기준 오른쪽 버튼 Rect
     */
    public void update(@NonNull NaverMap map,
                       @NonNull InfoWindow infoWindow,
                       @NonNull LatLng iwPosition,
                       int rootW, int rootH,
                       float anchorX, float anchorY,
                       int offsetXPx, int offsetYPx,
                       @NonNull Rect leftBtnRectInRoot,
                       @NonNull Rect rightBtnRectInRoot,
                       @NonNull State outState) {

        if (!infoWindow.isVisible() || rootW <= 0 || rootH <= 0) {
            outState.valid = false;
            return;
        }

        // InfoWindow 기준점(앵커가 가리키는 점)의 화면 좌표
        PointF p = map.getProjection().toScreenLocation(iwPosition);

        // 화면상 InfoWindow의 좌상단 계산
        int left = Math.round(p.x - (rootW * anchorX)) + offsetXPx;
        int top  = Math.round(p.y - (rootH * anchorY)) + offsetYPx;
        int right = left + rootW;
        int bottom = top + rootH;

        outState.infoWindowRectOnScreen.set(left, top, right, bottom);

        // 버튼 로컬 Rect들을 화면 기준으로 이동
        outState.leftBtnRectOnScreen.set(leftBtnRectInRoot);
        outState.leftBtnRectOnScreen.offset(left, top);

        outState.rightBtnRectOnScreen.set(rightBtnRectInRoot);
        outState.rightBtnRectOnScreen.offset(left, top);

        outState.valid = true;
    }

    /** 맵 클릭 포인트가 어떤 버튼인지 판정 */
    public Action hitTest(@NonNull PointF mapClickPoint, @NonNull State state) {
        if (!state.valid) return Action.NONE;

        int x = Math.round(mapClickPoint.x);
        int y = Math.round(mapClickPoint.y);

        if (state.leftBtnRectOnScreen.contains(x, y)) return Action.BUTTON_LEFT;
        if (state.rightBtnRectOnScreen.contains(x, y)) return Action.BUTTON_RIGHT;

        return Action.NONE;
    }
}
