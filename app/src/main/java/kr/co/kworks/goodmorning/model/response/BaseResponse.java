package kr.co.kworks.goodmorning.model.response;

import com.google.gson.annotations.SerializedName;

public class BaseResponse<T> {
    @SerializedName("header")
    public Header header;

    @SerializedName("body")
    public T body;
}
