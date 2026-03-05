package kr.co.kworks.goodmorning.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import kr.co.kworks.goodmorning.model.business_logic.Alert;
import kr.co.kworks.goodmorning.model.business_logic.DeviceInfo;
import kr.co.kworks.goodmorning.model.repository.DeviceInfoRepository;
import kr.co.kworks.goodmorning.model.repository.TokenRepository;
import kr.co.kworks.goodmorning.utils.Logger;


@HiltViewModel
public class SettingMainViewModel extends ViewModel {

    public MutableLiveData<Event<String>> _deviceInfoFragment, _softwareUpdateFragment, _networkStatusFragment;
    public MutableLiveData<String> _vehicleNumber, _deviceCode, _vehicleCode;
    public MutableLiveData<Event<Alert>> _alert, _forceMappingConfirm, _appExitConfirm;
    private DeviceInfoRepository deviceInfoRepository;
    private TokenRepository tokenRepository;

    @Inject
    public SettingMainViewModel(
        DeviceInfoRepository deviceInfoRepository,
        TokenRepository tokenRepository
    ) {
        this.deviceInfoRepository = deviceInfoRepository;
        this.tokenRepository = tokenRepository;
        init();
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.toString();
    }

    public void init() {
        _alert = new MutableLiveData<>();
        _forceMappingConfirm = new MutableLiveData<>();
        _deviceInfoFragment = new MutableLiveData<>();
        _softwareUpdateFragment = new MutableLiveData<>();
        _networkStatusFragment = new MutableLiveData<>();
        _appExitConfirm = new MutableLiveData<>();
        _vehicleNumber = new MutableLiveData<>();
        _deviceCode = new MutableLiveData<>();
        _vehicleCode = new MutableLiveData<>();
    }

    public void initData() {
        DeviceInfo deviceInfo = deviceInfoRepository.getDeviceInfoFromDB();
        _vehicleCode.postValue(deviceInfo.vehicleCode);
        _vehicleNumber.postValue(deviceInfo.vehicleNumber);
        _deviceCode.postValue(deviceInfo.routerSsid);
    }

    public void mapDeviceAndVehicle(String deviceCode, String vehicleNumber, boolean force) {
        tokenRepository.getTokenAsync(token -> {
            deviceInfoRepository.setVehicleDeviceMapping(token, deviceCode, vehicleNumber, force, response -> {
                if (response.isSuccessful()) {
                    String resultCode = response.body().header.resultCode;
                    if (resultCode.equals("00")) {
                        Logger.getInstance().info("setVehicleDeviceMapping success");
                        if (deviceInfoRepository.saveVehicleNumberToDatabase(response.body().body.vehicleCode, response.body().body.vehicleNumber)) {
                            Alert alert = new Alert("차량번호와 디바이스 고유번호 매핑에 성공했습니다. 앱을 다시 실행합니다.");
                            _alert.postValue(new Event<>(alert));
                        } else {
                            Logger.getInstance().error("mapDeviceAndVehicle", null);
                        }
                    } else if (resultCode.equals("04")) {
                        Alert alert = new Alert("알림","해당 단말기는 다른 차량에 매핑된 단말기입니다. 매핑된 차량을 변경할까요?");
                        _forceMappingConfirm.postValue(new Event<>(alert));
                    } else {
                        Logger.getInstance().error(response.body().header.resultMessage, null);
                        Alert alert = new Alert(String.format(Locale.KOREA, "오류(%s)%s", response.body().header.resultCode, response.body().header.resultMessage));
                        _alert.postValue(new Event<>(alert));
                    }
                } else {
                    try {
                        Alert alert = new Alert(String.format(Locale.KOREA, "fail: %s", response.errorBody().string()));
                        Logger.getInstance().info("alert: " + alert.body);
                        _alert.postValue(new Event<>(alert));
                    } catch (IOException e) {
                        Logger.getInstance().error("setVehicleDeviceMapping fail..", null);
                    }

                }
            });
        });
    }

}
