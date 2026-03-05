package kr.co.kworks.goodmorning.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

public class LocationManagerHandler {
    private LocationManager locationManager;
    public final MutableLiveData<Location> liveLocation = new MutableLiveData<>();
    public final MutableLiveData<Location> networkLocation = new MutableLiveData<>();
    private Context mContext;
    private LocationListener gpsListener, networkListener;
    private boolean updating = false;

    public LocationManagerHandler(Context context) {
        mContext = context;
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        gpsListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                liveLocation.postValue(location);
            }
        };

        networkListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                networkLocation.postValue(location);
            }
        };

    }


    @SuppressLint("MissingPermission") // 권한 체크는 호출측에서 보장
    public void start() {
        if (updating) return;
        updating = true;
        try {
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Logger.getInstance().error("LocationManagerHandler-start-PROVIDER-disable", null);
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener, Looper.getMainLooper());
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkListener, Looper.getMainLooper());
        } catch(Exception e) {
            Logger.getInstance().error("LocationManagerHandler-start", e);
        }

        Logger.getInstance().info("LocationManagerHandler - start()");
    }

    public void stop() {
        if (!updating) return;
        locationManager.removeUpdates(gpsListener);
        locationManager.removeUpdates(networkListener);
        updating = false;
    }

    public MutableLiveData<Location> getLiveLocation() {
        return this.liveLocation;
    }

    public boolean canUse() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}
