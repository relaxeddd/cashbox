package ru.cashbox.android.ui

import kotlinx.coroutines.launch
import ru.cashbox.android.App
import ru.cashbox.android.R
import ru.cashbox.android.model.EventType
import ru.cashbox.android.model.LogAppState
import ru.cashbox.android.model.NavigationEvent
import ru.cashbox.android.model.repositories.RepositoryUsers
import ru.cashbox.android.ui.base.ViewModelBase
import java.util.*

class ViewModelMain(app: App, private val repositoryUsers: RepositoryUsers) : ViewModelBase(app) {

    companion object {
        private const val TIMER_EXIT = 1000L
    }

    private var exitTimer: Timer? = null

    fun onActivityCreate() {}

    fun onActivityDestroy() {
        ioScope.launch { repositoryUsers.logAppState(LogAppState.CLOSE_APP) }
    }

    override fun onBackPressed() {
        if (exitTimer != null) {
            navigateEvent.value = NavigationEvent(EventType.EXIT)
        } else {
            showToast(R.string.push_back_another_one_to_exit)
            exitTimer = Timer()
            exitTimer?.schedule(object: TimerTask() {
                override fun run() {
                    exitTimer = null
                }
            }, TIMER_EXIT)
        }
    }
}