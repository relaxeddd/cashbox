package ru.cashbox.android.ui.checks_archive

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.launch
import retrofit2.Response
import ru.cashbox.android.App
import ru.cashbox.android.R
import ru.cashbox.android.common.ClickListenerCheck
import ru.cashbox.android.common.UNAUTHORIZED
import ru.cashbox.android.model.*
import ru.cashbox.android.model.repositories.RepositoryChecks
import ru.cashbox.android.printer.PrinterHelper
import ru.cashbox.android.ui.base.ViewModelBase

class ViewModelChecksArchive(app: App, private val repositoryChecks: RepositoryChecks) : ViewModelBase(app) {

    private val checks: LiveData<List<Bill>> = repositoryChecks.bills

    val archiveChecks = MutableLiveData<List<Bill>>(ArrayList())
    val checkItems = MutableLiveData<List<CheckItem>>(ArrayList())

    val textCheckNumber = MutableLiveData("")
    val textWaiterName = MutableLiveData("")
    val textCheckOpenTime = MutableLiveData("")
    val textCheckStatus = MutableLiveData("")
    val textCheckPayType = MutableLiveData("")
    val isEnabledChangeStatusButton = MutableLiveData(true)
    val isVisiblePayType = MutableLiveData(true)

    val selectedCheck = MutableLiveData<Bill?>(null)

    private val selectedCheckObserver = Observer<Bill?> { check ->
        var textStatus = CheckStatus.getName(getApplication<App>(), check?.status ?: "") + ""
        var textPayType = ""
        val newCheckItems = ArrayList<CheckItem>()
        newCheckItems.addAll(check?.items ?: ArrayList())
        newCheckItems.addAll(check?.techmaps ?: ArrayList())

        when(check?.status ?: "") {
            CheckStatus.PAYED -> textPayType = PayType.getName(getApplication<App>(), check?.payedType ?: "")
        }

        checkItems.value = newCheckItems
        textCheckNumber.value = if (check?.id != null) check.id.toString() else ""
        textWaiterName.value = check?.user?.fullname ?: ""
        textCheckOpenTime.value = check?.opened?.substring(0, check.opened.length - 9) ?: ""
        textCheckStatus.value = textStatus
        isEnabledChangeStatusButton.value = check?.status != CheckStatus.RETURNED
        textCheckPayType.value = textPayType
        isVisiblePayType.value = textPayType.isNotEmpty()
    }

    private val checksObserver = Observer<List<Bill>> { bills ->
        val filteredChecks = bills.filter { it.status != CheckStatus.OPENED }
        archiveChecks.value = filteredChecks

        if (selectedCheck.value == null && filteredChecks.isNotEmpty()) {
            selectedCheck.value = filteredChecks.first()
        } else if (!filteredChecks.contains(selectedCheck.value)) {
            if (filteredChecks.isEmpty()) {
                selectedCheck.value = null
            } else {
                selectedCheck.value = filteredChecks.first()
            }
        }
    }

    val clickListenerChecks = object: ClickListenerCheck {
        override fun onClick(check: Bill) {
            selectCheck(check)
        }
    }

    init {
        checks.observeForever(checksObserver)
        selectedCheck.observeForever(selectedCheckObserver)
    }

    override fun onCleared() {
        super.onCleared()
        checks.removeObserver(checksObserver)
        selectedCheck.removeObserver(selectedCheckObserver)
    }

    fun payCash() {
        changeBillStatus(getString(R.string.check_paid)) { repositoryChecks.payBillByCash(it) }
    }

    fun payCard() {
        changeBillStatus(getString(R.string.check_paid)) { repositoryChecks.payBillByWire(it) }
    }

    fun closeCheck() {
        changeBillStatus(getString(R.string.check_closed)) { repositoryChecks.closeBill(it) }
    }

    fun returnCheck() {
        changeBillStatus(getString(R.string.check_returned)) { repositoryChecks.returnBill(it) }
    }

    fun openCheck() {
        changeBillStatus(getString(R.string.check_opened)) { repositoryChecks.openBill(it) }
    }

    fun print() {
        val check = selectedCheck.value ?: return
        PrinterHelper.printCheck(getApplication(), check)
    }

    fun closeArchive() {
        navigateEvent.value = NavigationEvent(EventType.GO_BACK)
    }

    private fun selectCheck(check: Bill) {
        selectedCheck.value = check
    }

    private fun changeBillStatus(successText: String, changeRequest: suspend (Bill) -> Response<Bill>) {
        val bill = selectedCheck.value

        if (bill == null) {
            showToast(R.string.error_check_data)
            return
        }

        navigateEvent.value = NavigationEvent(EventType.LOADING_SHOW)

        uiScope.launch {
            val response = changeRequest(bill)
            val updatedCheck = response.body()

            navigateEvent.value = NavigationEvent(EventType.LOADING_HIDE)
            if (response.isSuccessful && updatedCheck != null) {
                selectedCheck.value = if (archiveChecks.value?.contains(updatedCheck) == true) updatedCheck else null
                showToast(successText)
            } else if (response.code() == UNAUTHORIZED) {
                showToast(R.string.error_unauthorized)
                navigateEvent.value = NavigationEvent(EventType.NAVIGATION_UNAUTHORIZED)
            } else {
                showToast(R.string.error_сheck_change)
            }
        }
    }
}