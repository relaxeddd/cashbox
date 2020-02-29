package ru.cashbox.android.ui.create_supply

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.launch
import ru.cashbox.android.App
import ru.cashbox.android.R
import ru.cashbox.android.common.ListenerBuyRecord
import ru.cashbox.android.common.UNAUTHORIZED
import ru.cashbox.android.model.*
import ru.cashbox.android.model.repositories.RepositoryBuys
import ru.cashbox.android.model.repositories.RepositoryCashsessions
import ru.cashbox.android.model.repositories.RepositoryGoods
import ru.cashbox.android.model.repositories.RepositoryUsers
import ru.cashbox.android.ui.base.ViewModelBase
import java.lang.NumberFormatException
import java.util.*
import kotlin.collections.ArrayList

class ViewModelCreateSupply(app: App, private val repositoryUsers: RepositoryUsers,
                            private val repositoryCashsessions: RepositoryCashsessions,
                            private val repositoryGoods: RepositoryGoods,
                            private val repositoryBuys: RepositoryBuys) : ViewModelBase(app) {

    val textAmount = MutableLiveData("")
    val textPrice = MutableLiveData("")
    val textTotalPrice = MutableLiveData("0")

    val accounts: LiveData<List<BuyAccount>> = repositoryBuys.accounts
    val vendors: LiveData<List<BuyVendor>> = repositoryBuys.vendors
    val buyIngredients: LiveData<List<Ingredient>> = repositoryGoods.buyIngredients

    val records: MutableLiveData<List<BuyRecord>> = MutableLiveData(ArrayList())

    val listenerBuyRecord = object: ListenerBuyRecord {
        override fun onSwipe(item: BuyRecord) {
            val recordsList = ArrayList(records.value ?: ArrayList())
            recordsList.remove(item)
            records.value = recordsList
        }
        override fun onClick(item: BuyRecord) {}
    }

    private val amountObserver = Observer<String> { textAmount ->
        updateTextTotalPrice()
    }
    private val priceObserver = Observer<String> { textPrice ->
        updateTextTotalPrice()
    }

    init {
        textAmount.observeForever(amountObserver)
        textPrice.observeForever(priceObserver)
    }

    override fun onViewResume() {
        super.onViewResume()
        navigateEvent.value = NavigationEvent(EventType.LOADING_SHOW)
        uiScope.launch {
            repositoryBuys.requestItems()
            navigateEvent.value = NavigationEvent(EventType.LOADING_HIDE)
        }
    }

    override fun onCleared() {
        super.onCleared()
        textAmount.observeForever(amountObserver)
        textPrice.removeObserver(priceObserver)
    }

    fun clickClose() {
        navigateEvent.value = NavigationEvent(EventType.GO_BACK)
    }

    fun addPosition(ingredient: Ingredient?) {
        var amount = 0.0
        var price = 0.0

        if (textAmount.value?.isEmpty() == true || textPrice.value?.isEmpty() == true || ingredient == null) {
            showToast(R.string.error_ingredient)
            return
        }
        try {
            amount = textAmount.value?.toDouble() ?: 0.0
            price = textPrice.value?.toDouble() ?: 0.0
        } catch (e: NumberFormatException) {
            showToast(R.string.error_ingredient)
            return
        }
        val record = BuyRecord(-1, amount, ingredient.id, ingredient.name, price, ItemType.INGREDIENT, UnitMeasurement.PIECE, System.currentTimeMillis())
        val editedRecords = ArrayList(records.value ?: ArrayList())
        editedRecords.add(record)
        records.value = editedRecords

        textAmount.value = ""
        textPrice.value = ""
    }

    fun createSupply(account: BuyAccount?, vendor: BuyVendor?, comment: String) {
        val records = this.records.value

        if (account == null || vendor == null || records == null || records.isEmpty()) {
            showToast(R.string.error_supply_validation)
            return
        }

        navigateEvent.value = NavigationEvent(EventType.LOADING_SHOW)
        uiScope.launch {
            val response = repositoryBuys.createSupply(Buy(-1, account, comment, "", records,
                repositoryUsers.sessionEmployee.value?.user?.id ?: 0, vendor))

            if (response.isSuccessful) {
                showToast(R.string.supply_created)
                navigateEvent.value = NavigationEvent(EventType.GO_BACK)
            } else if (response.code() == UNAUTHORIZED) {
                showToast(R.string.error_unauthorized)
                navigateEvent.value = NavigationEvent(EventType.NAVIGATION_UNAUTHORIZED)
            } else {
                showToast(R.string.error_supply)
            }
        }
    }

    private fun updateTextTotalPrice() {
        var totalPrice = 0.0

        if (textAmount.value?.isNotEmpty() == true && textPrice.value?.isNotEmpty() == true) {
            try {
                val amount = textAmount.value?.toDouble() ?: 0.0
                val price = textPrice.value?.toDouble() ?: 0.0

                totalPrice = amount * price
            } catch (e: NumberFormatException) {
                totalPrice = 0.0
            }

        }
        textTotalPrice.value = getString(R.string.total) + ": " + String.format(Locale.getDefault(), "%,.2f", totalPrice) + getString(R.string.rub)
    }

    private fun getPrice() : Double {
        var textPrice = textPrice.value ?: ""
        var price = 0.0

        try {
            price = textPrice.toDouble()
        } catch (e: NumberFormatException) {
            price = 0.0
        }
        return price
    }
}