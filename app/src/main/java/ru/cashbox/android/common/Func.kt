@file:Suppress("unused")
package ru.cashbox.android.common

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.MenuRes
import ru.cashbox.android.model.BillModificator
import ru.cashbox.android.model.ResponseTechMap
import ru.cashbox.android.model.TechMap
import java.text.SimpleDateFormat
import java.util.*
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.graphics.*
import android.text.TextPaint
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import ru.cashbox.android.R
import kotlin.math.roundToInt


fun convertDpToPixel(context: Context, dp: Float): Float {
    return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun showPopupMenu(context: Context?, view: View, @MenuRes menuResId: Int, listener: PopupMenu.OnMenuItemClickListener) {
    val popupMenu = PopupMenu(context, view)
    popupMenu.inflate(menuResId)
    popupMenu.setOnMenuItemClickListener(listener)
    popupMenu.show()
}

fun getCurrentDate() : String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    return dateFormat.format(Date())
}

fun getNameTechMap(innerTechmap: TechMap, modificators: List<BillModificator>?) : String {
    var name = innerTechmap.name

    if (modificators?.isNotEmpty() == true) {
        name += " ("
        modificators.forEach { name += it.getFinalName() + " (" + it.count.roundToInt() + ")" + ", " }
        name = name.removeRange(name.length - 2, name.length)
        if (modificators.isNotEmpty()) name += ")"
    }

    return name
}

fun getNameTechMap(techmap: ResponseTechMap, modificators: List<BillModificator>?) : String {
    var name = techmap.name

    if (modificators?.isNotEmpty() == true) {
        val filteredModificators = modificators.filter { it.getFinalName() != null }
        if (filteredModificators.isNotEmpty()) {
            name += " ("
            filteredModificators.forEach { name += it.getFinalName() + " (" + it.count.roundToInt() + ")" + ", " }
            name = name.removeRange(name.length - 2, name.length)
            name += ")"
        }
    }

    return name
}

fun showKeyboard(context: Context, view: View) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm!!.showSoftInput(view, SHOW_IMPLICIT)
}

fun formatNumber(value: Double) : String {
    return String.format("%,d", value)
}

fun getLetterImage(context: Context, objectName: String): Bitmap? {
    if (objectName.isEmpty()) {
        return null
    }

    val bitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = TextPaint()
    val canvasBorder = canvas.clipBounds
    val textBorder = Rect()
    val backgroundColor: Int = ContextCompat.getColor(context, R.color.colorPrimary)
    val foregroundColor: Int = ContextCompat.getColor(context, R.color.black)

    canvas.drawColor(backgroundColor)

    paint.typeface = Typeface.create("Roboto", Typeface.NORMAL)
    paint.color = foregroundColor
    paint.textSize = 300f
    paint.textAlign = Paint.Align.LEFT
    paint.getTextBounds(objectName.toUpperCase(), 0, 1, textBorder)

    val x = canvasBorder.width() / 2f - textBorder.width() / 2f - textBorder.left.toFloat()
    val y = canvasBorder.height() / 2f + textBorder.height() / 2f - textBorder.bottom
    canvas.drawText(objectName.toUpperCase(), 0, 1, x, y, paint)

    return bitmap
}