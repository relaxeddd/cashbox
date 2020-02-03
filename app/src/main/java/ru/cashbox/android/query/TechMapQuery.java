package ru.cashbox.android.query;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import ru.cashbox.android.model.TechMap;

public interface TechMapQuery {
    @GET("api/v1/techmap/all")
    Call<List<TechMap>> getAllTechMaps(@Header("Authorization") String token);
}
