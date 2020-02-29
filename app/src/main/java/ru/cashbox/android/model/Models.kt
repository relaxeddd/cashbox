@file:Suppress("unused")
package ru.cashbox.android.model

import android.content.Context
import android.os.Bundle
import androidx.annotation.Keep
import ru.cashbox.android.common.*
import ru.cashbox.android.R
import kotlin.math.roundToInt

@Keep
data class User(val id: Long, val username: String, val fullname: String, val activate: Boolean, val pin: String?,
                val roles: List<String>)

@Keep
data class Session(val token: String, val user: User)

@Keep
data class Cashsession(val id: Long, val openCashBalance: Double, val closedCashBalance: Double, val closed: Boolean,
                       val buys: List<Buy>)

//----------------------------------------------------------------------------------------------------------------------
@Keep
data class Ingredient(override val id: Long, override val name: String, override val imageUrl: String?,
                      val categoryId: Long, val type: String,  val price: Double, val costPrice: Double,
                      val unit: String) : Element {
    override val elementType: ElementType
        get() = ElementType.GOOD
}

@Keep
data class Category(override val id: Long, override val name: String, override val imageUrl: String?,
                    val parentId: Long, val type: String, val defaultCategory: Boolean) : Element {
    override val elementType: ElementType
        get() = ElementType.CATEGORY
}

@Keep
data class TechMap(override val id: Long, override val name: String, override val imageUrl: String?,
                   val categoryId: Long, val price: Double, val costPrice: Double, val weight: Double,
                   val modificatorGroups: List<TechMapGroup>) : Element {
    override val elementType: ElementType
        get() = ElementType.TECHMAP
}

@Keep
data class TechMapModificator(val id: Long, val imageUrl: String?, val name: String, val groupId: Long, val groupName: String,
                              var count: Double, val maxModificatorsCount: Int)

@Keep
data class TechMapGroup(val id: Long, val name: String, val modificatorsCount: Int, val techmapId: Long,
                        val modificators: List<TechMapModificator>)

@Keep
data class ResponseIngredient(override val id: Long, override val imageUrl: String?, override val name: String,
                      val price: Double, val costPrice: Double, val type: String, val unit: String, val category: Category? = null) : Element {
    override val elementType: ElementType
        get() = ElementType.GOOD
}

@Keep
data class ResponseCategory(override val id: Long, override val imageUrl: String?, override val name: String,
                            val parentId: Long, val ingredients: List<ResponseIngredient>, val defaultCategory: Boolean,
                            val type: String) : Element {
    override val elementType: ElementType
        get() = ElementType.CATEGORY
}

@Keep
data class ResponseTechMap(override val id: Long, override val imageUrl: String?, override val name: String,
                   val price: Double, val costPrice: Double, val weight: Double, val category: Category,
                   val modificatorGroups: List<ResponseTechMapGroup>) : Element {
    override val elementType: ElementType
        get() = ElementType.TECHMAP
}

@Keep
data class ResponseTechMapGroup(val id: Long, val name: String, val modificatorsCount: Int,
                        val modificators: List<ResponseTechMapModificator>)

@Keep
data class ResponseTechMapModificator(val id: Long, val imageUrl: String?, val name: String, val count: Double)



interface Element {
    val id: Long
    val imageUrl: String?
    val name: String
    val elementType: ElementType
}

//----------------------------------------------------------------------------------------------------------------------
@Keep
data class Bill(val id: Long, val sum: Double, val status: String, val payedType: String, val user: User,
                val items: List<BillItem>, val techmaps: List<BillTechMap>, val opened: String, val closed: String?,
                var lastChange: Long = System.currentTimeMillis()) {

    fun getFinalSum() : Double {
        var sum = 0.0
        items.forEach { sum += it.getFinalPrice() }
        techmaps.forEach { sum += it.getFinalPrice() }
        return sum
    }
}

@Keep
data class BillAnswer(val content: List<Bill>)

@Keep
data class BillItem(val itemId: Long, override val checkId: Long, override val price: Double, override val count: Int, override val discount: Int,
                    val costPrice: Double, val item: ResponseIngredient?, override val timestamp: Long, val id: Long = 0) : CheckItem {
    override val elementType: ElementType
        get() = ElementType.GOOD

    override val billName: String
        get() = item?.name ?: ""

    override val billId: Long
        get() = if (id != 0L) id else itemId
}

