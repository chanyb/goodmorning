package kr.co.kworks.goodmorning.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import kr.co.kworks.goodmorning.utils.SecurityManager;
import kr.co.kworks.goodmorning.R;
import kr.co.kworks.goodmorning.activity.IntroActivity;
import kr.co.kworks.goodmorning.model.business_logic.Alert;
import kr.co.kworks.goodmorning.model.business_logic.Azimuth;
import kr.co.kworks.goodmorning.model.business_logic.CameraStatus;
import kr.co.kworks.goodmorning.model.business_logic.Cr350;
import kr.co.kworks.goodmorning.model.business_logic.DeviceInfo;
import kr.co.kworks.goodmorning.model.business_logic.Queue;
import kr.co.kworks.goodmorning.model.business_logic.Token;
import kr.co.kworks.goodmorning.model.business_logic.WebSocketCommand;
import kr.co.kworks.goodmorning.model.network.ByteSocketServer;
import kr.co.kworks.goodmorning.model.network.NetworkModule;
import kr.co.kworks.goodmorning.model.repository.AzimuthRepository;
import kr.co.kworks.goodmorning.model.repository.CameraRepository;
import kr.co.kworks.goodmorning.model.repository.DeviceInfoRepository;
import kr.co.kworks.goodmorning.model.repository.LocationRepository;
import kr.co.kworks.goodmorning.model.repository.SensorRepository;
import kr.co.kworks.goodmorning.model.repository.TokenRepository;
import kr.co.kworks.goodmorning.model.request.SetVehicleRecptnDataRequest;
import kr.co.kworks.goodmorning.model.response.BaseResponse;
import kr.co.kworks.goodmorning.model.response.SetVehicleRecptnDataResponse;
import kr.co.kworks.goodmorning.utils.ApiConstants;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.LocationManagerHandler;
import kr.co.kworks.goodmorning.utils.Logger;
import kr.co.kworks.goodmorning.utils.MathManager;
import kr.co.kworks.goodmorning.utils.PreferenceHandler;
import kr.co.kworks.goodmorning.model.network.RequestInterface;
import kr.co.kworks.goodmorning.utils.SensorManagerHandler;
import kr.co.kworks.goodmorning.utils.SoapXmlParser;
import kr.co.kworks.goodmorning.utils.Utils;
import kr.co.kworks.goodmorning.view.RadialPadView;
import kr.co.kworks.goodmorning.viewmodel.GlobalViewModel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class LocationService extends LifecycleService {
    private Handler sensorDataTransferHandler;
    private SecurityManager securityManager;
    private NotificationCompat.Builder builder;
    private CalendarHandler calendarHandler;
    private PreferenceHandler preferenceHandler;

    private GlobalViewModel global;
    private LocationManagerHandler locationManagerHandler;
    private Observer locationObserver;
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> webSocketScheduled, sensorDataCollectScheduled, saveHeadingDegreeScheduled, locationScheduled, checkCameraStatusScheduled;
    private DeviceInfo deviceInfo;
    private WebSocket ws;
    private OkHttpClient client;
    private ByteSocketServer byteSocketServer;
    private Handler mHandler;

    private SensorManagerHandler sensorManagerHandler;
    private Queue azimuthQueue;

    private MutableLiveData<Location> locationMutableLiveData;

    private CameraStatus recentCameraStatus;

    @Inject
    @NetworkModule.IntegrateSystem
    RequestInterface integrateSystem;

    @Inject
    TokenRepository tokenRepository;

    @Inject
    DeviceInfoRepository deviceInfoRepository;

    @Inject
    CameraRepository cameraRepository;

    @Inject
    SensorRepository sensorRepository;

    @Inject
    LocationRepository locationRepository;

    @Inject
    AzimuthRepository azimuthRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        startScheduled();
    }

    @Override
    public int onStartCommand(Intent    intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        generateForegroundNotification();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForeground(Service.STOP_FOREGROUND_REMOVE);
        release();
        super.onDestroy();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return null;
    }

    private void init() {
        mHandler = new Handler(Looper.getMainLooper());
        sensorDataTransferHandler = new Handler(Looper.getMainLooper());
        securityManager = new SecurityManager(this);
        preferenceHandler = new PreferenceHandler(this);
        calendarHandler = new CalendarHandler();
        executor = Executors.newScheduledThreadPool(4);
        locationManagerHandler = new LocationManagerHandler(this);
        locationMutableLiveData = new MutableLiveData<>();
        locationObserver = loc -> {
            Location location = (Location) loc;
            if (locationMutableLiveData.getValue() != null) {
                double diffDistance = getDistance(locationMutableLiveData.getValue(), location);
                if (diffDistance < 0.01) {
                    float beforeBearing = locationMutableLiveData.getValue().getBearing();
                    location.setBearing(beforeBearing);
                }
            }

            if (location.hasSpeedAccuracy()) {
                float error = location.getSpeedAccuracyMetersPerSecond();
                float speed = location.getSpeed();
                float fixed_speed = speed - error;
                if(fixed_speed < 0) fixed_speed = 0f;
                location.setSpeed(fixed_speed);
            }

            locationMutableLiveData.postValue(location);
        };

        client = new OkHttpClient();

        byteSocketServer = new ByteSocketServer(7833);
        byteSocketServer.start();

        byteSocketServer.setListener(new ByteSocketServer.ServerListener() {
            @Override
            public void onImage(SocketChannel ch, byte[] image) {
                ByteSocketServer.ServerListener.super.onImage(ch, image);
            }
        });

        sensorManagerHandler = new SensorManagerHandler(this);
    }

    /**
     * Foreground 알림 생성
     */
    private void generateForegroundNotification() {
        Intent intent = new Intent(this, IntroActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        builder = new NotificationCompat.Builder(this, Utils.LOCATION_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_svg)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentText(getString(R.string.location_service_foreground_message))
            .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(Utils.WALK_DETECT_FOREGROUND_NOTIFICATION_ID, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(Utils.WALK_DETECT_FOREGROUND_NOTIFICATION_ID, builder.build());
        }
    }

    private void startScheduled() {
        Logger.getInstance().info("startScheduled()");
        deviceInfoRepository.getDeviceInfo(deviceInfo -> this.deviceInfo = deviceInfo);
        setLocationListener();
        scheduleWebSocket();
        startSensorDataCollect();
        registerSensors();
        startSaveHeadingScheduled();
        startLocationScheduled();
        startCheckCameraStatus();
    }


    public String encryptRSA(String value) {
//        return securityManager.encryptRSA(securityManager.getServerPublicKey(keyRepository.getValue(Database.title_server_public)), value);
        return "";
    }

    private String decryptRSA(String value) {
//        return securityManager.decryptRSA(value, keyRepository.getValue(Database.title_client_private));
        return "";
    }

    private void release() {
        removeLocationListener();
        stopWebsocketScheduled();
        stopSensorDataCollect();
        sensorManagerHandler.unregisterListeners();
        stopSaveHeadingScheduled();
        stopLocationScheduled();
        stopCheckCameraStatus();
    }

    private void setVehicleRecptnData(DeviceInfo deviceInfo, Location location) {
        if(deviceInfo.vehicleCode == null || deviceInfo.vehicleCode.isEmpty()) {
            Logger.getInstance().info("setVehicleRecptnData deviceInfo.vehicleCode null or empty");
            return;
        }
        if(deviceInfo.routerSsid == null || deviceInfo.routerSsid.isEmpty()) {
            Logger.getInstance().info("setVehicleRecptnData deviceInfo.vehicleCode null or empty");
            return;
        }

        SetVehicleRecptnDataRequest request = new SetVehicleRecptnDataRequest();
        request.date = calendarHandler.getCurrentDatetimeString().substring(0, 8);
        request.time = calendarHandler.getCurrentDatetimeString().substring(8, 14);
        request.realGpsDatetime = calendarHandler.getDatetimeStringFromTimeMillis(location.getTime());
        request.vehicleCode = deviceInfo.vehicleCode;
        request.longitude = String.valueOf(location.getLongitude());
        request.latitude = String.valueOf(location.getLatitude());
        request.speed = String.valueOf(location.getSpeed()*3.6);
        request.altitude = String.valueOf(location.getAltitude());
        request.direction = String.valueOf(location.getBearing());

        request.routerSsid = deviceInfo.routerSsid;

        Token token = tokenRepository.getTokenSync();
        Cr350 cr350 = sensorRepository.getRecentSensorDataFromDB();
        if (cr350 != null) {
            request.humidity = cr350.rhRunAvg;
            request.temperature = cr350.airtempInRunAvg;
            request.weatherDatetime = cr350.datetime;
            request.windDirection = String.valueOf(getRecentWindDirection());
            request.windSpeed = cr350.wsRunAvg;
        }

        Call<BaseResponse<SetVehicleRecptnDataResponse>> callGetTest = integrateSystem.setVehicleRecptnData(token.accessToken, calendarHandler.getCurrentDatetimeString(), request);
        callGetTest.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<BaseResponse<SetVehicleRecptnDataResponse>> call, Response<BaseResponse<SetVehicleRecptnDataResponse>> response) {
                if (response.isSuccessful()) {
                    String resultCode = response.body().header.resultCode;
                    if (resultCode.equals("00")) {
//                        Logger.getInstance().info("setVehicleRecptnData success");
                    } else {
                        Logger.getInstance().error(String.format(Locale.KOREA, "오류(%s)%s", resultCode, response.body().header.resultMessage), null);
                        Alert alert = new Alert(String.format(Locale.KOREA, "오류(%s)%s", resultCode, response.body().header.resultMessage));
                        if (resultCode.equals("02")) {
                            tokenRepository.getAutoRefresh(t -> {});
                        }
                    }
                } else {
                    try {
                        Alert alert = new Alert(String.format(Locale.KOREA, "fail: %s", response.errorBody().string()));
                    } catch (IOException e) {
                        Logger.getInstance().error("setVehicleRecptnData fail..", null);
                    }

                }
            }

            @Override
            public void onFailure(Call<BaseResponse<SetVehicleRecptnDataResponse>> call, Throwable throwable) {
                Logger.getInstance().error("setVehicleRecptnData.onFailure", throwable);
            }
        });
    }

    private void scheduleWebSocket() {
        stopWebsocketScheduled();
        webSocketScheduled = executor.scheduleWithFixedDelay(() -> {
            try {
                if(deviceInfo == null) return;
                initWebSocket();
            } catch(Exception e) {
                Logger.getInstance().error("scheduleWebSocket", e);
            }
        },0, 10_000, TimeUnit.MILLISECONDS);
    }

    private void stopWebsocketScheduled() {
        if(webSocketScheduled != null && !webSocketScheduled.isCancelled()) {
            webSocketScheduled.cancel(true);
            Logger.getInstance().info("stopWebsocketScheduled()");
        }
    }

    private void initWebSocket() {
        String wssUrl = String.format("wss://%s:443/ws/device?vehicle_code=%s&type=device", ApiConstants.INTEGRATE_SERVER_DOMAIN, deviceInfo.vehicleCode);
        Request request = new Request.Builder()
            .url(wssUrl)
            .build();

        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {
                super.onOpen(webSocket, response);
                Logger.getInstance().info("WebSocketListener Open");
            }
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Gson gson = new Gson();
                WebSocketCommand command = gson.fromJson(text, WebSocketCommand.class);

                Logger.getInstance().info("onMessage: " + text);
                switch (command.command) {
                    case "00" -> {
                        cameraRepository.setPtz(RadialPadView.UP);
                        mHandler.postDelayed(() -> {
                            cameraRepository.setPtz(RadialPadView.NONE);
                        }, 2000);
                    }
                    case "01" -> {
                        cameraRepository.setPtz(RadialPadView.RIGHT);
                        mHandler.postDelayed(() -> {
                            cameraRepository.setPtz(RadialPadView.NONE);
                        }, 2000);
                    }
                    case "02" -> {
                        cameraRepository.setPtz(RadialPadView.DOWN);
                        mHandler.postDelayed(() -> {
                            cameraRepository.setPtz(RadialPadView.NONE);
                        }, 2000);
                    }
                    case "03" -> {
                        cameraRepository.setPtz(RadialPadView.LEFT);
                        mHandler.postDelayed(() -> {
                            cameraRepository.setPtz(RadialPadView.NONE);
                        }, 2000);
                    }
                    case "04" -> { // center
                        cameraRepository.setPtz(RadialPadView.CENTER);
                    }
                }

                switch (command.command) {
                    case "05" -> {
                        command.pan = "0";
                        command.tilt = "0";
                        if (recentCameraStatus != null) {
                            command.pan = recentCameraStatus.pan;
                            command.tilt = recentCameraStatus.tilt;
                        }

                        command.returnCode = "00";
                        mHandler.post(() -> {
                            ws.send(gson.toJson(command));
                        });
                    }
                    default -> {
                        mHandler.postDelayed(() -> {
                            command.returnCode = "00";
                            ws.send(gson.toJson(command));
                        }, 5000);
                    }
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                Logger.getInstance().error("websocket fail", null);
                ws = null;
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosed(webSocket, code, reason);
                Logger.getInstance().info("webSocket onClosed");
                ws = null;
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosing(webSocket, code, reason);
                Logger.getInstance().info("webSocket onClosing");
                ws = null;
            }
        };

        if(ws == null) {
            try {
                Logger.getInstance().info("webSocket ws == null");
                ws = client.newWebSocket(request, listener);
            } catch(Exception e) {
                Logger.getInstance().error("websocket connect error", e);
            }

        }

