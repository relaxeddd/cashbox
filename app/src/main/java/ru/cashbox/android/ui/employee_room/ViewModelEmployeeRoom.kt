package ru.cashbox.android.ui.employee_room

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.launch
import ru.cashbox.android.App
import ru.cashbox.android.model.EventType
import ru.cashbox.android.model.NavigationEvent
import ru.cashbox.android.model.Session
import ru.cashbox.android.model.repositories.RepositoryUsers
import ru.cashbox.android.ui.base.ViewModelBase

class ViewModelEmployeeRoom(app: App, private val repositoryUsers: RepositoryUsers) : ViewModelBase(app) {

    val textEmployeeName = MutableLiveData("")

    private val sessionEmployeeObserver = Observer<Session?> { session ->
        textEmployeeName.value = session?.user?.fullname ?: ""
    }

    init {
        repositoryUsers.sessionEmployee.observeForever(sessionEmployeeObserver)
    }

    override fun onCleared() {
        super.onCleared()
        repositoryUsers.sessionEmployee.removeObserver(sessionEmployeeObserver)
    }

    fun clickButtonCashsession() {
        navigateEvent.value = NavigationEvent(EventType.NAVIGATION_EMPLOYEE_ROOM_TO_ENTER_NUMBER)
    }

    fun logout() {
        navigateEvent.value = NavigationEvent(EventType.LOADING_SHOW)
        uiScope.launch {
            val response = repositoryUsers.logoutEmployee()

            navigateEvent.value = NavigationEvent(EventType.LOADING_HIDE)
            showErrorIfIncorrect(response)
            navigateEvent.value = NavigationEvent(EventType.NAVIGATION_EMPLOYEE_ROOM_TO_LOGIN_EMPLOYEE)
        }
    }
}