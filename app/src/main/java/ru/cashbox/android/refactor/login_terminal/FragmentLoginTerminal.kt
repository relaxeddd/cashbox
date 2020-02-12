package ru.cashbox.android.refactor.login_terminal

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.refactor_fragment_login_terminal.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.cashbox.android.R
import ru.cashbox.android.common.FragmentBase
import ru.cashbox.android.databinding.RefactorFragmentLoginTerminalBinding
import ru.cashbox.android.model.EventType

class FragmentLoginTerminal : FragmentBase<ViewModelLoginTerminal, RefactorFragmentLoginTerminalBinding>() {

    private val clickListenerLogin = View.OnClickListener {
        val domain = text_login_terminal_domain.text.toString()
        val login = text_login_terminal_login.text.toString()
        val password = text_login_terminal_password.text.toString()

        viewModel.login(domain, login, password)
    }

    override fun getLayoutResId() = R.layout.refactor_fragment_login_terminal
    override val viewModel: ViewModelLoginTerminal by viewModel()

    override fun configureBinding() {
        super.configureBinding()
        binding.viewModel = viewModel
        binding.clickListenerLogin = clickListenerLogin
    }

    override fun onNavigationEvent(type: EventType, args: Bundle?) {
        when (type) {
            EventType.NAVIGATION_LOGIN_TERMINAL_TO_LOGIN_EMPLOYEE -> {
                navigationController.navigate(R.id.action_navigation_login_terminal_to_navigation_login_employee)
            }
            else -> super.onNavigationEvent(type, args)
        }
    }
}