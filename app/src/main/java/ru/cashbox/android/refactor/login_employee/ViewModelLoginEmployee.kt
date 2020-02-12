package ru.cashbox.android.refactor.login_employee

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.launch
import ru.cashbox.android.App
import ru.cashbox.android.common.CODE
import ru.cashbox.android.common.INTERNET_ERROR
import ru.cashbox.android.common.ViewModelBase
import ru.cashbox.android.model.EventType
import ru.cashbox.android.model.NavigationEvent
import ru.cashbox.android.model.repositories.RepositorySettings
import ru.cashbox.android.model.repositories.RepositoryUsers

class ViewModelLoginEmployee(app: App, val settings: RepositorySettings, val repositoryUsers: RepositoryUsers) : ViewModelBase(app) {

    companion object {
        private const val PIN_SIZE = 4
    }

    val textPin = MutableLiveData<String>("")
    val isErrorPinView = MutableLiveData(false)

    private val pinObserver = Observer<String?> {
        if (it != null && it.length >= PIN_SIZE) {
            loginEmployee(it)
        }
    }

    init {
        textPin.observeForever(pinObserver)
    }

    override fun onCleared() {
        super.onCleared()
        textPin.removeObserver(pinObserver)
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
        val args = Bundle()
        args.putInt(CODE, INTERNET_ERROR)
        navigateEvent.value = NavigationEvent(EventType.FRAGMENT_LOGIN_EMPLOYEE_PIN_ERROR, args)
        isErrorPinView.value = true
    }
}