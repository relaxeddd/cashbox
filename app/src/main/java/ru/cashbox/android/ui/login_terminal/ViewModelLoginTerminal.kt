package ru.cashbox.android.ui.login_terminal

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch
import ru.cashbox.android.App
import ru.cashbox.android.ui.base.ViewModelBase
import ru.cashbox.android.model.EventType
import ru.cashbox.android.model.NavigationEvent
import ru.cashbox.android.model.repositories.RepositorySettings
import ru.cashbox.android.model.repositories.RepositoryUsers

class ViewModelLoginTerminal(app: App, private val settings: RepositorySettings,
                             private val repositoryUsers: RepositoryUsers) : ViewModelBase(app) {

    enum class EditFields {
        DOMAIN, LOGIN, PASSWORD
    }

    private var selectedField: EditFields? = null

    val textDomain = MutableLiveData(settings.domain.value ?: "")
    val textLogin = MutableLiveData("")
    val textPassword = MutableLiveData("")

    val isSelectedFieldDomain = MutableLiveData(false)
    val isSelectedFieldLogin = MutableLiveData(false)
    val isSelectedFieldPassword = MutableLiveData(false)

    fun login(domain: String, login: String, password: String) {
        if (domain.isEmpty()) {
            return
        }

        settings.setDomain(domain)

        navigateEvent.value = NavigationEvent(EventType.LOADING_SHOW)

        ioScope.launch {
            val response = repositoryUsers.loginTerminal(login, password)

            uiScope.launch {
                navigateEvent.value = NavigationEvent(EventType.LOADING_HIDE)

                if (response.isSuccessful) {
                    navigateEvent.value = NavigationEvent(EventType.NAVIGATION_LOGIN_TERMINAL_TO_LOGIN_EMPLOYEE)
                } else {
                    showErrorIfIncorrect(response)
                    textPassword.value = ""
                }
            }
        }
    }

    fun selectField(selectedField: EditFields) {
        for (field in EditFields.values()) {
            getLiveDataFieldSelect(field).value = field == selectedField
        }
        FragmentLoginTerminal@this.selectedField = selectedField
    }

    fun enterSymbol(symbol: Char) {
        val filed = selectedField

        if (filed != null) {
            getLiveDataFieldText(filed).value += symbol
        }
    }

    fun deleteLastSymbol() {
        val filed = selectedField

        if (filed != null) {
            val text = getLiveDataFieldText(filed).value ?: ""

            if (text.isNotEmpty()) {
                getLiveDataFieldText(filed).value = text.substring(0, text.length - 1)
            }
        }
    }

    fun deleteSymbols() {
        val filed = selectedField

        if (filed != null) {
            getLiveDataFieldText(filed).value = ""
        }
    }

    private fun getLiveDataFieldText(field: EditFields) = when(field) {
        EditFields.DOMAIN -> textDomain
        EditFields.LOGIN -> textLogin
        EditFields.PASSWORD -> textPassword
    }

    private fun getLiveDataFieldSelect(field: EditFields) = when(field) {
        EditFields.DOMAIN -> isSelectedFieldDomain
        EditFields.LOGIN -> isSelectedFieldLogin
        EditFields.PASSWORD -> isSelectedFieldPassword
    }
}