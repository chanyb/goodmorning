package kr.co.kworks.goodmorning.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Locale;

public class Database extends SQLiteOpenHelper {
    private static final String DB_NAME = "forest_vehicle.db";
    private static final int DB_VERSION = 1;

    public Database() {
        super(GlobalApplication.getContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createDeviceInfoTable(db);
        createTokenTable(db);
        createLocationTable(db);
        createSensorTable(db);
        createAzimuthTable(db);
        createCameraStatusTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    private void createDeviceInfoTable(SQLiteDatabase db) {
        String sql = String.format(Locale.KOREA,"CREATE TABLE IF NOT EXISTS %s(" +
            "%s TEXT PRIMARY KEY," +
            "%s TEXT DEFAULT ''," +
            "%s TEXT DEFAULT ''," +
            "%s TEXT DEFAULT 'N'," +
            "%s TIMESTAMP DEFAULT CURRENT_TIMESTAMP);",
            Column.deviceInfo,
            Column.device_info_column_router_ssid,
            Column.device_info_column_vehicle_code,
            Column.device_info_column_vehicle_number,
            Column.device_info_column_video_relay_yn,
            Column.device_info_column_create_at
        );
        db.execSQL(sql);
    }

    private void createTokenTable(SQLiteDatabase db) {
        String sql = String.format(Locale.KOREA,"CREATE TABLE IF NOT EXISTS %s(" +
                "%s TEXT PRIMARY KEY," +
                "%s TEXT NOT NULL," +
                "%s TIMESTAMP DEFAULT CURRENT_TIMESTAMP);",
            Column.token,
            Column.token_access,
            Column.token_refresh,
            Column.token_create_at
        );
        db.execSQL(sql);
    }

    private void createLocationTable(SQLiteDatabase db) {
        String sql = String.format(Locale.KOREA,"CREATE TABLE IF NOT EXISTS %s(" +
                "%s TEXT PRIMARY KEY," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL);",
            Column.location,
            Column.location_datetime,
            Column.location_wgs_x,
            Column.location_wgs_y,
            Column.location_detect_time,
            Column.location_speed,
            Column.location_altitude,
            Column.location_direction,
            Column.location_vehicle_code,
            Column.location_router_ssid,
            Column.location_submit_yn
        );
        db.execSQL(sql);
    }

    private void createSensorTable(SQLiteDatabase db) {
        String sql = String.format(Locale.KOREA,"CREATE TABLE IF NOT EXISTS %s(" +
                "%s TEXT PRIMARY KEY," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL);",
            Column.sensor,
            Column.sensor_datetime,
            Column.sensor_airtemp_avg,
            Column.sensor_airtemp_in_runavg,
            Column.sensor_ws_runavg,
            Column.sensor_wd_runavg,
            Column.sensor_wd_gust,
            Column.sensor_ws_gust,
            Column.sensor_air_pressure_avg,
            Column.sensor_rh_avg,
            Column.sensor_rh_runavg,
            Column.sensor_batt_volt_min
        );
        db.execSQL(sql);
    }

    private void createAzimuthTable(SQLiteDatabase db) {
        String sql = String.format(Locale.KOREA,"CREATE TABLE IF NOT EXISTS %s(" +
                "%s TEXT PRIMARY KEY," +
                "%s TEXT NOT NULL);",
            Column.azimuth,
            Column.azimuth_datetime,
            Column.azimuth_heading
        );
        db.execSQL(sql);
    }

    private void createCameraStatusTable(SQLiteDatabase db) {
        String sql = String.format(Locale.KOREA,"CREATE TABLE IF NOT EXISTS %s(" +
                "%s TEXT PRIMARY KEY," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL," +
                "%s TEXT NOT NULL," +
                "%s TIMESTAMP DEFAULT CURRENT_TIMESTAMP);",
            Column.camera_status,
            Column.camera_status_datetime,
            Column.camera_status_pan,
            Column.camera_status_tilt,
            Column.camera_status_move_start,
            Column.camera_status_move_end,
            Column.camera_status_create_at
        );
        db.execSQL(sql);
    }

    public Cursor selectCursor(String tableName, String[] columns, String columnForWhereClause, String[] valueForWhereClause, String groupBy, String having, String orderBy, String limit) {
        Cursor c = getReadableDatabase().query(
            tableName,
            columns/* null == all column*/,
            columnForWhereClause /* column for where clause*/,
            valueForWhereClause/* value for where clause*/,
            groupBy,
            having,
            orderBy,
            limit);
        return c;
    }

    // 직접 작성하여 delete 하는 함수
    public long delete(String tableName, String whereClause, String[] whereArgs) {
        Log.d("db", "delete: " + tableName + " / " + whereClause + " " + whereArgs.toString());
        return getWritableDatabase().delete(tableName, whereClause, whereArgs); // success: bigger than 0, fail: 0
    }

    // ContentValue로 insert하는 함수
    public long insert(String tableName, ContentValues contentValues) {
        Log.d("db", "insert: " + tableName + " / " + contentValues.toString());
        return getWritableDatabase().insert(tableName, null, contentValues); // 성공 시 0보다 큰 정수, 실패 시 -1 반환
    }

    // 직접 작성하여 update하는 함수
    public int update(String tableName, ContentValues contentValue, String whereCluase, String[] whereArgs) {
        Log.d("db", "update: " + tableName + " / " + contentValue.toString());
        return getWritableDatabase().update(tableName, contentValue, whereCluase, whereArgs);
    }

}
