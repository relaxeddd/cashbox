package ru.cashbox.android.ui.cash

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.cashbox.android.R
import ru.cashbox.android.common.*
import ru.cashbox.android.databinding.FragmentCashBinding
import ru.cashbox.android.model.EventType
import ru.cashbox.android.model.TechMapModificator
import ru.cashbox.android.printer.PrinterSettingsActivity
import ru.cashbox.android.ui.base.FragmentBase
import ru.cashbox.android.ui.dialogs.DialogConfirmCloseCashbox
import ru.cashbox.android.ui.dialogs.DialogConfirmCloseEmployeeSession
import ru.cashbox.android.ui.dialogs.DialogModificators
import ru.cashbox.android.ui.enter_number.FragmentEnterNumber
import java.util.*
import kotlin.collections.ArrayList

class FragmentCash : FragmentBase<ViewModelCash, FragmentCashBinding>() {

    private lateinit var adapterElements: AdapterElements
    private lateinit var adapterElementNavigation: AdapterElementNavigation
    private lateinit var adapterCheckItems: AdapterCheckItems
    private lateinit var adapterModificators: AdapterModificators
    private lateinit var adapterCheckChips: AdapterCheckChips

    override fun getLayoutResId() = R.layout.fragment_cash
    override val viewModel: ViewModelCash by viewModel()

    override fun configureBinding() {
        super.configureBinding()

        binding.viewModel = viewModel

        adapterElements = AdapterElements(viewModel.apiAddress, viewModel.clickListenerElements)
        binding.recyclerViewCashGoods.layoutManager = GridLayoutManager(context, 3)
        binding.recyclerViewCashGoods.adapter = adapterElements
        viewModel.adapterElements.observe(viewLifecycleOwner, Observer { items ->
            if (items != null) adapterElements.submitList(items)
        })

        adapterElementNavigation = AdapterElementNavigation(viewModel.clickListenerElementNavigation)
        binding.recyclerViewCashGoodsNavigation.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewCashGoodsNavigation.adapter = adapterElementNavigation
        viewModel.navigationElements.observe(viewLifecycleOwner, Observer { items ->
            if (items != null) adapterElementNavigation.submitList(items)
        })

        adapterCheckItems = AdapterCheckItems(viewModel.listenerCheck)
        binding.recyclerViewGoodCheck.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewGoodCheck.adapter = adapterCheckItems
        viewModel.currentCheckItems.observe(viewLifecycleOwner, Observer { items ->
            if (items != null) adapterCheckItems.submitList(items)
        })
        val itemTouchHelper = ItemTouchHelper(adapterCheckItems.swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewGoodCheck)

        /*adapterModificators = AdapterModificators(viewModel.apiAddress)
        binding.recyclerViewCashModificators.layoutManager = GridLayoutManager(context, 3)
        binding.recyclerViewCashModificators.adapter = adapterModificators
        viewModel.modificators.observe(viewLifecycleOwner, Observer { items ->
            if (items != null) adapterModificators.submitList(items)
        })*/

        adapterCheckChips = AdapterCheckChips(viewModel.clickListenerChecks)
        binding.recyclerViewCashChecks.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewCashChecks.adapter = adapterCheckChips
        viewModel.openChecks.observe(viewLifecycleOwner, Observer { items ->
            if (items != null) {
                adapterCheckChips.submitList(items.sortedBy { it.id })
            }
        })
        viewModel.currentCheck.observe(viewLifecycleOwner, Observer { check ->
            adapterCheckChips.checkedCheck = check
        })

        binding.buttonCashDots.setOnClickListener { showPopupDots(it) }
        binding.buttonCashPay.setOnClickListener { showPopupPay(it) }
        binding.buttonCashModificatorsAddToCheck.setOnClickListener { viewModel.onClickAddTechMap(adapterModificators.selectedModificators) }
    }

    override fun onNavigationEvent(type: EventType, args: Bundle?) {
        when (type) {
            EventType.CLOSE_CASHOBOX -> {
                val dialog = DialogConfirmCloseCashbox()
                dialog.confirmListener = object : ListenerResult<Boolean> {
                    override fun onResult(result: Boolean) {
                        if (result) {
                            val args = Bundle()
                            args.putInt(CASHSESSION_TYPE, FragmentEnterNumber.TYPE_CASHSESSION_CLOSE)
                            navigationController.navigate(R.id.action_navigation_cash_to_navigation_enter_number, args)
                        }
                    }
                }
                dialog.show(childFragmentManager, dialog.javaClass.name)
            }
            EventType.CLOSE_EMPLOYEE_SESSION_QUESTION -> {
                val dialog = DialogConfirmCloseEmployeeSession()
                dialog.confirmListener = object : ListenerResult<Boolean> {
                    override fun onResult(result: Boolean) {
                        if (result) viewModel.closeEmployeeSession()
                    }
                }
                dialog.show(childFragmentManager, dialog.javaClass.name)
            }
            EventType.DIALOG_MODIFICATORS -> {
                val dialog = DialogModificators()
                val modificators: ArrayList<TechMapModificator> = ArrayList()

                (viewModel.modificators.value ?: ArrayList()).forEach { modificators.add(it.copy()) }
                dialog.apiAddress = args?.getString(API)
                dialog.titleText = args?.getString(TEXT)
                dialog.priceText = args?.getString(PRICE)
                dialog.modificators = modificators
                dialog.confirmListener = object: ListenerResult<List<TechMapModificator>> {
                    override fun onResult(result: List<TechMapModificator>) {
                        viewModel.addTechMapToCheckWithCheck(result)
                    }
                }
                dialog.show(childFragmentManager, "Enter Domain Dialog")
            }
            EventType.CLEAR_SELECTED_MODIFICATORS -> {
                adapterModificators.selectedModificators.clear()
            }
            EventType.NAVIGATION_CASH_TO_DISCOUNT -> {
                navigationController.navigate(R.id.action_navigation_cash_to_navigation_discount, args)
            }
            EventType.NAVIGATION_CASH_TO_LOGIN_EMPLOYEE -> {
                navigationController.navigate(R.id.action_navigation_cash_to_navigation_login_employee)
            }
            EventType.NAVIGATION_CASH_TO_CHECKS -> {
                navigationController.navigate(R.id.action_navigation_cash_to_navigation_checks_archive)
            }
            EventType.NAVIGATION_CASH_TO_CREATE_SUPPLY -> {
                navigationController.navigate(R.id.action_navigation_cash_to_navigation_create_supply)
            }
            EventType.NAVIGATION_CASH_TO_PRINTERS -> {
                startActivity(Intent(context, PrinterSettingsActivity::class.java))
            }
            else -> super.onNavigationEvent(type, args)
        }
    }

    private fun showPopupDots(view: View) {
        showPopupMenu(context, view, R.menu.menu_check_dots, PopupMenu.OnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.item_check_dots_clear -> {
                    viewModel.checkClear()
                    true
                }
                R.id.item_check_dots_close -> {
                    viewModel.checkClose()
                    true
                }
                else -> false
            }
        })
    }

    private fun showPopupPay(view: View) {
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