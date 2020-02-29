package ru.cashbox.android.ui.login_terminal

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_login_terminal.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.cashbox.android.R
import ru.cashbox.android.common.ListenerResult
import ru.cashbox.android.common.TEXT
import ru.cashbox.android.ui.base.FragmentBase
import ru.cashbox.android.databinding.FragmentLoginTerminalBinding
import ru.cashbox.android.model.EventType
import ru.cashbox.android.ui.dialogs.DialogEnterDomain

class FragmentLoginTerminal : FragmentBase<ViewModelLoginTerminal, FragmentLoginTerminalBinding>() {

    private val clickListenerLogin = View.OnClickListener {
        //val domain = text_login_terminal_domain.text.toString()
        val login = text_login_terminal_login.text.toString()
        val password = text_login_terminal_password.text.toString()

        viewModel.login(login, password)
    }

    override fun getLayoutResId() = R.layout.fragment_login_terminal
    override val viewModel: ViewModelLoginTerminal by viewModel()

    override fun configureBinding() {
        super.configureBinding()
        binding.viewModel = viewModel
        binding.clickListenerLogin = clickListenerLogin
    }

    override fun onNavigationEvent(type: EventType, args: Bundle?) {
        when (type) {
            EventType.DIALOG_ENTER_DOMAIN -> {
                val dialog = DialogEnterDomain()
                dialog.initText = args?.getString(TEXT)
                dialog.confirmListener = object: ListenerResult<String> {
                    override fun onResult(result: String) {
                        viewModel.onEnterDomain(result)
                    }
                }
                dialog.show(childFragmentManager, "Enter Domain Dialog")
            }
            EventType.NAVIGATION_LOGIN_TERMINAL_TO_LOGIN_EMPLOYEE -> {
                navigationController.navigate(R.id.action_navigation_login_terminal_to_navigation_login_employee)
            }
            else -> super.onNavigationEvent(type, args)
        }
    }
}