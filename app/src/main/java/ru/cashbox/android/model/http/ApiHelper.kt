package ru.cashbox.android.model.http

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.cashbox.android.common.*
import ru.cashbox.android.model.*
import ru.cashbox.android.model.repositories.RepositorySettings
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap
import kotlin.math.roundToInt

class ApiHelper(settings: RepositorySettings, private val networkHelper: NetworkHelper) {

    private var retrofit: Retrofit? = null
    private var apiHelper: IApi? = null

    init {
        initApi(settings.apiAddress.value)
        settings.apiAddress.observeForever {
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
    suspend fun requestIngredients(token: String?) = executeRequest({ api -> api.getIngredients(token!!) }, token)

    suspend fun requestCategories(token: String?, type: String? = CategoryType.TERMINAL, withDefault: Boolean = true)
            = executeRequest({ api -> api.getCategories(token!!, type!!, withDefault) }, token, type)

    suspend fun requestTechMaps(token: String?) = executeRequest({ api -> api.getTechMaps(token!!) }, token)

    //------------------------------------------------------------------------------------------------------------------
    suspend fun requestCreateBill(token: String?) = executeRequest(
        { api -> api.createBill(token!!, buildBillBody()) }, token)

    suspend fun requestBills(token: String?, sessionId: Long) = executeRequest(
        { api -> api.getBills(token!!, sessionId) }, token)

    suspend fun requestChangeBill(token: String?, id: Long, bill: Bill) = executeRequest(
        { api -> api.changeBill(token!!, id, buildBillBody(bill)) }, token)

    suspend fun requestChangeBillStatus(token: String?, id: Long, status: String?, paidType: String?) = executeRequest(
        { api -> api.changeBillStatus(token!!, id, status!!, paidType!!) }, token, status, paidType)

    //------------------------------------------------------------------------------------------------------------------
    suspend fun requestLogAppState(token: String?, state: String?) = executeRequest({ api -> api.logAppState(token!!, buildMapBody(Pair(TYPE, state))) }, token, state)

    //------------------------------------------------------------------------------------------------------------------
    suspend fun requestBuyVendors(token: String?) = executeRequest({ api -> api.getBuyVendors(token!!) }, token)

    suspend fun requestBuyAccounts(token: String?) = executeRequest({ api -> api.getBuyAccounts(token!!) }, token)

    suspend fun requestCreateBuy(token: String?, buy: Buy?) = executeRequest({ api -> api.createBuy(token!!, buildBuyBody(buy)) }, token)

    //------------------------------------------------------------------------------------------------------------------
    private fun initApi(apiAddress: String?) {
        if (apiAddress?.isNotEmpty() == true) {
            val localRetrofit = this.retrofit
            val okHttpClient = OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

            if (localRetrofit != null) {
                localRetrofit.newBuilder()
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(apiAddress)
                    .build()

                apiHelper = localRetrofit.create(IApi::class.java)
                retrofit = localRetrofit
            } else {
                val retrofit = Retrofit.Builder()
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(apiAddress)
                    .build()
                apiHelper = retrofit.create(IApi::class.java)
            }
        }
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

    private fun buildMapBody(vararg args: Pair<String, String?>) : Map<String, String> {
        val map = HashMap<String, String>()
        args.forEach {
            val value = it.second
            if (value != null) map[it.first] = value
        }
        return map
    }

    private fun buildBillBody() : HashMap<String, Any> {
        val body = HashMap<String, Any>()

        body[OPENED] = getCurrentDate()
        body[CLOSED] = getCurrentDate()
        body[SUM] = 0.0
        body[STATUS] = CheckStatus.OPENED
        body[PAYED_TYPE] = PayType.UNDEFINED
        body[ITEMS] = emptyList<HashMap<String, Any>>()
        body[TECHMAPS] = emptyList<HashMap<String, Any>>()

        return body
    }

    private fun buildBillBody(bill: Bill) : HashMap<String, Any> {
        val body = HashMap<String, Any>()

        body[OPENED] = bill.opened
        if (bill.closed != null) {
            body[CLOSED] = bill.closed
        }
        body[SUM] = bill.sum
        body[STATUS] = bill.status
        body[PAYED_TYPE] = bill.payedType
        body[ITEMS] = buildItemsBody(bill.items)
        body[TECHMAPS] = buildTechMapsBody(bill.techmaps)

        return body
    }

    private fun buildItemsBody(items: List<BillItem>) : List<Map<String, Any>> {
        val body = ArrayList<Map<String, Any>>()

        for (item in items) {
            val itemBody = HashMap<String, Any>()

            itemBody[ITEM_ID] = if (item.itemId != 0L) item.itemId else item.item?.id ?: 0L
            itemBody[COUNT] = item.count
            itemBody[PRICE] = item.price
            itemBody[DISCOUNT] = item.discount

            body.add(itemBody)
        }

        return body
    }

    private fun buildTechMapsBody(techMaps: List<BillTechMap>) : List<Map<String, Any>> {
        val body = ArrayList<Map<String, Any>>()

        for (item in techMaps) {
            val itemBody = HashMap<String, Any>()

            itemBody[TECHMAP_ID] = if (item.techmapId != 0L) item.techmapId else item.techmap?.id ?: 0L
            itemBody[COUNT] = item.count
            itemBody[PRICE] = item.price
            itemBody[DISCOUNT] = item.discount
            itemBody[MODIFICATORS] = buildModificators(item.modificators)

            body.add(itemBody)
        }

        return body
    }

    private fun buildModificators(items: List<BillModificator>?) : List<Map<String, Any>> {
        val body = ArrayList<Map<String, Any>>()
        items ?: return body

        for (item in items) {
            val itemBody = HashMap<String, Any>()

            itemBody[MODIFICATOR_ID] = item.getId()
            itemBody[COUNT] = item.count.roundToInt()
            itemBody[PRICE] = item.price
            itemBody[DISCOUNT] = 0
            itemBody[COST_PRICE] = item.costPrice

            body.add(itemBody)
        }

        return body
    }

    private fun buildBillsBody() : HashMap<String, Any> {
        val body = HashMap<String, Any>()

        body["offset"] = 0
        body["pageNumber"] = 0
        body["pageSize"] = 0
        body["pageSize"] = 0
        body["paged"] = false
        body["unpaged"] = true

        return body
    }

    private fun buildBuyBody(buy: Buy?) : HashMap<String, Any> {
        val body = HashMap<String, Any>()
        buy ?: return body

        body[ACCOUNT_ID] = buy.account.id
        body[COMMENT] = buy.comment
        body[DATE] = getCurrentDate()
        body[VENDOR_ID] = buy.vendor.id
        body[RECORDS] = buildBuyRecordsBody(buy.records)

        return body
    }

    private fun buildBuyRecordsBody(items: List<BuyRecord>?) : List<Map<String, Any>> {
        val body = ArrayList<Map<String, Any>>()
        items ?: return body

        for (item in items) {
            val itemBody = HashMap<String, Any>()

            itemBody[COUNT] = item.count
            itemBody[ITEM_ID] = item.itemId
            itemBody[PRICE] = item.price
            itemBody[TYPE] = item.type

            body.add(itemBody)
        }

        return body
    }
}