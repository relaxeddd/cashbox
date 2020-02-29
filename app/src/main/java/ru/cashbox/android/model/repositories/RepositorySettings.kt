package ru.cashbox.android.model.repositories

import androidx.lifecycle.MutableLiveData
import ru.cashbox.android.common.BASE_API_ADDRESS
import ru.cashbox.android.model.prefs.SharedHelper

class RepositorySettings(private val sharedHelper: SharedHelper) {

    val domain: MutableLiveData<String> = MutableLiveData(sharedHelper.getDomain())
    val apiAddress: MutableLiveData<String> = MutableLiveData(sharedHelper.getDomain())

    init {
        setApiAddress(domain.value)
    }

    fun setDomain(domainStr: String) {
        sharedHelper.setDomain(domainStr)
        domain.value = domainStr
        setApiAddress(domainStr)
    }

    private fun setApiAddress(domainStr: String?) {
        if (domainStr?.isNotEmpty() == true) {
            apiAddress.value = String.format(BASE_API_ADDRESS, domainStr)
        } else {
            apiAddress.value = ""
        }
    }
}