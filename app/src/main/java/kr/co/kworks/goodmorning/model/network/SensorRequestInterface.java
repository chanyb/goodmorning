package kr.co.kworks.goodmorning.model.network;

import kr.co.kworks.goodmorning.model.response.SensorQueryResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface SensorRequestInterface {

    @GET("/?command=dataquery&format=json&mode=most-recent&records=1&uri=dl:DATA_01M")
    Call<SensorQueryResponse> getSensorData(
    );

}
