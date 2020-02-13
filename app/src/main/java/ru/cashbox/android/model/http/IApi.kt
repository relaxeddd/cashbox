package ru.cashbox.android.model.http

import retrofit2.Response
import retrofit2.http.*
import ru.cashbox.android.model.Cashsession
import ru.cashbox.android.model.Session
import ru.cashbox.android.model.User

interface IApi {

    @POST("public/users/login")
    suspend fun loginTerminal(@Body body: Map<String, String>) : Response<Session?>

    @POST("users/terminal/login")
    suspend fun loginEmployee(@Header("Authorization") token: String, @Body jsonBody: Map<String, String>) : Response<Session?>

    @GET("users/current")
    suspend fun currentUser(@Header("Authorization") token: String) : Response<User?>

    /**
     * @param logoutAll removes all the user tokens.
     */
    @GET("users/logout")
    suspend fun logout(@Header("Authorization") token: String,
                       @Query("logoutAll") logoutAll: Boolean = true) : Response<Void>

    //------------------------------------cashsessions------------------------------------------------------------------
    @POST("api/v1/cashsession/open")
    suspend fun openSession(@Header("Authorization") token: String, @Body body: Map<String, String>) : Response<Cashsession?>

    @POST("api/v1/cashsession/close")
    suspend fun closeSession(@Header("Authorization") token: String, @Body body: Map<String, String>) : Response<Cashsession?>

    @POST("api/v1/cashsession/current")
    suspend fun currentSession(@Header("Authorization") token: String) : Response<Cashsession?>


}