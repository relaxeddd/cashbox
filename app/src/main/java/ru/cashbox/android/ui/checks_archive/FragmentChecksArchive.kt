package ru.cashbox.android.ui.checks_archive

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.cashbox.android.R
import ru.cashbox.android.common.showPopupMenu
import ru.cashbox.android.databinding.FragmentChecksArchiveBinding
import ru.cashbox.android.model.Bill
import ru.cashbox.android.model.CheckStatus
import ru.cashbox.android.model.EventType
import ru.cashbox.android.ui.base.FragmentBase
import ru.cashbox.android.ui.cash.AdapterChecks
import ru.cashbox.android.ui.cash.AdapterCheckItems

class FragmentChecksArchive : FragmentBase<ViewModelChecksArchive, FragmentChecksArchiveBinding>() {

    private lateinit var adapterChecks: AdapterChecks
    private lateinit var adapterCheckItems: AdapterCheckItems

    override fun getLayoutResId() = R.layout.fragment_checks_archive
    override val viewModel: ViewModelChecksArchive by viewModel()

    override fun configureBinding() {
        super.configureBinding()
        binding.viewModel = viewModel
        binding.buttonChecksArchiveChangeStatus.setOnClickListener {
            val checkStatus = adapterChecks.checkedCheck?.status ?: CheckStatus.RETURNED
            showPopupChangeStatus(it, checkStatus)
        }

        adapterChecks = AdapterChecks(viewModel.clickListenerChecks)
        binding.recyclerViewChecksArchive.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewChecksArchive.adapter = adapterChecks
        viewModel.archiveChecks.observe(viewLifecycleOwner, Observer { items ->
            if (adapterChecks.checkedCheck == null) adapterChecks.checkedCheck = viewModel.selectedCheck.value
            if (items != null) {
                adapterChecks.submitList(items.sortedWith(object: Comparator<Bill> {
                    override fun compare(o1: Bill, o2: Bill): Int {
                        if (o1.lastChange - o2.lastChange > 200) {
                            return o2.lastChange.compareTo(o1.lastChange)
                        } else {
                            return o2.id.compareTo(o1.id)
                        }
                    }

                }))
            }
        })
        viewModel.selectedCheck.observe(viewLifecycleOwner, Observer { selectedCheck ->
            if (adapterChecks.checkedCheck != selectedCheck) adapterChecks.checkedCheck = selectedCheck
        })

        adapterCheckItems = AdapterCheckItems(isEditable = false)
        binding.recyclerViewChecksArchiveGoods.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewChecksArchiveGoods.adapter = adapterCheckItems
        viewModel.checkItems.observe(viewLifecycleOwner, Observer { items ->
            if (items != null) adapterCheckItems.submitList(items)
        })
    }

    override fun onNavigationEvent(type: EventType, args: Bundle?) {
        when (type) {
            EventType.NAVIGATION_ARCHIVE_TO_CASH -> {
                navigationController.navigate(R.id.action_navigation_checks_archive_to_navigation_cash)
            }
            else -> super.onNavigationEvent(type, args)
        }
    }

    private fun showPopupChangeStatus(view: View, status: String) {
        val popupMenu = PopupMenu(context, view)
        val listener = PopupMenu.OnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.item_change_status_pay -> {
                    showPopupPayType(view)
                    true
                }
                R.id.item_change_status_close -> {
                    viewModel.closeCheck()
                    true
                }
                R.id.item_change_status_return -> {
                    viewModel.returnCheck()
                    true
                }
                R.id.item_change_status_open -> {
                    viewModel.openCheck()
                    true
                }
                else -> false
            }
        }
        popupMenu.inflate(R.menu.menu_archive_change_status)
        popupMenu.setOnMenuItemClickListener(listener)
        popupMenu.menu.findItem(R.id.item_change_status_close).isVisible = status == CheckStatus.PAYED
        popupMenu.menu.findItem(R.id.item_change_status_return).isVisible = status == CheckStatus.PAYED
        popupMenu.menu.findItem(R.id.item_change_status_open).isVisible = status == CheckStatus.PAYED || status == CheckStatus.CLOSED
        popupMenu.menu.findItem(R.id.item_change_status_pay).isVisible = status == CheckStatus.CLOSED

        popupMenu.show()
    }

    private fun showPopupPayType(view: View) {
        showPopupMenu(context, view, R.menu.menu_pay_type, PopupMenu.OnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.item_pay_type_cash -> {
                    viewModel.payCash()
                    true
                }
                R.id.item_pay_type_card -> {
                    viewModel.payCard()
                    true
                }
                else -> false
            }
        })
    }
}