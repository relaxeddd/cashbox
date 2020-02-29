package ru.cashbox.android.ui.enter_number

import android.os.Bundle
import ru.cashbox.android.R
import ru.cashbox.android.ui.base.FragmentBase

import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.cashbox.android.common.CASHSESSION_TYPE
import ru.cashbox.android.databinding.FragmentEnterNumberBinding
import ru.cashbox.android.model.EventType
import ru.cashbox.android.ui.base.ActivityBase

class FragmentEnterNumber : FragmentBase<ViewModelEnterNumber, FragmentEnterNumberBinding>() {

    companion object {
        const val TYPE_CASHSESSION_OPEN = 0
        const val TYPE_CASHSESSION_CLOSE = 1
    }

    override fun getLayoutResId() = R.layout.fragment_enter_number
    override val viewModel: ViewModelEnterNumber by viewModel()

    override fun configureBinding() {
        super.configureBinding()

        val type = arguments?.getInt(CASHSESSION_TYPE, TYPE_CASHSESSION_OPEN) ?: TYPE_CASHSESSION_OPEN

        viewModel.isOpenCashbox.value = type == TYPE_CASHSESSION_OPEN
        binding.viewModel = viewModel
    }

    override fun onNavigationEvent(type: EventType, args: Bundle?) {
        when (type) {
            EventType.NAVIGATION_ENTER_NUMBER_TO_CASH -> {
                navigationController.navigate(R.id.action_navigation_enter_number_to_navigation_cash)
            }
            EventType.NAVIGATION_ENTER_NUMBER_TO_EMPLOYEE_ROOM -> {
                navigationController.navigate(R.id.action_navigation_enter_number_to_navigation_employee_room)
            }
            else -> super.onNavigationEvent(type, args)
        }
    }

    /*override fun onBackPressed(): Boolean {
        val isLoadingCancel = super.onBackPressed()
        val currentActivity = activity

        if (!isLoadingCancel && currentActivity is ActivityBase<*, *>) {
            currentActivity.invokeOnBackPressed()
        }
        return true
    }*/
}