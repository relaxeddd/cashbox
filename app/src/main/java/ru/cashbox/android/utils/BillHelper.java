package ru.cashbox.android.utils;

import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.cashbox.android.R;
import ru.cashbox.android.auth.LoginTerminalActivity;
import ru.cashbox.android.model.bill.BillModificatorWrapper;
import ru.cashbox.android.model.bill.BillResponseNumberWrapper;
import ru.cashbox.android.model.check.Check;
import ru.cashbox.android.model.check.CheckItem;
import ru.cashbox.android.model.types.CheckItemCategoryType;
import ru.cashbox.android.model.types.CheckStatus;
import ru.cashbox.android.model.types.PayType;
import ru.cashbox.android.query.BillQuery;
import ru.cashbox.android.saver.CheckStateSaver;
import ru.cashbox.android.saver.SlideViewSaver;

public class BillHelper {
    private static BillHelper billHelper;
    private Storage storage;
    private CheckStateSaver checkStateSaver;
    private SlideViewSaver slideViewSaver;

    private BillHelper() {
        storage = Storage.getStorage();
        checkStateSaver = CheckStateSaver.getInstance();
        slideViewSaver = SlideViewSaver.getInstance();
    }

    public static BillHelper getInstance() {
        return billHelper = billHelper == null ? new BillHelper() : billHelper;
    }

    public void updateCurrentCheck() {
        new UpdateCurrentCheckTask().execute();
    }

