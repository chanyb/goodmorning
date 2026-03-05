package kr.co.kworks.goodmorning.viewmodel;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import kr.co.kworks.goodmorning.model.business_logic.Alert;
import kr.co.kworks.goodmorning.model.network.NetworkModule;
import kr.co.kworks.goodmorning.model.network.RequestInterface;
import kr.co.kworks.goodmorning.model.repository.DeviceInfoRepository;
import kr.co.kworks.goodmorning.model.repository.TokenRepository;
import kr.co.kworks.goodmorning.model.request.SetVehicleDeviceMappingRequest;
import kr.co.kworks.goodmorning.model.response.BaseResponse;
import kr.co.kworks.goodmorning.model.response.SetVehicleDeviceMappingResponseBody;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.Column;
import kr.co.kworks.goodmorning.utils.Database;
import kr.co.kworks.goodmorning.utils.Logger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class StepViewModel extends ViewModel {

    private CalendarHandler calendarHandler;
    public MutableLiveData<Event<Alert>> _forceMappingConfirm, _alert;
    public MutableLiveData<Event<String>> _mapFragment, _callNextPageInStep1, _callNextPageInStep2;
    public MutableLiveData<String> _step1_edit1, _step1_edit2, _step1_edit3, _step2_edit1;
    public MutableLiveData<String> _deviceCode;
    public MutableLiveData<Event<String>> _nextPage;

    public DeviceInfoRepository deviceInfoRepository;
    private RequestInterface integrateSystem;
    public TokenRepository tokenRepository;
    private Database db;

    @Inject
    public StepViewModel(
        TokenRepository tokenRepository,
        DeviceInfoRepository deviceInfoRepository,
        @NetworkModule.IntegrateSystem RequestInterface integrateSystem
    ) {
        this.tokenRepository = tokenRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.integrateSystem = integrateSystem;
        init();
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.toString();
    }

    public void init() {
        _mapFragment = new MutableLiveData<>();
        _step1_edit1 = new MutableLiveData<>();
        _step1_edit2 = new MutableLiveData<>();
        _step1_edit3 = new MutableLiveData<>();
        _step2_edit1 = new MutableLiveData<>();
        _deviceCode = new MutableLiveData<>();
        _callNextPageInStep1 = new MutableLiveData<>();
        _callNextPageInStep2 = new MutableLiveData<>();
        _forceMappingConfirm = new MutableLiveData<>();
        _alert = new MutableLiveData<>();

        calendarHandler = new CalendarHandler();
        db = new Database();
    }

    public void saveDeviceCode(String sDeviceCode) {
        if(isDeviceCodeDuplicate(sDeviceCode)) return;
        if(!validate(sDeviceCode)) return;

        ContentValues cv = new ContentValues();
        cv.put(Column.device_info_column_router_ssid, sDeviceCode);
        long success = db.insert(Column.deviceInfo, cv);
        if (success > 0) { // 성공
            _callNextPageInStep1.postValue(new Event<>("next"));
        } else { // 실패
        }
    }

    public void setVehicleDeviceMapping(String vehicleNumber, boolean force) {
        vehicleNumber = vehicleNumber.replace(" ", "");
        SetVehicleDeviceMappingRequest request = new SetVehicleDeviceMappingRequest();
        request.deviceCode = deviceInfoRepository.getDeviceInfoFromDB().routerSsid;
        request.vehicleNumber = vehicleNumber;
        request.force = force ? "Y":"N";

        tokenRepository.getTokenFromServer(deviceInfoRepository.getDeviceInfoFromDB().routerSsid, token -> {
            Call<BaseResponse<SetVehicleDeviceMappingResponseBody>> callSetVehicleDeviceMapping = integrateSystem.setVehicleDeviceMapping(token.accessToken, calendarHandler.getCurrentDatetimeString(), request);
            callSetVehicleDeviceMapping.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<BaseResponse<SetVehicleDeviceMappingResponseBody>> call, Response<BaseResponse<SetVehicleDeviceMappingResponseBody>> response) {
                    if (response.isSuccessful()) {
                        String resultCode = response.body().header.resultCode;
                        if (resultCode.equals("00")) {
                            Logger.getInstance().info("setVehicleDeviceMapping success");
                            if (deviceInfoRepository.saveVehicleNumberToDatabase(response.body().body.vehicleCode, response.body().body.vehicleNumber)) {
                                _callNextPageInStep2.postValue(new Event<>("next"));
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
                            _alert.postValue(new Event<>(alert));
                        } catch (IOException e) {
                            Logger.getInstance().error("setVehicleDeviceMapping fail..", null);
                        }

                    }
                }

                @Override
                public void onFailure(Call<BaseResponse<SetVehicleDeviceMappingResponseBody>> call, Throwable throwable) {
                    Logger.getInstance().error("setVehicleDeviceMapping.onFailure", throwable);
                    Alert alert = new Alert("통신 실패: " + throwable);
                    _alert.postValue(new Event<>(alert));
                }
            });
        });
    }

    public void setVehicleDeviceMapping(String deviceCode, String vehicleNumber, boolean force) {
        vehicleNumber = vehicleNumber.replace(" ", "");
        SetVehicleDeviceMappingRequest request = new SetVehicleDeviceMappingRequest();
        request.deviceCode = deviceCode;
        request.vehicleNumber = vehicleNumber;
        request.force = force ? "Y":"N";

        tokenRepository.getTokenFromServer(deviceCode, token -> {
            Call<BaseResponse<SetVehicleDeviceMappingResponseBody>> callSetVehicleDeviceMapping = integrateSystem.setVehicleDeviceMapping(token.accessToken, calendarHandler.getCurrentDatetimeString(), request);
            callSetVehicleDeviceMapping.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<BaseResponse<SetVehicleDeviceMappingResponseBody>> call, Response<BaseResponse<SetVehicleDeviceMappingResponseBody>> response) {
                    if (response.isSuccessful()) {
                        String resultCode = response.body().header.resultCode;
                        if (resultCode.equals("00")) {
                            Logger.getInstance().info("setVehicleDeviceMapping success");
                            saveDeviceCode(deviceCode);
                            if (deviceInfoRepository.saveVehicleNumberToDatabase(response.body().body.vehicleCode, response.body().body.vehicleNumber)) {
                                _callNextPageInStep2.postValue(new Event<>("next"));
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
                            _alert.postValue(new Event<>(alert));
                        } catch (IOException e) {
                            Logger.getInstance().error("setVehicleDeviceMapping fail..", null);
                        }

                    }
                }

                @Override
                public void onFailure(Call<BaseResponse<SetVehicleDeviceMappingResponseBody>> call, Throwable throwable) {
                    Logger.getInstance().error("setVehicleDeviceMapping.onFailure", throwable);
                    Alert alert = new Alert("통신 실패: " + throwable);
                    _alert.postValue(new Event<>(alert));
                }
            });
        });
    }

    private boolean isDeviceCodeDuplicate(String sDeviceCode) {

        Cursor cursor = db.selectCursor(
            Column.deviceInfo,
            Column.device_info_column_list,
            Column.device_info_column_router_ssid +"=?",
            new String[]{sDeviceCode},
            null,
            null,
            null,
            null
        );

        if (cursor.moveToNext()) {
            return true;
        }
        return false;
    }

    private boolean validate(String str) {
        boolean result = true;
        if (!str.startsWith("A")) result = false;
        if (str.length() != 7) result = false;
        if (!str.matches("^[a-zA-Z0-9]+$")) result = false;

        if (!result) {
//            _alert.setValue(new Event<>("잘못된 고유번호 입니다."));
        }

        return result;
    }
}
