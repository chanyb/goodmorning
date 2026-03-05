package kr.co.kworks.goodmorning.model.network;

import kr.co.kworks.goodmorning.model.request.SysCommandRequest;
import kr.co.kworks.goodmorning.model.response.SysCommandResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CameraRequestInterface {

    @GET("/api/ptz.cgi")
    Call<ResponseBody> setPtz(
        @Query("PTZNumber") int ptzNumber,
        @Query("PanSpeed") int panSpeed,
        @Query("TiltSpeed") int tiltSpeed,
        @Query("ZoomSpeed") int zoomSpeed
    );

    @POST("/cgi-bin/SysCommand.cgi?TYPE=json")
    Call<SysCommandResponse> getSysCommand(
        @Body SysCommandRequest sysCommandRequest
    );

    @GET("/api/ptz.cgi")
    Call<ResponseBody> setAbsolutePtz(
        @Query("PTZNumber") int ptzNumber,
        @Query("GotoAbsolutePosition") String goToAbsolutePosition // 0,0,1
    );

    @GET("/api/ptz.cgi?PTZNumber=1&GetPTZPosition=do")
    Call<ResponseBody> getPtzPosition(
    );

}