@Keep
data class BillTechMap(val techmapId: Long, override val checkId: Long, override val price: Double, override val count: Int, override val discount: Int,
                       val costPrice: Double, val modificators: List<BillModificator>?, val techmap: ResponseTechMap?, val innerTechmap: TechMap?,
                       override val timestamp: Long, val id: Long = 0) : CheckItem {
    override val elementType: ElementType
        get() = ElementType.TECHMAP

    override val billName: String
        get() {
            return if (techmap != null) getNameTechMap(techmap, modificators) else { if (innerTechmap != null) getNameTechMap(innerTechmap, modificators) else "" }
        }

    override val billId: Long
        get() = if (id != 0L) id else techmapId
}

@Keep
data class BillModificator(val modificatorId: Long, val name: String?, val price: Double, val count: Double, val costPrice: Double,
                           val modificator: ResponseTechMapModificator? = null) {

    fun getFinalName() : String {
        return modificator?.name ?: name ?: ""
    }

    fun getId() : Long {
        return modificator?.id ?: modificatorId
    }
}

interface CheckItem {
    val billId: Long
    val checkId: Long
    val count: Int
    val price: Double
    val discount: Int
    val billName: String
    val timestamp: Long
    val elementType: ElementType

    fun getFinalPrice() = ((price - price * discount / 100) * count).roundToInt().toDouble()
}

/*@Keep
data class BillModificatorWrapper(val id: Long, val count: Int, val price: Double)

@Keep
data class BillResponseNumberWrapper(val id: Long)*/



/*@Keep
data class TechMapElement(val id: Long, val selected: Boolean, val name: String, val imageUrl: String,
                          val price: Double, val count: Int)

@Keep
data class TechMapElementWrapper(val groupName: String, val maxSelected: Int, val elements: List<TechMapElement>)*/

//----------------------------------------------------------------------------------------------------------------------
/*@Keep
data class CheckItem(val id: Long?, val imageUrl: String, val name: String, val amount: Int, val price: Double,
                     val fullPrice: Double, val type: CheckItemCategoryType,
                     val modificators: List<BillModificatorWrapper>)

@Keep
data class Check(val number: Int, val hash: UUID, val items: List<CheckItem>, val status: CheckStatus,
                 val created: String, val closed: String, val historySelected: Boolean, val payType: PayType)

@Keep
data class ElementWrapper(val code: Int, val elements: List<Element>)*/

@Keep
data class Printer(val name: String, val connectionType: PrinterConnectionType, val ip: String, val port: String,
                   var selected: Boolean)

@Keep
data class PrinterQueueWrapper(val action: PrinterAction, val params: List<Any>)

@Keep
data class Date(val date: Int = 0, val day: Int = 0, val hours: Int = 0, val minutes: Int = 0, val month: Int = 0,
                val nanos: Long = 0, val seconds: Long = 0, val time: Long = 0, val timezoneOffset: Int = 0,
                val year: Int = 0)

@Keep
data class Buy(val id: Long, val account: BuyAccount, val comment: String, val fullName: String,
               val records: List<BuyRecord>, val userId: Long, val vendor: BuyVendor)

@Keep
data class BuyRecord(val id: Long, val count: Double, val itemId: Long, val name: String, val price: Double, val type: String,
                     val unit: String, val timestamp: Long)

@Keep
data class BuyVendor(val id: Long, val name: String, val phone: String, val comment: String,
                     val shipmentCount: Int, val totalShipmentPrice: Double)

@Keep
data class BuyAccount(val id: Long, val name: String, val type: String, val balance: Double,
                      val currency: String, val defaultAccount: Boolean)

//----------------------------------------------------------------------------------------------------------------------
@Keep
open class NavigationEvent(private val type: EventType, var args: Bundle? = null) {

    private var isHandled = false

    fun getTypeIfNotHandled(): EventType? {
        return if (isHandled) {
            null
        } else {
            isHandled = true
            type
        }
    }
}

@Keep
open class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}

//----------------------------------------------------------------------------------------------------------------------
class CheckStatus {

