package kr.co.kworks.goodmorning.viewmodel;

import android.database.Cursor;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import kr.co.kworks.goodmorning.model.business_logic.Alert;
import kr.co.kworks.goodmorning.model.business_logic.DeviceInfo;
import kr.co.kworks.goodmorning.model.business_logic.Token;
import kr.co.kworks.goodmorning.model.network.NetworkModule;
import kr.co.kworks.goodmorning.model.request.GetAuthRequest;
import kr.co.kworks.goodmorning.model.response.BaseResponse;
import kr.co.kworks.goodmorning.model.response.GetAuthBody;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.Column;
import kr.co.kworks.goodmorning.utils.Database;
import kr.co.kworks.goodmorning.utils.Logger;
import kr.co.kworks.goodmorning.model.network.RequestInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class VehicleSetViewModel extends ViewModel {
    private Token token;
    private CalendarHandler calendarHandler;
    private RequestInterface integrateSystem;
    private String routerSsid, vehicleNumber;

    public MutableLiveData<Event<Alert>> _alert, _forceMappingConfirm;
    public MutableLiveData<String> _routerSsid, _vehicleNumber;

    private Database db;

    @Inject
    public VehicleSetViewModel(@NetworkModule.IntegrateSystem RequestInterface integrateSystem) {
        this.integrateSystem = integrateSystem;
        init();
    }


    private void init() {
        calendarHandler = new CalendarHandler();
        _routerSsid = new MutableLiveData<>();
        _alert = new MutableLiveData<>();
        _vehicleNumber = new MutableLiveData<>();
        _forceMappingConfirm = new MutableLiveData<>();
        db = new Database();
        initView();
        initToken();
        getVehicleNumberFromDatabase();
    }

    private void initView() {
        routerSsid = getRouterSsid();
        _routerSsid.postValue(routerSsid);
    }

    private void initToken() {
        if (token() == null) getTokenFromServer();
        else {
            Logger.getInstance().info("It is a access token");
        }
    }

    private String getRouterSsid() {
        Cursor cursor = db.selectCursor(
            Column.deviceInfo,
            Column.device_info_column_list,
            null,
            null,
            null,
            null,
            String.format(Locale.KOREA, "%s desc", Column.device_info_column_create_at),
            "1"
        );

        if (cursor.moveToNext()) {
            return cursor.getString(cursor.getColumnIndexOrThrow(Column.device_info_column_router_ssid));
        }

        return null;
    }

    private Token getTokenByDB() {
        Cursor cursor = db.selectCursor(
            Column.token,
            Column.token_column_list,
            null,
            null,
            null,
            null,
            String.format(Locale.KOREA, "%s desc", Column.token_create_at),
            "1"
        );

        if (cursor.moveToNext()) {
            String accessToken = cursor.getString(cursor.getColumnIndexOrThrow(Column.token_access));
            String refreshToken = cursor.getString(cursor.getColumnIndexOrThrow(Column.token_refresh));
            return new Token(accessToken, refreshToken);
        }

        return null;
    }

    private boolean saveTokenToDatabase(Token token) {
        return db.insert(Column.token, token.getContentValues()) > 0;
    }

    private Token token() {
        if (token == null) token = getTokenByDB();
        return token;
    }

    private void getTokenFromServer() {
        GetAuthRequest request = new GetAuthRequest();
        request.routerSsid = routerSsid;

        Call<BaseResponse<GetAuthBody>> callGetAuth = integrateSystem.getAuth(calendarHandler.getCurrentDatetimeString(), request);
        callGetAuth.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<BaseResponse<GetAuthBody>> call, Response<BaseResponse<GetAuthBody>> response) {
                if (response.isSuccessful()) {
                    if (response.body().header.resultCode.equals("00")) {
                        Token token1 = new Token(response.body().body.accessToken, response.body().body.refreshToken);
                        if (saveTokenToDatabase(token1)) {
                            token = getTokenByDB();
                        }
                    } else {
                        Logger.getInstance().error(response.body().header.resultMessage, null);
                        Alert alert = new Alert(String.format(Locale.KOREA, "getAuth 오류(%s)%s", response.body().header.resultCode, response.body().header.resultMessage));
                        _alert.postValue(new Event<>(alert));
                    }
                } else {
                    Alert alert = new Alert(String.format(Locale.KOREA, "getAuth 오류(%d)%s", response.code(), response.body().header.resultMessage));
                    _alert.postValue(new Event<>(alert));
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<GetAuthBody>> call, Throwable throwable) {
                Logger.getInstance().error("getTokenFromServer.onFailure", throwable);
                Alert alert = new Alert("통신 실패: " + throwable);
                _alert.postValue(new Event<>(alert));
            }
        });
    }

    public boolean update(DeviceInfo deviceInfo) {
        long result = db.update(
            Column.deviceInfo,
            deviceInfo.getContentValues(),
            Column.device_info_column_router_ssid +"=?",
            new String[]{deviceInfo.routerSsid}
        );
        return result > 0;
    }

    private boolean saveVehicleNumberToDatabase(String vehicleCode, String vehicleNumber) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.routerSsid = routerSsid;
        deviceInfo.vehicleCode = vehicleCode;
        deviceInfo.vehicleNumber = vehicleNumber;
        return update(deviceInfo);
    }

    private void getVehicleNumberFromDatabase() {
        Cursor cursor = db.selectCursor(
            Column.deviceInfo,
            Column.device_info_column_list,
            null,
            null,
            null,
            null,
            String.format(Locale.KOREA, "%s desc", Column.token_create_at),
            "1"
        );

        if (cursor.moveToNext()) {
            String deviceCode = cursor.getString(cursor.getColumnIndexOrThrow(Column.device_info_column_router_ssid));
            String vehicleNumber = cursor.getString(cursor.getColumnIndexOrThrow(Column.device_info_column_vehicle_number));
            _vehicleNumber.postValue(vehicleNumber);
        }
    }

}
