package ru.cashbox.android.refactor

import androidx.lifecycle.MutableLiveData
import ru.cashbox.android.App
import ru.cashbox.android.common.ViewModelBase
import ru.cashbox.android.model.repositories.RepositorySettings

class ViewModelMain(app: App, settings: RepositorySettings) : ViewModelBase(app) {

    val isShowLoading = MutableLiveData(false)

    override fun onCleared() {
        super.onCleared()
        //TODO
    }

    fun onShowLoadingAction() {
        isShowLoading.value = true
    }

    fun onHideLoadingAction() {
        isShowLoading.value = false
    }

    fun onViewCreate() {
        //TODO
    }
}