package kr.co.kworks.goodmorning.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import kr.co.kworks.goodmorning.model.business_logic.Azimuth;
import kr.co.kworks.goodmorning.model.business_logic.Cr350;
import kr.co.kworks.goodmorning.model.repository.AzimuthRepository;
import kr.co.kworks.goodmorning.model.repository.SensorRepository;


@HiltViewModel
public class HeaderViewModel extends ViewModel {

    public MutableLiveData<Cr350> _cr350;
    public SensorRepository sensorRepository;
    public AzimuthRepository azimuthRepository;
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> weatherSensorSelectScheduled;

    @Inject
    public HeaderViewModel(
        SensorRepository sensorRepository,
        AzimuthRepository azimuthRepository
    ) {
        this.sensorRepository = sensorRepository;
        this.azimuthRepository = azimuthRepository;
        init();
        startWeatherSensorSelectScheduled();

    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.toString();
    }

    public void init() {
        executor = Executors.newScheduledThreadPool(2);
        _cr350 = new MutableLiveData<>();
    }

    private void startWeatherSensorSelectScheduled() {
        stopWeatherSensorSelectScheduled();
        weatherSensorSelectScheduled = executor.scheduleWithFixedDelay(() -> {
            Cr350 cr350 = sensorRepository.getRecentSensorDataFromDB();
            if(cr350 == null) return;
            _cr350.postValue(cr350);
        }, 0, 10_000, TimeUnit.MILLISECONDS);
    }

    private void stopWeatherSensorSelectScheduled() {
        if (weatherSensorSelectScheduled != null && !weatherSensorSelectScheduled.isCancelled())
            weatherSensorSelectScheduled.cancel(true);
    }

    public String getCurrentWindDirection() {
        return degreeToKorean(getRecentWindDirection());
    }

    private int getRecentWindDirection() {
        Cr350 cr350 = sensorRepository.getRecentSensorDataFromDB();
        if(cr350 == null) return 0;
        Azimuth azimuth = azimuthRepository.selectProximateAzimuth(cr350.datetime);
        int val = Integer.parseInt(azimuth.heading) + (int) Float.parseFloat(cr350.wdRunAvg);
        if (val != 0) val = val % 360;
        return val;
    }

    private String degreeToKorean(int degree) {
        if (degree < 0) degree = 360 + degree;
        StringBuilder sb = new StringBuilder();
        if(degree >= 338 || degree<=21) {
            sb.append("북");
        } else if (22 <= degree && degree <= 66) {
            sb.append("북동");
        } else if (67 <= degree && degree <= 111) {
            sb.append("동");
        } else if (112 <= degree && degree <= 157) {
            sb.append("남동");
        } else if (158 <= degree && degree <= 202) {
            sb.append("남");
        } else if (203 <= degree && degree <= 246) {
            sb.append("남서");
        } else if (247 <= degree && degree <= 291) {
            sb.append("서");
        } else if (292 <= degree && degree <= 337) {
            sb.append("북서");
        }

        return sb.toString();
    }

}
