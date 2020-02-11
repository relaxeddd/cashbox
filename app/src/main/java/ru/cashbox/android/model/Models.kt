package ru.cashbox.android.model

import android.os.Bundle
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.cashbox.android.common.*
import java.util.UUID

@Entity(tableName = USERS)
@Keep
data class User(@PrimaryKey val id: Long, val username: String, val fullname: String, val activate: Boolean,
                val pin: String)

@Keep
data class Session(val token: String, val user: User)

@Keep
data class BillModificatorWrapper(val id: Long, val count: Int, val price: Double)

@Keep
data class BillResponseNumberWrapper(val id: Int)

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

    NAVIGATION_LOGIN_TERMINAL_TO_LOGIN_EMPLOYEE
}