package ru.cashbox.android.common

import android.content.Context
import android.util.DisplayMetrics

fun convertDpToPixel(context: Context, dp: Float): Float {
    return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}