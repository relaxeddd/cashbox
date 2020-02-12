package ru.cashbox.android.refactor.preview

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.cashbox.android.App
import ru.cashbox.android.common.ViewModelBase
import ru.cashbox.android.model.EventType
import ru.cashbox.android.model.NavigationEvent
import ru.cashbox.android.model.repositories.RepositorySettings
import ru.cashbox.android.model.repositories.RepositoryUsers

class ViewModelPreview(app: App, val settings: RepositorySettings, val repositoryUsers: RepositoryUsers) : ViewModelBase(app) {

    override fun onViewResume() {
        super.onViewResume()

        ioScope.launch {
            val startTime = System.currentTimeMillis()
            val isLoggedIn = repositoryUsers.isLoggedTerminal()

            if (System.currentTimeMillis() - startTime < 1000) {
                delay(1000)
            }
            uiScope.launch {
                if (isLoggedIn) {
                    navigateEvent.value = NavigationEvent(EventType.NAVIGATION_PREVIEW_TO_LOGIN_EMPLOYEE)
                } else {
                    navigateEvent.value = NavigationEvent(EventType.NAVIGATION_PREVIEW_TO_LOGIN_TERMINAL)
                }
            }
        }
    }
}