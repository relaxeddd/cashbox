package ru.cashbox.android.printer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rengwuxian.materialedittext.MaterialEditText
import kotlinx.android.synthetic.main.activity_printer_settings.*
import mehdi.sakout.fancybuttons.FancyButton
import net.igenius.customcheckbox.CustomCheckBox
import ru.cashbox.android.App
import ru.cashbox.android.R
import ru.cashbox.android.model.Printer

class PrinterSettingsHistoryFragment : Fragment() {

    private var printerHelper: PrinterHelper = PrinterHelper
    private var adapter: PrinterSettingsHistoryAdapter? = null
    private var printerList: ListView? = null
    private var name: MaterialEditText? = null
    private var ip: MaterialEditText? = null
    private var port: MaterialEditText? = null
    private var btnTestPrint: FancyButton? = null
    private var btnDelete: FancyButton? = null
    private var checkBoxLayout: ConstraintLayout? = null
    private var checkBoxText: TextView? = null
    private var checkBox: CustomCheckBox? = null
    private var currentPrinter: Printer? = null
    private var fab: FloatingActionButton? = null
    private var backLayout: ConstraintLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_printer_settings_history, container, false)

        PrinterSettingsActivity.CURRENT_SCREEN = R.layout.fragment_printer_settings_history
        printerList = view.findViewById(R.id.printer_settings_history_list)
        name = view.findViewById(R.id.printer_settings_history_name_field)
        ip = view.findViewById(R.id.printer_settings_history_ip_field)
        port = view.findViewById(R.id.printer_settings_history_port_field)
        btnTestPrint = view.findViewById(R.id.btn_printer_history_test_print)
        btnDelete = view.findViewById(R.id.btn_printer_history_delete)
        checkBoxLayout = view.findViewById(R.id.printer_settings_history_checkbox_layout)
        checkBoxText = view.findViewById(R.id.printer_settings_history_checkbox_text)
        checkBox = view.findViewById(R.id.printer_settings_history_checkbox)
        fab = view.findViewById(R.id.btn_printer_settings_history_add)
        backLayout = view.findViewById(R.id.action_bar_printer_history)
        clearData()

        adapter = PrinterSettingsHistoryAdapter(view.context, printerHelper.printers ?: ArrayList())
        printerList!!.adapter = adapter

        printerList!!.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val printer = adapterView.getItemAtPosition(i) as Printer
            currentPrinter = printer
            setData(printer)
        }

        checkBoxLayout!!.setOnClickListener {
            if (currentPrinter != null) {
                if (currentPrinter!!.selected) {
                    checkBox!!.isChecked = false
                    printerHelper.unSelectPrinter(currentPrinter!!.ip, adapter!!)
                } else {
                    checkBox!!.isChecked = true
                    printerHelper.selectPrinter(currentPrinter!!.ip, adapter!!)
                }
                adapter!!.notifyDataSetChanged()
            }
        }

        checkBox!!.setOnClickListener(View.OnClickListener {
            // NOTHING
        })

        btnTestPrint!!.setOnClickListener {
            if (currentPrinter != null) {
                printerHelper.testPrint(App.context, currentPrinter!!.ip, currentPrinter!!.port)
            }
        }

        btnDelete!!.setOnClickListener {
            if (currentPrinter != null) {
                printerHelper.deletePrinter(currentPrinter!!.ip, adapter!!)
                adapter!!.notifyDataSetChanged()
                clearData()
            }
        }

        fab!!.setOnClickListener {
            (activity as PrinterSettingsActivity).goToSettingsAddFragment()
        }

        backLayout!!.setOnClickListener { activity?.finish() }

        return view
    }

    private fun setData(printer: Printer) {
        name!!.setText(printer.name)
        ip!!.setText(printer.ip)
        port!!.setText(printer.port)
        btnTestPrint!!.isEnabled = true
        btnDelete!!.isEnabled = true
        checkBoxLayout!!.isEnabled = true
        checkBoxText!!.isEnabled = true
        checkBoxText!!.setTextColor(ContextCompat.getColor(context!!, android.R.color.black))
        checkBox!!.isEnabled = true
        checkBox!!.isChecked = printer.selected
    }

    private fun clearData() {
        name!!.setText("")
        ip!!.setText("")
        port!!.setText("")
        btnTestPrint!!.isEnabled = false
        btnDelete!!.isEnabled = false
        checkBoxLayout!!.isEnabled = false
        checkBoxText!!.isEnabled = false
        checkBoxText!!.setTextColor(ContextCompat.getColor(context!!, android.R.color.secondary_text_dark))
        checkBox!!.isEnabled = false
        checkBox!!.isChecked = false
    }

}