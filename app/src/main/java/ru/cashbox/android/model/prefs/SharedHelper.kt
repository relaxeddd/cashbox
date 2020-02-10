package ru.cashbox.android.model.prefs

import android.content.Context
import android.content.SharedPreferences
import ru.cashbox.android.common.*

class SharedHelper(val context: Context) {

    private val prefs: SharedPreferences
        get() = context.getSharedPreferences(PACKAGE, Context.MODE_PRIVATE)

    fun getDomain() = prefs.getString(DOMAIN, "") ?: ""
    fun setDomain(value : String) = prefs.edit().putString(DOMAIN, value).apply()
}