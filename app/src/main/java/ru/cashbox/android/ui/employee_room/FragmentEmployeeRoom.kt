package ru.cashbox.android.ui.employee_room

import android.os.Bundle
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.cashbox.android.R
import ru.cashbox.android.databinding.FragmentEmployeeRoomBinding
import ru.cashbox.android.model.EventType
import ru.cashbox.android.ui.base.FragmentBase

class FragmentEmployeeRoom : FragmentBase<ViewModelEmployeeRoom, FragmentEmployeeRoomBinding>() {

    override fun getLayoutResId() = R.layout.fragment_employee_room
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
            EventType.NAVIGATION_EMPLOYEE_ROOM_TO_ENTER_NUMBER -> {
                navigationController.navigate(R.id.action_navigation_employee_room_to_navigation_enter_number)
            }
            else -> super.onNavigationEvent(type, args)
        }
    }
}