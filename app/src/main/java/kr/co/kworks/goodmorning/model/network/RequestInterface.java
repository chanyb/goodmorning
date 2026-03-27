package kr.co.kworks.goodmorning.model.network;

import kr.co.kworks.goodmorning.model.request.GetAutoRefreshRequest;
import kr.co.kworks.goodmorning.model.request.GetAuthRequest;
import kr.co.kworks.goodmorning.model.request.GetHelliListRequest;
import kr.co.kworks.goodmorning.model.request.GetVehicleListRequest;
import kr.co.kworks.goodmorning.model.request.UnlockRequest;
import kr.co.kworks.goodmorning.model.response.BaseResponse;
import kr.co.kworks.goodmorning.model.response.GetAuthBody;
import kr.co.kworks.goodmorning.model.response.GetHelliListResponse;
import kr.co.kworks.goodmorning.model.response.GetVehicleListResponse;
import kr.co.kworks.goodmorning.model.response.UnlockResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RequestInterface {

    @GET("/")
    Call<ResponseBody> serviceHealthCheck();

    @POST("/api/unlock.do")
    Call<UnlockResponse> unlock(
        @Body UnlockRequest unlockRequest
    );

    @POST("/api/vehicle/getVehicleList.do")
    Call<BaseResponse<GetVehicleListResponse>> getVehicleList(
        @Header("Authorization") String accessToken,
        @Header("stime") String sDatetime,
        @Body GetVehicleListRequest getVehicleListRequest
    );

    @POST("/api/vehicle/getHeliList.do")
    Call<BaseResponse<GetHelliListResponse>> getHelliList(
        @Header("Authorization") String accessToken,
        @Header("stime") String sDatetime,
        @Body GetHelliListRequest getHelliListRequest
    );

}
