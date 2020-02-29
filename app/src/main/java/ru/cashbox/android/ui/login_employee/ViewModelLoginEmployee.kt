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
import ru.cashbox.android.model.repositories.RepositoryCashsessions
import ru.cashbox.android.model.repositories.RepositoryUsers

class ViewModelLoginEmployee(app: App, private val repositoryUsers: RepositoryUsers,
                             private val repositoryCashsessions: RepositoryCashsessions) : ViewModelBase(app) {

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
        uiScope.launch {
            val response = repositoryUsers.logoutTerminal()

            navigateEvent.value = NavigationEvent(EventType.LOADING_HIDE)
            showErrorIfIncorrect(response)
            navigateEvent.value = NavigationEvent(EventType.NAVIGATION_LOGIN_EMPLOYEE_TO_LOGIN_TERMINAL)
        }
    }

    private fun loginEmployee(pin: String) {
        navigateEvent.value = NavigationEvent(EventType.LOADING_SHOW)
        uiScope.launch {
            val response = repositoryUsers.loginEmployee(pin)
            val currentCashsession = if (response.isSuccessful) repositoryCashsessions.currentCashsession() else null

            navigateEvent.value = NavigationEvent(EventType.LOADING_HIDE)
            if (response.isSuccessful) {
                if (currentCashsession?.isSuccessful == true) {
                    navigateEvent.value = NavigationEvent(EventType.NAVIGATION_LOGIN_EMPLOYEE_TO_CASH)
                } else {
                    navigateEvent.value = NavigationEvent(EventType.NAVIGATION_LOGIN_EMPLOYEE_TO_EMPLOYEE_ROOM)
                }
            } else {
                val args = Bundle()
                args.putInt(CODE, response.code())
                navigateEvent.value = NavigationEvent(EventType.FRAGMENT_LOGIN_EMPLOYEE_PIN_ERROR, args)
                isErrorPinView.value = true
            }
        }
    }
}