package kr.co.kworks.goodmorning.utils;

public class MathManager {
    private static MathManager instance;
    private MathManager() {}

    public static MathManager getInstance() {
        if(instance == null) instance = new MathManager();
        return instance;
    }

    /**
     * 두 좌표의 거리 계산 (거리 계산 후 Math.abs 사용)
     * @param y1
     * @param x1
     * @param y2
     * @param x2
     * @return killometer
     */
    public double getDistanceInKilometerByHaversine(double y1, double x1, double y2, double x2) {
        double radius = 6371; // 지구 반지름(km)
        double toRadian = Math.PI / 180;

        double deltaLatitude = Math.abs(x1 - x2) * toRadian;
        double deltaLongitude = Math.abs(y1 - y2) * toRadian;

        double sinDeltaLat = Math.sin(deltaLatitude / 2);
        double sinDeltaLng = Math.sin(deltaLongitude / 2);


        double a = sinDeltaLat * sinDeltaLat +
                        Math.cos(x1 * toRadian) * Math.cos(x2 * toRadian) * sinDeltaLng * sinDeltaLng;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance2 = 2 * radius * c;
        double squareRootA = Math.sqrt(a);
        double distance1 = 2 * radius * Math.asin(squareRootA);

        return (distance1+distance2)/2;
    }

    public double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        // 위도와 경도를 라디안으로 변환
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        // 경도의 차이
        double dLon = (lon2 - lon1);

        // 베어링 계산
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        double bearing = Math.atan2(y, x);

        // 라디안을 도로 변환
        bearing = Math.toDegrees(bearing);

        // 결과를 360으로 나눈 나머지를 반환 (결과가 음수일 경우 360을 더함)
        bearing = (bearing + 360) % 360;

        return bearing;
    }
}
