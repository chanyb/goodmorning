package kr.co.kworks.goodmorning.utils;

public class Column {
    private Column() {
    }

    /* device_info table */
    private SQLiteHandler deviceInfoTableHandler;
    public static String deviceInfo = "device_info";
    public static String device_info_column_router_ssid = "router_ssid";
    public static String device_info_column_vehicle_code = "vehicle_code";
    public static String device_info_column_vehicle_number = "vehicle_number";
    public static String device_info_column_video_relay_yn = "video_relay_yn";
    public static String device_info_column_create_at = "create_at";
    public static String[] device_info_column_list = new String[] {
        device_info_column_router_ssid, device_info_column_vehicle_code, device_info_column_vehicle_number, device_info_column_video_relay_yn, device_info_column_create_at
    };

    /* token table */
    public static String token = "token";
    public static String token_access = "access_token";
    public static String token_refresh = "refresh_token";
    public static String token_create_at = "create_at";
    public static String[] token_column_list = new String[] {
        token_access, token_refresh, token_create_at
    };

    /* Location */
    public static String location = "location";
    public static String location_datetime = "datetime";
    public static String location_wgs_x = "wgs_x";
    public static String location_wgs_y = "wgs_y";
    public static String location_detect_time = "detect_time";
    public static String location_speed = "speed";
    public static String location_altitude = "altitude";
    public static String location_direction = "direction";
    public static String location_vehicle_code = "vehicle_code";
    public static String location_router_ssid = "router_ssid";
    public static String location_submit_yn = "submit_yn";
    public static String[] location_column_list = new String[]{
        location_datetime, location_wgs_x, location_wgs_y, location_detect_time, location_speed, location_altitude, location_direction, location_vehicle_code, location_router_ssid, location_submit_yn
    };

    /* Sensor */
    public static String sensor = "sensor";
    public static String sensor_datetime = "datetime";
    public static String sensor_airtemp_avg = "airtemp_avg";
    public static String sensor_airtemp_in_runavg = "airtemp_in_runavg";
    public static String sensor_ws_runavg = "ws_runavg";
    public static String sensor_wd_runavg = "wd_runavg";
    public static String sensor_wd_gust = "wd_gust";
    public static String sensor_ws_gust = "ws_gust";
    public static String sensor_air_pressure_avg = "air_pressure_avg";
    public static String sensor_rh_avg = "rh_avg";
    public static String sensor_rh_runavg = "rh_runavg";
    public static String sensor_batt_volt_min = "batt_volt_min";
    public static String[] sensor_column_list = new String[]{
        sensor_datetime, sensor_airtemp_avg, sensor_airtemp_in_runavg, sensor_ws_runavg, sensor_wd_runavg, sensor_wd_gust, sensor_ws_gust, sensor_air_pressure_avg, sensor_rh_avg, sensor_rh_runavg, sensor_batt_volt_min
    };

    /* Azimuth */
    public static String azimuth = "azimuth";
    public static String azimuth_datetime = "datetime";
    public static String azimuth_heading = "heading";
    public static String[] azimuth_column_list = new String[]{
        azimuth_datetime, azimuth_heading
    };

    /* CameraStatus */
    public static String camera_status = "camera_status";
    public static String camera_status_datetime = "camera_status_datetime";
    public static String camera_status_create_at = "camera_status_create_at";
    public static String camera_status_pan = "camera_status_pan";
    public static String camera_status_tilt = "camera_status_tilt";
    public static String camera_status_move_start = "camera_status_move_start";
    public static String camera_status_move_end = "camera_status_move_end";
    public static String[] camera_status_column_list = new String[] {
        camera_status_datetime, camera_status_create_at, camera_status_pan, camera_status_tilt, camera_status_move_start, camera_status_move_end
    };

}
