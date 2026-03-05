package kr.co.kworks.goodmorning.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class FusedLocationProvider {
    private final FusedLocationProviderClient fusedClient;
    public final MutableLiveData<Location> liveLocation = new MutableLiveData<>();
    private LocationCallback callback;
    private Context mContext;
    private boolean updating = false;

    public FusedLocationProvider(Context context) {
        mContext = context;
        fusedClient = LocationServices.getFusedLocationProviderClient(mContext.getApplicationContext());
    }


    @SuppressLint("MissingPermission") // 권한 체크는 호출측에서 보장
    public void start() {
        if (updating) return;
        updating = true;

        LocationRequest req;
        // API 31+ 신규 빌더
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            req = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 300)
                .setMinUpdateIntervalMillis(1000) // 더 자주 오지 않게 최소 주기 명시
                .setMaxUpdateDelayMillis(0) // 지연 전송 막기
                .setMinUpdateDistanceMeters(0f) // 거리 제한 없음
                .setGranularity(Granularity.GRANULARITY_FINE) // 미세 위치
                .setWaitForAccurateLocation(false)       // 첫 fix 기다리느라 늦어지는 것 방지
                .build();
        } else {
            // 호환용(deprecated지만 널리 사용)
            req = LocationRequest.create()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setInterval(300)
                .setFastestInterval(300)
                .setSmallestDisplacement(1f);
        }

        try {
            callback = new LocationCallback() {
                @Override public void onLocationResult(LocationResult result) {
                    Location loc = result.getLastLocation();
                    if (loc != null) liveLocation.postValue(loc);
                }
            };
            fusedClient.requestLocationUpdates(req, callback, Looper.getMainLooper());
        } catch (Exception e) {
            Logger.getInstance().error("fusedLocationProvider", e);
        }

    }

    public void stop() {
        if (!updating || callback == null) return;
        fusedClient.removeLocationUpdates(callback);
        updating = false;
    }

    public MutableLiveData<Location> getLiveLocation() {
        return this.liveLocation;
    }

    public boolean canUse() {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext);
        return code == ConnectionResult.SUCCESS;
    }
}
