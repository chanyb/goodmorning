package kr.co.kworks.goodmorning.viewmodel;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import kr.co.kworks.goodmorning.model.business_logic.Alert;
import kr.co.kworks.goodmorning.model.business_logic.DeviceInfo;
import kr.co.kworks.goodmorning.model.business_logic.FrfrRoot;
import kr.co.kworks.goodmorning.model.business_logic.Token;
import kr.co.kworks.goodmorning.model.network.NetworkModule;
import kr.co.kworks.goodmorning.model.network.OpenApiRequestInterface;
import kr.co.kworks.goodmorning.model.repository.DeviceInfoRepository;
import kr.co.kworks.goodmorning.model.repository.IntegrateServerRepository;
import kr.co.kworks.goodmorning.model.repository.LocationRepository;
import kr.co.kworks.goodmorning.model.repository.TokenRepository;
import kr.co.kworks.goodmorning.model.request.GetHelliListRequest;
import kr.co.kworks.goodmorning.model.request.GetVehicleListRequest;
import kr.co.kworks.goodmorning.model.response.GetHelliListResponse;
import kr.co.kworks.goodmorning.model.response.GetVehicleListResponse;
import kr.co.kworks.goodmorning.utils.FrfrXmlMapper;
import kr.co.kworks.goodmorning.utils.Logger;
import kr.co.kworks.goodmorning.utils.MathManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@HiltViewModel
public class MapViewModel extends ViewModel {
    public MutableLiveData<Location> _location;
    public MutableLiveData<Integer> _distance;
    public MutableLiveData<List<GetVehicleListResponse.VehicleInfo>> vehicleInfoList;
    public MutableLiveData<List<GetHelliListResponse.HelliInfo>> helliInfoList;
    public MutableLiveData<List<GetVehicleListResponse.VehicleInfo>> _vehicleInfoList;
    public MutableLiveData<List<GetHelliListResponse.HelliInfo>> _helliInfoList;
    public MutableLiveData<FrfrRoot> fireList;
    public MutableLiveData<FrfrRoot> _fireList;
    public MutableLiveData<Boolean> _btnVehicleListSelected, _btnHelliListSelected;
    public MutableLiveData<Boolean> _videoFragmentSizeMax, _distanceOptionVisible;

    private TokenRepository tokenRepository;
    private DeviceInfoRepository deviceInfoRepository;
    private LocationRepository locationRepository;
    private IntegrateServerRepository integrateServerRepository;
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> getVehicleListScheduled;

    private OpenApiRequestInterface openApiRequestInterface;

    private FrfrXmlMapper frfrXmlMapper;
    @Inject
    public MapViewModel(
        TokenRepository tokenRepository,
        DeviceInfoRepository deviceInfoRepository,
        LocationRepository locationRepository,
        IntegrateServerRepository integrateServerRepository,
        @NetworkModule.OpenAPI OpenApiRequestInterface openApiRequestInterface
    ) {
        this.tokenRepository = tokenRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.locationRepository = locationRepository;
        this.integrateServerRepository = integrateServerRepository;
        this.openApiRequestInterface = openApiRequestInterface;
        init();
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.toString();
    }

    public void init() {
        executor = Executors.newScheduledThreadPool(1);
        vehicleInfoList = new MutableLiveData<>();
        helliInfoList = new MutableLiveData<>();
        _vehicleInfoList = new MutableLiveData<>();
        _helliInfoList = new MutableLiveData<>();
        _btnHelliListSelected = new MutableLiveData<>(false);
        _btnVehicleListSelected = new MutableLiveData<>(false);
        _videoFragmentSizeMax = new MutableLiveData<>(false);
        _distance = new MutableLiveData<>(10);
        _distanceOptionVisible = new MutableLiveData<>(false);
        _fireList = new MutableLiveData<>();
        fireList = new MutableLiveData<>();
        frfrXmlMapper = new FrfrXmlMapper();
        startGetVehicleList();
    }

    private void observerInit() {

    }

    public String getEmdKorean(Location location) {
        return locationRepository.getEmdKorean(location);
    }

