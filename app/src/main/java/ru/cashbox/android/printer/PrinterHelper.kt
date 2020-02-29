package ru.cashbox.android.printer

import android.content.Context
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.text.format.Formatter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.atol.drivers10.fptr.Fptr
import ru.atol.drivers10.fptr.IFptr
import ru.cashbox.android.App
import ru.cashbox.android.common.APP_NAME
import ru.cashbox.android.common.DEFAULT_PRINTER_PORT
import ru.cashbox.android.model.*
import ru.cashbox.android.model.repositories.RepositoryUsers
import java.io.IOException
import java.net.InetAddress
import java.util.*

object PrinterHelper {

    private val PRINTER_SETTINGS_TAG = "Printer_Settings"
    private var printerBusy = false

    var isScanExecuted = false
        private set
    private var scanTask: ScanTask? = null
    private val queue = ArrayDeque<PrinterQueueWrapper>()

    var repositoryUsers: RepositoryUsers? = null

    val printers: ArrayList<Printer>
        get() {
            val sharedPreferences = App.context.getSharedPreferences(APP_NAME + " Settings", Context.MODE_PRIVATE)
            val printerData = sharedPreferences.getString(PRINTER_SETTINGS_TAG + repositoryUsers?.sessionTerminal?.value?.user?.id, null)
            val gson = Gson()
            return if (printerData != null) {
                gson.fromJson<ArrayList<Printer>>(printerData, object : TypeToken<ArrayList<Printer>>() {
                }.type)
            } else ArrayList()
        }

    val selectedPrinter: Printer?
        get() {
            val printers = printers
            for (printer in printers) {
                if (printer.selected) {
                    return printer
                }
            }
            return null
        }

    interface ScanListener {
        fun onScanned(printer: Printer?)
    }

    private class ScanTask : AsyncTask<Void?, Void?, Printer?> {

        private var context: Context? = null
        private var fptr: Fptr? = null
        private var scanListener: ScanListener? = null
        private var ip: String? = null
        private var port: String? = null

        constructor(context: Context, scanListener: ScanListener) {
            this.context = context
            this.scanListener = scanListener
        }

        constructor(context: Context, scanListener: ScanListener, ip: String, port: String) {
            this.context = context
            this.scanListener = scanListener
            this.ip = ip
            this.port = port
        }

        override fun onPreExecute() {
            isScanExecuted = true
            printerBusy = true
        }

