package ru.cashbox.android.model.repositories

import retrofit2.Response
import ru.cashbox.android.common.CURRENT_ID_EMPLOYEE
import ru.cashbox.android.common.CURRENT_ID_TERMINAL
import ru.cashbox.android.model.Session
import ru.cashbox.android.model.db.AppDatabase
import ru.cashbox.android.model.http.ApiHelper

class RepositoryUsers(private val apiHelper: ApiHelper, private val appDatabase: AppDatabase) {

    val sessionTerminal = appDatabase.daoSession().getSession(CURRENT_ID_TERMINAL)
    val sessionEmployee = appDatabase.daoSession().getSession(CURRENT_ID_EMPLOYEE)

    init {
        sessionTerminal.observeForever { /*To init live data updates when db change*/ }
        sessionEmployee.observeForever {}
    }

    suspend fun loginTerminal(login: String, password: String) : Response<Session?> {
        val response = apiHelper.requestLoginTerminal(login, password)
        val session = response.body()

        if (response.isSuccessful && session != null) {
            session.innerId = CURRENT_ID_TERMINAL
            appDatabase.daoSession().insertSession(session)
        }

        return response
    }

    suspend fun loginEmployee(pin: String) : Response<Session?> {
        val response = apiHelper.requestLoginEmployee(sessionTerminal.value?.token, pin)
        val session = response.body()

        if (response.isSuccessful && session != null) {
            session.innerId = CURRENT_ID_EMPLOYEE
            appDatabase.daoSession().insertSession(session)
        }

        return response
    }

    suspend fun isLoggedTerminal() : Boolean {
        val token = appDatabase.daoSession().getSessionById(CURRENT_ID_TERMINAL)?.token ?: return false
        return apiHelper.requestCurrentUser(token).isSuccessful
    }

    suspend fun logoutTerminal() : Response<Void> {
        val response =  apiHelper.requestLogout(sessionTerminal.value?.token)

        if (response.isSuccessful) {
            appDatabase.daoSession().deleteSession(CURRENT_ID_TERMINAL)
        }

        return response
    }

    suspend fun logoutEmployee() : Response<Void> {
        val response =  apiHelper.requestLogout(sessionEmployee.value?.token)

        if (response.isSuccessful) {
            appDatabase.daoSession().deleteSession(CURRENT_ID_EMPLOYEE)
        }

        return response
    }
}