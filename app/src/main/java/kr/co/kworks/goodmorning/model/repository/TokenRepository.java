package kr.co.kworks.goodmorning.model.repository;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import kr.co.kworks.goodmorning.model.business_logic.Alert;
import kr.co.kworks.goodmorning.model.business_logic.Token;
import kr.co.kworks.goodmorning.model.network.NetworkModule;
import kr.co.kworks.goodmorning.model.network.RequestInterface;
import kr.co.kworks.goodmorning.model.request.GetAuthRequest;
import kr.co.kworks.goodmorning.model.request.GetAutoRefreshRequest;
import kr.co.kworks.goodmorning.model.response.BaseResponse;
import kr.co.kworks.goodmorning.model.response.GetAuthBody;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.Column;
import kr.co.kworks.goodmorning.utils.Database;
import kr.co.kworks.goodmorning.utils.Logger;
import kr.co.kworks.goodmorning.viewmodel.Event;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class TokenRepository {
    private final Executor io = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Event<String>> _requestResult = new MutableLiveData<>();
    private final MutableLiveData<Event<Alert>> _alert = new MutableLiveData<>();
    private final RequestInterface integrateSystem;
    private final CalendarHandler calendarHandler;
    private Token token;
    public final MutableLiveData<Event<String>> _tokenExpired = new MutableLiveData<>();

    private DeviceInfoRepository deviceInfoRepository;
    private Database db;

    @Inject
    public TokenRepository(@NetworkModule.IntegrateSystem RequestInterface integrateSystem, DeviceInfoRepository deviceInfoRepository) {
        this.integrateSystem = integrateSystem;
        this.deviceInfoRepository = deviceInfoRepository;
        calendarHandler = new CalendarHandler();
        db = new Database();
    }

    /** 동기(블로킹) */
    public Token getTokenSync() {
        token = getTokenFromDB();
        Logger.getInstance().info("token", "access: " + token.accessToken);
        Logger.getInstance().info("token", "refresh: " + token.refreshToken);
        return token;
    }

    /** 비동기 콜백 */
    public void getTokenAsync(Consumer<Token> callback) {
        io.execute(() -> {
            if(token == null || token.accessToken.isEmpty()) token = getTokenFromDB();
            callback.accept(token);
        });
    }

    private Token getTokenFromDB() {
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

    public void getAutoRefresh(Consumer<Token> callback) {
        if(token == null || token.accessToken.isEmpty()) return;
        GetAutoRefreshRequest getAutoRefreshRequest = new GetAutoRefreshRequest();
        getAutoRefreshRequest.refreshToken = token.refreshToken;
        integrateSystem.getAutoRefresh(calendarHandler.getCurrentDatetimeString(), getAutoRefreshRequest);
        Call<BaseResponse<GetAuthBody>> call = integrateSystem.getAutoRefresh(calendarHandler.getCurrentDatetimeString(), getAutoRefreshRequest);
        call.enqueue(new Callback<BaseResponse<GetAuthBody>>() {
            @Override
            public void onResponse(Call<BaseResponse<GetAuthBody>> call, Response<BaseResponse<GetAuthBody>> response) {
                if (response.isSuccessful()) {
                    String resultCode = response.body().header.resultCode;
                    switch (resultCode) {
                        case "00" -> {
                            Token newToken = new Token(response.body().body.accessToken, response.body().body.refreshToken);
                            deviceInfoRepository.saveVehicleNumberToDatabase(response.body().body.vehicleCode, response.body().body.vehicleNumber);
                            if (!saveTokenToDatabase(newToken)) {
                                _requestResult.setValue(new Event<>("fail"));
                            }
                            callback.accept(newToken);
                            _requestResult.setValue(new Event<>("success"));
                        }
                        case "02" -> {
                            Logger.getInstance().error("getAutoRefresh: " + response.body().header.resultMessage, null);
//                            _tokenExpired.postValue(new Event<>("expired"));
                        }
                        default -> {
                            Logger.getInstance().error("getAutoRefresh: " + response.body().header.resultMessage, null);
                        }
                    }
                } else {
                    Alert alert = new Alert(String.format(Locale.KOREA, "오류(%d)%s", response.code(), response.body().header.resultMessage));
                    _alert.postValue(new Event<>(alert));
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<GetAuthBody>> call, Throwable throwable) {
                Logger.getInstance().error("getAutoRefresh.onFailure", throwable);
                Alert alert = new Alert("통신 실패: " + throwable);
                _alert.postValue(new Event<>(alert));
            }
        });
    }

    private boolean saveTokenToDatabase(Token token) {
        return db.insert(Column.token, token.getContentValues()) > 0;
    }

    public boolean deleteTokenFromDatabase() {
        token = null;
        return db.delete(Column.token, null, new String[]{}) > 0;
    }

    public void getTokenFromServer(String routerSsid, Consumer<Token> callback) {
        io.execute(() -> {
            GetAuthRequest request = new GetAuthRequest();
            request.routerSsid = routerSsid;

            Call<BaseResponse<GetAuthBody>> callGetAuth = integrateSystem.getAuth(calendarHandler.getCurrentDatetimeString(), request);
            callGetAuth.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<BaseResponse<GetAuthBody>> call, Response<BaseResponse<GetAuthBody>> response) {
                    if (response.isSuccessful()) {
                        if (response.body().header.resultCode.equals("00")) {
                            Token token1 = new Token(response.body().body.accessToken, response.body().body.refreshToken);
                            deviceInfoRepository.saveVehicleNumberToDatabase(response.body().body.vehicleCode, response.body().body.vehicleNumber);
                            if (saveTokenToDatabase(token1)) {
                                callback.accept(token1);
                            }
                        } else {
                            // 01 라우터와 매핑된 차량정보가 없을 때
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
        });

    }



    public LiveData<Event<String>> getRequestResult() {
        return this._requestResult;
    }
}
