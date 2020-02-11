package ru.cashbox.android.model.repositories

import androidx.lifecycle.MutableLiveData
import ru.cashbox.android.model.Session
import ru.cashbox.android.model.http.ApiHelper

class RepositoryUser(val apiHelper: ApiHelper) {

    var liveDataSessionTerminal = MutableLiveData<Session?>(null)
    var liveDataSessionUser = MutableLiveData<Session?>(null)

    suspend fun loginTerminal(login: String, password: String) {
        val response =  apiHelper.requestLoginTerminal(login, password)

        if (response.isSuccessful) {
            liveDataSessionTerminal.value = response.body()
        }


    }
}