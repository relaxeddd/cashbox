package ru.cashbox.android.ui.login_employee

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.launch
import ru.cashbox.android.App
import ru.cashbox.android.common.CODE
import ru.cashbox.android.ui.base.ViewModelBase
import ru.cashbox.android.model.EventType
import ru.cashbox.android.model.NavigationEvent
import ru.cashbox.android.model.Session
import ru.cashbox.android.model.repositories.RepositorySettings
import ru.cashbox.android.model.repositories.RepositoryUsers

class ViewModelLoginEmployee(app: App, private val settings: RepositorySettings,
                             private val repositoryUsers: RepositoryUsers) : ViewModelBase(app) {

    companion object {
        private const val PIN_SIZE = 4
    }

    val textPin = MutableLiveData<String>("")
    val textTerminalName = MutableLiveData<String>("")
    val isErrorPinView = MutableLiveData(false)

    private val sessionTerminalObserver = Observer<Session?> { session ->
        textTerminalName.value = session?.user?.fullname ?: ""
    }

    private val pinObserver = Observer<String?> {
        if (it != null && it.length >= PIN_SIZE) {
            loginEmployee(it)
        }
    }

    init {
        textPin.observeForever(pinObserver)
        repositoryUsers.sessionTerminal.observeForever(sessionTerminalObserver)
    }

    override fun onCleared() {
        super.onCleared()
        textPin.removeObserver(pinObserver)
        repositoryUsers.sessionTerminal.removeObserver(sessionTerminalObserver)
    }

    fun enterNumber(symbol: Char) {
        val text = textPin.value ?: return

        if (text.length < PIN_SIZE) {
            textPin.value += symbol
        } else {
            loginEmployee(text)
        }
    }

    fun deleteNumbers() {
        textPin.value = ""
        isErrorPinView.value = false
    }

    fun deleteLastNumber() {
        val text = textPin.value ?: ""

        if (text.isNotEmpty()) {
            textPin.value = text.substring(0, text.length - 1)
        }
        isErrorPinView.value = false
    }

    fun logout() {
        navigateEvent.value = NavigationEvent(EventType.LOADING_SHOW)

        ioScope.launch {
            val response = repositoryUsers.logoutTerminal()

            uiScope.launch {
                navigateEvent.value = NavigationEvent(EventType.LOADING_HIDE)

                if (response.isSuccessful) {
                    navigateEvent.value = NavigationEvent(EventType.NAVIGATION_LOGIN_EMPLOYEE_TO_LOGIN_TERMINAL)
                } else {
                    showErrorIfIncorrect(response)
                }
            }
        }
    }

    private fun loginEmployee(pin: String) {
        navigateEvent.value = NavigationEvent(EventType.LOADING_SHOW)

        ioScope.launch {
            val response = repositoryUsers.loginEmployee(pin)

            uiScope.launch {
                navigateEvent.value = NavigationEvent(EventType.LOADING_HIDE)

                if (response.isSuccessful) {
                    navigateEvent.value = NavigationEvent(EventType.NAVIGATION_LOGIN_EMPLOYEE_TO_EMPLOYEE_ROOM)
                } else {
                    val args = Bundle()
                    args.putInt(CODE, response.code())
                    navigateEvent.value = NavigationEvent(EventType.FRAGMENT_LOGIN_EMPLOYEE_PIN_ERROR, args)
                    isErrorPinView.value = true
                }
            }
        }
    }
}