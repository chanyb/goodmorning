package kr.co.kworks.goodmorning.model.repository;

import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import kr.co.kworks.goodmorning.model.network.NetworkModule;
import kr.co.kworks.goodmorning.model.network.RequestInterface;
import kr.co.kworks.goodmorning.model.request.GetHelliListRequest;
import kr.co.kworks.goodmorning.model.request.GetVehicleListRequest;
import kr.co.kworks.goodmorning.model.response.BaseResponse;
import kr.co.kworks.goodmorning.model.response.GetHelliListResponse;
import kr.co.kworks.goodmorning.model.response.GetVehicleListResponse;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class IntegrateServerRepository {
    private RequestInterface api;
    private CalendarHandler calendarHandler;

    @Inject
    public IntegrateServerRepository(@NetworkModule.IntegrateSystem RequestInterface requestInterface) {
        api = requestInterface;
        calendarHandler = new CalendarHandler();
    }

    public void serviceHealthCheck(Consumer<Response<ResponseBody>> responseCallback, Consumer<Throwable> throwableCallback) {
        Call<ResponseBody> callServiceHealthCheck = api.serviceHealthCheck();
        callServiceHealthCheck.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                responseCallback.accept(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                throwableCallback.accept(throwable);
            }
        });
    }

    public void getVehicleList(
        String accessToken,
        GetVehicleListRequest getVehicleListRequest,
        Consumer<Response<BaseResponse<GetVehicleListResponse>>> responseCallback,
        Consumer<Throwable> throwableCallback
    ) {
        Call<BaseResponse<GetVehicleListResponse>> callGetVehicleList = api.getVehicleList(accessToken, calendarHandler.getCurrentDatetimeString(), getVehicleListRequest);
        callGetVehicleList.enqueue(new Callback<BaseResponse<GetVehicleListResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<GetVehicleListResponse>> call, Response<BaseResponse<GetVehicleListResponse>> response) {
                responseCallback.accept(response);
            }

            @Override
            public void onFailure(Call<BaseResponse<GetVehicleListResponse>> call, Throwable throwable) {
                throwableCallback.accept(throwable);
            }
        });
    }

    public void getHelliList(
        String accessToken,
        GetHelliListRequest getHelliListRequest,
        Consumer<Response<BaseResponse<GetHelliListResponse>>> responseCallback,
        Consumer<Throwable> throwableCallback
    ) {
        Call<BaseResponse<GetHelliListResponse>> callGetVehicleList = api.getHelliList(accessToken, calendarHandler.getCurrentDatetimeString(), getHelliListRequest);
        callGetVehicleList.enqueue(new Callback<BaseResponse<GetHelliListResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<GetHelliListResponse>> call, Response<BaseResponse<GetHelliListResponse>> response) {
                responseCallback.accept(response);
            }

            @Override
            public void onFailure(Call<BaseResponse<GetHelliListResponse>> call, Throwable throwable) {
                throwableCallback.accept(throwable);
            }
        });
    }

}
