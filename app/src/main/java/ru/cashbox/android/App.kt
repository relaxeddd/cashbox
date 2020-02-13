package ru.cashbox.android

import android.annotation.SuppressLint
import android.content.Context
import androidx.multidex.MultiDexApplication
import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.cashbox.android.common.DATABASE_NAME
import ru.cashbox.android.model.NetworkHelper
import ru.cashbox.android.model.db.AppDatabase
import ru.cashbox.android.model.http.ApiHelper
import ru.cashbox.android.model.prefs.SharedHelper
import ru.cashbox.android.model.repositories.RepositoryCashsessions
import ru.cashbox.android.model.repositories.RepositorySettings
import ru.cashbox.android.model.repositories.RepositoryUsers
import ru.cashbox.android.ui.ViewModelMain
import ru.cashbox.android.ui.employee_room.ViewModelEmployeeRoom
import ru.cashbox.android.ui.login_employee.ViewModelLoginEmployee
import ru.cashbox.android.ui.login_terminal.ViewModelLoginTerminal
import ru.cashbox.android.ui.preview.ViewModelPreview

class App : MultiDexApplication() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        initDependencies()
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

                single { SharedHelper(this@App) }

                single { RepositorySettings(get()) }
                single { ApiHelper(get(), get()) }

                single { RepositoryUsers(get(), get()) }
                single { RepositoryCashsessions(get(), get(), get()) }

                viewModel { ViewModelMain(this@App, get()) }
                viewModel { ViewModelPreview(this@App, get(), get()) }
                viewModel { ViewModelLoginTerminal(this@App, get(), get()) }
                viewModel { ViewModelLoginEmployee(this@App, get(), get()) }
                viewModel { ViewModelEmployeeRoom(this@App, get(), get()) }
            })
        }
    }
}