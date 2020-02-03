package ru.cashbox.android.utils;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.cashbox.android.query.CashSessionQuery;

public class CashSessionHelper {

    private static CashSessionHelper cashSessionHelper;
    private Storage storage;

    public CashSessionHelper() {
        storage = Storage.getStorage();
    }

    public static CashSessionHelper getInstance() {
        return cashSessionHelper = cashSessionHelper == null ? new CashSessionHelper() : cashSessionHelper;
    }

    public void openSession() {
        CashSessionQuery cashSessionQuery = RetrofitInstance.getRetrofit().create(CashSessionQuery.class);
        cashSessionQuery.openSession(storage.getUserEmployeeSession().getToken(), buildPayload()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

        storage.getShiftManager().openShift();
    }

    public void closeSession() {
        CashSessionQuery cashSessionQuery = RetrofitInstance.getRetrofit().create(CashSessionQuery.class);
        cashSessionQuery.closeSession(storage.getUserEmployeeSession().getToken(), buildPayload()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

        storage.getShiftManager().closeShift();
    }

    private Map<String, Object> buildPayload() {
        Map<String, Object> data = new HashMap<>();
        data.put("cashBalance", storage.getBalance().replaceAll(",", "."));
        return data;
    }

}
