package kr.co.kworks.goodmorning.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import dagger.hilt.android.AndroidEntryPoint;
import kr.co.kworks.goodmorning.R;
import kr.co.kworks.goodmorning.activity.IntroActivity;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.LocationManagerHandler;
import kr.co.kworks.goodmorning.utils.Logger;
import kr.co.kworks.goodmorning.utils.PreferenceHandler;
import kr.co.kworks.goodmorning.utils.SecurityManager;
import kr.co.kworks.goodmorning.utils.SensorManagerHandler;
import kr.co.kworks.goodmorning.utils.Utils;
import kr.co.kworks.goodmorning.viewmodel.GlobalViewModel;
import okhttp3.OkHttpClient;
import okhttp3.WebSocket;

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
    private WebSocket ws;
    private OkHttpClient client;
    private Handler mHandler;

    private SensorManagerHandler sensorManagerHandler;

    private MutableLiveData<Location> locationMutableLiveData;

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

        client = new OkHttpClient();

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
        sensorManagerHandler.unregisterListeners();
    }


}
