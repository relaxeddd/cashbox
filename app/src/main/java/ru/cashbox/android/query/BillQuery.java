package ru.cashbox.android.query;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.cashbox.android.model.bill.BillResponseNumberWrapper;

public interface BillQuery {
    @POST("api/v1/bill")
    Call<BillResponseNumberWrapper> createBill(@Header("Authorization") String token, @Body Map<String, Object> payload);

    @PUT("api/v1/bill/{id}/change")
    Call<ResponseBody> updateBill(@Header("Authorization") String token, @Path("id") Integer id, @Body Map<String, Object> payload);

    @PATCH("api/v1/bill/{id}/status")
    Call<ResponseBody> changeStatus(@Header("Authorization") String token, @Path("id") Integer id, @Query("status") String status);

    @PATCH("api/v1/bill/{id}/status")
    Call<ResponseBody> changeStatus(@Header("Authorization") String token, @Path("id") Integer id,
                                    @Query("status") String status, @Query("payedType") String payedType);
}
