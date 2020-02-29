package ru.cashbox.android.ui.dialogs

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import ru.cashbox.android.R
import ru.cashbox.android.common.ListenerResult


class DialogConfirmCloseCashbox : DialogFragment() {

    var confirmListener: ListenerResult<Boolean>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            builder.setMessage(R.string.question_close_cashbox)
                .setPositiveButton(R.string.yes) { _, _ -> confirmListener?.onResult(true) }
                .setNegativeButton(R.string.no) { _, _ -> confirmListener?.onResult(false) }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onStart() {
        super.onStart()
        (dialog as AlertDialog?)?.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(context ?: return, R.color.colorPrimary2))
        (dialog as AlertDialog?)?.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(context ?: return, R.color.colorPrimary2))
    }
}