package ru.cashbox.android.ui.discount

import android.os.Bundle
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.cashbox.android.R
import ru.cashbox.android.common.ITEM_ID
import ru.cashbox.android.common.ITEM_TIMESTAMP
import ru.cashbox.android.databinding.FragmentDiscountBinding
import ru.cashbox.android.model.EventType
import ru.cashbox.android.ui.base.FragmentBase

class FragmentDiscount : FragmentBase<ViewModelDiscount, FragmentDiscountBinding>() {

    override fun getLayoutResId() = R.layout.fragment_discount
    override val viewModel: ViewModelDiscount by viewModel()

    override fun configureBinding() {
        super.configureBinding()
        val itemId = arguments?.getLong(ITEM_ID) ?: 0
        val itemTimestamp = arguments?.getLong(ITEM_TIMESTAMP) ?: 0

        viewModel.initCheckItem(itemId, itemTimestamp)
        binding.viewModel = viewModel
    }

    override fun onNavigationEvent(type: EventType, args: Bundle?) {
        when (type) {
            EventType.NAVIGATION_DISCOUNT_TO_CASH -> {
                navigationController.navigate(R.id.action_navigation_discount_to_navigation_cash)
            }
            else -> super.onNavigationEvent(type, args)
        }
    }
}