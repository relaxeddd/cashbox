package ru.cashbox.android.query;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import ru.cashbox.android.model.Session;
import ru.cashbox.android.model.User;

public interface AuthQuery {

    @POST("public/users/login")
    Call<Session> loginTerminal(@Body Map<String, Object> payload);

    @GET("users/current")
    Call<User> getUserByToken(@Header("Authorization") String token);

    @GET("users/logout")
    Call<ResponseBody> logout(@Header("Authorization") String token,
                              @Query("logoutAll") Boolean logoutAll);

    @POST("users/terminal/login")
    Call<Session> loginEmployee(@Header("Authorization") String token,
                                            @Body Map<String, Object> payload);

}
