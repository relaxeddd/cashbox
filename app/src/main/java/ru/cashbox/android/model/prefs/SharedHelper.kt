package ru.cashbox.android.model.prefs

import android.content.Context
import android.content.SharedPreferences
import ru.cashbox.android.common.*

class SharedHelper(val context: Context) {

    private val prefs: SharedPreferences
        get() = context.getSharedPreferences(PACKAGE, Context.MODE_PRIVATE)

    fun getDomain() = prefs.getString(DOMAIN, "") ?: ""
    fun setDomain(value : String) = prefs.edit().putString(DOMAIN, value).apply()

    fun getTokenTerminal() = prefs.getString(TOKEN_TERMINAL, "") ?: ""
    fun setTokenTerminal(value : String) = prefs.edit().putString(TOKEN_TERMINAL, value).apply()

    fun getTokenEmployee() = prefs.getString(TOKEN_EMPLOYEE, "") ?: ""
    fun setTokenEmployee(value : String) = prefs.edit().putString(TOKEN_EMPLOYEE, value).apply()
}