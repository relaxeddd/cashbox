package ru.cashbox.android.refactor.login_terminal

import android.view.View
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch
import ru.cashbox.android.App
import ru.cashbox.android.R
import ru.cashbox.android.common.ViewModelBase
import ru.cashbox.android.model.EventType
import ru.cashbox.android.model.NavigationEvent
import ru.cashbox.android.model.repositories.RepositorySettings
import ru.cashbox.android.model.repositories.RepositoryUser

class ViewModelLoginTerminal(app: App, val settings: RepositorySettings, val repositoryUser: RepositoryUser) : ViewModelBase(app) {

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

        uiScope.launch {
            navigateEvent.value = NavigationEvent(EventType.LOADING_SHOW)
            //repositoryUser.loginTerminal(login, password)
            navigateEvent.value = NavigationEvent(EventType.LOADING_HIDE)
            navigateEvent.value = NavigationEvent(EventType.NAVIGATION_LOGIN_TERMINAL_TO_LOGIN_EMPLOYEE)
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