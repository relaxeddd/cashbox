package ru.cashbox.android.utils;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {
    public static Retrofit getRetrofit()
    {
        Storage storage = Storage.getStorage();
        return new Retrofit.Builder()
                    .baseUrl(storage.getBaseApiAddress())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
    }
}
