package ru.cashbox.android.ui.login_terminal

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.launch
import ru.cashbox.android.App
import ru.cashbox.android.R
import ru.cashbox.android.common.TEXT
import ru.cashbox.android.ui.base.ViewModelBase
import ru.cashbox.android.model.EventType
import ru.cashbox.android.model.LogAppState
import ru.cashbox.android.model.NavigationEvent
import ru.cashbox.android.model.repositories.RepositorySettings
import ru.cashbox.android.model.repositories.RepositoryUsers

class ViewModelLoginTerminal(app: App, private val settings: RepositorySettings,
                             private val repositoryUsers: RepositoryUsers) : ViewModelBase(app) {

    enum class EditFields {
        DOMAIN, LOGIN, PASSWORD
    }

    private val domain: LiveData<String> = settings.domain
    private var selectedField: EditFields? = if ((domain.value ?: "").isEmpty()) EditFields.DOMAIN else EditFields.LOGIN

    val textDomain = MutableLiveData(domain.value ?: "")
    val textLogin = MutableLiveData("")
    val textPassword = MutableLiveData("")

    //states: 1 - selected, 2 - error, else - not selected
    val isSelectedFieldDomain = MutableLiveData(if ((domain.value ?: "").isEmpty()) 1 else 0)
    val isSelectedFieldLogin = MutableLiveData(if ((domain.value ?: "").isEmpty()) 0 else 1)
    val isSelectedFieldPassword = MutableLiveData(0)
    val isVisibleFieldDomain = MutableLiveData((domain.value ?: "").isEmpty())

    private val domainObserver = Observer<String> { domainText ->
        isVisibleFieldDomain.value = domainText.isEmpty()
    }

    init {
        domain.observeForever(domainObserver)
    }

    override fun onCleared() {
        super.onCleared()
        domain.removeObserver(domainObserver)
    }

    override fun onViewResume() {
        super.onViewResume()
        if (domain.value?.isEmpty() == true) {
            val args = Bundle()
            args.putString(TEXT, domain.value ?: "")
            navigateEvent.value = NavigationEvent(EventType.DIALOG_ENTER_DOMAIN, args)
        }
    }

    fun login(login: String, password: String) {
        val domain = this.domain.value ?: ""

        if (domain.isEmpty()) {
            showToast("Введите поддомен")
            return
        }

        if (domain.isEmpty() || login.isEmpty() || password.isEmpty()) {
            getLiveDataFieldSelect(EditFields.DOMAIN).value = if (domain.isEmpty()) 2 else 0
            getLiveDataFieldSelect(EditFields.LOGIN).value = if (login.isEmpty()) 2 else 0
            getLiveDataFieldSelect(EditFields.PASSWORD).value = if (password.isEmpty()) 2 else 0
            showToast(R.string.check_data_is_correct)
            return
        }

        settings.setDomain(domain)

        navigateEvent.value = NavigationEvent(EventType.LOADING_SHOW)
        uiScope.launch {
            val response = repositoryUsers.loginTerminal(login, password)

            navigateEvent.value = NavigationEvent(EventType.LOADING_HIDE)
            if (response.isSuccessful) {
                repositoryUsers.logAppState(LogAppState.OPEN_APP)
                navigateEvent.value = NavigationEvent(EventType.NAVIGATION_LOGIN_TERMINAL_TO_LOGIN_EMPLOYEE)
            } else {
                showErrorIfIncorrect(response)
                textPassword.value = ""
            }
        }
    }

    fun clickSettings() {
        val args = Bundle()
        args.putString(TEXT, domain.value ?: "")
        navigateEvent.value = NavigationEvent(EventType.DIALOG_ENTER_DOMAIN, args)
    }

    fun onEnterDomain(domain: String) {
        settings.setDomain(domain)
    }

    fun selectField(selectedField: EditFields) {
        for (field in EditFields.values()) {
            getLiveDataFieldSelect(field).value = if (field == selectedField) 1 else 0
        }
        FragmentLoginTerminal@this.selectedField = selectedField
    }

    fun enterSymbol(symbol: Char) {
        val field = selectedField

        if (field != null) {
            getLiveDataFieldText(field).value += symbol
            getLiveDataFieldSelect(field).value = 1
        }
    }

    fun deleteLastSymbol() {
        val field = selectedField

        if (field != null) {
            val text = getLiveDataFieldText(field).value ?: ""

            if (text.isNotEmpty()) {
                getLiveDataFieldText(field).value = text.substring(0, text.length - 1)
            }
            getLiveDataFieldSelect(field).value = 1
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