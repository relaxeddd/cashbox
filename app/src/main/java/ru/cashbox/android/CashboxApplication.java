package ru.cashbox.android;

import android.app.Application;
import android.util.Log;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.distribute.Distribute;

import ru.cashbox.android.application.ContextProvider;
import ru.cashbox.android.application.ContextProviderImpl;
import ru.cashbox.android.saver.CheckStateSaver;
import ru.cashbox.android.saver.SlideViewSaver;
import ru.cashbox.android.utils.Storage;
import ru.cashbox.android.utils.Updater;

public class CashboxApplication extends Application implements ApplicationUtils {

    private static CashboxApplication instance;

    private ContextProvider contextProvider;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        contextProvider = new ContextProviderImpl();
        contextProvider.init(this);
        Storage.getStorage().init(getApplicationContext());
        CheckStateSaver.getInstance().init();
        SlideViewSaver.getInstance().init();

        AppCenter.setLogLevel(Log.VERBOSE);
        /***************************************************************************************/
        /** Этот кусок никуда не переносить, работает только после super.onCreate */
        //Distribute.setEnabledForDebuggableBuild(true);
        Distribute.setListener(new Updater());
        Distribute.setEnabled(true);
        AppCenter.start(this, getString(R.string.app_center_key),
                Analytics.class, Crashes.class, Distribute.class);
        /***************************************************************************************/
	}

	public static ApplicationUtils getInstance() {
        return instance;
    }

    @Override
    public ContextProvider getContextProvider() {
        return contextProvider;
    }
}
