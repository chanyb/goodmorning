package kr.co.kworks.goodmorning.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;

import androidx.lifecycle.MutableLiveData;

import kr.co.kworks.goodmorning.activity.LockScreenActivity;
import kr.co.kworks.goodmorning.service.GoodmorningService;

public class GoodmorningBroadcastReceiver extends BroadcastReceiver {

    public GoodmorningBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED -> {
                startForeground(context);
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
            case Intent.ACTION_SCREEN_ON -> {
            }
            case Intent.ACTION_SCREEN_OFF -> {
                Intent lockScreenIntent = new Intent(context, LockScreenActivity.class);
                lockScreenIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                lockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                lockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(lockScreenIntent);
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
