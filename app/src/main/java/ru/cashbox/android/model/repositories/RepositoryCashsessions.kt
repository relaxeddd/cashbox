package ru.cashbox.android.model.repositories

import retrofit2.Response
import ru.cashbox.android.common.CURRENT_ID_CASHSESSION
import ru.cashbox.android.model.Cashsession
import ru.cashbox.android.model.db.AppDatabase
import ru.cashbox.android.model.http.ApiHelper

class RepositoryCashsessions(private val apiHelper: ApiHelper, private val appDatabase: AppDatabase,
                             repositoryUsers: RepositoryUsers) {

    val cashsession = appDatabase.daoSession().getSession(CURRENT_ID_CASHSESSION)
    val sessionEmployee = repositoryUsers.sessionEmployee

    init {
        cashsession.observeForever { /*To init live data updates when db change*/ }
    }

    suspend fun openCashsession(balance: Double?) : Response<Cashsession?> {
        val response = apiHelper.requestOpenCashsession(sessionEmployee.value?.token, balance)
        val session = response.body()

        if (response.isSuccessful && session != null) {
            session.innerId = CURRENT_ID_CASHSESSION
            appDatabase.daoCashsession().insertCashsession(session)
        }

        return response
    }

    suspend fun closeCashsession(balance: Double?) : Response<Cashsession?> {
        val response = apiHelper.requestCloseCashsession(sessionEmployee.value?.token, balance)

        if (response.isSuccessful) {
            appDatabase.daoCashsession().deleteCashsession(CURRENT_ID_CASHSESSION)
        }

        return response
    }

    suspend fun currentCashsession() : Response<Cashsession?> {
        val response = apiHelper.requestCurrentCashsession(sessionEmployee.value?.token)
        val session = response.body()

        if (response.isSuccessful && session != null) {
            session.innerId = CURRENT_ID_CASHSESSION
            appDatabase.daoCashsession().insertCashsession(session)
        }

        return response
    }
}