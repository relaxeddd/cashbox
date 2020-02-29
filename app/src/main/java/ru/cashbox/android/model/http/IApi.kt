package ru.cashbox.android.model.http

import retrofit2.Response
import retrofit2.http.*
import ru.cashbox.android.model.*

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

    @GET("api/v1/cashsession/current")
    suspend fun currentSession(@Header("Authorization") token: String) : Response<Cashsession?>

    //-------------------------------------------goods------------------------------------------------------------------
    @GET("api/v1/category/all")
    suspend fun getCategories(@Header("Authorization") token: String, @Query("type") type: String,
                              @Query("withDefault") withDefault: Boolean) : Response<List<ResponseCategory>>

    @GET("api/v1/techmap/all")
    suspend fun getTechMaps(@Header("Authorization") token: String) : Response<List<ResponseTechMap>>

    @GET("api/v1/ingredient/all")
    suspend fun getIngredients(@Header("Authorization") token: String) : Response<List<ResponseIngredient>>

    //-------------------------------------------bills------------------------------------------------------------------
    @POST("api/v1/bill")
    suspend fun createBill(@Header("Authorization") token: String, @Body payload: HashMap<String, Any>) : Response<Bill>

    @GET("api/v1/bill/all")
    suspend fun getBills(@Header("Authorization") token: String, /*@Body body: HashMap<String, Any>, */@Query("sessionId") sessionId: Long,
                         @Query("paged") paged: Boolean = false) : Response<BillAnswer>

    @PUT("api/v1/bill/{id}/change")
    suspend fun changeBill(@Header("Authorization") token: String, @Path("id") id: Long,
                           @Body body: HashMap<String, Any>): Response<Bill>

    @PATCH("api/v1/bill/{id}/status")
    suspend fun changeBillStatus(@Header("Authorization") token: String, @Path("id") id: Long,
                             @Query("status") status: String,
                             @Query("payedType") payedType: String) : Response<Bill>

    //--------------------------------------------logs------------------------------------------------------------------
    @POST("api/v1/terminal/log")
    suspend fun logAppState(@Header("Authorization") token: String, @Body body: Map<String, String>): Response<Void>

    //--------------------------------------------buys------------------------------------------------------------------
    @GET("api/v1/vendor/all")
    suspend fun getBuyVendors(@Header("Authorization") token: String): Response<List<BuyVendor>>

    @GET("api/v1/account/all")
    suspend fun getBuyAccounts(@Header("Authorization") token: String): Response<List<BuyAccount>>

    @POST("api/v1/buy")
    suspend fun createBuy(@Header("Authorization") token: String, @Body buyForm: HashMap<String, Any>): Response<Buy>
}