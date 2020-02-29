package ru.cashbox.android.ui.discount

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import ru.cashbox.android.App
import ru.cashbox.android.R
import ru.cashbox.android.model.*
import ru.cashbox.android.model.repositories.RepositoryChecks
import ru.cashbox.android.ui.base.ViewModelBase
import java.lang.NumberFormatException

class ViewModelDiscount(app: App, private val repositoryChecks: RepositoryChecks) : ViewModelBase(app) {

    private var checkItem: CheckItem? = null
    val discount = MutableLiveData(0)
    val textDiscountTitle = MutableLiveData("")
    val textButtonDiscount = MutableLiveData("")
    val isEnabledButtonApplyDiscount = MutableLiveData(false)

    private val discountObserver = Observer<Int> { discount ->
        isEnabledButtonApplyDiscount.value = discount != 0
        textButtonDiscount.value = getString(R.string.make_discount, discount)
    }

    init {
        discount.observeForever(discountObserver)
    }

    override fun onCleared() {
        super.onCleared()
        discount.removeObserver(discountObserver)
    }

    fun initCheckItem(itemId: Long, itemTimestamp: Long) {
        checkItem = repositoryChecks.findCheckItem(itemId, itemTimestamp)
        discount.value = checkItem?.discount ?: 0
    }

    fun clickButtonApplyDiscount() {
        val item = checkItem
        val discount = this.discount.value ?: 0

        if (item != null) {
            if (item.discount != discount) {
                val changedElement = when (item.elementType) {
                    ElementType.GOOD -> (item as BillItem).copy(discount = discount)
                    ElementType.TECHMAP -> (item as BillTechMap).copy(discount = discount)
                    else -> null
                }
                repositoryChecks.updateCheckItem(changedElement ?: return)
            }
            navigateEvent.value = NavigationEvent(EventType.GO_BACK)
        } else {
            showToast(R.string.error_сheck_item)
        }
    }

    fun clickButtonClose() {
        navigateEvent.value = NavigationEvent(EventType.GO_BACK)
    }

    fun enterNumber(number: Char) {
        val currentDiscount = discount.value ?: 0

        if (currentDiscount > 11 || (currentDiscount == 10 && number.toInt() != 0)) {
            showToast(R.string.rule_discount)
        } else {
            discount.value = (discount.value.toString() + number).toInt()
        }
    }

    fun clickDel() {
        val textDiscount = discount.value.toString()

        if (textDiscount.isNotEmpty()) {
            try {
                discount.value = textDiscount.substring(0, textDiscount.length - 1).toInt()
            } catch (e: NumberFormatException) {
                discount.value = 0
            }
        }
    }

    fun clickClearNumbers() {
        discount.value = 0
    }

    fun clickDiscount25() {
        discount.value = 25
    }

    fun clickDiscount50() {
        discount.value = 50
    }

    fun clickDiscount100() {
        discount.value = 100
    }
}