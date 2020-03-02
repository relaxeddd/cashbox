package ru.cashbox.android.model.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import okhttp3.ResponseBody
import retrofit2.Response
import ru.cashbox.android.App
import ru.cashbox.android.common.FORBIDDEN
import ru.cashbox.android.model.LogAppState
import ru.cashbox.android.model.Session
import ru.cashbox.android.model.http.ApiHelper
import ru.cashbox.android.model.prefs.SharedHelper
import ru.cashbox.android.printer.PrinterHelper

class RepositoryUsers(private val apiHelper: ApiHelper, sharedHelper: SharedHelper) {

    val tokenTerminal = MutableLiveData(sharedHelper.getTokenTerminal())
    val tokenEmployee = MutableLiveData(sharedHelper.getTokenEmployee())
    val sessionTerminal = MutableLiveData<Session>(null)
    val sessionEmployee = MutableLiveData<Session>(null)
    private var isLogAppStart = false
    private var isLogAppClose = false
    private var isFirstAppExpanded = false

    init {
        tokenTerminal.observeForever { sharedHelper.setTokenTerminal(it) }
        tokenEmployee.observeForever { sharedHelper.setTokenEmployee(it) }
    }

    suspend fun loginTerminal(login: String, password: String) : Response<Session?> {
        val response = apiHelper.requestLoginTerminal(login, password)
        val session = response.body()

        if (response.isSuccessful && session != null) {
            tokenTerminal.value = session.token
            sessionTerminal.value = session
        }

        return response
    }

    suspend fun loginEmployee(pin: String) : Response<Session?> {
        val token = tokenEmployee.value
        val responseCurrentEmployee = apiHelper.requestCurrentUser(token)
        val currentEmployee = responseCurrentEmployee.body()

        if (responseCurrentEmployee.isSuccessful && currentEmployee != null && token != null) {
            if (pin == currentEmployee.pin) {
                val session = Session(token, currentEmployee)
                sessionEmployee.value = session
                return Response.success(session)
            } else {
                return Response.error(FORBIDDEN, ResponseBody.create(null, ""))
            }
        } else {
            val response = apiHelper.requestLoginEmployee(tokenTerminal.value, pin)
            val session = response.body()

            if (response.isSuccessful && session != null) {
                tokenEmployee.value = session.token
                sessionEmployee.value = session
            } else {
                tokenEmployee.value = ""
            }

            return response
        }
    }

    suspend fun isLoggedTerminal() : Boolean {
        val token = tokenTerminal.value
        val responseUser = apiHelper.requestCurrentUser(token)
        val user = responseUser.body()

        if (responseUser.isSuccessful && user != null && !token.isNullOrEmpty()) {
            sessionTerminal.value = Session(token, user)
        }

        return responseUser.isSuccessful
    }

    suspend fun logoutTerminal() : Response<Void> {
        val response =  apiHelper.requestLogout(tokenTerminal.value)

        if (response.isSuccessful) {
            tokenTerminal.value = ""
            sessionTerminal.value = null
        }

        return response
    }

    suspend fun logoutEmployee() : Response<Void> {
        val response =  apiHelper.requestLogout(tokenEmployee.value)

        if (response.isSuccessful) {
            tokenEmployee.value = ""
            sessionEmployee.value = null
            //PrinterHelper.closeShift(App.context)
        }

        return response
    }

    suspend fun logAppState(state: String) : Response<Void> {
        val token = tokenTerminal.value

        Log.e("WWWWW", state)
        if ((isLogAppStart && state == LogAppState.OPEN_APP) || (isLogAppClose && state == LogAppState.CLOSE_APP) || token?.isEmpty() == true) {
            return Response.error(FORBIDDEN, ResponseBody.create(null, ""))
        }
        if (!isFirstAppExpanded && (state == LogAppState.EXPAND_SCREEN)) {
            isFirstAppExpanded = true
            return Response.error(FORBIDDEN, ResponseBody.create(null, ""))
        }
        if (state == LogAppState.CLOSE_APP) {
            isLogAppClose = true
        }

        val response = apiHelper.requestLogAppState(token, state)

        Log.e("WWWWW", "response: " + response.code())
        if (response.isSuccessful && state == LogAppState.OPEN_APP) {
            isLogAppStart = true
        }

        return response
    }
}