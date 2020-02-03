package ru.cashbox.android.query;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import ru.cashbox.android.model.Category;

public interface CategoryQuery {
    @GET("api/v1/category/all")
    Call<List<Category>> getAllCategories(@Header("Authorization") String token,
                                          @Query("withDefault") Boolean withDefault);
}
