package ru.cashbox.android.ui.create_supply

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.cashbox.android.R
import ru.cashbox.android.common.showKeyboard
import ru.cashbox.android.databinding.FragmentCreateSupplyBinding
import ru.cashbox.android.model.BuyAccount
import ru.cashbox.android.model.BuyVendor
import ru.cashbox.android.model.EventType
import ru.cashbox.android.model.Ingredient
import ru.cashbox.android.ui.base.FragmentBase
import java.text.DecimalFormat

class FragmentCreateSupply : FragmentBase<ViewModelCreateSupply, FragmentCreateSupplyBinding>() {

    private lateinit var adapterSupplyItems: AdapterSupplyItems

    override fun getLayoutResId() = R.layout.fragment_create_supply
    override val viewModel: ViewModelCreateSupply by viewModel()

    override fun configureBinding() {
        super.configureBinding()
        binding.viewModel = viewModel
        binding.textCreateSupplyAmount.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.textAmount.value = s.toString()
            }
        })
        binding.textCreateSupplyAmount.setOnKeyListener(object: View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (event?.action == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                            || keyCode == KeyEvent.KEYCODE_ENTER)) {
                    binding.containerTextCreateSupplyPrice.requestFocus()
                    //showKeyboard(context ?: return true, binding.textCreateSupplyPrice)
                    return true
                }
                return false
            }
        })
        binding.textCreateSupplyPrice.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.textPrice.value = s.toString()
            }
        })
        binding.buttonCreateSupplyAddPosition.setOnClickListener {
            val item = binding.spinnerCreateSupplyName.selectedItem
            viewModel.addPosition(if (item is Ingredient) item else null)
        }
        binding.buttonCreateSupply.setOnClickListener {
            val account = binding.spinnerCreateSupplyAccount.selectedItem
            val vendor = binding.spinnerCreateSupplyVendor.selectedItem

            viewModel.createSupply(if (account is BuyAccount) account else null,
                if (vendor is BuyVendor) vendor else null, binding.textCreateSupplyComment.text.toString())
        }

        viewModel.vendors.observe(viewLifecycleOwner, Observer { items ->
            if (items != null && items.isNotEmpty()) {
                val adapter = VendorsAdapter(context ?: return@Observer, items)
                binding.spinnerCreateSupplyVendor.adapter = adapter
            }
        })
        viewModel.accounts.observe(viewLifecycleOwner, Observer { items ->
            if (items != null && items.isNotEmpty()) {
                val adapter = AccountsAdapter(context ?: return@Observer, items)
                binding.spinnerCreateSupplyAccount.adapter = adapter
            }
        })
        viewModel.buyIngredients.observe(viewLifecycleOwner, Observer { items ->
            if (items != null && items.isNotEmpty()) {
                val adapter = IngredientAdapter(context ?: return@Observer, items)
                binding.spinnerCreateSupplyName.adapter = adapter
            }
        })

        adapterSupplyItems = AdapterSupplyItems(viewModel.listenerBuyRecord)
        binding.recyclerViewCreateSupply.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewCreateSupply.adapter = adapterSupplyItems
        viewModel.records.observe(viewLifecycleOwner, Observer { items ->
            if (items != null) adapterSupplyItems.submitList(items)
        })
        val itemTouchHelper = ItemTouchHelper(adapterSupplyItems.swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewCreateSupply)
    }

    override fun onNavigationEvent(type: EventType, args: Bundle?) {
        when (type) {
            EventType.NAVIGATION_CREATE_SUPPLY_TO_CASH -> {
                navigationController.navigate(R.id.action_navigation_create_supply_to_navigation_cash)
            }
            else -> super.onNavigationEvent(type, args)
        }
    }

    class AccountsAdapter(context: Context, accounts: List<BuyAccount>) : ArrayAdapter<BuyAccount>(context, android.R.layout.simple_list_item_1, accounts) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup) : View {
            return createSpinnerView(context, convertView, getItem(position)?.name ?: "")
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup) : View {
            return createSpinnerView(context, convertView, getItem(position)?.name ?: "")
        }
    }

    class VendorsAdapter(context: Context, vendors: List<BuyVendor>) : ArrayAdapter<BuyVendor>(context, android.R.layout.simple_list_item_1, vendors) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup) : View {
            return createSpinnerView(context, convertView, getItem(position)?.name ?: "")
        }
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup) : View {
            return createSpinnerView(context, convertView, getItem(position)?.name ?: "")
        }
    }

    class IngredientAdapter(context: Context, ingredients: List<Ingredient>) : ArrayAdapter<Ingredient>(context, android.R.layout.simple_list_item_1, ingredients) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup) : View {
            return createSpinnerView(context, convertView, getItem(position)?.name ?: "")
        }
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup) : View {
            return createSpinnerView(context, convertView, getItem(position)?.name ?: "")
        }
    }

    companion object {

        private fun createSpinnerView(context: Context, convertView: View?, text: String) : View {
            var view = convertView

            if (view == null) {
                view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null)
            }

            val textView = view?.findViewById<TextView>(android.R.id.text1)
            textView?.ellipsize = TextUtils.TruncateAt.END
            textView?.setLines(1)
            textView?.text = text

            return view!!
        }
    }
}