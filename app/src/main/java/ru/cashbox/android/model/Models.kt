package ru.cashbox.android.model

import android.os.Bundle
import androidx.annotation.Keep
import androidx.room.*
import ru.cashbox.android.common.*
import java.util.UUID

@Keep
@Entity(tableName = USERS)
data class User(@PrimaryKey val id: Long, val username: String, val fullname: String, val activate: Boolean,
                val pin: String?, val roles: List<String>)

@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
@Keep
@Entity(tableName = SESSIONS)
data class Session(@PrimaryKey var innerId: String, val token: String, @Embedded val user: User)

@Keep
data class BillModificatorWrapper(val id: Long, val count: Int, val price: Double)

@Keep
data class BillResponseNumberWrapper(val id: Long)

@Keep
data class TechMapModificator(val id: Long, val imageUrl: String, val name: String)

@Keep
data class TechMapGroup(val id: Long, val name: String, var modificatorsCount: Int?,
                        val modificators: List<TechMapModificator>)

@Keep
data class TechMapElement(val id: Long, var selected: Boolean, val name: String, val imageUrl: String,
                          val price: Double, var count: Int)

@Keep
data class TechMapElementWrapper(val groupName: String, val maxSelected: Int, val elements: List<TechMapElement>)

@Keep
data class Ingredient(override val id: Long?, override val imageUrl: String, override val name: String,
                      val price: Double,
                      val type: ItemType,
                      val unit: UnitMeasurement) : Element {
    override val elementType: ElementType
        get() = ElementType.GOOD
}

@Keep
data class Category(override val id: Long?, override val imageUrl: String, override val name: String,
                    val parentId: Long?,
                    val ingredients: List<Ingredient>,
                    val defaultCategory: Boolean,
                    val type: CategoryType) : Element {
    override val elementType: ElementType
        get() = ElementType.CATEGORY
}

@Keep
data class TechMap(override val id: Long?, override val imageUrl: String, override val name: String,
                   val price: Double,
                   val category: Category,
                   val modificatorGroups: List<TechMapGroup>) : Element {
    override val elementType: ElementType
        get() = ElementType.TECHMAP
}

@Keep
data class CheckItem(val id: Long?, val imageUrl: String, val name: String, var amount: Int, val price: Double,
                     var fullPrice: Double, val type: CheckItemCategoryType,
                     val modificators: List<BillModificatorWrapper>)

@Keep
data class Check(val number: Int, val hash: UUID, val items: List<CheckItem>, var status: CheckStatus,
                 val created: String, var closed: String, var historySelected: Boolean, var payType: PayType)

@Keep
data class ElementWrapper(val code: Int, val elements: List<Element>)

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
@Entity(tableName = CASHSESSIONS)
data class Cashsession(@PrimaryKey var innerId: String, val id: Long, val openCashBalance: Long,
                       val closedCashBalance: Long, val closed: Boolean, val buys: List<Buy>)

@Keep
data class Buy(@Embedded val account: BuyAccount, val comment: String, val fullName: String,
               val id: Long, val records: List<BuyRecord>, val userId: String, @Embedded val vendor: BuyVendor)

@Keep
data class BuyAccount(val id: Long, val balance: Int, val currency: String, val defaultAccount: Boolean,
                      val name: String, val type: String)

@Keep
data class BuyRecord(val id: Long, val count: Int, val itemId: Int, val name: String, val price: Int, val type: String,
                     val unit: String)

@Keep
data class BuyVendor(val id: Long, val comment: String, val name: String, val phone: String,
                     val shipmentCount: Int, val totalShipmentCount: Double)

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
interface Element {

    val id: Long?
    val imageUrl: String
    val name: String
    val elementType: ElementType
}

enum class CheckStatus {
    OPENED, CLOSED, PAYED, RETURNED, DELETED;

    fun getTranslatedName() = when(this) {
        OPENED -> "Открыт"
        CLOSED -> "Закрыт"
        PAYED -> "Оплачен"
        RETURNED -> "Возврат"
        DELETED -> "Удалён"
    }
}

enum class ElementType {
    CATEGORY, GOOD, TECHMAP
}

enum class ItemType {
    INGREDIENT, SEMIPRODUCT, TECHMAP, GOOD
}

enum class UnitMeasurement {
    PIECE, KILOGRAM, LITER
}

enum class CategoryType {
    WAREHOUSE, TERMINAL
}

enum class CheckItemCategoryType {
    ITEM, TECHMAP
}

enum class PayType {
    CASH, WIRE, UNDEFINED
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
    EXIT,

    NAVIGATION_LOGIN_TERMINAL_TO_LOGIN_EMPLOYEE,

    NAVIGATION_LOGIN_EMPLOYEE_TO_LOGIN_TERMINAL,
    NAVIGATION_LOGIN_EMPLOYEE_TO_EMPLOYEE_ROOM,

    NAVIGATION_PREVIEW_TO_LOGIN_TERMINAL,
    NAVIGATION_PREVIEW_TO_LOGIN_EMPLOYEE,

    NAVIGATION_EMPLOYEE_ROOM_TO_LOGIN_EMPLOYEE,
    NAVIGATION_EMPLOYEE_ROOM_TO_CASH,

    FRAGMENT_LOGIN_EMPLOYEE_PIN_ERROR
}