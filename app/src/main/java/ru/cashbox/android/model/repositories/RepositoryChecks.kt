package ru.cashbox.android.model.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import okhttp3.ResponseBody
import retrofit2.Response
import ru.cashbox.android.common.FORBIDDEN
import ru.cashbox.android.model.*
import ru.cashbox.android.model.http.ApiHelper

class RepositoryChecks(private val apiHelper: ApiHelper, repositoryUsers: RepositoryUsers, repositoryCashsessions: RepositoryCashsessions) {

    private var isChecksInit = false
    private val employeeToken: LiveData<String> = repositoryUsers.tokenEmployee
    private val cashsession: LiveData<Cashsession> = repositoryCashsessions.cashsession

    val bills = MutableLiveData<List<Bill>>(ArrayList())
    val checkItems = MutableLiveData<List<CheckItem>>(ArrayList())

    fun clear() {
        isChecksInit = false
        bills.value = ArrayList()
        checkItems.value = ArrayList()
    }

    fun findCheckItem(itemId: Long, itemTimestamp: Long) : CheckItem? {
        for (checkItem in checkItems.value ?: ArrayList()) {
            if (checkItem.billId == itemId && checkItem.timestamp == itemTimestamp) {
                return checkItem
            }
        }

        return null
    }

    fun addCheckItem(item: CheckItem) {
        val items = ArrayList(checkItems.value ?: ArrayList())
        items.add(item)
        checkItems.value = items
    }

    fun removeCheckItem(item: CheckItem) {
        val items = ArrayList(checkItems.value ?: ArrayList())
        items.remove(item)
        checkItems.value = items
    }

    fun updateCheckItem(item: CheckItem) {
        val items = ArrayList(checkItems.value ?: ArrayList())
        val removeItem = items.find { it.timestamp == item.timestamp }

        if (removeItem != null) {
            val itemIx = items.indexOf(removeItem)

            if (itemIx != -1) {
                items.remove(removeItem)
                items.add(itemIx, item)
            }
            checkItems.value = items
        }
    }

    fun clearCheckItems(checkId: Long) {
        val items = ArrayList(checkItems.value ?: ArrayList()).filter { it.checkId != checkId }
        checkItems.value = items
    }

    //------------------------------------------------------------------------------------------------------------------
    suspend fun requestBills() : Response<BillAnswer> {
        if (isChecksInit) {
            return Response.error(FORBIDDEN, ResponseBody.create(null, ""))
        }
        isChecksInit = true

        val sessionId = cashsession.value?.id ?: return Response.error(FORBIDDEN, ResponseBody.create(null, ""))
        val response = apiHelper.requestBills(employeeToken.value, sessionId)
        val bills = response.body()

        if (response.isSuccessful && bills != null && bills.content.isNotEmpty()) {
            val checkItems = ArrayList<CheckItem>()

            bills.content.forEach { bill ->
                bill.items.forEach { checkItems.add(it.copy(checkId = bill.id)) }
                bill.techmaps.forEach { checkItems.add(it.copy(checkId = bill.id)) }
            }
            this.checkItems.value = checkItems
            this.bills.value = bills.content
        }

        return response
    }

    suspend fun createEmptyBill() : Response<Bill> {
        val response = apiHelper.requestCreateBill(employeeToken.value)
        val bill = response.body()

        if (response.isSuccessful && bill != null) {
            val billsList = ArrayList<Bill>(bills.value ?: ArrayList())
            bill.lastChange = System.currentTimeMillis()
            billsList.add(bill)
            bills.value = billsList
        }

        return response
    }

    suspend fun closeBill(bill: Bill) : Response<Bill> {
        return changeBillStatus(getChangedBill(bill, getCheckItems(bill.id)), CheckStatus.CLOSED)
    }

    suspend fun payBillByCash(bill: Bill) : Response<Bill> {
        return changeBillStatus(getChangedBill(bill, getCheckItems(bill.id)), CheckStatus.PAYED, PayType.CASH)
    }

    suspend fun payBillByWire(bill: Bill) : Response<Bill> {
        return changeBillStatus(getChangedBill(bill, getCheckItems(bill.id)), CheckStatus.PAYED, PayType.WIRE)
    }

    suspend fun returnBill(bill: Bill) : Response<Bill> {
        return changeBillStatus(getChangedBill(bill, getCheckItems(bill.id)), CheckStatus.RETURNED)
    }

    suspend fun openBill(bill: Bill) : Response<Bill> {
        return changeBillStatus(getChangedBill(bill, getCheckItems(bill.id)), CheckStatus.OPENED, payType = PayType.UNDEFINED)
    }

    suspend fun changeBillItems(bill: Bill, isNeedUpdate: Boolean = true) : Response<Bill> {
        return changeBillItems(bill, getCheckItems(bill.id), isNeedUpdate)
    }

    private suspend fun getChangedBill(bill: Bill, items: List<CheckItem>) : Bill {
        var finalBill = bill

        if (items.isNotEmpty()) {
            val response = changeBillItems(bill, items)
            val responseBill = response.body()
            if (response.isSuccessful && responseBill != null) {
                finalBill = responseBill
            }
        }

        return finalBill
    }

    private suspend fun changeBillItems(bill: Bill, items: List<CheckItem>, isNeedUpdate: Boolean = true) : Response<Bill> {
        val billIngredients = ArrayList<BillItem>()
        val billTechMaps = ArrayList<BillTechMap>()
        var newSum = 0.0

        for (item in items) {
            when(item) {
                is BillItem -> billIngredients.add(item.copy())
                is BillTechMap -> billTechMaps.add(item.copy())
            }
        }
        billIngredients.forEach { newSum += it.getFinalPrice() }
        billTechMaps.forEach { newSum += it.getFinalPrice() }

        val response = apiHelper.requestChangeBill(employeeToken.value, bill.id, bill.copy(sum = newSum,
            items = billIngredients, techmaps = billTechMaps))
        val responseBill = response.body()

        if (response.isSuccessful && responseBill != null && isNeedUpdate) {
            val billsList = ArrayList<Bill>(bills.value ?: ArrayList())
            responseBill.lastChange = System.currentTimeMillis()
            billsList.remove(bill)
            billsList.add(responseBill)
            bills.value = billsList
        }

        return response
    }

    private suspend fun changeBillStatus(bill: Bill, status: String, payType: String = bill.payedType) : Response<Bill>  {
        val response = apiHelper.requestChangeBillStatus(employeeToken.value, bill.id, status, payType)
        val responseBill = response.body()

        if (response.isSuccessful && responseBill != null) {
            val billsList = ArrayList<Bill>(bills.value ?: ArrayList())
            responseBill.lastChange = System.currentTimeMillis()
            billsList.remove(bill)
            billsList.add(responseBill)
            bills.value = billsList
        }

        return response
    }

    private suspend fun getCheckItems(checkId: Long) : List<CheckItem> {
        return (checkItems.value ?: ArrayList()).filter { it.checkId == checkId }
    }
}