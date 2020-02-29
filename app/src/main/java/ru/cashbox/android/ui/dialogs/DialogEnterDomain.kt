package ru.cashbox.android.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import mehdi.sakout.fancybuttons.FancyButton
import ru.cashbox.android.R
import ru.cashbox.android.common.ListenerResult

class DialogEnterDomain : DialogFragment() {

    private var editText: TextInputEditText? = null
    private var buttonApply: FancyButton? = null
    var confirmListener: ListenerResult<String>? = null
    var initText: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            builder.setView(R.layout.dialog_enter_domain)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onStart() {
        super.onStart()
        editText = dialog?.findViewById(R.id.edit_text_enter_domain)
        buttonApply = dialog?.findViewById(R.id.button_enter_domain_apply)

        editText?.setText(initText ?: "")
        editText?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { editText?.error = null }
        })
        buttonApply?.setOnClickListener {
            val domain = editText?.text.toString()

            if (domain.isNotEmpty()) {
                confirmListener?.onResult(domain)
                dismiss()
            } else {
                editText?.error = getString(R.string.error_text)
            }
        }
    }
}