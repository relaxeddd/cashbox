package ru.cashbox.android.model.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Response
import ru.cashbox.android.App
import ru.cashbox.android.model.Cashsession
import ru.cashbox.android.model.http.ApiHelper
import ru.cashbox.android.printer.PrinterHelper

class RepositoryCashsessions(private val apiHelper: ApiHelper, repositoryUsers: RepositoryUsers) {

    private val tokenEmployee: LiveData<String> = repositoryUsers.tokenEmployee
    val cashsession = MutableLiveData<Cashsession>(null)
    var repositoryChecks: RepositoryChecks? = null

    fun clear() {
        cashsession.value = null
    }

    suspend fun openCashsession(balance: Double?) : Response<Cashsession?> {
        val response = apiHelper.requestOpenCashsession(tokenEmployee.value, balance)
        val session = response.body()

        if (response.isSuccessful && session != null) {
            cashsession.value = session
        }

        return response
    }

    suspend fun closeCashsession(balance: Double?) : Response<Cashsession?> {
        val response = apiHelper.requestCloseCashsession(tokenEmployee.value, balance)

        if (response.isSuccessful) {
            cashsession.value = null
            repositoryChecks?.clear()
            PrinterHelper.closeShift(App.context)
        }

        return response
    }

    suspend fun currentCashsession() : Response<Cashsession?> {
        val response = apiHelper.requestCurrentCashsession(tokenEmployee.value)
        val session = response.body()

        if (response.isSuccessful && session != null) {
            cashsession.value = session
        }

        return response
    }
}