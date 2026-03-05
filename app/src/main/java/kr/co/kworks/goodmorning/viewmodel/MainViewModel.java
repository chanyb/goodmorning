package kr.co.kworks.goodmorning.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.function.Consumer;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import kr.co.kworks.goodmorning.model.business_logic.DeviceInfo;
import kr.co.kworks.goodmorning.model.network.StringSocketServer;
import kr.co.kworks.goodmorning.model.repository.CameraRepository;
import kr.co.kworks.goodmorning.model.repository.DeviceInfoRepository;
import kr.co.kworks.goodmorning.model.repository.IntegrateServerRepository;
import kr.co.kworks.goodmorning.model.repository.TokenRepository;

@HiltViewModel
public class MainViewModel extends ViewModel {

    public MutableLiveData<Event<String>> _tokenExpired;

    private CameraRepository cameraRepository;
    private IntegrateServerRepository integrateServerRepository;
    private DeviceInfoRepository deviceInfoRepository;
    private TokenRepository tokenRepository;

    private StringSocketServer.ServerListener socketServerListener;

    @Inject
    public MainViewModel(
        CameraRepository cameraRepository, IntegrateServerRepository integrateServerRepository,
        DeviceInfoRepository deviceInfoRepository, TokenRepository tokenRepository
    ) {
        this.cameraRepository = cameraRepository;
        this.integrateServerRepository = integrateServerRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.tokenRepository = tokenRepository;
        _tokenExpired = tokenRepository._tokenExpired;
    }

    public void getDeviceInfo(Consumer<DeviceInfo> deviceInfoConsumer) {
        deviceInfoRepository.getDeviceInfo(deviceInfoConsumer);
    }

    public boolean deleteToken() {
        return tokenRepository.deleteTokenFromDatabase();
    }

    public boolean deleteVehicle() {
        return deviceInfoRepository.deleteDeviceInfoFromDatabase();
    }
}
