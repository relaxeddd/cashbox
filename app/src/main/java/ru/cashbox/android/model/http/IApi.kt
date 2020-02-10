package ru.cashbox.android.model.http

import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.*
import ru.cashbox.android.model.Session
import ru.cashbox.android.model.User

interface IApi {

    @POST("public/users/login")
    suspend fun loginTerminal(@Body jsonBody: JSONObject) : Response<Session?>

    @POST("users/terminal/login")
    suspend fun loginEmployee(@Body jsonBody: JSONObject) : Response<Session?>

    @GET("users/terminal/login")
    suspend fun currentEmployee(@Header("Authorization") token: String) : Response<User?>

    /**
     * @param logoutAll removes all the user tokens.
     */
    @GET("users/terminal/login")
    suspend fun logoutEmployee(@Header("Authorization") token: String,
                               @Query("logoutAll") logoutAll: Boolean) : Response<Void>

    //------------------------------------cashsessions------------------------------------------------------------------

}