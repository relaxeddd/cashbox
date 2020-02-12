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

    suspend fun requestLoginTerminal(username: String, password: String, rememberMe: Boolean = true) : Response<Session?> {
        val api = apiHelper

        if (!networkHelper.isNetworkAvailable() || api == null) {
            return Response.error(INTERNET_ERROR, ResponseBody.create(null, ""))
        }

        return api.loginTerminal(buildMapBody(Pair(USERNAME, username), Pair(PASSWORD, password), Pair(REMEMBER_ME, rememberMe.toString())))
    }

    suspend fun requestLoginEmployee(token: String, pin: String) : Response<Session?> {
        val api = apiHelper

        if (!networkHelper.isNetworkAvailable() || api == null) {
            return Response.error(INTERNET_ERROR, ResponseBody.create(null, ""))
        }

        return api.loginEmployee(buildJsonBody(Pair(TOKEN, token), Pair(PIN, pin)))
    }

    suspend fun requestCurrentUser(token: String?) : Response<User?> {
        val api = apiHelper

        if (!networkHelper.isNetworkAvailable() || api == null) {
            return Response.error(INTERNET_ERROR, ResponseBody.create(null, ""))
        }
        if (token == null || token.isEmpty()) {
            return Response.error(ARGS_ERROR, ResponseBody.create(null, ""))
        }

        return api.currentUser(token)
    }

    suspend fun requestLogout(token: String?) : Response<Void> {
        val api = apiHelper

        if (!networkHelper.isNetworkAvailable() || api == null) {
            return Response.error(INTERNET_ERROR, ResponseBody.create(null, ""))
        }
        if (token == null || token.isEmpty()) {
            return Response.error(ARGS_ERROR, ResponseBody.create(null, ""))
        }

        return api.logout(token, false)
    }

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

    private fun buildMapBody(vararg args: Pair<String, String>) : Map<String, String> {
        val map = HashMap<String, String>()
        args.forEach { map[it.first] = it.second }
        return map
    }

    private fun buildJsonBody(vararg args: Pair<String, Any>) : JSONObject {
        val json = JSONObject()
        args.forEach { json.put(it.first, it.second) }
        return json
    }
}