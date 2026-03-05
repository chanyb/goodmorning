package kr.co.kworks.goodmorning.model.repository;

import android.database.Cursor;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import kr.co.kworks.goodmorning.model.business_logic.Location;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.Column;
import kr.co.kworks.goodmorning.utils.Database;

@Singleton
public class LocationRepository {
    private final Executor io = Executors.newSingleThreadExecutor();
    private CalendarHandler calendarHandler;
    private Database db;

    @Inject
    public LocationRepository() {
        calendarHandler = new CalendarHandler();
        db = new Database();
        removePastDataFromSqlite();
    }

    public boolean insertLocation(Location location) {
        return db.insert(Column.location, location.getContentValues()) > 0;
    }

    public boolean deleteLocation(Location location) {
        return db.delete(Column.location, Column.location_datetime + "=?", new String[]{location.datetime}) > 0;
    }

    public String getEmdKorean(android.location.Location location) {
        if(!isAttached("juso")) return "";
        Cursor cursor = db.getReadableDatabase().rawQuery(
            String.format(Locale.KOREA, "select c.ctp_kor_nm ||' ' || b.sig_kor_nm || ' ' || a.emd_kor_nm from juso.emd3 a inner join juso.sgg b on substr(a.emd_cd,1,5) = b.sig_cd inner join juso.sido c on substr(a.emd_cd,1,2) = c.ctprvn_cd WHERE a.minX <= %s AND a.maxX >= %s AND a.minY <= %s AND a.maxY >= %s;", location.getLongitude(), location.getLongitude(), location.getLatitude(), location.getLatitude()), null
        );

        if (cursor.moveToNext()) {
            return cursor.getString(0);
        }

        return "";
    }

    private boolean isAttached(String dbName) {
        Cursor cursor = db.getReadableDatabase().rawQuery("PRAGMA database_list;", null);
        while (cursor.moveToNext()) {
            int seq = cursor.getInt(0);        // DB 번호 (0 = main, 1 = temp, 2 이상 = attach된 DB)
            String name = cursor.getString(1); // DB 이름 (main, temp, sales 등)
            String file = cursor.getString(2); // 실제 파일 경로
            if (name.equals(dbName)) {
                return true;
            }
        }
        cursor.close();
        return false;
    }

    private void removePastDataFromSqlite() {
        Calendar before3days = calendarHandler.getCalendarAfter(-1*60*60*24*3);
        String datetimeString = calendarHandler.getDatetimeStringFromTimeMillis(before3days.getTimeInMillis());
        db.delete(Column.location, Column.location_datetime+"<=?", new String[]{datetimeString});
    }

    public Location getRecentLocationFromDB() {
        Cursor cursor = db.selectCursor(Column.location, Column.location_column_list, null, null, null, null,
            String.format(Locale.KOREA, "%s desc", Column.location_datetime), "1");

        if(cursor.moveToNext()) {
            Location location = new Location();
            location.datetime = cursor.getString(cursor.getColumnIndexOrThrow(Column.location_datetime));
            location.wgsX = cursor.getString(cursor.getColumnIndexOrThrow(Column.location_wgs_x));
            location.wgsY = cursor.getString(cursor.getColumnIndexOrThrow(Column.location_wgs_y));
            location.detect_datetime = cursor.getString(cursor.getColumnIndexOrThrow(Column.location_detect_time));
            location.speed = cursor.getString(cursor.getColumnIndexOrThrow(Column.location_speed));
            location.altitude = cursor.getString(cursor.getColumnIndexOrThrow(Column.location_altitude));
            location.direction = cursor.getString(cursor.getColumnIndexOrThrow(Column.location_direction));
            location.vehicle_code = cursor.getString(cursor.getColumnIndexOrThrow(Column.location_vehicle_code));
            location.router_ssid = cursor.getString(cursor.getColumnIndexOrThrow(Column.location_router_ssid));
            location.submit_yn = cursor.getString(cursor.getColumnIndexOrThrow(Column.location_submit_yn));
            return location;
        }

        return null;
    }

}
