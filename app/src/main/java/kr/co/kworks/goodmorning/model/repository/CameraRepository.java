package kr.co.kworks.goodmorning.model.repository;

import android.database.Cursor;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import kr.co.kworks.goodmorning.model.business_logic.CameraStatus;
import kr.co.kworks.goodmorning.model.business_logic.DeviceInfo;
import kr.co.kworks.goodmorning.model.network.CameraRequestInterface;
import kr.co.kworks.goodmorning.model.network.NetworkModule;
import kr.co.kworks.goodmorning.utils.ApiConstants;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.Column;
import kr.co.kworks.goodmorning.utils.Database;
import kr.co.kworks.goodmorning.utils.Logger;
import kr.co.kworks.goodmorning.utils.OnvifDeviceMgmt;
import kr.co.kworks.goodmorning.utils.OnvifMediaProfiles;
import kr.co.kworks.goodmorning.utils.OnvifPtzController;
import kr.co.kworks.goodmorning.utils.OnvifSoapClient;
import kr.co.kworks.goodmorning.view.RadialPadView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class CameraRepository {

    private final Executor io = Executors.newSingleThreadExecutor();
    private CameraRequestInterface api;
    private CalendarHandler calendarHandler;
    private String rtspUrl;
    private String user;
    private String pass;
    private Database db;

    @Inject
    public CameraRepository(@NetworkModule.Camera CameraRequestInterface cameraRequestInterface) {
        api = cameraRequestInterface;
        rtspUrl = ApiConstants.WONWOO_VIDEO_URL;
        user = "root";
        pass = "kworks0001819!!";
        db = new Database();
        calendarHandler = new CalendarHandler();
        removePastDataFromSqlite();
    }

    /** 비동기 콜백 */
    private void example(Consumer<DeviceInfo> callback) {
        io.execute(() -> {
            DeviceInfo deviceInfo = getDeviceInfoFromDB();
            callback.accept(deviceInfo);
        });
    }

    private DeviceInfo getDeviceInfoFromDB() {
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

            deviceInfo.routerSsid = routerSsid;
            deviceInfo.vehicleCode = vehicleCode;
            deviceInfo.vehicleNumber = vehicleNumber;

            return deviceInfo;
        }

        return null;
    }

//    public void getSysCommand(Consumer<SysCommandResponse> callback, Consumer<Throwable> error) {
//        SysCommandRequest sysCommandRequest = new SysCommandRequest(new SysCommandRequest.JsonData(new SysCommandRequest.Data()));
//        Call<SysCommandResponse> callSysCommand = api.getSysCommand(sysCommandRequest);
//        callSysCommand.enqueue(new Callback<SysCommandResponse>() {
//            @Override
//            public void onResponse(Call<SysCommandResponse> call, Response<SysCommandResponse> response) {
//                callback.accept(response.body());
//            }
//
//            @Override
//            public void onFailure(Call<SysCommandResponse> call, Throwable throwable) {
//                error.accept(throwable);
//            }
//        });
//    }

    public void setPtz2(int direction) {
        io.execute(() -> {
            try {
                int i = rtspUrl.indexOf("@");
                int j = rtspUrl.indexOf(":554");
                String ip = rtspUrl.substring(i+1,j);
                String onvifUrl = String.format("http://%s:80/onvif/device_service", ip);
                OnvifPtzController ptz = getOnvifPtzController2(onvifUrl, user, pass);

                switch (direction) {
                    case RadialPadView.UP -> {
                        ptz.continuousMove2(0.0, +0.6, null, 2);
                    }
                    case RadialPadView.LEFT -> {
                        ptz.continuousMove2(-0.6, 0.0, null, 2);
                    }
                    case RadialPadView.RIGHT -> {
                        ptz.continuousMove2(+0.6, 0.0, null, 2);
                    }
                    case RadialPadView.DOWN ->  {
                        ptz.continuousMove2(0.0, -0.6, null, 2);
                    }
                    case RadialPadView.CENTER -> {
                        ptz.absoluteMove2(ptz.parsePanValue(0), -1.0, 0.0);
                    }
                    default -> {
                        ptz.continuousMove2(0.0, 0.0, null, 2);
                    }

                }

            } catch(Exception e) {
                Logger.getInstance().error("setPtz2: ", e);
            }
        });
    }

    @NonNull
    private OnvifPtzController getOnvifPtzController(String deviceUrl, String user, String pass) throws Exception {
        OnvifSoapClient soap = new OnvifSoapClient(user, pass);

        // (A) Media XAddr 확보
        OnvifDeviceMgmt dev = new OnvifDeviceMgmt(soap, deviceUrl);
        OnvifDeviceMgmt.MediaAddrs addrs = dev.getMediaServiceAddrs();

        // (B) 프로필 토큰 확보 (Media2 우선)
        OnvifMediaProfiles media = new OnvifMediaProfiles(soap);
        String profileToken = media.getFirstProfileToken(addrs.media2, addrs.media1);

        // (D) PTZ 제어
        OnvifPtzController ptz = new OnvifPtzController(soap, deviceUrl, profileToken);
        return ptz;
    }

    @NonNull
    private OnvifPtzController getOnvifPtzController2(String onvifUrl, String user, String pass) throws Exception {
        OnvifSoapClient soap = new OnvifSoapClient(user, pass);

        // (A) Media XAddr 확보
        OnvifDeviceMgmt dev = new OnvifDeviceMgmt(soap, onvifUrl);
        OnvifDeviceMgmt.MediaAddrs addrs = dev.getMediaServiceAddrs();

        // (B) 프로필 토큰 확보 (Media2 우선)
        OnvifMediaProfiles media = new OnvifMediaProfiles(soap);
        String profileToken = media.getFirstProfileToken2(addrs.media2, addrs.media1);

        // (D) PTZ 제어
        OnvifPtzController ptz = new OnvifPtzController(soap, onvifUrl, profileToken);
        return ptz;
    }
