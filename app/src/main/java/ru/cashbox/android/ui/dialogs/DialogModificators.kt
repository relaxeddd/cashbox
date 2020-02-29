package ru.cashbox.android.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import mehdi.sakout.fancybuttons.FancyButton
import ru.cashbox.android.R
import ru.cashbox.android.common.ListenerResult
import ru.cashbox.android.model.TechMapModificator
import ru.cashbox.android.ui.cash.AdapterModificators





class DialogModificators : DialogFragment() {

    private lateinit var adapterModificators: AdapterModificators
    private var buttonApply: FancyButton? = null
    var confirmListener: ListenerResult<List<TechMapModificator>>? = null
    var titleText: String? = null
    var apiAddress: String? = null
    var priceText: String? = null
    var modificators: List<TechMapModificator> = ArrayList()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(context!!)
        dialog.setContentView(R.layout.dialog_modificators)

        return dialog
        /*return activity?.let {
            val builder = AlertDialog.Builder(it)
            val dialog = Dialog(context!!)
            dialog.setContentView(R.layout.dialog_modificators)

            builder.setView(R.layout.dialog_modificators)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")*/
    }

    override fun onStart() {
        super.onStart()

        val width = (resources.displayMetrics.widthPixels * 0.80).toInt()
        val window = dialog?.window
        window?.setLayout(width, ViewGroup.LayoutParams.MATCH_PARENT)

        buttonApply = dialog?.findViewById(R.id.button_modificators_add_to_check)
        val close = dialog?.findViewById<ImageView>(R.id.image_modificators_close)
        val title = dialog?.findViewById<TextView>(R.id.text_modificators_title)
        val price = dialog?.findViewById<TextView>(R.id.text_modificators_total)
        val recyclerView = dialog?.findViewById<RecyclerView>(R.id.recycler_view_modificators)
        val priceTxt = getString(R.string.total) + ": " + priceText.toString()

        title?.text = titleText
        price?.text = priceTxt

        adapterModificators = AdapterModificators(apiAddress)
        recyclerView?.layoutManager = GridLayoutManager(context, 3)
        recyclerView?.adapter = adapterModificators
        adapterModificators.submitList(modificators)
        val itemTouchHelper = ItemTouchHelper(adapterModificators.swipeCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        buttonApply?.setOnClickListener {
            val selected = adapterModificators.selectedModificators

            if (selected.isNotEmpty()) {
                confirmListener?.onResult(adapterModificators.selectedModificators)
                dismiss()
            } else {
                Toast.makeText(context, R.string.error_internal, Toast.LENGTH_SHORT).show()
            }
        }
        close?.setOnClickListener { dismiss() }
    }
}