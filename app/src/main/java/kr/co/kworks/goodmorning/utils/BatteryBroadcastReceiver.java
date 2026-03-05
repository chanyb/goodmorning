package kr.co.kworks.goodmorning.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import androidx.lifecycle.MutableLiveData;

public class BatteryBroadcastReceiver extends BroadcastReceiver {

    private MutableLiveData<Integer> batteryLiveData;
    private MutableLiveData<Boolean> isChargingLiveData;

    public BatteryBroadcastReceiver() {
        batteryLiveData = new MutableLiveData<>();
        isChargingLiveData = new MutableLiveData<>();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            // level: 현재 배터리 레벨
            // scale: 배터리 레벨의 최대 값
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            // 배터리 잔량을 백분율로 계산
            float batteryPercentage = level / (float) scale * 100;


            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

            batteryLiveData.postValue((int) batteryPercentage);
            isChargingLiveData.postValue(isCharging);

            Logger.getInstance().info("battery: " + batteryPercentage);
        }
    }

    public MutableLiveData<Boolean> getIsChargingLiveData() {
        return isChargingLiveData;
    }

    public MutableLiveData<Integer> getBatteryLiveData() {
        return batteryLiveData;
    }
}
