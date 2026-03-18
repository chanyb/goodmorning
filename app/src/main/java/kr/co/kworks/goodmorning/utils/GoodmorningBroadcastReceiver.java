package kr.co.kworks.goodmorning.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import androidx.lifecycle.MutableLiveData;

public class GoodmorningBroadcastReceiver extends BroadcastReceiver {

    public GoodmorningBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED -> {
                Logger.getInstance().info("BOOT_COMPLETE");
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
                Logger.getInstance().info("screen on");
            }
        }
    }
}
