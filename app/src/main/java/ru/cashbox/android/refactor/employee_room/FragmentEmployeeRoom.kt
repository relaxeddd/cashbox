package ru.cashbox.android.refactor.employee_room

import android.os.Bundle
import ru.cashbox.android.R
import ru.cashbox.android.common.FragmentBase
import ru.cashbox.android.databinding.RefactorFragmentEmployeeRoomBinding

import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.cashbox.android.model.EventType

class FragmentEmployeeRoom : FragmentBase<ViewModelEmployeeRoom, RefactorFragmentEmployeeRoomBinding>() {

    override fun getLayoutResId() = R.layout.refactor_fragment_employee_room
    override val viewModel: ViewModelEmployeeRoom by viewModel()

    override fun configureBinding() {
        super.configureBinding()
        binding.viewModel = viewModel
    }

    override fun onNavigationEvent(type: EventType, args: Bundle?) {
        when (type) {
            EventType.NAVIGATION_EMPLOYEE_ROOM_TO_LOGIN_EMPLOYEE -> {
                navigationController.navigate(R.id.action_navigation_employee_room_to_navigation_login_employee)
            }
            EventType.NAVIGATION_EMPLOYEE_ROOM_TO_CASH -> {
                navigationController.navigate(R.id.action_navigation_login_employee_to_navigation_login_terminal)
            }
            else -> super.onNavigationEvent(type, args)
        }
    }
}