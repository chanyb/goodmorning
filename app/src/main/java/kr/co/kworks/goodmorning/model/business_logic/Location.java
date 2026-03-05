package kr.co.kworks.goodmorning.model.business_logic;

import android.content.ContentValues;

import kr.co.kworks.goodmorning.utils.Column;

public class Location {
    public String datetime;
    public String wgsX;
    public String wgsY;
    public String detect_datetime;
    public String speed;
    public String altitude;
    public String direction;
    public String vehicle_code;
    public String router_ssid;
    public String submit_yn;

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Column.location_datetime, datetime);
        contentValues.put(Column.location_wgs_x, wgsX);
        contentValues.put(Column.location_wgs_y, wgsY);
        contentValues.put(Column.location_detect_time, detect_datetime);
        contentValues.put(Column.location_speed, speed);
        contentValues.put(Column.location_altitude, altitude);
        contentValues.put(Column.location_direction, direction);
        contentValues.put(Column.location_vehicle_code, vehicle_code);
        contentValues.put(Column.location_router_ssid, router_ssid);
        contentValues.put(Column.location_submit_yn, submit_yn);
        return contentValues;
    }
}
