package ru.cashbox.android.common

import ru.cashbox.android.model.*

interface ClickListenerElements {

    fun onClick(element: Element)
}

interface ClickListenerCheck {

    fun onClick(check: Bill)
}

interface CheckListenerBillElements {

    fun onSwipe(element: CheckItem)
    fun onMinus(element: CheckItem)
    fun onPlus(element: CheckItem)
    fun onClick(element: CheckItem)
}

interface ListenerBuyRecord {

    fun onSwipe(item: BuyRecord)
    fun onClick(item: BuyRecord)
}

interface ClickListenerModificators {

    fun onClick(item: TechMapModificator)
}

interface ListenerResult<T> {

    fun onResult(result: T)
}