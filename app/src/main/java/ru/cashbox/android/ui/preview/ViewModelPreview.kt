package ru.cashbox.android.ui.preview

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.cashbox.android.App
import ru.cashbox.android.ui.base.ViewModelBase
import ru.cashbox.android.model.EventType
import ru.cashbox.android.model.LogAppState
import ru.cashbox.android.model.NavigationEvent
import ru.cashbox.android.model.repositories.RepositoryUsers

class ViewModelPreview(app: App, private val repositoryUsers: RepositoryUsers) : ViewModelBase(app) {

    override fun onViewResume() {
        super.onViewResume()

        uiScope.launch {
            val startTime = System.currentTimeMillis()
            val isLoggedIn = repositoryUsers.isLoggedTerminal()

            if (System.currentTimeMillis() - startTime < 1000) {
                delay(1000)
            }
            if (isLoggedIn) {
                repositoryUsers.logAppState(LogAppState.OPEN_APP)
                navigateEvent.value = NavigationEvent(EventType.NAVIGATION_PREVIEW_TO_LOGIN_EMPLOYEE)
            } else {
                navigateEvent.value = NavigationEvent(EventType.NAVIGATION_PREVIEW_TO_LOGIN_TERMINAL)
            }
        }
    }
}