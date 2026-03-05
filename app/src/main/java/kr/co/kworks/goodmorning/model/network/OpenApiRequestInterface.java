package kr.co.kworks.goodmorning.model.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface OpenApiRequestInterface {

    @GET("/ffas/openAPI/todayFire.do")
    Call<ResponseBody> todayFire(
    );

}
