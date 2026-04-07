package kr.co.kworks.goodmorning.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.lifecycle.MutableLiveData;

import kr.co.kworks.goodmorning.activity.LockScreenActivity;
import kr.co.kworks.goodmorning.model.business_logic.Unlock;
import kr.co.kworks.goodmorning.service.GoodmorningService;

public class GoodmorningBroadcastReceiver extends BroadcastReceiver {
    private Database database;

    public GoodmorningBroadcastReceiver() {
        database = new Database();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        switch (action) {
            case Intent.ACTION_SCREEN_OFF -> {
                Logger.getInstance().info("Screen Off");
                Intent lockScreenIntent = new Intent(context, LockScreenActivity.class);
                lockScreenIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                lockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                try {
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
                    } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                        // 통화 종료
                        Logger.getInstance().info("EXTRA_STATE_IDLE");
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
}
