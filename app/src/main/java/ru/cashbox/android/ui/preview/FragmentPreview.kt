package ru.cashbox.android.ui.preview

import android.os.Bundle
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.cashbox.android.R
import ru.cashbox.android.ui.base.FragmentBase
import ru.cashbox.android.databinding.RefactorFragmentPreviewBinding
import ru.cashbox.android.model.EventType

class FragmentPreview : FragmentBase<ViewModelPreview, RefactorFragmentPreviewBinding>() {

    override fun getLayoutResId() = R.layout.refactor_fragment_preview
    override val viewModel: ViewModelPreview by viewModel()

    override fun configureBinding() {
        super.configureBinding()
        binding.viewModel = viewModel
    }

    override fun onNavigationEvent(type: EventType, args: Bundle?) {
        when (type) {
            EventType.NAVIGATION_PREVIEW_TO_LOGIN_TERMINAL -> {
                navigationController.navigate(R.id.action_navigation_preview_to_navigation_login_terminal)
            }
            EventType.NAVIGATION_PREVIEW_TO_LOGIN_EMPLOYEE -> {
                navigationController.navigate(R.id.action_navigation_preview_to_navigation_login_employee)
            }
            else -> super.onNavigationEvent(type, args)
        }
    }
}