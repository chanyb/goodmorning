package kr.co.kworks.goodmorning.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;

import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;
import java.util.Locale;

import kr.co.kworks.goodmorning.activity.LockScreenActivity;
import kr.co.kworks.goodmorning.model.business_logic.Unlock;
import kr.co.kworks.goodmorning.service.GoodmorningService;

public class GoodmorningBroadcastReceiver extends BroadcastReceiver {
    private Database database;
    private CalendarHandler calendarHandler;

    public GoodmorningBroadcastReceiver() {
        database = new Database();
        calendarHandler = new CalendarHandler();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Handler mHandler = new Handler(Looper.getMainLooper());
        if (action == null) return;

        switch (action) {
            case Intent.ACTION_SCREEN_OFF -> {
                Logger.getInstance().info("Screen Off");
                Intent lockScreenIntent = new Intent(context, LockScreenActivity.class);
                lockScreenIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                lockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                lockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                try {
//                    mHandler.postDelayed(() -> {
//                        context.startActivity(lockScreenIntent);
//                    }, 500);
                    context.startActivity(lockScreenIntent);
                } catch(Exception e) {
                    Logger.getInstance().error("LockScreenActivityError", e);
                }
            }
            case Intent.ACTION_BOOT_COMPLETED -> {
                if(database.isLogin()) startForeground(context);
            }
            case Intent.ACTION_BATTERY_CHANGED -> {
                // level: 현재 배터리 레벨
                // scale: 배터리 레벨의 최대 값
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                // 배터리 잔량을 백분율로 계산
                float batteryPercentage = level / (float) scale * 100;


                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
                Logger.getInstance().info("battery: " + batteryPercentage);
            }
            case Intent.ACTION_USER_PRESENT -> {
                Logger.getInstance().info("ACTION_USER_PRESENT");
                Unlock unlock = new Unlock();
                database.insert(Column.unlock, unlock.getContentValues());
            }
            case TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                Logger.getInstance().info("ACTION_PHONE_STATE_CHANGED");
                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                if (state != null) {
                    if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                        // 전화 수신
                        Logger.getInstance().info("EXTRA_STATE_RINGING");
                    } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
                        // 통화 시작
                        Logger.getInstance().info("EXTRA_STATE_OFFHOOK");
                        setIsHook(context, true);
                        setOffHookTimeInMillis(context, Calendar.getInstance().getTimeInMillis());
                    } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                        // 통화 종료
                        if (!getIsHook(context)) return;
                        setIsHook(context, false);

                        Logger.getInstance().info("EXTRA_STATE_IDLE");
                        long end = Calendar.getInstance().getTimeInMillis();
                        long start = getOffHookTimeInMillis(context);

                        if(start == -1L) return;
                        double duration = (end - start) / 1000f;

                        Unlock unlock = new Unlock();
                        unlock.type = 2;
                        unlock.etc = String.format(Locale.KOREA, "%.3f", duration);
                        database.insert(Column.unlock, unlock.getContentValues());
                    }
                }
            }
        }
    }

    private void startForeground(Context context) {
        Intent serviceIntent = new Intent(context, GoodmorningService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    private void setOffHookTimeInMillis(Context context, long value) {
        SharedPreferences prefs = context.getSharedPreferences("broadcast_prefs", Context.MODE_PRIVATE);
        prefs.edit()
            .putLong("off_hook_start", value)
            .apply();
    }

    private long getOffHookTimeInMillis(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("broadcast_prefs", Context.MODE_PRIVATE);
        return prefs.getLong("off_hook_start", -1);
    }

    private void setIsHook(Context context, boolean bool) {
        SharedPreferences prefs = context.getSharedPreferences("broadcast_prefs", Context.MODE_PRIVATE);
        prefs.edit()
            .putBoolean("is_hook", bool)
            .apply();
    }

    private boolean getIsHook(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("broadcast_prefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("is_hook", false);
    }
}
