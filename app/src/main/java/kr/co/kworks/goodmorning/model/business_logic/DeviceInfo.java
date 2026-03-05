package kr.co.kworks.goodmorning.model.business_logic;

import android.content.ContentValues;

import kr.co.kworks.goodmorning.utils.Column;

public class DeviceInfo {
    public String routerSsid;
    public String vehicleCode;
    public String vehicleNumber;
    public String videoRelayYn;

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Column.device_info_column_router_ssid, routerSsid);
        contentValues.put(Column.device_info_column_vehicle_code, vehicleCode);
        contentValues.put(Column.device_info_column_vehicle_number, vehicleNumber);
        contentValues.put(Column.device_info_column_video_relay_yn, videoRelayYn);
        return contentValues;
    }
}