//        client.dispatcher().executorService().shutdown();
    }

    private void startSensorDataCollect() {
        stopSensorDataCollect();
        sensorDataCollectScheduled = executor.scheduleWithFixedDelay(() -> {
            sensorRepository.getSensorDataFromServer();
        }, 0, 10_000, TimeUnit.MILLISECONDS);
    }

    private void stopSensorDataCollect() {
        if (sensorDataCollectScheduled != null && !sensorDataCollectScheduled.isCancelled()) {
            sensorDataCollectScheduled.cancel(true);
            Logger.getInstance().info("stopSensorDataCollect()");
        }
    }

    private void saveLocationToDatabase(Location location) {
        kr.co.kworks.goodmorning.model.business_logic.Location loc = new kr.co.kworks.goodmorning.model.business_logic.Location();
        loc.datetime = calendarHandler.getCurrentDatetimeString();
        loc.wgsX = String.valueOf(location.getLongitude());
        loc.wgsY = String.valueOf(location.getLatitude());
        loc.detect_datetime = calendarHandler.getDatetimeStringFromTimeMillis(location.getTime());
        loc.speed = String.valueOf(location.getSpeed());
        loc.altitude = String.valueOf(location.getAltitude());
        loc.direction = String.valueOf(location.getBearing());
        loc.vehicle_code = deviceInfoRepository.getDeviceInfoFromDB().vehicleCode;
        loc.router_ssid = deviceInfoRepository.getDeviceInfoFromDB().routerSsid;
        loc.submit_yn = "N";
        locationRepository.insertLocation(loc);
    }

    private void registerSensors() {
        // 0~360도 방위각 계산을 위한 센서 등록, pitch, roll은 정확도를 위한 것
        azimuthQueue = new Queue(30);
        azimuthQueue.setTune(value -> azimuthTuning(azimuthQueue.getRear().getValue(), value));

        mHandler.postDelayed(() -> {
            sensorManagerHandler.addAzimuthListener((degree) -> {
                azimuthQueue.push(degree);
                degree = azimuthQueue.getAverage();
//                StringBuilder sb = new StringBuilder();
                int newDegree = Math.round(degree) % 360;
                if (newDegree < 0) newDegree = 360 + newDegree;
//                sb.append(newDegree);
//                sb.append("º ");
//                if(newDegree >= 338 || newDegree<=21) {
//                    sb.append("북");
//                } else if (22 <= newDegree && newDegree <= 66) {
//                    sb.append("북동");
//                } else if (67 <= newDegree && newDegree <= 111) {
//                    sb.append("동");
//                } else if (112 <= newDegree && newDegree <= 157) {
//                    sb.append("남동");
//                } else if (158 <= newDegree && newDegree <= 202) {
//                    sb.append("남");
//                } else if (203 <= newDegree && newDegree <= 246) {
//                    sb.append("남서");
//                } else if (247 <= newDegree && newDegree <= 291) {
//                    sb.append("서");
//                } else if (292 <= newDegree && newDegree <= 337) {
//                    sb.append("북서");
//                }

            });
        }, 500);
    }

    public float azimuthTuning(float before, float now) {
        // 359(0) -> 0(359) 갈 때 휙 돌아가는 현상 보정
        float diff = before-now;
        if(diff >= 300) {
            for(int n=0; ;n++) {
                if((360*n-60) <= diff && diff <= (360*(n+1)-60)) return now+(360*n);
            }
        } else if (diff <= -300) {
            for(int n=0; ;n++) {
                if(-360*(n+1)-60 <= diff && diff <= -360*n-60) return now-(360*(n+1));
            }
        }
        return now;
    }

    private int getHeadingDegree() {
        float degree = azimuthQueue.getAverage();
        degree += 90f;
        int newDegree = Math.round(degree) % 360;
        if (newDegree < 0) newDegree = 360 + newDegree;
        return newDegree;
    }

    private void stopSaveHeadingScheduled() {
        if (saveHeadingDegreeScheduled != null && !saveHeadingDegreeScheduled.isCancelled()) {
            saveHeadingDegreeScheduled.cancel(true);
            Logger.getInstance().info("stopSaveHeadingScheduled()");
        }
    }

    private void startSaveHeadingScheduled() {
        stopSaveHeadingScheduled();
        saveHeadingDegreeScheduled = executor.scheduleWithFixedDelay(() -> {
            Azimuth azimuth = new Azimuth();
            azimuth.datetime = calendarHandler.getCurrentDatetimeString();
            azimuth.heading = String.valueOf(getHeadingDegree());
            azimuthRepository.insertAzimuth(azimuth);
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void setLocationListener() {
        locationManagerHandler.liveLocation.observe(this, locationObserver);
        locationManagerHandler.start();
    }

    private void removeLocationListener() {
        locationManagerHandler.liveLocation.removeObserver(locationObserver);
        locationManagerHandler.stop();
    }

    @SuppressLint("DiscouragedApi")
    private void startLocationScheduled() {
        stopLocationScheduled();
        locationScheduled = executor.scheduleAtFixedRate(() -> {
            Token token = tokenRepository.getTokenSync();
            if(token == null || deviceInfo == null) {
                Logger.getInstance().info("LocationService - startLocationScheduled - token null or deviceInfo null");
                return;
            }

            Location location = locationMutableLiveData.getValue();
            if(location == null) {
                Logger.getInstance().info("LocationService - startLocationScheduled - location is null");
                return;
            }
            setVehicleRecptnData(deviceInfo, location);
            saveLocationToDatabase(location);
        }, 500, 5000, TimeUnit.MILLISECONDS);
    }

    private void stopLocationScheduled() {
        if (locationScheduled != null && !locationScheduled.isCancelled()) {
            locationScheduled.cancel(true);
            Logger.getInstance().info("stopLocationScheduled()");
        }
    }

    private void startCheckCameraStatus() {
        stopCheckCameraStatus();
        checkCameraStatusScheduled = executor.scheduleWithFixedDelay(() -> {
            try {
                cameraRepository.getStatusAsync().thenAccept(resp -> {
                    if (resp == null) return;
                    try {
                        SoapXmlParser soapXmlParser = new SoapXmlParser(resp);
                        String utcTimeString = soapXmlParser.getTextByLocalName("UtcTime");
                        if(utcTimeString == null || utcTimeString.isEmpty()) return;
                        Calendar utcCalendar = calendarHandler.convertCalendarFromUtcTimeString(utcTimeString);
                        Calendar kstCalendar = calendarHandler.convertKstCalendarFromUtcCalendar(utcCalendar);
                        long diff = calendarHandler.getSecondsCal1MinusCal2(Calendar.getInstance(), kstCalendar);
                        if (Math.abs(diff) > 30) {
                            Logger.getInstance().info("diff > 30 sec");
                            cameraRepository.setSystemDateAndTime();
                        }
                    } catch (Exception e) {
                        Logger.getInstance().error("checkCameraStatusScheduled - SoapXmlParser error", e);
                    }
                });

                CameraStatus cameraStatus = new CameraStatus();
                cameraStatus.datetime = calendarHandler.getCurrentDatetimeString();
                cameraStatus.moveStart = "";
                cameraStatus.moveEnd = "";
                cameraRepository.getPositionFromCgi(response -> {
                    if (response.isSuccessful()) {
                        try {
                            String resp = response.body().string();
                            resp = resp.split("=")[1];
                            String[] split = resp.split(",");
                            double pan = Double.parseDouble(split[0]);
                            double tilt = Double.parseDouble(split[1]);
                            int iPanValue = 0;
                            int iTiltValue = 0;
                            if (pan > 0.2 && 180 > pan) {
                                iPanValue = (int) Math.round(pan);
                            } else if (359.8 > pan && pan >= 180) {
                                iPanValue = (int) Math.round((360-pan))*-1;
                            }

                            if (tilt > 0.4 && tilt <= 90) {
                                iTiltValue = (int) Math.round(tilt);
                            } else if (tilt >= 330 && tilt <= 359.7) {
                                iTiltValue = (int) Math.round((360-tilt))*-1;
                            }

                            cameraStatus.pan = String.valueOf(iPanValue);
                            cameraStatus.tilt = String.valueOf(iTiltValue);
                            cameraRepository.insertCameraStatus(cameraStatus);
                            boolean isUpdated = isUpdatedCameraStatus(cameraStatus);
                            if (isUpdated && ws != null) {
                                Gson gson = new Gson();
                                WebSocketCommand command = new WebSocketCommand();
                                command.commandId = "";
                                command.command = "05";
                                command.returnCode = "00";
                                command.pan = recentCameraStatus.pan;
                                command.tilt = recentCameraStatus.tilt;
                                ws.send(gson.toJson(command));
                            }
                        } catch (Exception e) {
                        }
                    } else {
                        Logger.getInstance().error(String.format(Locale.KOREA, "getPositionFromCgi is not successful"), null);
                    }
                }, error -> {
                    Logger.getInstance().error(String.format(Locale.KOREA, "getPositionFromCgi throwable"), error);
                });

            } catch (Exception e) {
                Logger.getInstance().error("checkCameraStatusScheduled - error", e);
            }
        }, 500, 3_000, TimeUnit.MILLISECONDS);
    }

    private void stopCheckCameraStatus() {
        if (checkCameraStatusScheduled != null && !checkCameraStatusScheduled.isCancelled()) {
            checkCameraStatusScheduled.cancel(true);
            Logger.getInstance().info("stopCheckCameraStatus()");
        }
    }

    /**
     *
     * @param currentLocation
     * @param targetLocation
     * @return double km
     */
    public double getDistance(Location currentLocation, Location targetLocation) {
        return MathManager.getInstance().getDistanceInKilometerByHaversine(currentLocation.getLatitude(), currentLocation.getLongitude(), targetLocation.getLatitude(), targetLocation.getLongitude());
    }


    /**
     * newCameraStatus가 이 전 최신 pan, tilt 값과 다르면 업데이트 후 true 반환
     * @param newCameraStatus
     * @return true: 변경됨, false: 변경 없음
     */
    public boolean isUpdatedCameraStatus(CameraStatus newCameraStatus) {
        if (newCameraStatus == null) return false;
        if (recentCameraStatus == null) {
            recentCameraStatus = newCameraStatus;
            return true;
        }

        if (!recentCameraStatus.pan.equals(newCameraStatus.pan)) {
            recentCameraStatus = newCameraStatus;
            return true;
        }
        if (!recentCameraStatus.tilt.equals(newCameraStatus.tilt)) {
            recentCameraStatus = newCameraStatus;
            return true;
        }

        recentCameraStatus.datetime = newCameraStatus.datetime;
        return false;
    }

    private int getRecentWindDirection() {
        Cr350 cr350 = sensorRepository.getRecentSensorDataFromDB();
        if(cr350 == null) return 0;
        Azimuth azimuth = azimuthRepository.selectProximateAzimuth(cr350.datetime);
        int val = Integer.parseInt(azimuth.heading) + (int) Float.parseFloat(cr350.wdRunAvg);
        if (val != 0) val = val % 360;
        return val;
    }

}
