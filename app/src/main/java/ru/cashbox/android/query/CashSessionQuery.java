package ru.cashbox.android.query;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface CashSessionQuery {
    @POST("api/v1/cashsession/open")
    Call<ResponseBody> openSession(@Header("Authorization") String token, @Body Map<String, Object> payload);

    @POST("api/v1/cashsession/close")
    Call<ResponseBody> closeSession(@Header("Authorization") String token, @Body Map<String, Object> payload);
}
