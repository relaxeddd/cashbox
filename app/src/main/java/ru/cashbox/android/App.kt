package ru.cashbox.android

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.cashbox.android.model.LogAppState
import ru.cashbox.android.model.NetworkHelper
import ru.cashbox.android.model.http.ApiHelper
import ru.cashbox.android.model.prefs.SharedHelper
import ru.cashbox.android.model.repositories.*
import ru.cashbox.android.printer.PrinterHelper
import ru.cashbox.android.ui.StateService
import ru.cashbox.android.ui.ViewModelMain
import ru.cashbox.android.ui.cash.ViewModelCash
import ru.cashbox.android.ui.checks_archive.ViewModelChecksArchive
import ru.cashbox.android.ui.create_supply.ViewModelCreateSupply
import ru.cashbox.android.ui.discount.ViewModelDiscount
import ru.cashbox.android.ui.employee_room.ViewModelEmployeeRoom
import ru.cashbox.android.ui.enter_number.ViewModelEnterNumber
import ru.cashbox.android.ui.login_employee.ViewModelLoginEmployee
import ru.cashbox.android.ui.login_terminal.ViewModelLoginTerminal
import ru.cashbox.android.ui.preview.ViewModelPreview
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import java.util.*


class App : MultiDexApplication(), LifecycleObserver {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private lateinit var repositoryUsers: RepositoryUsers

    override fun onCreate() {
        super.onCreate()
        context = this

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        initDependencies()
        repositoryUsers = get().koin.get()
        StateService.repositoryUsers = repositoryUsers
        Locale.setDefault(Locale("pl", "PL"))
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppStop() {
        ioScope.launch { repositoryUsers.logAppState(LogAppState.MINIMIZE_SCREEN) }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppExpand() {
        ioScope.launch { repositoryUsers.logAppState(LogAppState.EXPAND_SCREEN) }
    }

    private fun initDependencies() {
        startKoin {

            androidLogger()
            androidContext(this@App)
            modules(module {
                factory { NetworkHelper(this@App) }

                single { SharedHelper(this@App) }

                single { RepositorySettings(get()) }
                single { ApiHelper(get(), get()) }

                single { RepositoryUsers(get(), get()) }
                single { RepositoryGoods(get(), get()) }
                single { RepositoryCashsessions(get(), get()) }
                single { RepositoryChecks(get(), get(), get()) }
                single { RepositoryBuys(get(), get()) }

                viewModel { ViewModelMain(this@App, get()) }
                viewModel { ViewModelPreview(this@App, get()) }
                viewModel { ViewModelLoginTerminal(this@App, get(), get()) }
                viewModel { ViewModelLoginEmployee(this@App, get(), get()) }
                viewModel { ViewModelEmployeeRoom(this@App, get()) }
                viewModel { ViewModelEnterNumber(this@App, get(), get()) }
                viewModel { ViewModelCash(this@App, get(), get(), get(), get(), get()) }
                viewModel { ViewModelChecksArchive(this@App, get()) }
                viewModel { ViewModelDiscount(this@App, get()) }
                viewModel { ViewModelCreateSupply(this@App, get(), get(), get(), get()) }
            })
            PrinterHelper.repositoryUsers = get().koin.get()
            RepositoryController.init(get().koin.get(), get().koin.get(), get().koin.get(), get().koin.get())
        }
    }
}