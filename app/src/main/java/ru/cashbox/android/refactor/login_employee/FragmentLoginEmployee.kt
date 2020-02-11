package ru.cashbox.android.refactor.login_employee

import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.cashbox.android.R
import ru.cashbox.android.common.FragmentBase
import ru.cashbox.android.databinding.RefactorFragmentLoginEmployeeBinding

class FragmentLoginEmployee : FragmentBase<ViewModelLoginEmployee, RefactorFragmentLoginEmployeeBinding>() {

    override fun getLayoutResId() = R.layout.refactor_fragment_login_employee
    override val viewModel: ViewModelLoginEmployee by viewModel()

    override fun configureBinding() {
        super.configureBinding()
        binding.viewModel = viewModel
    }
}