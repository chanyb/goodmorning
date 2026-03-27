package kr.co.kworks.goodmorning.model.repository;

import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import kr.co.kworks.goodmorning.model.network.NetworkModule;
import kr.co.kworks.goodmorning.model.network.RequestInterface;
import kr.co.kworks.goodmorning.model.request.GetHelliListRequest;
import kr.co.kworks.goodmorning.model.request.GetVehicleListRequest;
import kr.co.kworks.goodmorning.model.request.UnlockRequest;
import kr.co.kworks.goodmorning.model.response.BaseResponse;
import kr.co.kworks.goodmorning.model.response.GetHelliListResponse;
import kr.co.kworks.goodmorning.model.response.GetVehicleListResponse;
import kr.co.kworks.goodmorning.model.response.UnlockResponse;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class ServerRepository {
    private RequestInterface api;

    @Inject
    public ServerRepository(@NetworkModule.Server RequestInterface requestInterface) {
        api = requestInterface;
    }

    public void unlock(
        UnlockRequest unlockRequest,
        Consumer<Response<UnlockResponse>> responseCallback,
        Consumer<Throwable> throwableCallback
    ) {
        Call<UnlockResponse> callUnlock = api.unlock(unlockRequest);
        callUnlock.enqueue(new Callback<UnlockResponse>() {
            @Override
            public void onResponse(Call<UnlockResponse> call, Response<UnlockResponse> response) {
                responseCallback.accept(response);
            }

            @Override
            public void onFailure(Call<UnlockResponse> call, Throwable t) {
                throwableCallback.accept(t);
            }
        });
    }

}
