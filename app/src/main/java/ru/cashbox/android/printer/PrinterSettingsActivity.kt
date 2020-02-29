package ru.cashbox.android.printer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.cashbox.android.R

class PrinterSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printer_settings)
        CURRENT_SCREEN = null

        goToSettingsHistoryFragment()
    }

    override fun onBackPressed() {
        if (CURRENT_SCREEN != null) {
            when (CURRENT_SCREEN) {
                R.layout.fragment_printer_settings_add -> {
                    goToSettingsHistoryFragment()
                }
                R.layout.fragment_printer_settings_history -> super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
        PrinterHelper.stopScan()
    }

    override fun onDestroy() {
        super.onDestroy()
        PrinterHelper.stopScan()
    }

    fun goToSettingsHistoryFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        val printerSettingsHistoryFragment = PrinterSettingsHistoryFragment()
        transaction.replace(R.id.printer_settings_container, printerSettingsHistoryFragment)
        transaction.commit()
    }

    fun goToSettingsAddFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        val printerSettingsAddFragment = PrinterSettingsAddFragment()
        transaction.replace(R.id.printer_settings_container, printerSettingsAddFragment)
        transaction.commit()
    }

    companion object {
        var CURRENT_SCREEN: Int? = null
    }
}