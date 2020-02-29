package ru.cashbox.android.ui.enter_number

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.launch
import ru.cashbox.android.App
import ru.cashbox.android.R
import ru.cashbox.android.common.UNAUTHORIZED
import ru.cashbox.android.ui.base.ViewModelBase
import ru.cashbox.android.model.EventType
import ru.cashbox.android.model.NavigationEvent
import ru.cashbox.android.model.Session
import ru.cashbox.android.model.repositories.RepositoryCashsessions
import ru.cashbox.android.model.repositories.RepositoryUsers
import java.lang.NumberFormatException

class ViewModelEnterNumber(app: App, private val repositoryUsers: RepositoryUsers,
                           private val repositoryCashsessions: RepositoryCashsessions) : ViewModelBase(app) {

    val textEmployeeName = MutableLiveData("")
    val textBalance = MutableLiveData("")
    val textMainButton = MutableLiveData(getString(R.string.open_cashsession))
    val isErrorBalance = MutableLiveData(false)
    val isOpenCashbox = MutableLiveData(true)

    private val sessionEmployeeObserver = Observer<Session?> { session ->
        textEmployeeName.value = session?.user?.fullname ?: ""
    }

    private val isOpenCashboxObserver = Observer<Boolean> { isOpenCashbox ->
        if (!isOpenCashbox) {
            showToast(R.string.enter_balance)
        }
        textMainButton.value = getString(if (isOpenCashbox) R.string.open_cashsession else R.string.close_cashsession)
    }

    init {
        repositoryUsers.sessionEmployee.observeForever(sessionEmployeeObserver)
        isOpenCashbox.observeForever(isOpenCashboxObserver)
    }

    override fun onCleared() {
        super.onCleared()
        repositoryUsers.sessionEmployee.removeObserver(sessionEmployeeObserver)
        isOpenCashbox.removeObserver(isOpenCashboxObserver)
    }

    fun enterNumber(symbol: Char) {
        textBalance.value += symbol
        isErrorBalance.value = false
    }

    fun deleteNumbers() {
        textBalance.value = ""
        isErrorBalance.value = false
    }

    fun clickButtonCashsession() {
        var balance = 0.0
        val textBalanceValue = textBalance.value
        val isOpen = isOpenCashbox.value ?: true

        if (textBalanceValue == null || textBalanceValue.isEmpty()) {
            isErrorBalance.value = true
            showToast(R.string.enter_balance)
            return
        }

        try {
            balance = textBalanceValue.toDouble()
        } catch (e: NumberFormatException) {
            isErrorBalance.value = true
            showToast("Введите правильный остаток в кассе")
            return
        }

        if (balance == null) {
            isErrorBalance.value = true
            showToast(R.string.enter_balance)
            return
        }

        navigateEvent.value = NavigationEvent(EventType.LOADING_SHOW)
        uiScope.launch {
            val response = if (isOpen) repositoryCashsessions.openCashsession(balance) else repositoryCashsessions.closeCashsession(balance)

            navigateEvent.value = NavigationEvent(EventType.LOADING_HIDE)
            if (response.isSuccessful) {
                if (isOpen) {
                    navigateEvent.value = NavigationEvent(EventType.NAVIGATION_ENTER_NUMBER_TO_CASH)
                } else if (response.code() == UNAUTHORIZED) {
                    showToast(R.string.error_unauthorized)
                    navigateEvent.value = NavigationEvent(EventType.NAVIGATION_UNAUTHORIZED)
                } else {
                    textBalance.value = ""
                    isOpenCashbox.value = true
                    showToast(R.string.cashbox_closed)
                    navigateEvent.value = NavigationEvent(EventType.NAVIGATION_ENTER_NUMBER_TO_EMPLOYEE_ROOM)
                }
            } else {
                isErrorBalance.value = true
                showErrorIfIncorrect(response)
            }
        }
    }

    fun clickClose() {
        if (isOpenCashbox.value == true) {
            navigateEvent.value = NavigationEvent(EventType.NAVIGATION_ENTER_NUMBER_TO_EMPLOYEE_ROOM)
        } else {
            navigateEvent.value = NavigationEvent(EventType.NAVIGATION_ENTER_NUMBER_TO_CASH)
        }
    }
}