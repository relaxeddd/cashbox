package ru.cashbox.android.printer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.rengwuxian.materialedittext.MaterialEditText
import mehdi.sakout.fancybuttons.FancyButton
import ru.cashbox.android.App
import ru.cashbox.android.R
import ru.cashbox.android.model.Printer

class PrinterSettingsAddFragment : Fragment(), PrinterHelper.ScanListener {

    private var printerHelper: PrinterHelper = PrinterHelper
    private var progressBar: ProgressBar? = null
    private var viewContext: Context? = null
    private var title: TextView? = null
    private var btnScan: FancyButton? = null
    private var ip: MaterialEditText? = null
    private var port: MaterialEditText? = null
    private var btnTestPrint: FancyButton? = null
    private var btnSave: FancyButton? = null
    private var printer: Printer? = null
    private var btnBack: ConstraintLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_printer_settings_add, container, false)

        PrinterSettingsActivity.CURRENT_SCREEN = R.layout.fragment_printer_settings_add
        viewContext = view.context
        btnSave = view.findViewById(R.id.btn_printer_save)
        progressBar = view.findViewById(R.id.progress_bar_printer_add)
        title = view.findViewById(R.id.printer_settings_printer_name)
        btnScan = view.findViewById(R.id.btn_printer_scan)
        ip = view.findViewById(R.id.printer_settings_ip)
        port = view.findViewById(R.id.printer_settings_port)
        btnTestPrint = view.findViewById(R.id.btn_printer_test_print)
        btnSave = view.findViewById(R.id.btn_printer_save)
        btnBack = view.findViewById(R.id.action_bar_printer_add)

        if (printerHelper.isScanExecuted) {
            setElementsState(true)
        } else {
            setElementsState(false)
        }

        btnScan!!.setOnClickListener {
            val ipView = ip?.text
            val portView = port?.text

            if (ipView != null && portView != null) {
                printer = null
                setElementsState(true)
                title?.text = ""
                if (ipView.toString().isNotEmpty() && portView.toString().isNotEmpty()) {
                    printerHelper.startScan(context!!, this@PrinterSettingsAddFragment, ipView.toString(), portView.toString())
                }
                if (ipView.toString().isEmpty() || portView.toString().isEmpty()) {
                    printerHelper.startScan(context!!, this@PrinterSettingsAddFragment)
                }
            }
        }

        btnSave!!.setOnClickListener {
            if (printer != null) {
                if (printerHelper.getSavedPrinterByIp(printer!!.ip) == null) {
                    val printers = printerHelper.printers
                    printers.add(printer!!)
                    printerHelper.savePrinters(printers)
                    printerHelper.selectPrinter(printer!!.ip)

                    (activity as PrinterSettingsActivity).goToSettingsHistoryFragment()

                    Toast.makeText(viewContext, "Принтер сохранен и установлен по умолчанию", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(viewContext, "Данный принтер уже существует", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(viewContext, "Сначала выполните поиск принтера", Toast.LENGTH_SHORT).show()
            }
        }

        btnTestPrint!!.setOnClickListener {
            if (printer != null) {
                printerHelper.testPrint(App.context, printer!!.ip, printer!!.port)
            } else {
                Toast.makeText(viewContext, "Сначала выполните поиск принтера", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack!!.setOnClickListener {
            (activity as PrinterSettingsActivity).goToSettingsHistoryFragment()
        }

        return view
    }

    private fun setElementsState(loading: Boolean) {
        progressBar!!.visibility = if (loading) View.VISIBLE else View.INVISIBLE
        ip!!.isEnabled = !loading
        port!!.isEnabled = !loading
        btnTestPrint!!.isEnabled = !loading
        btnSave!!.isEnabled = !loading
    }

    override fun onScanned(printer: Printer?) {
        setElementsState(false)
        if (printer != null) {
            this.printer = printer
            title!!.text = printer.name
        } else {
            title!!.text = "Принтер не найден"
        }
    }
}