    public void startGetVehicleList() {
        stopGetVehicleList();
        getVehicleListScheduled = executor.scheduleWithFixedDelay(() -> {
            Logger.getInstance().info("startGetVehicleList()");
            Token token = tokenRepository.getTokenSync();
            DeviceInfo deviceInfo = deviceInfoRepository.getDeviceInfoFromDB();
            if(token == null || deviceInfo == null) return;

            GetVehicleListRequest getVehicleListRequest = new GetVehicleListRequest(
                deviceInfo.vehicleCode,
                "36.3292381",
                "127.3347515"
            );
            integrateServerRepository.getVehicleList(token.accessToken, getVehicleListRequest, response -> {
                if (response.isSuccessful()) {
                    String resultCode = response.body().header.resultCode;
                    switch (resultCode) {
                        case "00" -> {
                            vehicleInfoList.postValue(response.body().body.vehicleList);
                            // TODO: 2026-01-09 실시간반영되게해야함 지금 안됨 
                        }
                        case "02" -> {
                            tokenRepository.getAutoRefresh(t -> {});
                        }
                        default -> {
                            Logger.getInstance().error("getAutoRefresh: " + response.body().header.resultMessage, null);
                        }
                    }
                } else {
                    Logger.getInstance().error(String.format(Locale.KOREA, "오류(%d)%s", response.code(), response.body().header.resultMessage), null);
                }
            }, throwable -> {
                Logger.getInstance().error("getVehicleList 오류(throwable)", null);
            });

            GetHelliListRequest getHelliListRequest = new GetHelliListRequest(
                deviceInfo.vehicleCode,
                "36.3292381",
                "127.3347515"
            );
            integrateServerRepository.getHelliList(token.accessToken, getHelliListRequest, response -> {
                if (response.isSuccessful()) {
                    String resultCode = response.body().header.resultCode;
                    switch (resultCode) {
                        case "00" -> {
                            setAppropriateHelliList(response.body().body.helliList);
                            // TODO: 2026-01-09 실시간반영되게해야함 지금 안됨
                        }
                        case "02" -> {
                            tokenRepository.getAutoRefresh(t -> {});
                        }
                        default -> {
                            Logger.getInstance().error("getAutoRefresh: " + response.body().header.resultMessage, null);
                        }
                    }
                } else {
                    Logger.getInstance().error(String.format(Locale.KOREA, "오류(%d)%s", response.code(), response.body().header.resultMessage), null);
                }
            }, throwable -> {
                Logger.getInstance().error("getHelliList 오류(throwable)", null);
            });

            getTodayFire();
        }, 0, 10_000, TimeUnit.MILLISECONDS);
    }

    public void stopGetVehicleList() {
        if (getVehicleListScheduled != null && !getVehicleListScheduled.isCancelled()) {
            getVehicleListScheduled.cancel(true);
        }
    }

    public void getTodayFire() {
        Call<ResponseBody> todayFire = openApiRequestInterface.todayFire();
        todayFire.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String resp = response.body().string();
                        FrfrRoot frfrRoot = frfrXmlMapper.parse(resp);
                        fireList.postValue(frfrRoot);
                    } catch (Exception e) {
                        Logger.getInstance().error("todayFire-onResponse-Exception", e);
                    }
                } else {
                    Logger.getInstance().error("todayFire-not success", null);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                Logger.getInstance().error("getTodayFire.onFailure", throwable);
                Alert alert = new Alert("통신 실패: " + throwable);
            }
        });
    }

    private void setAppropriateHelliList(List<GetHelliListResponse.HelliInfo> originList) {
        kr.co.kworks.goodmorning.model.business_logic.Location location = locationRepository.getRecentLocationFromDB();
        if (_distance.getValue() == null) return;
        if (location == null) return;
        ArrayList<GetHelliListResponse.HelliInfo> modifyList = new ArrayList<>();
        for (GetHelliListResponse.HelliInfo helliInfo : originList) {
            double distance_km = MathManager.getInstance().getDistanceInKilometerByHaversine(Double.parseDouble(helliInfo.wgs84x), Double.parseDouble(helliInfo.wgs84y), Double.parseDouble(location.wgsX), Double.parseDouble(location.wgsY));
            if (distance_km < _distance.getValue()) modifyList.add(helliInfo);
        }
        helliInfoList.postValue(modifyList);
    }

}
