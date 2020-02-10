package ru.cashbox.android.model.repositories

import androidx.lifecycle.MutableLiveData
import ru.cashbox.android.model.prefs.SharedHelper

class RepositorySettings(val sharedHelper: SharedHelper) {

    val domain: MutableLiveData<String> = MutableLiveData(sharedHelper.getDomain())

    fun setDomain(domain: String) {
        sharedHelper.setDomain(domain)
        this.domain.value = domain
    }
}