    public void createCheckAndInsertItem(final CheckItem checkItem) {
        BillQuery billQuery = RetrofitInstance.getRetrofit().create(BillQuery.class);
        billQuery.createBill(storage.getUserEmployeeSession().getToken(), buildBill(null, CheckStatus.OPENED)).enqueue(new Callback<BillResponseNumberWrapper>() {
            @Override
            public void onResponse(Call<BillResponseNumberWrapper> call, Response<BillResponseNumberWrapper> response) {
                if (response.code() == HttpStatus.SC_UNAUTHORIZED) {
                    Intent intent = new Intent(storage.getContext(), LoginTerminalActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    storage.getContext().startActivity(intent);
                }
                if (response.code() == HttpStatus.SC_OK) {
                    BillResponseNumberWrapper body = response.body();
                    Check check = checkStateSaver.createCheck(body.getId());
                    checkStateSaver.setCheckTitle(check.getNumber());
                    checkStateSaver.setCurrentCheck(check.getHash());
                    checkStateSaver.visibleCheck(true);

                    checkStateSaver.addItemToCurrentCheck(checkItem);

                    slideViewSaver.returnToStartState();
                    slideViewSaver.clear();

                    updateCurrentCheck();

                }
            }

            @Override
            public void onFailure(Call<BillResponseNumberWrapper> call, Throwable t) {

            }
        });
    }

    public void createBill() {
        BillQuery billQuery = RetrofitInstance.getRetrofit().create(BillQuery.class);
        billQuery.createBill(storage.getUserEmployeeSession().getToken(),
                buildBill(null, CheckStatus.OPENED)).enqueue(new Callback<BillResponseNumberWrapper>() {
            @Override
            public void onResponse(Call<BillResponseNumberWrapper> call, Response<BillResponseNumberWrapper> response) {
                if (response.code() == HttpStatus.SC_OK) {
                    BillResponseNumberWrapper body = response.body();
                    Check check = checkStateSaver.createCheck(body.getId());
                    checkStateSaver.setCheckTitle(check.getNumber());
                    checkStateSaver.setCurrentCheck(check.getHash());
                    checkStateSaver.visibleCheck(true);

                    slideViewSaver.returnToStartState();
                    slideViewSaver.clear();

                    Toast.makeText(storage.getContext(), String.format(storage.getContext()
                                    .getString(R.string.check_created), String.valueOf(check.getNumber())),
                            Toast.LENGTH_SHORT).show();
                }


                if (response.code() == HttpStatus.SC_UNAUTHORIZED) {
                    Intent intent = new Intent(storage.getContext(), LoginTerminalActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    storage.getContext().startActivity(intent);
                }

            }

            @Override
            public void onFailure(Call<BillResponseNumberWrapper> call, Throwable t) {

            }
        });
    }

    public void closeCheck(Integer number) {
        BillQuery billQuery = RetrofitInstance.getRetrofit().create(BillQuery.class);
        billQuery.changeStatus(storage.getUserEmployeeSession().getToken(), number,
                CheckStatus.CLOSED.name()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == HttpStatus.SC_UNAUTHORIZED) {
                    Intent intent = new Intent(storage.getContext(), LoginTerminalActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    storage.getContext().startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void paidCheck(Integer number, PayType payType) {
        BillQuery billQuery = RetrofitInstance.getRetrofit().create(BillQuery.class);
        billQuery.changeStatus(storage.getUserEmployeeSession().getToken(), number,
                CheckStatus.PAYED.name(), payType.name()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == HttpStatus.SC_UNAUTHORIZED) {
                    Intent intent = new Intent(storage.getContext(), LoginTerminalActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    storage.getContext().startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private class UpdateCurrentCheckTask extends AsyncTask<Void, Void, Response<ResponseBody>> {


        @Override
        protected void onPostExecute(Response<ResponseBody> responseBodyResponse) {
            if (responseBodyResponse.code() == HttpStatus.SC_UNAUTHORIZED) {
                Intent intent = new Intent(storage.getContext(), LoginTerminalActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                storage.getContext().startActivity(intent);
            }
        }

        @Override
        protected Response<ResponseBody> doInBackground(Void... params) {
            BillQuery billQuery = RetrofitInstance.getRetrofit().create(BillQuery.class);
            Check currentCheck = checkStateSaver.getCurrentCheck();
            if (currentCheck != null) {
                try {
                    return billQuery.updateBill(storage.getUserEmployeeSession().getToken(), currentCheck.getNumber(),
                            buildBill(currentCheck, CheckStatus.OPENED)).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

    public Map<String, Object> buildBill(Check check, CheckStatus checkStatus) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = new Date();
        Map<String, Object> data = new HashMap<>();
        data.put("opened", check == null ? dateFormat.format(date) : check.getCreated());
        data.put("closed", check == null ? dateFormat.format(date) : check.getCreated());
        data.put("sum", check == null ? 0.0 : checkStateSaver.countFullPrice(check));
        data.put("status", checkStatus.name());
        data.put("payedType", check == null ? PayType.UNDEFINED : check.getPayType());
        data.put("items", check == null ? new ArrayList<>() : buildItems(check.getItems()));
        data.put("techmaps", check == null ? new ArrayList<>() : buildTechMaps(check.getItems()));
        return data;
    }

    private List<Map<String, Object>> buildItems(List<CheckItem> items) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (CheckItem item : items) {
            if (item.getType().equals(CheckItemCategoryType.ITEM)) {
                Map<String, Object> data = new HashMap<>();
                data.put("itemId", item.getId());
                data.put("count", item.getAmount());
                data.put("price", item.getPrice());
                result.add(data);
            }
        }
        return result;
    }

    private List<Map<String, Object>> buildTechMaps(List<CheckItem> items) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (CheckItem item : items) {
            if (item.getType().equals(CheckItemCategoryType.TECHMAP)) {
                Map<String, Object> data = new HashMap<>();
                data.put("techmapId", item.getId());
                data.put("count", item.getAmount());
                data.put("price", item.getPrice());
                data.put("modificators", buildModificators(item.getModificators()));
                result.add(data);
            }
        }
        return result;
    }

    private List<Map<String, Object>> buildModificators(List<BillModificatorWrapper> modificators) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (modificators != null) {
            for (BillModificatorWrapper modificator : modificators) {
                Map<String, Object> data = new HashMap<>();
                data.put("modificatorId", modificator.getId());
                data.put("count", modificator.getCount());
                data.put("price", modificator.getPrice());
                result.add(data);
            }
        }
        return result;
    }

}