    companion object {
        const val OPENED = "OPENED"
        const val CLOSED = "CLOSED"
        const val PAYED = "PAYED"
        const val RETURNED = "RETURNED"
        const val DELETED = "DELETED"

        fun getName(context: Context, value: String) = when(value) {
            OPENED -> context.getString(R.string.opened)
            CLOSED -> context.getString(R.string.closed)
            PAYED -> context.getString(R.string.paid)
            RETURNED -> context.getString(R.string.returning)
            DELETED -> context.getString(R.string.removed)
            else -> context.getString(R.string.unknown)
        }
    }
}

enum class ElementType {
    CATEGORY, GOOD, TECHMAP
}

class ItemType {

    companion object {
        const val INGREDIENT = "INGREDIENT"
        const val SEMIPRODUCT = "SEMIPRODUCT"
        const val TECHMAP = "TECHMAP"
        const val GOOD = "GOOD"
    }
}

class UnitMeasurement {

    companion object {
        const val PIECE = "PIECE"
        const val KILOGRAM = "KILOGRAM"
        const val LITER = "LITER"
    }
}

class CategoryType {

    companion object {
        const val WAREHOUSE = "WAREHOUSE"
        const val TERMINAL = "TERMINAL"
    }
}

class LogAppState {

    companion object {
        const val MINIMIZE_SCREEN = "MINIMIZE_SCREEN"
        const val EXPAND_SCREEN = "EXPAND_SCREEN"
        const val CLOSE_APP = "CLOSE_APP"
        const val OPEN_APP = "OPEN_APP"
    }
}

class PayType {

    companion object {
        const val CASH = "CASH"
        const val WIRE = "WIRE"
        const val UNDEFINED = "UNDEFINED"

        fun getName(context: Context, value: String) = when(value) {
            CASH -> context.getString(R.string.cash)
            WIRE -> context.getString(R.string.card)
            else -> context.getString(R.string.not_paid)
        }
    }
}

enum class CheckItemCategoryType {
    ITEM, TECHMAP
}

enum class PrinterConnectionType {
    WIFI, BLUETOOTH
}

enum class PrinterAction {
    PRINT, TEST_PRINT, CLOSE_SHIFT
}

enum class EventType {

    LOADING_SHOW,
    LOADING_HIDE,
    PRESS_BACK,
    GO_BACK,
    EXIT,

    NAVIGATION_UNAUTHORIZED,

    NAVIGATION_LOGIN_TERMINAL_TO_LOGIN_EMPLOYEE,
    DIALOG_ENTER_DOMAIN,

    NAVIGATION_LOGIN_EMPLOYEE_TO_LOGIN_TERMINAL,
    NAVIGATION_LOGIN_EMPLOYEE_TO_EMPLOYEE_ROOM,
    NAVIGATION_LOGIN_EMPLOYEE_TO_CASH,

    NAVIGATION_PREVIEW_TO_LOGIN_TERMINAL,
    NAVIGATION_PREVIEW_TO_LOGIN_EMPLOYEE,

    NAVIGATION_EMPLOYEE_ROOM_TO_LOGIN_EMPLOYEE,
    NAVIGATION_EMPLOYEE_ROOM_TO_CASH,
    NAVIGATION_EMPLOYEE_ROOM_TO_ENTER_NUMBER,

    NAVIGATION_CREATE_SUPPLY_TO_CASH,

    NAVIGATION_ENTER_NUMBER_TO_EMPLOYEE_ROOM,
    NAVIGATION_ENTER_NUMBER_TO_CASH,

    NAVIGATION_CASH_TO_LOGIN_EMPLOYEE,
    NAVIGATION_CASH_TO_CHECKS,
    NAVIGATION_CASH_TO_PRINTERS,
    NAVIGATION_CASH_TO_DISCOUNT,
    NAVIGATION_CASH_TO_CREATE_SUPPLY,
    CLEAR_SELECTED_MODIFICATORS,
    CLOSE_CASHOBOX,
    CLOSE_EMPLOYEE_SESSION_QUESTION,
    DIALOG_MODIFICATORS,

    NAVIGATION_DISCOUNT_TO_CASH,

    NAVIGATION_ARCHIVE_TO_CASH,

    FRAGMENT_LOGIN_EMPLOYEE_PIN_ERROR
}