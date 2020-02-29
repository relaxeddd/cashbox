package ru.cashbox.android.printer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import net.igenius.customcheckbox.CustomCheckBox
import ru.cashbox.android.R
import ru.cashbox.android.model.Printer

class PrinterSettingsHistoryAdapter(context: Context, val printers: ArrayList<Printer>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return printers.size
    }

    override fun getItem(i: Int): Printer {
        return printers[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, convertView: View, viewGroup: ViewGroup): View {
        var view: View? = convertView
        if (view == null) {
            view = inflater.inflate(R.layout.printer_settings_history_item, viewGroup, false)
        }

        val item = getItem(i)
        val title = view!!.findViewById<TextView>(R.id.printer_settings_item_title)
        title.text = item.name
        val ip = view.findViewById<TextView>(R.id.printer_settings_item_ip)
        ip.text = item.ip
        val checkBox = view.findViewById<CustomCheckBox>(R.id.printer_settings_item_checkbox)
        checkBox.isChecked = item.selected

        return view
    }
}