//
//    public void setPtz(int ptzNumber, int panSpeed, int tiltSpeed, int zoomSpeed) {
//        Call<ResponseBody> callGetTest = api.setPtz(ptzNumber, panSpeed, tiltSpeed, zoomSpeed);
//        callGetTest.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                Logger.getInstance().info("onResponse");
//                Logger.getInstance().info(response.isSuccessful() ? "success" : "fail");
//                Logger.getInstance().info("code: " + response.code());
//                Logger.getInstance().info("message: " + response.message());
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
//                Logger.getInstance().error("setPtz.onFailure(CameraRepository)", throwable);
//            }
//        });
//    }
//
//    public void setDefaultPtz() {
//        Call<ResponseBody> callGetTest = api.setAbsolutePtz(1, "0,0,1");
//        callGetTest.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                Logger.getInstance().info("setDefaultPtz: onResponse");
//                Logger.getInstance().info(response.isSuccessful() ? "success" : "fail");
//                Logger.getInstance().info("code: " + response.code());
//                Logger.getInstance().info("message: " + response.message());
//                Logger.getInstance().info("response.body: " + response.body().toString());
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
//                Logger.getInstance().error("setDefaultPtz: onFailure", throwable);
//            }
//        });
//    }

    public void setPtz(int direction) {
        setPtz2(direction);
    }

