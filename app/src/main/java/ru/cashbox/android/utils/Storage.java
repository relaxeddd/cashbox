package ru.cashbox.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.DecimalFormat;

import lombok.Getter;
import lombok.Setter;
import ru.cashbox.android.data.session.ShiftRepositoryImpl;
import ru.cashbox.android.data.session.UserEmployeeRepositoryImpl;
import ru.cashbox.android.data.session.UserTerminalRepositoryImpl;
import ru.cashbox.android.domain.session.ShiftRepository;
import ru.cashbox.android.domain.session.UserEmployeeRepository;
import ru.cashbox.android.domain.session.UserSessionInteractor;
import ru.cashbox.android.domain.session.UserSessionInteractorImpl;
import ru.cashbox.android.domain.session.UserTerminalRepository;
import ru.cashbox.android.model.auth.Session;
import ru.cashbox.android.query.AuthQuery;

public class Storage {

    private static final String BASE_API_ADDRESS = "https://%s.inactivesearch.ru/_api/cashbox/";
    public static final String APP_NAME = "Cashbox";
    public static int DEFAULT_POPUP_WIDTH = 500;
    public static int DEFAULT_POPUP_HISTORY_WIDTH = 800;
    public static String DEFAULT_PRINTER_PORT = "5555";
    public static String EMPTY = "";
    private static final String DOMAIN_TAG = "DOMAIN";

    private static Storage storage;
    private Context context;

    private UserSessionInteractor userSessions;

    public Session getUserEmployeeSession() {
        return userSessions.getUserEmployeeSession();
    }

    public void setUserEmployeeSession(Session userEmployeeSession) {
        userSessions.saveEmployeeSession(userEmployeeSession);
    }

    public Session getUserTerminalSession() {
        return userSessions.getUserTerminalSession();
    }

    public void setUserTerminalSession(Session userTerminalSession) {
        userSessions.saveTerminalSession(userTerminalSession);
    }

    public void logoutFromTerminal() {
        userSessions.dropAllSessions();
    }

    public ShiftRepository getShiftManager() {
        return userSessions.getShiftRepository();
    }

    @Getter @Setter
    private String balance;

    public static Storage getStorage()
    {
        return storage = storage == null ? new Storage() : storage;
    }

    public void init(Context context)
    {
        this.context = context;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Storage.APP_NAME + " Settings", Context.MODE_PRIVATE);
        AuthQuery authQuery = RetrofitInstance.getRetrofit().create(AuthQuery.class);
        UserTerminalRepository terminalRepository = new UserTerminalRepositoryImpl(sharedPreferences, authQuery);
        UserEmployeeRepository employeeRepository = new UserEmployeeRepositoryImpl(sharedPreferences, authQuery);
        ShiftRepository shiftRepository = new ShiftRepositoryImpl(sharedPreferences);
        userSessions = new UserSessionInteractorImpl(terminalRepository, employeeRepository, shiftRepository);
    }

    public Context getContext() {
        return context;
    }

    public String checkBalanceValue(String value) {
        try {
            double result = Double.parseDouble(value);
            if (result < 0) {
                return null;
            }
            DecimalFormat df = new DecimalFormat("#.##");
            return df.format(result);
        } catch (Exception ignore) {}
        return null;
    }

    public void saveDomain(String domain) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Storage.APP_NAME + " Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DOMAIN_TAG, domain);
        editor.apply();
    }

    public String getDomain() {
        SharedPreferences sharedPreferences = storage.getContext()
                .getSharedPreferences(Storage.APP_NAME + " Settings", Context.MODE_PRIVATE);
        return sharedPreferences.getString(DOMAIN_TAG, null);
    }

    public String getBaseApiAddress() {
        String domain = getDomain();
        if (domain != null) {
            return String.format(BASE_API_ADDRESS, domain);
        }
        return String.format(BASE_API_ADDRESS, "cashbox");
    }
}

