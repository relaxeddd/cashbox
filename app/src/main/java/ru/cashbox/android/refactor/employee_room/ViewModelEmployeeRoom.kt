package ru.cashbox.android.refactor.employee_room

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.launch
import ru.cashbox.android.App
import ru.cashbox.android.common.ViewModelBase
import ru.cashbox.android.model.EventType
import ru.cashbox.android.model.NavigationEvent
import ru.cashbox.android.model.Session
import ru.cashbox.android.model.repositories.RepositoryCashsessions
import ru.cashbox.android.model.repositories.RepositoryUsers

class ViewModelEmployeeRoom(app: App, private val settings: RepositoryCashsessions,
                            private val repositoryUsers: RepositoryUsers) : ViewModelBase(app) {

    val textEmployeeName = MutableLiveData("")
    val textBalance = MutableLiveData("")
    val isErrorPinView = MutableLiveData(false)

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

    fun enterNumber(symbol: Char) {
        textBalance.value += symbol
        isErrorPinView.value = false
    }

    fun deleteNumbers() {
        textBalance.value = ""
        isErrorPinView.value = false
    }

    fun deleteLastNumber() {
        val text = textBalance.value ?: ""

        if (text.isNotEmpty()) {
            textBalance.value = text.substring(0, text.length - 1)
        }
        isErrorPinView.value = false
    }

    fun openCashsession() {
        isErrorPinView.value = true
    }

    fun logout() {
        navigateEvent.value = NavigationEvent(EventType.LOADING_SHOW)

        ioScope.launch {
            val response = repositoryUsers.logoutEmployee()

            uiScope.launch {
                navigateEvent.value = NavigationEvent(EventType.LOADING_HIDE)

                if (response.isSuccessful) {
                    navigateEvent.value = NavigationEvent(EventType.NAVIGATION_EMPLOYEE_ROOM_TO_LOGIN_EMPLOYEE)
                } else {
                    showErrorIfIncorrect(response)
                }
            }
        }
    }
}