package ru.cashbox.android.model.repositories

import androidx.lifecycle.MutableLiveData
import retrofit2.Response
import ru.cashbox.android.model.Buy
import ru.cashbox.android.model.BuyAccount
import ru.cashbox.android.model.BuyVendor
import ru.cashbox.android.model.http.ApiHelper

class RepositoryBuys(private val apiHelper: ApiHelper, repositoryUsers: RepositoryUsers) {

    private var isItemsAlreadyInit = false
    private val tokenEmployee = repositoryUsers.tokenEmployee

    val accounts = MutableLiveData<List<BuyAccount>>(ArrayList())
    val vendors = MutableLiveData<List<BuyVendor>>(ArrayList())

    fun clear() {
        isItemsAlreadyInit = false
    }

    suspend fun requestItems() : Boolean {
        if (isItemsAlreadyInit) {
            return true
        }

        val responseAccounts = apiHelper.requestBuyAccounts(tokenEmployee.value)
        val responseVendors = apiHelper.requestBuyVendors(tokenEmployee.value)
        val accountsAnswer = responseAccounts.body()
        val vendorsAnswer = responseVendors.body()
        val isSuccessAnswer = responseAccounts.isSuccessful && responseVendors.isSuccessful

        if (responseAccounts.isSuccessful && accountsAnswer != null) {
            accounts.value = accountsAnswer
        }
        if (responseVendors.isSuccessful && vendorsAnswer != null) {
            vendors.value = vendorsAnswer
        }

        if (isSuccessAnswer) {
            isItemsAlreadyInit = true
        }

        return isSuccessAnswer
    }

    suspend fun createSupply(buy : Buy) : Response<Buy> {
        return apiHelper.requestCreateBuy(tokenEmployee.value, buy)
    }
}