package kr.co.kworks.goodmorning.model.repository;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import kr.co.kworks.goodmorning.model.business_logic.Alert;
import kr.co.kworks.goodmorning.model.business_logic.DeviceInfo;
import kr.co.kworks.goodmorning.model.business_logic.Token;
import kr.co.kworks.goodmorning.model.network.NetworkModule;
import kr.co.kworks.goodmorning.model.network.RequestInterface;
import kr.co.kworks.goodmorning.model.request.SetVehicleDeviceMappingRequest;
import kr.co.kworks.goodmorning.model.response.BaseResponse;
import kr.co.kworks.goodmorning.model.response.SetVehicleDeviceMappingResponseBody;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.Column;
import kr.co.kworks.goodmorning.utils.Database;
import kr.co.kworks.goodmorning.utils.Logger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class DeviceInfoRepository {
    private final Executor io = Executors.newSingleThreadExecutor();
    private RequestInterface integrateSystem;
    private CalendarHandler calendarHandler;
    private Database db;

    @Inject
    public DeviceInfoRepository(@NetworkModule.IntegrateSystem RequestInterface integrateSystem) {
        this.integrateSystem = integrateSystem;
        calendarHandler = new CalendarHandler();
        db = new Database();
    }

    /** 비동기 콜백 */
    public void getDeviceInfo(Consumer<DeviceInfo> callback) {
        io.execute(() -> {
            DeviceInfo deviceInfo = getDeviceInfoFromDB();
            callback.accept(deviceInfo);
        });
    }

    public DeviceInfo getDeviceInfoFromDB() {
        Cursor cursor = db.selectCursor(
            Column.deviceInfo,
            Column.device_info_column_list,
            null,
            null,
            null,
            null,
            String.format(Locale.KOREA, "%s desc", Column.device_info_column_create_at),
            "1"
        );

        if (cursor.moveToNext()) {
            DeviceInfo deviceInfo = new DeviceInfo();
            String routerSsid = cursor.getString(cursor.getColumnIndexOrThrow(Column.device_info_column_router_ssid));
            String vehicleCode = cursor.getString(cursor.getColumnIndexOrThrow(Column.device_info_column_vehicle_code));
            String vehicleNumber = cursor.getString(cursor.getColumnIndexOrThrow(Column.device_info_column_vehicle_number));
            String videoRelayYn = cursor.getString(cursor.getColumnIndexOrThrow(Column.device_info_column_video_relay_yn));

            deviceInfo.routerSsid = routerSsid;
            deviceInfo.vehicleCode = vehicleCode;
            deviceInfo.vehicleNumber = vehicleNumber;
            deviceInfo.videoRelayYn = videoRelayYn;

            return deviceInfo;
        }

        return null;
    }

    public boolean deleteDeviceInfoFromDatabase() {
        return db.delete(Column.deviceInfo, null, new String[]{}) > 0;
    }

    public String getSsidFromDatabase() {
        Cursor cursor = db.selectCursor(
            Column.deviceInfo,
            Column.device_info_column_list,
            null,
            null,
            null,
            null,
            String.format(Locale.KOREA, "%s desc", Column.device_info_column_create_at),
            "1"
        );

        if (cursor.moveToNext()) {
            return cursor.getString(cursor.getColumnIndexOrThrow(Column.device_info_column_router_ssid));
        }

        return null;
    }

    public boolean saveVehicleNumberToDatabase(String vehicleCode, String vehicleNumber) {
        DeviceInfo deviceInfo = getDeviceInfoFromDB();
        deviceInfo.vehicleCode = vehicleCode;
        deviceInfo.vehicleNumber = vehicleNumber;
        return update(deviceInfo);
    }

    public boolean update(DeviceInfo deviceInfo) {
        long result = db.update(
            Column.deviceInfo,
            deviceInfo.getContentValues(),
            Column.device_info_column_router_ssid +"=?",
            new String[]{deviceInfo.routerSsid}
        );
        return result > 0;
    }

    public void setVehicleDeviceMapping(Token token, String deviceCode, String vehicleNumber, boolean force, Consumer<Response<BaseResponse<SetVehicleDeviceMappingResponseBody>>> callback) {
        SetVehicleDeviceMappingRequest request = new SetVehicleDeviceMappingRequest();
        request.deviceCode = deviceCode;
        request.vehicleNumber = vehicleNumber;
        request.force = force ? "Y":"N";

        Call<BaseResponse<SetVehicleDeviceMappingResponseBody>> callSetVehicleDeviceMapping = integrateSystem.setVehicleDeviceMapping(token.accessToken, calendarHandler.getCurrentDatetimeString(), request);
        callSetVehicleDeviceMapping.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<BaseResponse<SetVehicleDeviceMappingResponseBody>> call, Response<BaseResponse<SetVehicleDeviceMappingResponseBody>> response) {
                callback.accept(response);
            }

            @Override
            public void onFailure(Call<BaseResponse<SetVehicleDeviceMappingResponseBody>> call, Throwable throwable) {
                Logger.getInstance().error("setVehicleDeviceMapping.onFailure", throwable);
                Alert alert = new Alert("통신 실패: " + throwable);
            }
        });
    }

    public boolean saveSsidToDatabase(String ssid) {
        if(isSsidDuplicate(ssid)) return true;
        if(!isSsidValid(ssid)) return false;

        ContentValues cv = new ContentValues();
        cv.put(Column.device_info_column_router_ssid, ssid);
        long success = db.insert(Column.deviceInfo, cv);
        return success > 0;
    }

    private boolean isSsidDuplicate(String ssid) {
        Cursor cursor = db.selectCursor(
            Column.deviceInfo,
            Column.device_info_column_list,
            Column.device_info_column_router_ssid +"=?",
            new String[]{ssid},
            null,
            null,
            null,
            null
        );

        if (cursor.moveToNext()) {
            return true;
        }
        return false;
    }

    public boolean isSsidValid(String ssid) {
        if (ssid == null || ssid.isEmpty()) return false;
        if (!ssid.startsWith("KW_")) return false;
        if (ssid.length() != 10) return false;
        return true;
    }
}
