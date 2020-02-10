package ru.cashbox.android

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDexApplication
import androidx.room.Room
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.cashbox.android.common.DATABASE_NAME
import ru.cashbox.android.model.NetworkHelper
import ru.cashbox.android.model.db.AppDatabase
import ru.cashbox.android.model.http.ApiHelper
import ru.cashbox.android.model.prefs.SharedHelper
import ru.cashbox.android.model.repositories.RepositorySettings
import ru.cashbox.android.saver.CheckStateSaver
import ru.cashbox.android.saver.SlideViewSaver
import ru.cashbox.android.utils.Storage
import ru.cashbox.android.utils.Updater

class App : MultiDexApplication() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        initDependencies()

        Storage.getStorage().init(applicationContext)
        CheckStateSaver.getInstance().init()
        SlideViewSaver.getInstance().init()
        AppCenter.setLogLevel(Log.VERBOSE)
        /***************************************************************************************/
        /** Этот кусок никуда не переносить, работает только после super.onCreate */
        //Distribute.setEnabledForDebuggableBuild(true);
        Distribute.setListener(Updater())
        Distribute.setEnabled(true)
        AppCenter.start(this, getString(R.string.app_center_key),
            Analytics::class.java, Crashes::class.java, Distribute::class.java)
        /***************************************************************************************/
    }

    private fun initDependencies() {
        startKoin {
            androidContext(this@App)
            modules(module {
                single {
                    Room.databaseBuilder(this@App, AppDatabase::class.java, DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build()
                }
                factory { NetworkHelper(this@App) }

                single { ApiHelper(get()) }

                single { SharedHelper(this@App) }
                single { RepositorySettings(get()) }
            })
        }
    }
}