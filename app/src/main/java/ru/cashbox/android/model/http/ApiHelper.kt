package ru.cashbox.android.model.http

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.cashbox.android.common.*
import ru.cashbox.android.model.NetworkHelper
import ru.cashbox.android.model.Session
import ru.cashbox.android.model.User
import ru.cashbox.android.model.repositories.RepositorySettings
import java.util.concurrent.TimeUnit

class ApiHelper(settings: RepositorySettings, private val networkHelper: NetworkHelper) {

    private var retrofit: Retrofit? = null
    private var apiHelper: IApi? = null
    private val BASE_API_ADDRESS = "https://%s.inactivesearch.ru/_api/cashbox/"

    init {
        initApi(settings.domain.value)
        settings.domain.observeForever {
            initApi(it)
        }
    }

    suspend fun requestLoginTerminal(username: String?, password: String?, rememberMe: Boolean = true)
            = executeRequest({ api -> api.loginTerminal(buildMapBody(Pair(USERNAME, username), Pair(PASSWORD, password),
                Pair(REMEMBER_ME, rememberMe.toString()))) }, username, password)

    suspend fun requestLoginEmployee(token: String?, pin: String?) = executeRequest({ api ->
        api.loginEmployee(token!!, buildMapBody(Pair(PIN, pin))) }, token, pin)

    suspend fun requestCurrentUser(token: String?) = executeRequest({ api -> api.currentUser(token!!) }, token)

    suspend fun requestLogout(token: String?) = executeRequest({ api -> api.logout(token!!, false) }, token)

    //------------------------------------------------------------------------------------------------------------------
    suspend fun requestOpenCashsession(token: String?, balance: Double?) = executeRequest({ api ->
        api.openSession(token!!, buildMapBody(Pair(CASH_BALANCE, balance!!.toString()))) }, token, balance.toString())

    suspend fun requestCloseCashsession(token: String?, balance: Double?) = executeRequest({ api ->
        api.closeSession(token!!, buildMapBody(Pair(CASH_BALANCE, balance!!.toString()))) }, token, balance.toString())

    suspend fun requestCurrentCashsession(token: String?) = executeRequest({ api -> api.currentSession(token!!) }, token)

    //------------------------------------------------------------------------------------------------------------------
    private fun initApi(domain: String?) {
        if (domain?.isNotEmpty() == true) {
            val localRetrofit = ApiHelper@this.retrofit
            val okHttpClient = OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

            if (localRetrofit != null) {
                localRetrofit.newBuilder()
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(String.format(BASE_API_ADDRESS, domain))
                    .build()

                apiHelper = localRetrofit.create(IApi::class.java)
                retrofit = localRetrofit
            } else {
                val retrofit = Retrofit.Builder()
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(String.format(BASE_API_ADDRESS, domain))
                    .build()
                apiHelper = retrofit.create(IApi::class.java)
            }
        }
    }

    private fun buildMapBody(vararg args: Pair<String, String?>) : Map<String, String> {
        val map = HashMap<String, String>()
        args.forEach {
            val value = it.second
            if (value != null) map[it.first] = value
        }
        return map
    }

    private fun buildJsonBody(vararg args: Pair<String, Any>) : JSONObject {
        val json = JSONObject()
        args.forEach { json.put(it.first, it.second) }
        return json
    }

    private suspend fun <T> executeRequest(request: suspend (IApi) -> Response<T>, vararg args: String?) : Response<T> {
        val api = apiHelper

        if (!networkHelper.isNetworkAvailable() || api == null) {
            return Response.error(INTERNET_ERROR, ResponseBody.create(null, ""))
        }
        for (arg in args) {
            if (arg == null || arg.isEmpty()) {
                return Response.error(ARGS_ERROR, ResponseBody.create(null, ""))
            }
        }

        return request(api)
    }
}