//    private void setWonwooPtz(int direction) {
//        Logger.getInstance().info("setWonwooPtz");
//        switch (direction) {
//            case RadialPadView.UP:
//                setPtz(1, 0, 64, 0);
//                break;
//            case RadialPadView.DOWN:
//                setPtz(1, 0, -64, 0);
//                break;
//            case RadialPadView.LEFT:
//                setPtz(1, -64, 0, 0);
//                break;
//            case RadialPadView.RIGHT:
//                setPtz(1, 64, 0, 0);
//                break;
//            case RadialPadView.CENTER:
//                break;
//            default:
//                setPtz(1, 0, 0, 0);
//        }
//    }
//
//    private void setUbitronPtz(int direction) {
//        int i = ApiConstants.UBITRON_URL.indexOf("@");
//        int j = ApiConstants.UBITRON_URL.indexOf(":554");
//        String ip = ApiConstants.UBITRON_URL.substring(i+1,j);
//
//        setPtz2(
//            String.format("http://%s:80/onvif/device_service", ip),
//            ApiConstants.CURRENT_PLAY_URL.equals(ApiConstants.UBITRON_URL) ? "admin":"root",
//            "kworks0001819!!",
//            direction);
//    }

    private String parseIp(String rtspUrl) {
        int i = rtspUrl.indexOf("@");
        int j = rtspUrl.indexOf(":554");
        return rtspUrl.substring(i+1,j);
    }

    @NonNull
    private String getProfileToken(String onvifUrl) throws Exception {
        OnvifSoapClient soap = new OnvifSoapClient(user, pass);

        // (A) Media XAddr 확보
        OnvifDeviceMgmt dev = new OnvifDeviceMgmt(soap, onvifUrl);
        OnvifDeviceMgmt.MediaAddrs addrs = dev.getMediaServiceAddrs();

        // (B) 프로필 토큰 확보 (Media2 우선)
        OnvifMediaProfiles media = new OnvifMediaProfiles(soap);
        return media.getFirstProfileToken2(addrs.media2, addrs.media1);
    }

    public String getStatus() throws Exception {
        OnvifSoapClient soap = new OnvifSoapClient(user, pass);

        String body = String.format(Locale.KOREA, ""
            + "<tptz:GetStatus>"
            + "<tptz:ProfileToken>%s</tptz:ProfileToken>"
            + "</tptz:GetStatus>", getProfileToken(getOnvifUrlFromRtspUrl(rtspUrl)));
        return soap.postSoap2(getOnvifUrlFromRtspUrl(rtspUrl), body);
    }

    public String getOnvifUrlFromRtspUrl(String rtspUrl) {
        int i = rtspUrl.indexOf("@");
        int j = rtspUrl.indexOf(":554");
        String ip = rtspUrl.substring(i+1,j);
        return String.format("http://%s:80/onvif/device_service", ip);
    }

    public void setSystemDateAndTime() {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int year = calendarHandler.getYear(utc);
        int month = calendarHandler.getMonth(utc);
        int day = calendarHandler.getDay(utc);
        int hourOf24 = calendarHandler.getHourOf24(utc);
        int minute = calendarHandler.getMinute(utc);
        int second = calendarHandler.getSecond(utc);
        OnvifSoapClient soap = new OnvifSoapClient(user, pass);
        String body = String.format(Locale.KOREA, ""
            + "<tds:SetSystemDateAndTime>"
                + "<tds:DateTimeType>Manual</tds:DateTimeType>"
                + "<tds:DaylightSavings>false</tds:DaylightSavings>"
                + "<tds:TimeZone>"
                    + "<tt:TZ>UTC+09:00</tt:TZ>"
                + "</tds:TimeZone>"
                + "<tds:UTCDateTime>"
                    + "<tt:Date>"
                        + "<tt:Year>%d</tt:Year>"
                        + "<tt:Month>%d</tt:Month>"
                        + "<tt:Day>%d</tt:Day>"
                    + "</tt:Date>"
                    + "<tt:Time>"
                        + "<tt:Hour>%d</tt:Hour>"
                        + "<tt:Minute>%d</tt:Minute>"
                        + "<tt:Second>%d</tt:Second>"
                    + "</tt:Time>"
                + "</tds:UTCDateTime>"
            + "</tds:SetSystemDateAndTime>"
            , year, month, day, hourOf24, minute, second
        );

        Thread thread = new Thread(() -> {
            try {
                soap.postSoap2(getOnvifUrlFromRtspUrl(rtspUrl), body);
            } catch (Exception e) {
                Logger.getInstance().error("setSystemDateAndTime-Thread:", e);
            }
        });
        thread.start();
    }

    public CompletableFuture<String> getStatusAsync() throws Exception {
        OnvifSoapClient soap = new OnvifSoapClient(user, pass);
        String body = String.format(Locale.KOREA, ""
        + "<tptz:GetStatus>"
            + "<tptz:ProfileToken>%s</tptz:ProfileToken>"
        + "</tptz:GetStatus>"
        , getProfileToken(getOnvifUrlFromRtspUrl(rtspUrl))
        );

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = getOnvifUrlFromRtspUrl(rtspUrl);
                return soap.postSoap2(url, body);
            } catch (Exception e) {
                Logger.getInstance().error("getSystemDateAndTimeAsync failed", e);
                return null;
            }
        }, io);
    }

    public void getPositionFromCgi(Consumer<Response<ResponseBody>> callback, Consumer<Throwable> error) {
        Call<ResponseBody> callSysCommand = api.getPtzPosition();
        callSysCommand.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                callback.accept(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                error.accept(throwable);
            }
        });
    }

    public String createCameraStatus() {
        String datetime = calendarHandler.getCurrentDatetimeString();
        CameraStatus cameraStatus = new CameraStatus(datetime);
        cameraStatus.datetime = datetime;
        db.insert(Column.camera_status, cameraStatus.getContentValues());
        return datetime;
    }

    public long insertCameraStatus(CameraStatus cameraStatus) {
        return db.insert(Column.camera_status, cameraStatus.getContentValues());
    }

    public CameraStatus getRecentCameraStatusFromDB() {
        Cursor cursor = db.selectCursor(Column.camera_status, null, Column.camera_status_pan+" IS NOT NULL AND "+Column.camera_status_pan +" <> ''", null, null, null,
            String.format(Locale.KOREA, "%s desc", Column.camera_status_datetime), "1");

        if(cursor.moveToNext()) {
            CameraStatus cameraStatus = new CameraStatus();
            cameraStatus.datetime = cursor.getString(cursor.getColumnIndexOrThrow(Column.camera_status_datetime));
            cameraStatus.pan = cursor.getString(cursor.getColumnIndexOrThrow(Column.camera_status_pan));
            cameraStatus.tilt = cursor.getString(cursor.getColumnIndexOrThrow(Column.camera_status_tilt));
            cameraStatus.moveStart = cursor.getString(cursor.getColumnIndexOrThrow(Column.camera_status_move_start));
            cameraStatus.moveEnd = cursor.getString(cursor.getColumnIndexOrThrow(Column.camera_status_move_end));
            return cameraStatus;
        }

        return null;
    }

    private void removePastDataFromSqlite() {
        Calendar before10minutes = calendarHandler.getCalendarAfter(-1*60*10); // 10분 전
        String datetimeString = calendarHandler.getDatetimeStringFromTimeMillis(before10minutes.getTimeInMillis());
        db.delete(Column.camera_status, Column.camera_status_datetime+"<=?", new String[]{datetimeString});
    }
}
