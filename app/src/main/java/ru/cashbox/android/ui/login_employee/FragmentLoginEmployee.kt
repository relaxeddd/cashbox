package ru.cashbox.android.ui.login_employee

import android.os.Bundle
import android.view.animation.AnimationUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.cashbox.android.R
import ru.cashbox.android.common.CODE
import ru.cashbox.android.ui.base.FragmentBase
import ru.cashbox.android.common.INTERNET_ERROR
import ru.cashbox.android.databinding.RefactorFragmentLoginEmployeeBinding
import ru.cashbox.android.model.EventType

class FragmentLoginEmployee : FragmentBase<ViewModelLoginEmployee, RefactorFragmentLoginEmployeeBinding>() {

    override fun getLayoutResId() = R.layout.refactor_fragment_login_employee
    override val viewModel: ViewModelLoginEmployee by viewModel()

    override fun configureBinding() {
        super.configureBinding()
        binding.viewModel = viewModel
        binding.pinViewLoginEmployee.setAnimationEnable(true)
        binding.pinViewLoginEmployee.setHideLineWhenFilled(false)
    }

    override fun onNavigationEvent(type: EventType, args: Bundle?) {
        when (type) {
            EventType.NAVIGATION_LOGIN_EMPLOYEE_TO_EMPLOYEE_ROOM -> {
                navigationController.navigate(R.id.action_navigation_login_employee_to_navigation_employee_room)
            }
            EventType.NAVIGATION_LOGIN_EMPLOYEE_TO_LOGIN_TERMINAL -> {
                navigationController.navigate(R.id.action_navigation_login_employee_to_navigation_login_terminal)
            }
            EventType.FRAGMENT_LOGIN_EMPLOYEE_PIN_ERROR -> {
                val errorCode = args?.get(CODE) ?: INTERNET_ERROR
                val animShake = AnimationUtils.loadAnimation(context, R.anim.shake)

                binding.pinViewLoginEmployee.startAnimation(animShake)
                if (errorCode == INTERNET_ERROR) {
                    viewModel.showToast(R.string.error_server_connect)
                } else {
                    viewModel.showToast(R.string.check_data_is_correct)
                }
            }
            else -> super.onNavigationEvent(type, args)
        }
    }
}