package kr.co.kworks.goodmorning.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import kr.co.kworks.goodmorning.R;
import kr.co.kworks.goodmorning.activity.IntroActivity;
import kr.co.kworks.goodmorning.model.business_logic.Unlock;
import kr.co.kworks.goodmorning.model.repository.ServerRepository;
import kr.co.kworks.goodmorning.model.request.UnlockRequest;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.Column;
import kr.co.kworks.goodmorning.utils.Database;
import kr.co.kworks.goodmorning.utils.GoodmorningBroadcastReceiver;
import kr.co.kworks.goodmorning.utils.Logger;
import kr.co.kworks.goodmorning.utils.Utils;

@AndroidEntryPoint
public class GoodmorningService extends LifecycleService {
    private NotificationCompat.Builder builder;
    private CalendarHandler calendarHandler;

    private GoodmorningBroadcastReceiver goodmorningBroadcastReceiver;
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> unLockDataScheduled;
    private Handler mHandler;
    private Database database;

    private boolean screenOff;

    @Inject
    ServerRepository serverRepository;

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
        registerBroadcastReceiver();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        release();
        stopForeground(Service.STOP_FOREGROUND_REMOVE);
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
        calendarHandler = new CalendarHandler();
        executor = Executors.newScheduledThreadPool(4);
        goodmorningBroadcastReceiver = new GoodmorningBroadcastReceiver();
        database = new Database();
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

        builder = new NotificationCompat.Builder(this, Utils.GOODMORNING_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentText(getString(R.string.service_foreground_message))
            .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(Utils.GOODMORNING_FOREGROUND_NOTIFICATION_ID, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(Utils.GOODMORNING_FOREGROUND_NOTIFICATION_ID, builder.build());
        }
    }

    private void startScheduled() {
        stopUnLockDataScheduled();
        unLockDataScheduled = executor.scheduleWithFixedDelay(() -> {
            Unlock unlock = database.getUnlockInfo();
            String token = database.getFcmToken();
            if(unlock == null || token == null || token.isEmpty()) return;
            UnlockRequest unlockRequest = new UnlockRequest();
            unlockRequest.token = token;
            serverRepository.unlock(
                unlockRequest,
                unlockResponseResponse -> {
                    if (!unlockResponseResponse.isSuccessful()) return;
                    if (unlockResponseResponse.body().result.equals("200")) {
                        ContentValues whereCv = new ContentValues();
                        whereCv.put(Column.unlock_datetime, unlock.datetime);
                        unlock.submit = 1;
                        database.update(Column.unlock, unlock.getContentValues(), whereCv);
                    }
                },
                throwable -> {
                    Logger.getInstance().error("unlockRequest", throwable);
                }
            );
        },1000, 1000, TimeUnit.MILLISECONDS);

    }


    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(goodmorningBroadcastReceiver, intentFilter);
    }

    private void unregisterBroadcastReceiver() {
        unregisterReceiver(goodmorningBroadcastReceiver);
    }


    private void release() {
        unregisterBroadcastReceiver();
        stopUnLockDataScheduled();
    }

    private void stopUnLockDataScheduled() {
        if (unLockDataScheduled != null && !unLockDataScheduled.isCancelled()) {
            unLockDataScheduled.cancel(true);
        }
    }


}
