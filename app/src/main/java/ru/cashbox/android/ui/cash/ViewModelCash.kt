package ru.cashbox.android.ui.cash

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.launch
import retrofit2.Response
import ru.cashbox.android.App
import ru.cashbox.android.R
import ru.cashbox.android.common.*
import ru.cashbox.android.model.*
import ru.cashbox.android.model.repositories.*
import ru.cashbox.android.printer.PrinterHelper
import ru.cashbox.android.ui.base.ViewModelBase
import java.util.*
import kotlin.collections.ArrayList

class ViewModelCash(app: App, private val repositoryUsers: RepositoryUsers,
                    private val repositoryCashsessions: RepositoryCashsessions,
                    private val repositoryGoods: RepositoryGoods,
                    private val repositoryChecks: RepositoryChecks,
                    settings: RepositorySettings) : ViewModelBase(app) {

    val apiAddress: LiveData<String> = settings.apiAddress
    private val checks: LiveData<List<Bill>> = repositoryChecks.bills
    private val checkItems: LiveData<List<CheckItem>> = repositoryChecks.checkItems

    val textEmployeeName = MutableLiveData(repositoryUsers.sessionEmployee.value?.user?.fullname ?: "")
    val textCheckNumber = MutableLiveData("")
    val textModificatorsTitle = MutableLiveData("")
    val textDiscount = MutableLiveData(0.0)
    val textTotal = MutableLiveData(0.0)
    val textToPay = MutableLiveData(0.0)
    val textModificatorsTotal = MutableLiveData(0.0)
    val isVisibleButtonNewCheck = MutableLiveData(false)
    val isEnabledButtonPay = MutableLiveData(true)

    val adapterElements = MutableLiveData<List<Element>>(ArrayList())
    val navigationElements = MutableLiveData<List<Element>>(ArrayList())
    val modificators = MutableLiveData<List<TechMapModificator>>(ArrayList())
    val isVisibleContainerModificators = MutableLiveData(false)
    val isVisibleContainerMenu = MutableLiveData(false)

    val currentCheckItems = MutableLiveData<List<CheckItem>>(ArrayList())
    val currentCheck = MutableLiveData<Bill?>(null)
    val openChecks = MutableLiveData<List<Bill>>(ArrayList())

    private val ingredients: LiveData<List<Ingredient>> = repositoryGoods.ingredients
    private val categories: LiveData<List<Category>> = repositoryGoods.categories
    private val techMaps: LiveData<List<TechMap>> = repositoryGoods.techMaps

    //------------------------------------------------------------------------------------------------------------------
    val clickListenerElements = object: ClickListenerElements {
        override fun onClick(element: Element) {
            onClickElement(element)
        }
    }

    val clickListenerElementNavigation = object: ClickListenerElements {
        override fun onClick(element: Element) {
            onClickElement(element)
        }
    }

    val clickListenerChecks = object: ClickListenerCheck {
        override fun onClick(check: Bill) {
            selectCheck(check)
        }
    }

    val listenerCheck = object: CheckListenerBillElements {
        override fun onSwipe(element: CheckItem) {
            onRemoveBillElement(element)
        }
        override fun onMinus(element: CheckItem) {
            onClickBillElementMinus(element)
        }
        override fun onPlus(element: CheckItem) {
            onClickBillElementPlus(element)
        }
        override fun onClick(element: CheckItem) {
            val args = Bundle()
            args.putLong(ITEM_ID, element.billId)
            args.putLong(ITEM_TIMESTAMP, element.timestamp)
            navigateEvent.value = NavigationEvent(EventType.NAVIGATION_CASH_TO_DISCOUNT, args)
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    private val currentCheckObserver = Observer<Bill?> { bill ->
        isVisibleButtonNewCheck.value = bill == null
        textCheckNumber.value = if (bill != null) getString(R.string.check_number, bill.id) else ""
        updateCurrentCheckItems()
    }

    private val checksObserver = Observer<List<Bill>> { checks ->
        val currentChecks = checks.filter { it.status == CheckStatus.OPENED }
        openChecks.value = currentChecks
        if (currentCheck.value == null && currentChecks.isNotEmpty()) {
            currentCheck.value = currentChecks.first()
        }
    }

    private val checkItemsObserver = Observer<List<CheckItem>> { items ->
        updateCurrentCheckItems(items)
    }

    private val currentCheckItemsObserver = Observer<List<CheckItem>> { bills ->
        var sum = 0.0
        var finalSum = 0.0
        bills.forEach {
            sum += it.price * it.count
            finalSum += it.getFinalPrice()
        }

        textTotal.value = sum
        textToPay.value = finalSum
        textDiscount.value = if (sum != 0.0) (sum - finalSum) else 0.0
        isEnabledButtonPay.value = bills.isNotEmpty()

        requestUpdateCheckWithoutNotify()
    }

    //------------------------------------------------------------------------------------------------------------------
    init {
        currentCheckItems.observeForever(currentCheckItemsObserver)
        currentCheck.observeForever(currentCheckObserver)
        checks.observeForever(checksObserver)
        checkItems.observeForever(checkItemsObserver)
    }

    override fun onCleared() {
        super.onCleared()
        currentCheckItems.removeObserver(currentCheckItemsObserver)
        currentCheck.removeObserver(currentCheckObserver)
        checks.removeObserver(checksObserver)
        checkItems.removeObserver(checkItemsObserver)
    }

    override fun onViewResume() {
        super.onViewResume()

        navigateEvent.value = NavigationEvent(EventType.LOADING_SHOW)
        uiScope.launch {
            repositoryGoods.requestItems()
            repositoryChecks.requestBills()
            homeGoodsNavigation()
            navigateEvent.value = NavigationEvent(EventType.LOADING_HIDE)
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    fun homeGoodsNavigation() {
        val mainCategory = categories.value?.find { it.defaultCategory }
        val elements = if (mainCategory != null) getElementsByCategoryId(mainCategory.id) else ArrayList()

        updateAdapters(elements, ArrayList())
    }

    fun closeModificators() {
        navigateEvent.value = NavigationEvent(EventType.CLEAR_SELECTED_MODIFICATORS)
        modificators.value = ArrayList()
        textModificatorsTotal.value = 0.0
        textModificatorsTitle.value = ""
        isVisibleContainerModificators.value = false
    }

    fun onClickAddTechMap(modificators: List<TechMapModificator>) {
        if (currentCheck.value == null) {
            newCheck {addTechMapToCheck(modificators)}
        } else {
            addTechMapToCheck(modificators)
        }
    }

    fun checkClear() {
        repositoryChecks.clearCheckItems(currentCheck.value?.id ?: UNDEFINED_ID)
    }

    fun payCash() {
        changeBillStatus(getString(R.string.check_paid, true)) { repositoryChecks.payBillByCash(it) }
    }

    fun payCard() {
        changeBillStatus(getString(R.string.check_paid), true) { repositoryChecks.payBillByWire(it) }
    }

    fun checkClose() {
        changeBillStatus(getString(R.string.check_closed)) { repositoryChecks.closeBill(it) }
    }

    fun onClickCloseEmployeeSession() {
        navigateEvent.value = NavigationEvent(EventType.CLOSE_EMPLOYEE_SESSION_QUESTION)
    }

    fun onClickCloseCashsession() {
        navigateEvent.value = NavigationEvent(EventType.CLOSE_CASHOBOX)
    }

    fun closeEmployeeSession() {
        navigateEvent.value = NavigationEvent(EventType.LOADING_SHOW)

        uiScope.launch {
            val responseCloseEmployeeSession = repositoryUsers.logoutEmployee()

            navigateEvent.value = NavigationEvent(EventType.LOADING_HIDE)

            if (responseCloseEmployeeSession.code() == UNAUTHORIZED) {
                showToast(R.string.error_unauthorized)
                navigateEvent.value = NavigationEvent(EventType.NAVIGATION_UNAUTHORIZED)
                return@launch
            }
            if (!responseCloseEmployeeSession.isSuccessful) {
                showToast(R.string.error_close_employee_session)
            }

            navigateEvent.value = NavigationEvent(EventType.NAVIGATION_CASH_TO_LOGIN_EMPLOYEE)
        }
    }

    fun newCheck() {
        newCheck(null)
    }

    private fun newCheck(successListener: (() -> Unit)? = null) {
        navigateEvent.value = NavigationEvent(EventType.LOADING_SHOW)
        uiScope.launch {
            val responseCheck = repositoryChecks.createEmptyBill()

            navigateEvent.value = NavigationEvent(EventType.LOADING_HIDE)
            val check = responseCheck.body()

            if (responseCheck.isSuccessful && check != null) {
                showToast(getString(R.string.check_created, check.id))
                //saveCurrentCheckState()
                currentCheck.value = check
                successListener?.invoke()
            } else if (responseCheck.code() == UNAUTHORIZED) {
                showToast(R.string.error_unauthorized)
                navigateEvent.value = NavigationEvent(EventType.NAVIGATION_UNAUTHORIZED)
            } else {
                showToast(R.string.error_create_check)
            }
        }
    }

    fun clickMenu() {
        isVisibleContainerMenu.value = true
    }

    fun clickHome() {
        isVisibleContainerMenu.value = false
    }

    fun showCheckHistory() {
        val finishedChecks = checks.value?.filter { it.status != CheckStatus.OPENED }

        if (finishedChecks?.isNotEmpty() == true) {
            navigateEvent.value = NavigationEvent(EventType.NAVIGATION_CASH_TO_CHECKS)
        } else {
            showToast(R.string.check_history_empty)
        }
    }

    fun showPrinters() {
        navigateEvent.value = NavigationEvent(EventType.NAVIGATION_CASH_TO_PRINTERS)
    }

    fun clickCreateSupply() {
        navigateEvent.value = NavigationEvent(EventType.NAVIGATION_CASH_TO_CREATE_SUPPLY)
    }

    //------------------------------------------------------------------------------------------------------------------
    private fun addTechMapToCheck(techMap: TechMap) {
        val checkId = currentCheck.value?.id ?: return
        val billTechMap = BillTechMap(techMap.id, checkId, techMap.price, 1, 0, techMap.costPrice,
            ArrayList(), null, techMap, System.currentTimeMillis())

        repositoryChecks.addCheckItem(billTechMap)
    }

    fun addTechMapToCheck(modificators: List<TechMapModificator>) {
        var selectedTechMap: TechMap? = null

        if (modificators.isNotEmpty()) {
            for (techMap in techMaps.value ?: ArrayList()) {
                if (techMap.modificatorGroups.isNotEmpty()) {
                    val group = techMap.modificatorGroups.find { it.id == modificators.first().groupId }

                    if (group != null) {
                        selectedTechMap = techMap
                        break
                    }
                }
            }
        }

        if (selectedTechMap != null) {
            val checkId = currentCheck.value?.id ?: return
            val billModificators = ArrayList<BillModificator>()

            modificators.forEach { billModificators.add(BillModificator(it.id, it.name,0.0, it.count, 0.0)) }
            val techMap = BillTechMap(selectedTechMap.id, checkId, selectedTechMap.price, 1, 0, selectedTechMap.costPrice,
                billModificators, null, selectedTechMap, System.currentTimeMillis())

            repositoryChecks.addCheckItem(techMap)
        }

        //closeModificators()
    }

    private fun onClickElement(element: Element) {
        when(element.elementType) {
            ElementType.GOOD -> if (element is Ingredient) onClickGood(element)
            ElementType.CATEGORY -> if (element is Category) onClickCategory(element)
            ElementType.TECHMAP -> if (element is TechMap) onClickTechMap(element)
        }
    }

    private fun onClickGood(item: Ingredient) {
        if (currentCheck.value == null) {
            newCheck { addGoodToCheck(item) }
        } else {
            addGoodToCheck(item)
        }
    }

    private fun addGoodToCheck(item: Ingredient) {
        val checkId = currentCheck.value?.id ?: return
        val responseIngredient = ResponseIngredient(item.id, item.imageUrl, item.name, item.price, item.costPrice, item.type, item.unit)
        val addedItem = BillItem(item.id, checkId, item.price, 1, 0, item.costPrice, responseIngredient, System.currentTimeMillis())

        repositoryChecks.addCheckItem(addedItem)
    }

    private fun onClickCategory(category: Category) {
        updateAdapters(getElementsByCategoryId(category.id), getElementsPathByCategory(category))
    }

    private fun onClickTechMap(techMap: TechMap) {
        val techMapModificators = ArrayList<TechMapModificator>()

        if (techMap.modificatorGroups.isNotEmpty()) {
            techMap.modificatorGroups.forEach { group -> group.modificators.forEach { techMapModificators.add(it) } }

            modificators.value = techMapModificators

            val args = Bundle()
            args.putString(TEXT, getString(R.string.modificators_title, techMap.name))
            args.putString(API, apiAddress.value)
            args.putString(PRICE, String.format(Locale.getDefault(), "%,.2f", techMap.price))

            navigateEvent.value = NavigationEvent(EventType.DIALOG_MODIFICATORS, args)
        } else {
            if (currentCheck.value == null) {
                newCheck { addTechMapToCheck(techMap) }
            } else {
                addTechMapToCheck(techMap)
            }
        }
    }

    private fun onRemoveBillElement(element: CheckItem) {
        repositoryChecks.removeCheckItem(element)
    }

    private fun onClickBillElementMinus(item: CheckItem) {
        if (item.count > 1) {
            val changedElement = when (item.elementType) {
                ElementType.GOOD -> (item as BillItem).copy(count = item.count - 1)
                ElementType.TECHMAP -> (item as BillTechMap).copy(count = item.count - 1)
                else -> null
            }
            repositoryChecks.updateCheckItem(changedElement ?: return)
        } else {
            repositoryChecks.removeCheckItem(item)
        }
    }

    private fun onClickBillElementPlus(element: CheckItem) {
        val changedElement = when (element.elementType) {
            ElementType.GOOD -> (element as BillItem).copy(count = element.count + 1)
            ElementType.TECHMAP -> (element as BillTechMap).copy(count = element.count + 1)
            else -> null
        }
        repositoryChecks.updateCheckItem(changedElement ?: return)
    }

    //------------------------------------------------------------------------------------------------------------------
    private fun getElementsByCategoryId(categoryId: Long) : List<Element> {
        val elements = ArrayList<Element>()

        elements.addAll(categories.value?.filter { it.parentId == categoryId } ?: ArrayList())
        elements.addAll(ingredients.value?.filter { it.categoryId == categoryId } ?: ArrayList())
        elements.addAll(techMaps.value?.filter { it.categoryId == categoryId } ?: ArrayList())

        return elements
    }

    private fun getElementsPathByCategory(category: Category) : List<Element> {
        val elements = ArrayList<Element>()
        var parentCategory: Category? = category

        while(parentCategory != null) {
            elements.add(parentCategory)
            parentCategory = categories.value?.find { it.id == parentCategory?.parentId && !it.defaultCategory }
        }
        elements.reverse()

        return elements
    }

    private fun updateAdapters(elements: List<Element>, navigation: List<Element>) {
        adapterElements.value = elements
        navigationElements.value = navigation
    }

    /*private fun saveCurrentCheckState() {
        val currentCheck = currentCheck.value

        if (currentCheck != null) {
            var existsCheckState: Pair<Bill, List<CheckItem>>? = null

            for (existsCheckPair in savedChecks) {
                if (existsCheckPair.first.id == currentCheck.id) {
                    existsCheckState = existsCheckPair
                }
            }

            if (existsCheckState != null) {
                savedChecks.remove(existsCheckState)
            }
            savedChecks.add(Pair(currentCheck, currentCheckItems.value ?: ArrayList()))
        }
    }*/

    private fun selectCheck(bill: Bill?) {
        //saveCurrentCheckState()
        if (bill != currentCheck.value && bill != null) {
            /*for (existsCheckPair in savedChecks) {
                if (existsCheckPair.first.id == bill.id) {
                    currentCheck.value = existsCheckPair.first
                    currentCheckItems.value = existsCheckPair.second
                    break
                }
            }*/
            currentCheck.value = bill
        } else if (bill == null) {
            currentCheck.value = null
            currentCheckItems.value = ArrayList()
        }
    }

    private fun changeBillStatus(successText: String, isPaid: Boolean = false, changeRequest: suspend (Bill) -> Response<Bill>) {
        val bill = currentCheck.value

        if (bill == null) {
            showToast(R.string.error_check_data)
            return
        }

        navigateEvent.value = NavigationEvent(EventType.LOADING_SHOW)

        uiScope.launch {
            val response = changeRequest(bill)
            val check = response.body()

            navigateEvent.value = NavigationEvent(EventType.LOADING_HIDE)
            if (response.isSuccessful) {
                val checks = openChecks.value

                if (successText.isNotEmpty()) showToast(successText)
                if (checks?.isNotEmpty() == true) {
                    selectCheck(checks.first())
                } else {
                    currentCheck.value = null
                    selectCheck(null)
                }
                if (isPaid) {
                    if (check != null) {
                        PrinterHelper.printCheck(getApplication(), check)
                    } else {
                        showToast(R.string.error_сheck_print)
                    }
                }
            } else if (response.code() == UNAUTHORIZED) {
                showToast(R.string.error_unauthorized)
                navigateEvent.value = NavigationEvent(EventType.NAVIGATION_UNAUTHORIZED)
            } else {
                showToast(R.string.error_сheck_change)
            }
        }
    }

    private fun updateCurrentCheckItems(items: List<CheckItem>? = checkItems.value) {
        val checkId = currentCheck.value?.id ?: -1L
        currentCheckItems.value = if (checkId != -1L) items?.filter { it.checkId == checkId } else ArrayList()
    }

    private fun requestUpdateCheckWithoutNotify() {
        uiScope.launch {
            val response = repositoryChecks.changeBillItems(currentCheck.value ?: return@launch, false)

            if (response.code() == UNAUTHORIZED) {
                showToast(R.string.error_unauthorized)
                navigateEvent.value = NavigationEvent(EventType.NAVIGATION_UNAUTHORIZED)
            }
        }
    }
}