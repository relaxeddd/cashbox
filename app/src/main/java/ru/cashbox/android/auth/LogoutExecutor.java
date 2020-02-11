package ru.cashbox.android.auth;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Response;
import ru.cashbox.android.R;
import ru.cashbox.android.query.AuthQuery;
import ru.cashbox.android.utils.RetrofitInstance;
import ru.cashbox.android.utils.Storage;

public class LogoutExecutor extends AsyncTask<Boolean, Void, Integer> {

    private Logout logout;
    private AuthQuery authQuery;
    private Storage storage;
    private static final String LOGOUT_TAG = "LOGOUT";
    private String balance;

    public LogoutExecutor(Logout logout, String balance) {
        this.logout = logout;
        this.balance = balance;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        storage = Storage.getStorage();
        authQuery = RetrofitInstance.getRetrofit().create(AuthQuery.class);
    }

    @Override
    protected Integer doInBackground(Boolean... booleans) {
        Boolean logoutAll = booleans[0];
        try {
            Response<ResponseBody> execute = authQuery.logout(storage.getUserEmployeeSession().getToken(), logoutAll).execute();
            if (execute == null) {
                return null;
            }
            return execute.code();
        } catch (IOException e) {
            Log.e(LOGOUT_TAG, storage.getContext().getString(R.string.internal_error), e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Integer code) {
        super.onPostExecute(code);
        logout.onFinishedLogout(code, balance);
    }
}