        override fun doInBackground(vararg voids: Void?): Printer? {
            if (ip != null && port != null) {
                val fptr = Fptr(context)
                this.fptr = fptr

                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_MODEL, IFptr.LIBFPTR_MODEL_ATOL_AUTO.toString())
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_PORT, IFptr.LIBFPTR_PORT_TCPIP.toString())
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPADDRESS, ip)
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPPORT, port)
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_BAUDRATE, IFptr.LIBFPTR_PORT_BR_115200.toString())
                fptr.applySingleSettings()

                fptr.open()

                if (fptr.isOpened) {
                    fptr.setParam(IFptr.LIBFPTR_PARAM_DATA_TYPE, IFptr.LIBFPTR_DT_STATUS)
                    fptr.queryData()
                    val modelName = fptr.getParamString(IFptr.LIBFPTR_PARAM_MODEL_NAME)
                    val printer = Printer(modelName, PrinterConnectionType.WIFI, ip!!, DEFAULT_PRINTER_PORT, false)
                    fptr.close()
                    fptr.destroy()

                    return printer
                }
                return null
            }

            val printers = ArrayList<Printer>()
            val wifiManager = context!!.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (wifiManager != null) {
                val ipSplit = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
                    .split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val baseIpBuilder = StringBuilder()
                for (i in 0..2) {
                    baseIpBuilder.append(ipSplit[i]).append(".")
                }
                val baseIp = baseIpBuilder.toString()

                val ips = ArrayList<String>()
                for (i in 1..255) {
                    if (!isScanExecuted) {
                        return null
                    }
                    try {
                        val tempAddress = InetAddress.getByName(baseIp + i)
                        if (tempAddress.isReachable(150)) {
                            ips.add(baseIp + i)
                        }
                    } catch (ignore: IOException) { }
                }

                for (ip in ips) {
                    if (!isScanExecuted) {
                        return null
                    }
                    val fptr = Fptr(context)
                    this.fptr = fptr

                    fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_MODEL, IFptr.LIBFPTR_MODEL_ATOL_AUTO.toString())
                    fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_PORT, IFptr.LIBFPTR_PORT_TCPIP.toString())
                    fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPADDRESS, ip)
                    fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPPORT, DEFAULT_PRINTER_PORT)
                    fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_BAUDRATE, IFptr.LIBFPTR_PORT_BR_115200.toString())
                    fptr.applySingleSettings()

                    fptr.open()

                    if (fptr.isOpened) {
                        fptr.setParam(IFptr.LIBFPTR_PARAM_DATA_TYPE, IFptr.LIBFPTR_DT_STATUS)
                        fptr.queryData()
                        val modelName = fptr.getParamString(IFptr.LIBFPTR_PARAM_MODEL_NAME)
                        val printer = Printer(modelName, PrinterConnectionType.WIFI, ip, DEFAULT_PRINTER_PORT, false)
                        printers.add(printer)
                        fptr.close()
                        fptr.destroy()
                    }
                }

                for (printer in printers) {
                    if (getSavedPrinterByIp(printer.ip) == null) {
                        return printer
                    }
                }
            }

            return null
        }

        override fun onPostExecute(printer: Printer?) {
            isScanExecuted = false
            printerBusy = false
            scanListener!!.onScanned(printer)
        }
    }

    private class PrintTask(private val context: Context, private val check: Bill) : AsyncTask<Void?, Void?, Void?>() {

        override fun onPreExecute() {
            printerBusy = true
        }

        override fun doInBackground(vararg voids: Void?): Void? {
            val selectedPrinter = selectedPrinter

            if (selectedPrinter != null) {
                val fptr = Fptr(context)
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_MODEL, IFptr.LIBFPTR_MODEL_ATOL_AUTO.toString())
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_PORT, IFptr.LIBFPTR_PORT_TCPIP.toString())
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPADDRESS, selectedPrinter.ip)
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPPORT, selectedPrinter.port)
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_BAUDRATE, IFptr.LIBFPTR_PORT_BR_115200.toString())
                fptr.applySingleSettings()

                fptr.open()

                val user = repositoryUsers?.sessionEmployee?.value?.user
                fptr.setParam(1021, user?.fullname)
                fptr.operatorLogin()

                fptr.setParam(IFptr.LIBFPTR_PARAM_RECEIPT_TYPE, IFptr.LIBFPTR_RT_SELL)
                fptr.openReceipt()

                val checkItems = check.items

                for (checkItem in checkItems) {
                    fptr.setParam(IFptr.LIBFPTR_PARAM_COMMODITY_NAME, checkItem.billName)
                    fptr.setParam(IFptr.LIBFPTR_PARAM_PRICE, checkItem.price)
                    fptr.setParam(IFptr.LIBFPTR_PARAM_QUANTITY, checkItem.count)
                    fptr.setParam(IFptr.LIBFPTR_PARAM_TAX_TYPE, IFptr.LIBFPTR_TAX_VAT0)
                    fptr.setParam(IFptr.LIBFPTR_PARAM_PAYMENT_TYPE, if (check.payedType == PayType.WIRE) IFptr.LIBFPTR_PT_ELECTRONICALLY else IFptr.LIBFPTR_PT_CASH)
                    fptr.setParam(IFptr.LIBFPTR_PARAM_POSITION_SUM, checkItem.getFinalPrice())
                    fptr.registration()
                }

                fptr.payment()
                fptr.closeReceipt()
                fptr.continuePrint()
                fptr.close()
                fptr.destroy()
            }

            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            nextAction()
        }
    }

    private class CloseShiftTask(private val context: Context) : AsyncTask<Void?, Void?, Void?>() {

        override fun onPreExecute() {
            printerBusy = true
        }

        override fun doInBackground(vararg voids: Void?): Void? {
            val selectedPrinter = selectedPrinter

            if (selectedPrinter != null) {
                val fptr = Fptr(context)
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_MODEL, IFptr.LIBFPTR_MODEL_ATOL_AUTO.toString())
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_PORT, IFptr.LIBFPTR_PORT_TCPIP.toString())
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPADDRESS, selectedPrinter.ip)
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPPORT, selectedPrinter.port)
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_BAUDRATE, IFptr.LIBFPTR_PORT_BR_115200.toString())
                fptr.applySingleSettings()

                fptr.open()

                val user = repositoryUsers?.sessionEmployee?.value?.user
                fptr.setParam(1021, user?.fullname)
                fptr.operatorLogin()

                fptr.setParam(IFptr.LIBFPTR_PARAM_REPORT_TYPE, IFptr.LIBFPTR_RT_CLOSE_SHIFT)
                fptr.report()

                fptr.close()
                fptr.destroy()

            }

            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            nextAction()
        }
    }

    private class TestPrintTask(private val context: Context, private val ip: String, private val port: String) :
        AsyncTask<Void, Void, Void>() {

        override fun onPreExecute() {
            printerBusy = true
        }

        override fun doInBackground(vararg voids: Void?): Void? {
            val fptr = Fptr(context)
            fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_MODEL, IFptr.LIBFPTR_MODEL_ATOL_AUTO.toString())
            fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_PORT, IFptr.LIBFPTR_PORT_TCPIP.toString())
            fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPADDRESS, ip)
            fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPPORT, port)
            fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_BAUDRATE, IFptr.LIBFPTR_PORT_BR_115200.toString())
            fptr.applySingleSettings()

            fptr.open()

            if (fptr.isOpened) {
                fptr.setParam(IFptr.LIBFPTR_PARAM_TEXT, "Успешное подключение\n\n\n\n\n")
                fptr.setParam(IFptr.LIBFPTR_PARAM_ALIGNMENT, IFptr.LIBFPTR_ALIGNMENT_CENTER)
                fptr.setParam(IFptr.LIBFPTR_PARAM_FONT, 2)
                fptr.setParam(IFptr.LIBFPTR_PARAM_FONT_DOUBLE_WIDTH, true)
                fptr.setParam(IFptr.LIBFPTR_PARAM_FONT_DOUBLE_HEIGHT, true)
                fptr.printText()
                fptr.cut()
                fptr.close()
                fptr.destroy()
            }

            return null
        }

        override fun onPostExecute(aVoid: Void) {
            nextAction()
        }
    }

    private fun nextAction() {
        val poll = queue.poll()
        if (poll != null) {
            val params = poll.params
            when (poll.action) {
                PrinterAction.PRINT -> {
                    val printerContext = params[0] as Context
                    val check = params[1] as Bill
                    PrintTask(printerContext, check).execute()
                }
                PrinterAction.CLOSE_SHIFT -> {
                    val closeShiftContext = params[0] as Context
                    CloseShiftTask(closeShiftContext).execute()
                }
                PrinterAction.TEST_PRINT -> {
                    val testPrintContext = params[0] as Context
                    val ip = params[1] as String
                    val port = params[2] as String
                    TestPrintTask(testPrintContext, ip, port).execute()
                }
            }
        } else {
            printerBusy = false
        }
    }

    fun startScan(context: Context, scanListener: ScanListener) {
        scanTask = ScanTask(context, scanListener)
        scanTask!!.execute()
    }

    fun startScan(context: Context, scanListener: ScanListener, ip: String, port: String) {
        scanTask = ScanTask(context, scanListener, ip, port)
        scanTask!!.execute()
    }

    fun printCheck(context: Context, check: Bill) {
        if (printerBusy) {
            val params = ArrayList<Any>()
            params.add(context)
            params.add(check)
            queue.offer(PrinterQueueWrapper(PrinterAction.PRINT, params))
        } else {
            PrintTask(context, check).execute()
        }
    }

    fun closeShift(context: Context) {
        if (printerBusy) {
            val params = ArrayList<Any>()
            params.add(context)
            queue.offer(PrinterQueueWrapper(PrinterAction.CLOSE_SHIFT, params))
        } else {
            CloseShiftTask(context).execute()
        }
    }

    fun closeShiftAsync(context: Context) {
        Thread(Runnable {
            val selectedPrinter = selectedPrinter
            if (selectedPrinter != null) {
                val fptr = Fptr(context)

                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_MODEL, IFptr.LIBFPTR_MODEL_ATOL_AUTO.toString())
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_PORT, IFptr.LIBFPTR_PORT_TCPIP.toString())
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPADDRESS, selectedPrinter.ip)
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_IPPORT, selectedPrinter.port)
                fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_BAUDRATE, IFptr.LIBFPTR_PORT_BR_115200.toString())
                fptr.applySingleSettings()

                fptr.open()

                fptr.setParam(IFptr.LIBFPTR_PARAM_REPORT_TYPE, IFptr.LIBFPTR_RT_CLOSE_SHIFT)
                fptr.report()

                fptr.close()
                fptr.destroy()
            }
        }).start()
    }

    fun testPrint(context: Context, ip: String, port: String) {
        if (printerBusy) {
            val params = ArrayList<Any>()
            params.add(context)
            params.add(ip)
            params.add(port)
            queue.offer(PrinterQueueWrapper(PrinterAction.TEST_PRINT, params))
        } else {
            TestPrintTask(context, ip, port).execute()
        }
    }

    fun stopScan() {
        isScanExecuted = false
    }

    fun savePrinters(printerList: List<Printer>) {
        val sharedPreferences = App.context.getSharedPreferences(APP_NAME + " Settings", Context.MODE_PRIVATE)
        val gson = Gson()
        val editor = sharedPreferences.edit()
        val printerData = gson.toJson(printerList)

        editor.putString(PRINTER_SETTINGS_TAG + repositoryUsers?.sessionTerminal?.value?.user?.id, printerData)
        editor.apply()
    }

    fun getSavedPrinterByIp(ip: String): Printer? {
        val printers = printers
        for (printer in printers) {
            if (printer.ip == ip) {
                return printer
            }
        }
        return null
    }

    fun deletePrinter(ip: String) {
        val printers = printers
        for (printer in printers) {
            if (printer.ip == ip) {
                printers.remove(printer)
                break
            }
        }
        savePrinters(printers)
    }

    fun deletePrinter(ip: String, adapter: PrinterSettingsHistoryAdapter) {
        val printers = printers
        for (printer in printers) {
            if (printer.ip == ip) {
                printers.remove(printer)
                break
            }
        }
        val adapterPrinters = adapter.printers
        for (printer in adapterPrinters) {
            if (printer.ip == ip) {
                adapterPrinters.remove(printer)
                break
            }
        }
        savePrinters(printers)
    }

    fun selectPrinter(ip: String) {
        val printers = printers
        for (printer in printers) {
            printer.selected = false
            if (printer.ip == ip) {
                printer.selected = true
            }
        }
        savePrinters(printers)
    }

    fun selectPrinter(ip: String, adapter: PrinterSettingsHistoryAdapter) {
        val printers = printers
        for (printer in printers) {
            printer.selected = false
            if (printer.ip == ip) {
                printer.selected = true
            }
        }
        val adapterPrinters = adapter.printers
        for (printer in adapterPrinters) {
            printer.selected = false
            if (printer.ip == ip) {
                printer.selected = true
            }
        }
        savePrinters(printers)
    }

    fun unSelectPrinter(ip: String, adapter: PrinterSettingsHistoryAdapter) {
        val printers = printers
        for (printer in printers) {
            if (printer.ip == ip) {
                printer.selected = false
            }
        }
        val adapterPrinters = adapter.printers
        for (printer in adapterPrinters) {
            if (printer.ip == ip) {
                printer.selected = false
            }
        }
        savePrinters(printers)
    }
}