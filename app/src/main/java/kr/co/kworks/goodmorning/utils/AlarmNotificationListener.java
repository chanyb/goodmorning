package kr.co.kworks.goodmorning.utils;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.HashMap;
import java.util.Map;

public class AlarmNotificationListener extends NotificationListenerService {

    private final Map<String, Long> ringingMap = new HashMap<>();

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();
        String key = sbn.getKey();

        if (isClockAlarmNotification(sbn)) {
            ringingMap.put(key, System.currentTimeMillis());

            // 알람 시작 추정
            Logger.getInstance().info("alarm started: " + key);
        } else {
            Logger.getInstance().info("another notification started: " + key);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        String key = sbn.getKey();

        if (ringingMap.containsKey(key)) {
            long startedAt = ringingMap.remove(key);
            long endedAt = System.currentTimeMillis();

            // 알람 종료 추정
            Logger.getInstance().info("ALARM", "alarm ended: " + key + ", duration=" + (endedAt - startedAt));
        }
    }

    private boolean isClockAlarmNotification(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();
        if (pkg == null) return false;

        // 예시 패키지
        boolean knownClockApp =
            "com.google.android.deskclock".equals(pkg) ||
                "com.sec.android.app.clockpackage".equals(pkg);

        if (!knownClockApp) return false;

        Notification n = sbn.getNotification();
        Bundle extras = n.extras;
        CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);

        String t1 = title == null ? "" : title.toString().toLowerCase();
        String t2 = text == null ? "" : text.toString().toLowerCase();

        return t1.contains("alarm") || t2.contains("alarm")
            || t1.contains("알람") || t2.contains("알람");
    }
}
