package kr.co.kworks.goodmorning.model.network;

import kr.co.kworks.goodmorning.model.request.GetAutoRefreshRequest;
import kr.co.kworks.goodmorning.model.request.GetAuthRequest;
import kr.co.kworks.goodmorning.model.request.GetHelliListRequest;
import kr.co.kworks.goodmorning.model.request.GetVehicleListRequest;
import kr.co.kworks.goodmorning.model.request.SetVehicleRecptnDataRequest;
import kr.co.kworks.goodmorning.model.response.BaseResponse;
import kr.co.kworks.goodmorning.model.response.GetAuthBody;
import kr.co.kworks.goodmorning.model.request.SetVehicleDeviceMappingRequest;
import kr.co.kworks.goodmorning.model.response.GetHelliListResponse;
import kr.co.kworks.goodmorning.model.response.GetNewFireInfoResponse;
import kr.co.kworks.goodmorning.model.response.GetVehicleListResponse;
import kr.co.kworks.goodmorning.model.response.SetVehicleDeviceMappingResponseBody;
import kr.co.kworks.goodmorning.model.response.SetVehicleRecptnDataResponse;
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

    @POST("/api/vehicle/getAuto.do")
    Call<BaseResponse<GetAuthBody>> getAuth(
        @Header("stime") String sDatetime,
        @Body GetAuthRequest getAuthRequest
    );

    @POST("/api/vehicle/getAutoRefresh.do")
    Call<BaseResponse<GetAuthBody>> getAutoRefresh(
        @Header("stime") String sDatetime,
        @Body GetAutoRefreshRequest getAutoRefreshRequest
    );

    @POST("/api/vehicle/setVehicleDeviceMapping.do")
    Call<BaseResponse<SetVehicleDeviceMappingResponseBody>> setVehicleDeviceMapping(
        @Header("Authorization") String accessToken,
        @Header("stime") String sDatetime,
        @Body SetVehicleDeviceMappingRequest setVehicleDeviceMappingReqBody
    );

    @POST("/api/vehicle/setVehicleRecptnData.do")
    Call<BaseResponse<SetVehicleRecptnDataResponse>> setVehicleRecptnData(
        @Header("Authorization") String accessToken,
        @Header("stime") String sDatetime,
        @Body SetVehicleRecptnDataRequest setVehicleRecptnDataRequest
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

    @POST("/api/vehicle/aaa.do")
    Call<BaseResponse<GetNewFireInfoResponse>> test(
        @Header("Authorization") String accessToken,
        @Header("stime") String sDatetime,
        @Body SetVehicleRecptnDataRequest setVehicleRecptnDataRequest
    );


}
