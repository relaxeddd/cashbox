package ru.cashbox.android.model

import java.util.UUID

data class User(val id: Long, val username: String, val fullname: String, val activate: Boolean,
                val pin: String)

data class Session(val token: String, val user: User)

data class BillModificatorWrapper(val id: Long, val count: Int, val price: Double)

data class BillResponseNumberWrapper(val id: Int)

data class TechMapModificator(val id: Long, val imageUrl: String, val name: String)

data class TechMapGroup(val id: Long, val name: String, var modificatorsCount: Int?,
                        val modificators: List<TechMapModificator>)

data class TechMapElement(val id: Long, var selected: Boolean, val name: String, val imageUrl: String,
                          val price: Double, var count: Int)

data class TechMapElementWrapper(val groupName: String, val maxSelected: Int, val elements: List<TechMapElement>)

data class Ingredient(override val id: Long?, override val imageUrl: String, override val name: String,
                      val price: Double,
                      val type: ItemType,
                      val unit: UnitMeasurement) : Element {
    override val elementType: ElementType
        get() = ElementType.GOOD
}

data class Category(override val id: Long?, override val imageUrl: String, override val name: String,
                    val parentId: Long?,
                    val ingredients: List<Ingredient>,
                    val defaultCategory: Boolean,
                    val type: CategoryType) : Element {
    override val elementType: ElementType
        get() = ElementType.CATEGORY
}

data class TechMap(override val id: Long?, override val imageUrl: String, override val name: String,
                   val price: Double,
                   val category: Category,
                   val modificatorGroups: List<TechMapGroup>) : Element {
    override val elementType: ElementType
        get() = ElementType.TECHMAP
}

data class CheckItem(val id: Long?, val imageUrl: String, val name: String, var amount: Int, val price: Double,
                     var fullPrice: Double, val type: CheckItemCategoryType,
                     val modificators: List<BillModificatorWrapper>)

data class Check(val number: Int, val hash: UUID, val items: List<CheckItem>, var status: CheckStatus,
                 val created: String, var closed: String, var historySelected: Boolean, var payType: PayType)

data class ElementWrapper(val code: Int, val elements: List<Element>)

data class Printer(val name: String, val connectionType: PrinterConnectionType, val ip: String, val port: String,
                   var selected: Boolean)

data class PrinterQueueWrapper(val action: PrinterAction, val params: List<Any>)

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